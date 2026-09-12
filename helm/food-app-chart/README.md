
Helm Setup — Success Summary
1. Install Helm

brew install helm
helm version
Result: v4.3.0

2. Create chart scaffold

cd ~/Documents/Projects/food-app
mkdir -p helm
cd helm
helm create food-app-chart
Result: food-app-chart/ directory created with default templates.

3. Clean default templates

cd ~/Documents/Projects/food-app/helm/food-app-chart
rm -rf templates/* tests/

4. Write Chart.yaml

cat > Chart.yaml << 'EOF'
apiVersion: v2
name: food-app-chart
description: Helm chart for the Food App microservices stack
type: application
version: 1.0.0
appVersion: "0.0.1"
EOF

5. Write values.yaml
Contains all configurable values — services, images, ports, replicas, env vars, infra images.

Key content:

8 services (config-server, eureka, userinfo, restaurant, foodcatalogue, order, api-gateway, frontend)

3 infra (mysql, mongo, redis)

namespace: food-app

6. Write templates/services.yaml
One templated file that generates Deployments + Services for all services via a range loop over .Values.services.

Also:

Adds enableServiceLinks: false (avoids the EUREKA_PORT collision)
Adds waitForMysql initContainer when flag is true
Adds ENCRYPT_KEY from the Secret when useEncryptKey: true
Uses NodePort when nodePort is set on a service

7. Write templates/infra.yaml
Deployments + Services for MySQL, MongoDB, Redis.

MySQL mounts the mysql-init ConfigMap so it auto-creates userdb, restaurantdb, foodcataloguedb on first boot.

8. Write templates/mysql-configmap.yaml
Contains the CREATE DATABASE statements for the three databases.

9. Dry-run verification

helm template food-app . --namespace food-app | grep -E "^kind:|^  name:"
Output confirmed : 
1 ConfigMap
11 Services
11 Deployments

10. Pre-create the encryption secret

kubectl create namespace food-app
kubectl create secret generic encrypt-key \
  --from-literal=ENCRYPT_KEY='VSbQ6BaokSq3eyAjFG+V0xGVGju/7sis52HNE0qNsSE=' \
  -n food-app
(The namespace may already have existed with the secret — that's fine.)

11. Clean prior kubectl apply resources

kubectl delete deployment,service,configmap --all -n food-app
Verified empty:

kubectl get all -n food-app
# No resources found

12. Install the Helm release

cd ~/Documents/Projects/food-app/helm/food-app-chart
helm install food-app . -n food-app
Result: STATUS: deployed, REVISION: 1

13. Verify release

helm list -n food-app
helm status food-app -n food-app
kubectl get pods -n food-app
Result :
Helm release food-app deployed
11 Deployments all 1/1 Ready
11 Pods Running
3 infra + 8 services all healthy

14. Port-forward for local access
15. 
Three separate terminals:

bash
kubectl port-forward svc/api-gateway 8080:8080 -n food-app
kubectl port-forward svc/frontend 4200:80 -n food-app
kubectl port-forward svc/eureka 8761:8761 -n food-app
Local access :
Gateway: http://localhost:8080
Frontend: http://localhost:4200
Eureka: http://localhost:8761

15. Day-to-day Helm commands

# Show release status
helm list -n food-app
helm status food-app -n food-app

# Change values + apply
helm upgrade food-app . -n food-app

# Override on the fly
helm upgrade food-app . -n food-app --set services.userinfo.image=siddhant9960/fa-userinfo-service:0.0.2

# See revision history
helm history food-app -n food-app

# Rollback to a previous revision
helm rollback food-app 1 -n food-app

# Tear down the whole stack
helm uninstall food-app -n food-app

# Re-deploy
helm install food-app . -n food-app
Result — What You Now Have
Item	Value
Chart location	~/Documents/Projects/food-app/helm/food-app-chart/
Release name	food-app
Namespace	food-app
Chart version	1.0.0
Revision	1
Deployments	11 (3 infra + 8 services)
Services	11
ConfigMap	mysql-init
Secret	encrypt-key
Full stack up/down	helm install / helm uninstall — one command each
The entire stack now spins up or down with a single Helm command.

Next Time You Want to Start Everything
bash
# 1. Ensure kind cluster is running
kind get clusters

# 2. Ensure secret exists (once, persists across installs)
kubectl get secret encrypt-key -n food-app

# 3. Install (or upgrade)
cd ~/Documents/Projects/food-app/helm/food-app-chart
helm install food-app . -n food-app

# 4. Port-forward in separate terminals
kubectl port-forward svc/api-gateway 8080:8080 -n food-app
kubectl port-forward svc/frontend 4200:80 -n food-app
