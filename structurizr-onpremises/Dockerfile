FROM tomcat:10.1.35-jre21-temurin-noble
ENV PORT=8080

RUN set -eux; \
	apt-get update; \
	apt-get install -y --no-install-recommends graphviz

RUN sed -i 's/port="8080"/port="${http.port}" maxPostSize="10485760"/' conf/server.xml \
  && echo 'export CATALINA_OPTS="-Xms512M -Xmx512M -Dhttp.port=$PORT"' > bin/setenv.sh

ADD build/libs/structurizr-onpremises.war /usr/local/tomcat/webapps/ROOT.war

EXPOSE ${PORT}

CMD ["catalina.sh", "run"]