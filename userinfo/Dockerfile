FROM eclipse-temurin:21-jdk
WORKDIR /opt
COPY target/*.jar /opt/app.jar
COPY uploads/users/*.jpg /opt/uploads/users/
ENTRYPOINT exec java $JAVA_OPTS -jar app.jar