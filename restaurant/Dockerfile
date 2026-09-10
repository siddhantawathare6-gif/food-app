FROM eclipse-temurin:21-jdk
WORKDIR /opt

# Copy the JAR
COPY target/*.jar /opt/app.jar

# Create the uploads directory structure
RUN mkdir -p /opt/uploads/restaurants

# Copy all images from local uploads/restaurants into the image
COPY uploads/restaurants/*.jpg /opt/uploads/restaurants/

EXPOSE 9091

ENTRYPOINT exec java $JAVA_OPTS -jar app.jar