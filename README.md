🚀 Food Delivery Application - Complete DevOps & Deployment Guide
📋 Table of Contents
Project Overview

Prerequisites

Docker Setup & Configuration

Microservices Deployment

Jenkins CI/CD Pipeline

SonarQube Integration

Angular Frontend Deployment

Troubleshooting Guide

Cleanup Commands

📖 Project Overview
This document provides a comprehensive guide for deploying a microservices-based food delivery application with Docker and Jenkins CI/CD pipeline.

🏗️ Architecture Components
Component	Port	Technology
Eureka Server	8761	Service Discovery
Restaurant Service	9091	Spring Boot + MySQL
User Service	9093	Spring Boot + MySQL
Food Catalogue Service	9095	Spring Boot + MySQL
Order Service	9097	Spring Boot + MongoDB
Angular Frontend	4200	Angular 19 + Nginx
Jenkins	8080	CI/CD Pipeline
SonarQube	9000	Code Quality Analysis
🛠️ Prerequisites
Required Software
bash
# Check your versions
docker --version        # Docker Desktop 20.10+
java -version          # Java 21+
node --version         # Node 18+
npm --version         # npm 9+
ng version            # Angular CLI 19+

# For macOS users
brew --version        # Homebrew (for ngrok installation)
Docker Images Used

siddhant9960/fa-eureka-server:0.0.1
siddhant9960/fa-restaurant-service:0.0.1
siddhant9960/fa-foodcatalogue-service:0.0.1
siddhant9960/fa-userinfo-service:0.0.1
siddhant9960/fa-order-service:0.0.1
siddhant9960/fa-foodapp-angular:0.0.1

Creating images:
docker build -t siddhant9960/fa-eureka-server:0.0.1 .  
docker build -t siddhant9960/fa-userinfo-service:0.0.1 .   
docker build -t siddhant9960/fa-order-service:0.0.1 .  
docker build -t siddhant9960/fa-foodcatalogue-service:0.0.1 .   
docker build -t siddhant9960/fa-restaurant-service:0.0.1 .  

🐳 Docker Setup & Configuration
1. Create Docker Network
bash
# Create a dedicated network for all services
docker network create food-app-network

# Verify network creation
docker network ls
2. Create Persistent Volumes
bash
# Jenkins persistent storage
docker volume create jenkins-data

# SonarQube persistent storage
docker volume create sonarqube_data
docker volume create sonarqube_extensions
docker volume create sonarqube_logs 

📦 Microservices Deployment
Step 1: Deploy MySQL Database
bash
# Start MySQL container
docker run -d \
  --name mysql-foodapp-db \
  --network food-app-network \
  -e MYSQL_ROOT_PASSWORD=root \
  -e MYSQL_DATABASE=restaurant_db \
  -p 3307:3306 \
  mysql:8.0

# Wait 15-20 seconds for MySQL to initialize
# Check logs to verify healthy startup
docker logs mysql-foodapp-db
Step 2: Create Databases for Each Service
sql
-- Connect to MySQL and create databases
CREATE DATABASE IF NOT EXISTS foodcatalogue_db;
CREATE DATABASE IF NOT EXISTS userinfo_db;
Step 3: Deploy Eureka Server (Service Discovery)
bash
docker run -d \
  --name eureka-server \
  --network food-app-network \
  -p 8761:8761 \
  siddhant9960/fa-eureka-server:0.0.1
Step 4: Deploy Restaurant Service
bash
docker run -d \
  --name restaurant-service \
  --network food-app-network \
  -e SPRING_DATASOURCE_URL=jdbc:mysql://mysql-foodapp-db:3306/restaurant_db \
  -e SPRING_DATASOURCE_USERNAME=root \
  -e SPRING_DATASOURCE_PASSWORD=root \
  -e EUREKA_CLIENT_SERVICEURL_DEFAULTZONE=http://eureka-server:8761/eureka \
  -p 9091:9091 \
  siddhant9960/fa-restaurant-service:0.0.1
Step 5: Deploy Food Catalogue Service
bash
docker run -d \
  --name food-catalogue-service \
  --network food-app-network \
  -e SPRING_DATASOURCE_URL=jdbc:mysql://mysql-foodapp-db:3306/foodcatalogue_db \
  -e SPRING_DATASOURCE_USERNAME=root \
  -e SPRING_DATASOURCE_PASSWORD=root \
  -e EUREKA_CLIENT_SERVICEURL_DEFAULTZONE=http://eureka-server:8761/eureka \
  -p 9095:9095 \
  siddhant9960/fa-foodcatalogue-service:0.0.1
