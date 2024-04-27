FROM tomcat:10.1.16-jre17-temurin-jammy

RUN set -eux; \
	apt-get update; \
	apt-get install -y --no-install-recommends graphviz

RUN sed -i 's/port="8080"/port="8080" maxPostSize="10485760"/' conf/server.xml \
    && sed -i 's/port="8080"/port="${port.http}"/' conf/server.xml \
    && echo '#!/bin/bash\nCATALINA_OPTS="$CATALINA_OPTS -Dport.http=$PORT"\nexec catalina.sh run' > wrapper.sh && chmod +x wrapper.sh

ADD build/libs/structurizr-onpremises.war /usr/local/tomcat/webapps/ROOT.war

ENV CATALINA_OPTS="-Xms512M -Xmx512M"

CMD exec "$CATALINA_HOME/wrapper.sh"
