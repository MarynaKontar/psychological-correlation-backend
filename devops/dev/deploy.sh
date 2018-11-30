#! /bin/sh

docker-compose stop
docker container prune --force

docker-compose up -d