Step 6: Deploy User Service
bash
docker run -d \
  --name userinfo-service \
  --network food-app-network \
  -e SPRING_DATASOURCE_URL=jdbc:mysql://mysql-foodapp-db:3306/userinfo_db \
  -e SPRING_DATASOURCE_USERNAME=root \
  -e SPRING_DATASOURCE_PASSWORD=root \
  -e EUREKA_CLIENT_SERVICEURL_DEFAULTZONE=http://eureka-server:8761/eureka \
  -p 9093:9093 \
  siddhant9960/fa-userinfo-service:0.0.1
Step 7: Deploy MongoDB
bash
docker run -d \
  --name mongodb-container \
  --network food-app-network \
  -e MONGO_INITDB_ROOT_USERNAME=root \
  -e MONGO_INITDB_ROOT_PASSWORD=root \
  -e MONGO_INITDB_DATABASE=order_db \
  -p 27018:27017 \
  mongo:latest
Step 8: Deploy Order Service
bash
docker run -d \
  --name order-service \
  --network food-app-network \
  -e MONGODB_URI=mongodb://root:root@mongodb-container:27017/order_db?authSource=admin \
  -e EUREKA_CLIENT_SERVICEURL_DEFAULTZONE=http://eureka-server:8761/eureka \
  -p 9097:9097 \
  siddhant9960/fa-order-service:0.0.1
Verify All Services
bash
# Check running containers
docker ps

# Monitor logs for a specific service
docker logs -f restaurant-service  # Press Ctrl+C to exit
🔧 Jenkins CI/CD Pipeline Setup
Option A: Jenkins in Docker (Recommended)
1. Install and Run Jenkins
bash
# Run Jenkins container
docker run \
  --name jenkins \
  --restart=on-failure \
  --detach \
  --network food-app-network \
  --publish 8080:8080 \
  --publish 50000:50000 \
  --volume jenkins-data:/var/jenkins_home \
  jenkins/jenkins:lts-jdk21
2. Get Initial Admin Password
bash
docker exec jenkins cat /var/jenkins_home/secrets/initialAdminPassword
# Example output: fbc311fbff3448b784d9fc6f55295e57
3. Access Jenkins
Open browser: http://localhost:8080

Enter the initial password from above

Install suggested plugins

Create admin user:

Username: admin

Password: admin@123

Full Name: Siddhant Awathare

Email: sidawathare6@gmail.com

🔗 GitHub Webhook Setup with ngrok
Why ngrok?
GitHub cannot reach localhost:8080 from the internet. ngrok creates a public URL that tunnels to your local Jenkins.

1. Install ngrok
bash
# Install via Homebrew (macOS)
brew install ngrok

# Sign up for free account at ngrok.com and get your auth token
ngrok config add-authtoken YOUR_AUTH_TOKEN
2. Start ngrok Tunnel
bash
# Create tunnel to Jenkins
ngrok http 8080

# You'll get a URL like:
# https://pursuable-chamomile-pester.ngrok-free.dev -> http://localhost:8080
3. Configure Jenkins URL
bash
# Set Jenkins URL to ngrok URL
# Jenkins → Manage Jenkins → Configure System → Jenkins Location → Jenkins URL
# Set: https://pursuable-chamomile-pester.ngrok-free.dev/
4. Configure GitHub Webhook
Go to your GitHub repository

Settings → Webhooks → Add webhook

Payload URL: https://pursuable-chamomile-pester.ngrok-free.dev/github-webhook/

Content type: application/json

Events: Just the push event

Active: ✓ Checked

📝 Jenkins Pipeline Configuration
Required Plugins
Install these plugins via Jenkins Dashboard → Manage Jenkins → Plugins:

✅ Docker Pipeline

✅ Git

✅ Pipeline

✅ Credentials Binding

✅ SonarQube Scanner (for code quality)

✅ Blue Ocean (optional - nicer UI)

Add Docker Hub Credentials
bash
Jenkins → Manage Jenkins → Credentials → System → Global credentials → Add Credentials
Kind: Username with password
ID: dockerhub-creds
Username: your-dockerhub-username
Password: your-dockerhub-password
Add SonarQube Credentials
bash
Jenkins → Manage Jenkins → Credentials → System → Global credentials → Add Credentials
Kind: Secret text
ID: sonar-token
Secret: YOUR_SONAR_TOKEN
Sample Jenkinsfile (order-service)
Create order/Jenkinsfile in your repository:

