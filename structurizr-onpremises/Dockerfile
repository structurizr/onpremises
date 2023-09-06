FROM tomcat:9.0.70-jre17-temurin-jammy

RUN set -eux; \
	apt-get update; \
	apt-get install -y --no-install-recommends graphviz

RUN sed -i 's/port="8080"/port="8080" maxPostSize="10485760"/' conf/server.xml

ADD build/libs/structurizr-onpremises.war /usr/local/tomcat/webapps/ROOT.war

ENV CATALINA_OPTS="-Xms512M -Xmx512M"
EXPOSE 8080

CMD ["catalina.sh", "run"]
