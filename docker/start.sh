#!/bin/bash
adduser --disabled-password --gecos '' r
adduser r sudo
echo '%sudo ALL=(ALL) NOPASSWD:ALL' >> /etc/sudoers
cd /yacy_grid_mcp
sleep 1s;
gradle run > ./log/yacy-mcp.log
