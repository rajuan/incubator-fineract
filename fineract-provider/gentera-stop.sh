#!/bin/sh

CURRENT_DIR=$(pwd)

cd build/gentera/apache-tomcat-7.0.76/bin
./shutdown.sh

cd $CURRENT_DIR