pipeline {
    agent any
    tools {
        maven 'Maven3'
        jdk 'JDK21'
    }
    environment {
        DOCKER_IMAGE = "siddhant9960/fa-order-service"
        DOCKER_TAG = "0.0.${BUILD_NUMBER}"
        DOCKERHUB_CREDENTIALS = credentials('dockerhub-creds')
        SONAR_PROJECT_KEY = "order-service"
    }
    stages {
        stage('Checkout') {
            steps {
                checkout scm
            }
        }
        stage('Build') {
            steps {
                dir('order') {                          // <-- ADDED (as all services is under single repo)
                    sh 'mvn clean compile'
                }                                           // <-- ADDED
            }
        }
        stage('Run Tests') {
            steps {
                dir('order') {                          // <-- ADDED
                    sh 'mvn test'
                }                                           // <-- ADDED
            }
            post {
                always {
                    junit 'order/target/surefire-reports/*.xml'   // <-- CHANGED path
                }
            }
        }
        stage('Code Coverage Report') {
            steps {
                dir('order') {                          // <-- ADDED
                    sh 'mvn jacoco:report'
                }                                           // <-- ADDED
            }
        }
        stage('SonarQube Analysis') {
            steps {
                dir('order') {                          // <-- ADDED
                    withSonarQubeEnv('SonarQube') {
                        sh """
                            mvn org.sonarsource.scanner.maven:sonar-maven-plugin:5.1.0.4751:sonar \
                            -Dsonar.projectKey=${SONAR_PROJECT_KEY} \
                            -Dsonar.java.coveragePlugin=jacoco \
                            -Dsonar.coverage.jacoco.xmlReportPaths=target/site/jacoco/jacoco.xml
                        """
                    }
                }                                           // <-- ADDED
            }
        }
        stage('Quality Gate') {
            steps {
                timeout(time: 5, unit: 'MINUTES') {
                    waitForQualityGate abortPipeline: true
                }
            }
        }
        stage('Package JAR') {
            steps {
                dir('order') {                          // <-- ADDED
                    sh 'mvn package -DskipTests'
                }                                           // <-- ADDED
            }
        }
        stage('Build Docker Image') {
            steps {
                dir('order') {                          // <-- ADDED
                    sh "docker build -t ${DOCKER_IMAGE}:${DOCKER_TAG} -t ${DOCKER_IMAGE}:latest ."
                }                                           // <-- ADDED
            }
        }
        stage('Push to Docker Hub') {
            steps {
                sh "echo ${DOCKERHUB_CREDENTIALS_PSW} | docker login -u ${DOCKERHUB_CREDENTIALS_USR} --password-stdin"
                sh "docker push ${DOCKER_IMAGE}:${DOCKER_TAG}"
                sh "docker push ${DOCKER_IMAGE}:latest"
            }
        }
        stage('Deploy') {
            steps {
                sh """
                    docker stop order-service || true
                    docker rm order-service || true
                    docker run -d --name order-service --network food-app-network -p 9097:9097 ${DOCKER_IMAGE}:latest
                """
            }
        }
    }
    post {
        success {
            echo "order-service pipeline succeeded"
        }
        failure {
            echo "order-service pipeline failed"
        }
        always {
            sh 'docker logout'
            cleanWs()
        }
    }
}

Configure Jenkins Pipeline Job
Dashboard → New Item

Name: order-service-pipeline

Type: Pipeline → OK

Scroll to Pipeline section:

Definition: Pipeline script from SCM

SCM: Git

Repository URL: https://github.com/yourusername/food-app.git

Credentials: Add GitHub credentials (Personal Access Token)

Branch: */main

Script Path: order/Jenkinsfile

Save

📊 SonarQube Integration
1. Run SonarQube
bash
docker run -d \
  --name sonarqube \
  --network food-app-network \
  -p 9000:9000 \
  -v sonarqube_data:/opt/sonarqube/data \
  -v sonarqube_extensions:/opt/sonarqube/extensions \
  -v sonarqube_logs:/opt/sonarqube/logs \
  sonarqube:lts-community
2. Access SonarQube
Open browser: http://localhost:9000

Default login: admin / admin

Change password on first login

3. Generate SonarQube Token
Click your profile icon → My Account → Security

Under "Generate Tokens":

Name: jenkins-token

Click Generate

Copy token: sqa_843636a174bb4d2677de32da86504f7d1282d848

4. Configure SonarQube in Jenkins
Jenkins → Manage Jenkins → System

Scroll to SonarQube servers → Add SonarQube

Name: SonarQube (matches Jenkinsfile)

Server URL: http://sonarqube:9000

Server authentication token: Add credential (secret text with sonar-token)

5. Configure Webhook in SonarQube
SonarQube → Administration → Configuration → Webhooks → Create

Name: jenkins

URL: http://jenkins:8080/sonarqube-webhook/

6. Add JaCoCo Plugin to pom.xml
xml
<plugin>
    <groupId>org.jacoco</groupId>
    <artifactId>jacoco-maven-plugin</artifactId>
    <version>0.8.12</version>
    <executions>
        <execution>
            <goals>
                <goal>prepare-agent</goal>
            </goals>
        </execution>
        <execution>
            <id>report</id>
            <phase>test</phase>
            <goals>
                <goal>report</goal>
            </goals>
        </execution>
    </executions>
