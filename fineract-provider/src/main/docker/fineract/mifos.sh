#!/bin/sh
echo "Waiting for database..."
pwd
# NOTE: needs mysqladmin package installed
# while ! mysqladmin ping -h"$DB_HOST" --silent; do
#    sleep 1
# done


# NOTE: without any dependencies
until $(nc -z -v -w30 db 3306)
do
    echo "Waiting for database connection..."
    # wait for 5 seconds before check again
    sleep 5
done
echo "Database ready!"
catalina.sh run
