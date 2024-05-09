FROM tomcat:jre21-temurin-jammy

RUN apt-get update && apt-get install -y --no-install-recommends graphviz

RUN sed -i 's/port="8080"/port="8080" maxPostSize="10485760"/' conf/server.xml

COPY target/structurizr-onpremises.war /usr/local/tomcat/webapps/ROOT.war

ENV CATALINA_OPTS="-Xms512M -Xmx3G"
EXPOSE 8080

CMD ["catalina.sh", "run"]