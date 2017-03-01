#!/bin/sh

CURRENT_DIR=$(pwd)
TARGET="root@138.197.34.190"

scp $CURRENT_DIR/build/libs/fineract-provider.war $TARGET:/tmp
ssh $TARGET chown tomcat.tomcat /tmp/fineract-provider.war -Rv
ssh $TARGET service tomcat stop
ssh $TARGET sleep 2
ssh $TARGET rm -rf /opt/tomcat/webapps/fineract-provider*
ssh $TARGET mv /tmp/fineract-provider.war /opt/tomcat/webapps
ssh $TARGET service tomcat start