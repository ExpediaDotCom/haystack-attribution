FROM registry.hub.docker.com/library/openjdk:8-jre
MAINTAINER Haystack <haystack@expedia.com>

ENV APP_NAME haystack-attribution-persistence-s3
ENV APP_HOME /app/bin
ENV JMXTRANS_AGENT jmxtrans-agent-1.2.6

RUN mkdir -p ${APP_HOME}

COPY ./target/${APP_NAME}.jar ${APP_HOME}/
COPY build/docker/start-app.sh ${APP_HOME}/

RUN chmod +x ${APP_HOME}/start-app.sh

COPY build/docker/jmxtrans-agent.xml ${APP_HOME}/
ADD https://github.com/jmxtrans/jmxtrans-agent/releases/download/${JMXTRANS_AGENT}/${JMXTRANS_AGENT}.jar ${APP_HOME}/

WORKDIR ${APP_HOME}

ENTRYPOINT ["./start-app.sh"]
