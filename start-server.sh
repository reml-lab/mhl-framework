#!/usr/bin/env bash

MHL_FRAMEWORK_ROOT=$PWD

#Start server
cd "$MHL_FRAMEWORK_ROOT/mhl-server"
docker-compose up -d mhl-ingest mhl-vs mhl-kibana mhl-analytics mhl-estap 

cd ..