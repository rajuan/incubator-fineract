FROM tomcat:7-jre8-alpine
MAINTAINER Aleksandar Vidakovic "aleks@mifos.org"

ARG BUILD_VERSION=1.0.0

COPY fineract-provider.war /usr/local/tomcat/webapps/
COPY server.xml /usr/local/tomcat/conf/
COPY keystore.jks /usr/local/tomcat/conf/mifos.jks
COPY mifos.sh /usr/local/tomcat/bin/

RUN apk update \
  && apk add ca-certificates wget \
  && update-ca-certificates \
  && wget --quiet -O /usr/local/tomcat/lib/mysql-connector-java-5.1.40.jar https://repo1.maven.org/maven2/mysql/mysql-connector-java/5.1.40/mysql-connector-java-5.1.40.jar

EXPOSE 8443

CMD ["mifos.sh"]