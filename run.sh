#!/usr/bin/env bash

source ./env.sh

set -e

if [ "$#" -ne 1 ]; then
	echo "Usage: $0 [API key]"
	exit 1
fi

export API_KEY=$1

docker run \
    -it \
    --network container:${DB_CONTAINER} \
    --rm \
    --name perspective-app \
    -e API_KEY \
    -v $PWD:/root:delegated \
    hseeberger/scala-sbt \
    sbt run

echo "Connect Postgres client to localhost:5432/postgres to inspect results"
