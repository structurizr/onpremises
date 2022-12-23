FROM tomcat:9.0.70-jre17-temurin-jammy

RUN set -eux; \
	apt-get update; \
	apt-get install -y --no-install-recommends graphviz

ADD structurizr-onpremises.war /usr/local/tomcat/webapps/ROOT.war

ENV CATALINA_OPTS="-Xms512M -Xmx512M"
EXPOSE 8080

CMD ["catalina.sh", "run"]
