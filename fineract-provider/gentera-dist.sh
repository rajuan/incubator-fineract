#!/bin/sh

CURRENT_DIR=$(pwd)

GENTERA_DIR=$CURRENT_DIR/build/gentera
TOMCAT_MAJOR=7
TOMCAT_VERSION=7.0.75
#TOMCAT_TGZ_URL=https://www.apache.org/dyn/closer.cgi?action=download&filename=tomcat/tomcat-$TOMCAT_MAJOR/v$TOMCAT_VERSION/bin/apache-tomcat-$TOMCAT_VERSION.tar.gz
#TOMCAT_TGZ_URL=http://mirrors.fe.up.pt/pub/apache/tomcat/tomcat-$TOMCAT_MAJOR/v$TOMCAT_VERSION/bin/apache-tomcat-$TOMCAT_VERSION.tar.gz
TOMCAT_TGZ_URL=http://www-eu.apache.org/dist/tomcat/tomcat-$TOMCAT_MAJOR/v$TOMCAT_VERSION/bin/apache-tomcat-$TOMCAT_VERSION.tar.gz
TOMCAT_DIR=$GENTERA_DIR/apache-tomcat-$TOMCAT_VERSION

#./gradlew dist

mkdir -p $GENTERA_DIR/.mifosx

# download Tomcat
wget -O $GENTERA_DIR/apache-tomcat-$TOMCAT_VERSION.tar.gz $TOMCAT_TGZ_URL
cd $GENTERA_DIR
tar xzvf apache-tomcat-$TOMCAT_VERSION.tar.gz
cd $CURRENT_DIR

cp $CURRENT_DIR/build/libs/fineract-provider.war $TOMCAT_DIR/webapps/
cp $CURRENT_DIR/src/main/docker/fineract/server-localhost.xml $TOMCAT_DIR/conf/server.xml
cp $CURRENT_DIR/src/main/docker/fineract/keystore.jks $TOMCAT_DIR/conf/mifos.jks
cp $CURRENT_DIR/src/main/docker/fineract/mifos.sh $TOMCAT_DIR/bin/

# download Mysql driver
wget --quiet -O $TOMCAT_DIR/lib/mysql-connector-java-5.1.40.jar https://repo1.maven.org/maven2/mysql/mysql-connector-java/5.1.40/mysql-connector-java-5.1.40.jar

# copy Pentaho reports
cp -r $CURRENT_DIR/src/main/pentahoReports $GENTERA_DIR/.mifosx/

# build and deploy community app
cd $CURRENT_DIR/../../community-app
./build.sh
cd $CURRENT_DIR
cp -r $CURRENT_DIR/../../community-app/dist/community-app $TOMCAT_DIR/webapps/
cp -r $TOMCAT_DIR/webapps/ROOT/WEB-INF $TOMCAT_DIR/webapps/community-app

# cleanup
rm -rf $TOMCAT_DIR/webapps/docs
rm -rf $TOMCAT_DIR/webapps/examples
rm -rf $TOMCAT_DIR/webapps/host-manager
rm -rf $TOMCAT_DIR/webapps/manager
rm -rf $TOMCAT_DIR/webapps/ROOT
rm -rf $GENTERA_DIR/apache-tomcat-$TOMCAT_VERSION.tar.gz

# package
cd $CURRENT_DIR/build
tar c gentera > gentera.tar
gzip gentera.tar
rm -rf gentera
