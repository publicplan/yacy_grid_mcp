#!/usr/bin/env python

import os
import time
import socket
import urllib
import subprocess

elasticversion = 'elasticsearch-5.5.2'
    
path_apphome = os.path.dirname(os.path.abspath(__file__)) + '/..'
os.chdir(path_apphome)
# os.system('ls')

def checkportopen(port):
    sock = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
    return sock.connect_ex(('127.0.0.1', port)) == 0

def mkapps():
    if not os.path.isdir(path_apphome + '/data'): os.makedirs(path_apphome + '/data')
    if not os.path.isdir(path_apphome + '/data/mcp-8100'): os.makedirs(path_apphome + '/data/mcp-8100')
    if not os.path.isdir(path_apphome + '/data/mcp-8100/apps'): os.makedirs(path_apphome + '/data/mcp-8100/apps')

if checkportopen(9200):
    print('elasticsearch is already running!')
else:
    print('elasticsearch is not running')
    mkapps()
    if not os.path.isfile(path_apphome + '/data/mcp-8100/apps/' + elasticversion + '.tar.gz'):
        print('downloading ' + elasticversion)
        urllib.urlretrieve ('https://artifacts.elastic.co/downloads/elasticsearch/' + elasticversion + '.tar.gz', path_apphome + '/data/mcp-8100/apps/' + elasticversion + '.tar.gz')
    elasticpath = path_apphome + '/data/mcp-8100/apps/elasticsearch'
    firstrun = False
    if not os.path.isdir(elasticpath):
        print('decompressing' + elasticversion)
        os.system('tar xfz ' + path_apphome + '/data/mcp-8100/apps/' + elasticversion + '.tar.gz -C ' + path_apphome + '/data/mcp-8100/apps/')
        os.rename(path_apphome + '/data/mcp-8100/apps/' + elasticversion, elasticpath)
        firstrun = True
    # run elasticsearch
    print('starting elasticsearch...')
    os.chdir(elasticpath + '/bin')
    if not os.path.isdir(elasticpath + '/data'): os.makedirs(elasticpath + '/data')
    logpath = elasticpath + '/log'
    if not os.path.isfile(logpath): os.system('touch ' + logpath)
    subprocess.call('./elasticsearch >> ' + logpath + ' &', shell=True)
    if firstrun: 
        while not checkportopen(9200):
            print('waiting until elasticsearch is running...')
            time.sleep(3)
        print('preparing index...')
        os.chdir(path_apphome)
        subprocess.call('bin/prepare_index.sh', shell=True)

