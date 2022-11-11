# syntax=docker/dockerfile:1
FROM amazoncorretto:18-alpine3.15-jdk
WORKDIR /blogserver
COPY gradle/ gradle
COPY gradlew build.gradle ./
COPY src ./src
CMD ["./gradlew","bootRun","-Dspring-boot.run.jvmArguments='-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:8000'"]