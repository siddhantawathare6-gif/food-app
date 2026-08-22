FROM eclipse-temurin:21-jdk
WORKDIR /opt
COPY target/*.jar /opt/app.jar
EXPOSE 9095
ENTRYPOINT exec java $JAVA_OPTS -jar app.jar