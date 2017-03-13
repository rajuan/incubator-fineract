#!/bin/sh

CURRENT_DIR=$(pwd)

cd build/gentera/apache-tomcat-7.0.75/bin
./shutdown.sh

cd $CURRENT_DIR