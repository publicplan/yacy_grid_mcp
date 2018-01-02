/**
 *  RabbitQueueFactory
 *  Copyright 14.01.2017 by Michael Peter Christen, @0rb1t3r
 *
 *  This library is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Lesser General Public
 *  License as published by the Free Software Foundation; either
 *  version 2.1 of the License, or (at your option) any later version.
 *  
 *  This library is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *  Lesser General Public License for more details.
 *  
 *  You should have received a copy of the GNU Lesser General Public License
 *  along with this program in the file lgpl21.txt
 *  If not, see <http://www.gnu.org/licenses/>.
 */

package net.yacy.grid.io.messages;

import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.GetResponse;
import com.rabbitmq.client.MessageProperties;

import net.yacy.grid.mcp.Data;

import com.rabbitmq.client.Connection;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeoutException;

import com.rabbitmq.client.Channel;

/**
 * to monitor the rabbitMQ queue, open the admin console at
 * http://127.0.0.1:15672/
 * and log with admin/admin
 */
public class RabbitQueueFactory implements QueueFactory<byte[]> {
    
    private static int DEFAULT_PORT = 5672;
    private static String DEFAULT_EXCHANGE = "";
    public static String PROTOCOL_PREFIX = "amqp://";
    
    
    private String server, username, password;
    private int port;
    private Connection connection;
    private Channel channel;
    private Map<String, Queue<byte[]>> queues;
    
    /**
     * create a queue factory for a rabbitMQ message server
     * @param server the host name of the rabbitMQ server
     * @param port a port for the access to the rabbitMQ server. If given -1, then the default port will be used
     * @throws IOException
     */
    public RabbitQueueFactory(String server, int port, String username, String password) throws IOException {
        this.server = server;
        this.port = port;
        this.username = username;
        this.password = password;
        this.init();
    }
    
    private void init() throws IOException {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setAutomaticRecoveryEnabled(true);
        factory.setHost(this.server);
        if (this.port > 0) factory.setPort(this.port);
        if (this.username != null && this.username.length() > 0) factory.setUsername(this.username);
        if (this.password != null && this.password.length() > 0) factory.setPassword(this.password);
        try {
            this.connection = factory.newConnection();
            //Map<String, Object> map = this.connection.getServerProperties();
            if (!this.connection.isOpen()) throw new IOException("no connection");
            this.channel = connection.createChannel();
            if (!this.channel.isOpen()) throw new IOException("no channel");
            this.queues = new ConcurrentHashMap<>();
        } catch (TimeoutException e) {
            throw new IOException(e.getMessage());
        }
    }

    @Override
    public String getConnectionURL() {
    	return PROTOCOL_PREFIX +
               (this.username != null && this.username.length() > 0 ? username + (this.password != null && this.password.length() > 0 ? ":" + this.password : "") + "@" : "") +
               this.getHost() + ((this.hasDefaultPort() ? "" : ":" + this.getPort()));
    }

    @Override
    public String getHost() {
        return this.server;
    }

    @Override
    public boolean hasDefaultPort() {
        return this.port == -1 || this.port == DEFAULT_PORT;
    }

    @Override
    public int getPort() {
        return hasDefaultPort() ? DEFAULT_PORT : this.port;
    }
    
    @Override
    public Queue<byte[]> getQueue(String queueName) throws IOException {
        Queue<byte[]> queue = queues.get(queueName);
        if (queue != null) return queue;
        synchronized (this) {
            queue = queues.get(queueName);
            if (queue != null) return queue;
            queue = new RabbitMessageQueue(queueName);
            this.queues.put(queueName, queue);
            return queue;
        }
    }
    
    private class RabbitMessageQueue implements Queue<byte[]> {
        private String queueName;
        public RabbitMessageQueue(String queueName) throws IOException {
            this.queueName = queueName;
            RabbitQueueFactory.this.channel.queueDeclare(this.queueName, true, false, false, null);
        }

        @Override
        public void checkConnection() throws IOException {
            available();
        }
        
        @Override
        public Queue<byte[]> send(byte[] message) throws IOException {
            try {
                channel.basicPublish(DEFAULT_EXCHANGE, this.queueName, MessageProperties.PERSISTENT_BASIC, message);
            } catch (Exception e) {
                // try to reconnect
                RabbitQueueFactory.this.channel.queueDeclare(this.queueName, true, false, false, null);
                channel.basicPublish(DEFAULT_EXCHANGE, this.queueName, MessageProperties.PERSISTENT_BASIC, message);
            }
            return this;
        }
        
        @Override
        public byte[] receive(long timeout) throws IOException {
            if (timeout <= 0) timeout = Long.MAX_VALUE;
            long termination = timeout <= 0 || timeout == Long.MAX_VALUE ? Long.MAX_VALUE : System.currentTimeMillis() + timeout;
            while (System.currentTimeMillis() < termination) {
                try {
                    GetResponse response = channel.basicGet(this.queueName, true);
                    if (response != null) return response.getBody();
                    //Data.logger.warn("receive failed: response empty");
                } catch (Throwable e) {
                    Data.logger.warn("receive failed: " + e.getMessage(), e);
                }
                try {Thread.sleep(1000);} catch (InterruptedException e) {return null;}
            }
            return null;
        }

        @Override
        public int available() throws IOException {
            return channel.queueDeclarePassive(this.queueName).getMessageCount();
        }
    }
    
    @Override
    public void close() {
        this.queues.clear();
        try {
            this.channel.close();
        } catch (IOException | TimeoutException e) {}
        try {
            this.connection.close();
        } catch (IOException e) {}
        this.queues = null;
    }
    
    public static void main(String[] args) {
        RabbitQueueFactory qc;
        try {
            qc = new RabbitQueueFactory("127.0.0.1", -1, null, null);
            qc.getQueue("test").send("Hello World".getBytes());
            System.out.println(qc.getQueue("test2").receive(60000));
            qc.close();
        } catch (IOException e) {
            Data.logger.warn("", e);
        }
    }
}
