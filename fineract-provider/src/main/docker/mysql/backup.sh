#!/bin/sh
NOW=$(date +"%Y-%m-%d-%H-%M")
tomcat service stop
mysqldump -h 127.0.0.1 -u root -pmysql mifostenant-default > mifostenant-default-$NOW.sql
tomcat service start