</plugin>
7. Configure Jenkins Tools
Manage Jenkins → Tools:

Maven Installations: Name: Maven3, Install automatically

JDK Installations: Name: JDK21, Install automatically

🎨 Angular Frontend Deployment
Deploy Angular Application
bash
docker run -d \
  --name food-app-frontend \
  --network food-app-network \
  -p 4200:80 \
  siddhant9960/fa-foodapp-angular:0.0.1
Access the Application
Open browser: http://localhost:4200

The Angular app will connect to backend services via service discovery

🧹 Cleanup Commands
Stop All Services
bash
docker stop restaurant-service mysql-foodapp-db food-catalogue-service user-info-service order-service mongodb-container eureka-server food-app-frontend sonarqube jenkins
Remove All Containers
bash
docker rm restaurant-service mysql-foodapp-db food-catalogue-service user-info-service order-service mongodb-container eureka-server food-app-frontend sonarqube jenkins
Remove Network
bash
docker network rm food-app-network
Remove Volumes (⚠️ Deletes all data)
bash
docker volume rm jenkins-data sonarqube_data sonarqube_extensions sonarqube_logs
One Command Clean All
bash
docker stop $(docker ps -aq) && docker rm $(docker ps -aq) && docker network rm food-app-network
🐛 Troubleshooting Guide
Issue: MySQL Connection Refused
Solution: Wait 15-20 seconds after MySQL starts before connecting other services.

bash
# Check if MySQL is ready
docker logs mysql-foodapp-db | grep "ready for connections"
Issue: Port Already in Use
Solution: Check and kill process using the port

bash
# Find process using port
lsof -i :8080
# Kill the process
kill -9 <PID>
Issue: Jenkins Can't Access Docker
Solution: Mount Docker socket

bash
docker run -d \
  --name jenkins \
  -v /var/run/docker.sock:/var/run/docker.sock \
  --network food-app-network \
  -p 8080:8080 \
  jenkins/jenkins:lts-jdk21
Issue: SonarQube Quality Gate Timeout
Solution: Check webhook configuration

bash
# Verify SonarQube can reach Jenkins
# In SonarQube → Administration → Webhooks → Test webhook
# Should show "Webhook sent successfully"
Issue: Container Can't Find Service by Name
Solution: Ensure all containers are on the same network

bash
# Check network connections
docker network inspect food-app-network
Issue: Angular App Can't Connect to Backend
Solution: Update API endpoints to use service discovery or container names

javascript
// In Angular environment.ts
export const environment = {
  apiUrl: 'http://eureka-server:8761/eureka/apps'
};
Issue: Docker Image Build Fails
Solution: Check Dockerfile and ensure all required files are present

dockerfile
# Example Dockerfile for Spring Boot
FROM openjdk:21-jdk-slim
COPY target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app.jar"]
✅ Verification Checklist
Services Status
□ Eureka Server: http://localhost:8761
□ Restaurant Service: http://localhost:9091/actuator/health
□ User Service: http://localhost:9093/actuator/health
□ Food Catalogue: http://localhost:9095/actuator/health
□ Order Service: http://localhost:9097/actuator/health
□ Jenkins: http://localhost:8080
□ SonarQube: http://localhost:9000
□ Angular App: http://localhost:4200
CI/CD Pipeline
□ GitHub webhook triggers Jenkins on push
□ Build passes with 100% success rate
□ Tests run and pass
□ Code coverage > 80%
□ SonarQube quality gate passes
□ Docker image builds successfully
□ Image pushed to Docker Hub
□ Container deploys automatically
📚 Useful Commands Reference
bash
# Docker Commands
docker ps -a                                    # List all containers
docker logs -f <container-name>                # Follow container logs
docker exec -it <container> bash               # Enter container
docker network inspect food-app-network        # Network details

# Jenkins Commands
docker exec -it jenkins cat /var/jenkins_home/secrets/initialAdminPassword

# SonarQube Commands
docker exec -it sonarqube cat /opt/sonarqube/logs/access.log

# Database Commands
docker exec -it mysql-foodapp-db mysql -uroot -proot
🔗 Quick Links
Service	URL
Eureka Dashboard	http://localhost:8761
Jenkins	http://localhost:8080
SonarQube	http://localhost:9000
Angular App	http://localhost:4200
H2 Console (Restaurant)	http://localhost:9091/h2-console
H2 Console (User)	http://localhost:9093/h2-console
📝 Notes
Eureka Registration: All services must register with Eureka to discover each other

Environment Variables: Update SPRING_DATASOURCE_URL and MONGODB_URI if you change database names or ports

Container Restart: Use --restart=on-failure to automatically restart failed containers

Security: In production, use secrets management (like HashiCorp Vault) instead of hardcoded passwords

Scaling: Add --scale parameter to Docker Compose for horizontal scaling
