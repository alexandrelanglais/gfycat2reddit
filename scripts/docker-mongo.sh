#!/usr/bin/env bash

CONTAINER_NAME=mongoGfycat2Reddit

DATA=~/docker-datas/mongo/gfycat2reddit

mkdir -p $DATA

echo "Launching MongoDB $CONTAINER_NAME with data $DATA"

docker run --name $CONTAINER_NAME -p 27917:27017 -v $DATA:/data/db -d mongo

docker ps

