#!/usr/bin/env bash

source ./env.sh

set -e

if [ "$#" -ne 1 ]; then
	echo "Usage: $0 [SQL file]"
	exit 1
fi

SQL_FILE=$1

if [ ! -f ${SQL_FILE} ]; then
    echo "SQL file not found: $SQL_FILE"
    exit 1
fi

set -x

if docker container inspect ${DB_CONTAINER} > /dev/null 2>&1 ; then
    echo "Existing Docker container [$DB_CONTAINER] found"
else
    echo "Creating Docker container [$DB_CONTAINER]"

    docker run \
        --name ${DB_CONTAINER} \
        -p 5432:5432 \
        -v ${SQL_FILE}:/docker-entrypoint-initdb.d/dump.sql:ro \
        postgres:alpine \
        postgres
fi

