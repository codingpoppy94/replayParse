From openjdk:17-alpine

ARG JAR_FILE=replay-0.0.1-SNAPSHOT.jar
ARG CONFIG_FILE=application.properties

# Set the timezone environment variable
ENV TZ=Asia/Seoul

# Install tzdata to ensure timezone data is available
RUN mkdir /league

COPY ${JAR_FILE} league/replayparse.jar
COPY ${CONFIG_FILE} league/application.properties

WORKDIR /league

# ENTRYPOINT [ "java" "-jar","replayparse.jar" ]
# CMD ["sh", "-c", "java -jar replayparse.jar"]
ENTRYPOINT ["sh", "-c", "java -jar replayparse.jar"]
