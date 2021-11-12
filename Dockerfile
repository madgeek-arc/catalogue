FROM tomcat:8.5-jre8-alpine
MAINTAINER "spyroukon@gmail.com"
RUN ["rm", "-fr", "/usr/local/tomcat/webapps/ROOT"]
COPY ./target/catalogue-0.0.1-SNAPSHOT.war /usr/local/tomcat/webapps/catalogue.war
COPY ./src/main/resources/application.properties /usr/local/tomcat/lib/application.properties
COPY ./src/main/resources/registry.properties /usr/local/tomcat/lib/registry.properties
#COPY ./server.xml /usr/local/tomcat/conf/server.xml
RUN ["cat", "/usr/local/tomcat/lib/registry.properties"]
CMD ["catalina.sh", "run"]
