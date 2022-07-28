
#!/usr/bin/env bash

MHL_FRAMEWORK_ROOT=$PWD

#Build MHL server applications and copy to docker compose root 
cd "$MHL_FRAMEWORK_ROOT/mhl-server-app-src"
./build.sh

#Copy secret keys to docker compose root
cd "$MHL_FRAMEWORK_ROOT"
cp -r mhl-secrets mhl-server

#Build all docker containers
cd "$MHL_FRAMEWORK_ROOT/mhl-server"
docker-compose build




