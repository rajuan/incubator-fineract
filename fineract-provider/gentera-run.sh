#!/bin/sh

CURRENT_DIR=$(pwd)

cd build
rm -rf gentera
tar xzvf gentera.tar.gz
cd gentera/apache-tomcat-7.0.76/bin
./startup.sh
tail -f ../logs/catalina.out

cd $CURRENT_DIR