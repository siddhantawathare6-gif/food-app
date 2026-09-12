
K8s Deployment Summary — Full Step-by-Step

Part 1 — Cluster Setup
Enabled Kubernetes in Docker Desktop via Settings → Kubernetes → Enable → Apply.
Chose kind as the provisioner with 2 nodes, K8s v1.36.1.
Created a dedicated kind cluster named food-app-cluster: bash  kind create cluster --name food-app-cluster 
Verified cluster: bash  kubectl get nodes  Output: food-app-cluster-control-plane Ready control-plane 30s v1.34.0
kubectl context auto-switched to kind-food-app-cluster.

Part 2 — Build & Load Images into Kind
The cluster runs its own container runtime, so host images need to be loaded into it.
Images built (already present on host from earlier docker builds):

Loaded each image into the kind cluster :
kind load docker-image siddhant9960/fa-<service>:0.0.1 --name food-app-cluster

Verified images are inside kind :
docker exec -it food-app-cluster-control-plane crictl images | grep siddhant

Part 3 — Namespace & Secrets
Created namespace: bash  kubectl create namespace food-app
kubectl config set-context --current --namespace=food-app 
Created ENCRYPT_KEY secret (from ~/.food-app/encrypt.key) so config-server can decrypt {cipher} values: bash  kubectl create secret generic encrypt-key \
  --from-literal=ENCRYPT_KEY='VSbQ6BaokSq3eyAjFG+V0xGVGju/7sis52HNE0qNsSE=' 

Part 4 — Manifest Files Created
All in ~/Documents/Projects/food-app/k8s/:

ConfigMap for MySQL init :
kubectl create configmap mysql-init --from-file=init.sql=$HOME/Documents/Projects/food-app/mysql-init/init.sql
Contains CREATE DATABASE for userdb, restaurantdb, foodcataloguedb.

Part 5 — Issues Encountered & Fixed
5.1 Mongo latest crash on kernel 6.19+
Error: MongoDB cannot start: Linux kernel versions 6.19 and newer...
Fix: downgraded to mongo:8.0.4
5.2 Invalid value '${EUREKA_PORT:8761}' for server.port
Root cause: Kubernetes auto-injects env vars like EUREKA_PORT=tcp://10.96.167.47:8761 for every Service (Service Links feature). Spring Boot tried to parse that as an integer port → failure.
Fix: bash  for d in $(kubectl get deploy -o name); do
  kubectl patch $d --type=strategic -p '{"spec":{"template":{"spec":{"enableServiceLinks":false}}}}'
done  enableServiceLinks: false disables K8s service-link env var injection.
5.3 MySQL databases missing in cluster
Root cause: mysql-init/init.sql was mounted from local filesystem via Docker Compose, but K8s doesn't auto-mount local folders.
Fix: created ConfigMap mysql-init and mounted it at /docker-entrypoint-initdb.d in mysql.yaml. Then recreated MySQL deployment (empty emptyDir → init script runs again).
5.4 Hibernate "Unable to determine Dialect"
Root cause: services couldn't reach MySQL because the databases didn't exist yet.
Fix: after 5.3 fix, DBs came up. Added wait-for-mysql initContainer to client deployments to wait for MySQL: yaml  initContainers:
  - name: wait-for-mysql
    image: busybox:1.36
    command: ["sh","-c","until nc -z mysql 3306; do echo waiting for mysql; sleep 3; done;"] 
5.5 NXDOMAIN / UnknownHostException: userinfo-7b95df69d-dpfjv
Root cause: The Eureka client registered the pod hostname (e.g. userinfo-7b95df69d-dpfjv) with Eureka. The gateway tried to reach that hostname via DNS → NXDOMAIN (pod names are not DNS-resolvable in Kubernetes).
Fix: eureka.instance.prefer-ip-address: true in the shared config-repo/application.yml. This makes each service register with its pod IP (10.244.x.x) instead. The gateway then resolves by IP → no DNS lookup.
After applying, restart all client services, then restart api-gateway specifically (it caches old hostnames in its LoadBalancer).
5.6 Duplicate entry '1' for key 'user.PRIMARY'
Root cause: data.sql seeded admin with explicit id=1. The user_seq sequence table was still starting from 1, so new registrations collided with the seeded row.
Fix (temporary): bash  kubectl exec -it deployment/mysql -- mysql -uroot -psystem@123 userdb \
  -e "UPDATE user_seq SET next_val = 100;" 
Fix (permanent, pending): make data.sql insert admin without explicit id, or reset the sequence after seeding.

Part 6 — Service Startup Order
text
1. mysql, mongo, redis
2. config-server      (uses encrypt-key secret)
3. eureka
4. userinfo, restaurant, foodcatalogue, order
5. api-gateway
6. frontend
Kubernetes handles most of it via depends_on-equivalent manifests (initContainers).

Part 7 — Verification
All pods Running
bash
kubectl get pods
Output:
text
api-gateway-xxx       1/1 Running
config-server-xxx     1/1 Running
eureka-xxx            1/1 Running
foodcatalogue-xxx     1/1 Running
frontend-xxx          1/1 Running
mongo-xxx             1/1 Running
mysql-xxx             1/1 Running
order-xxx             1/1 Running
redis-xxx             1/1 Running
restaurant-xxx        1/1 Running
userinfo-xxx          1/1 Running
Config-server decrypts secrets

kubectl exec -it deployment/config-server -- wget -qO- http://localhost:8888/user-service/default
Returns JSON with plaintext password: system@123 and secret: 9dd08... (decrypted from {cipher}).
Eureka registrations use pod IPs

kubectl exec -it deployment/api-gateway -- wget -qO- http://eureka:8761/eureka/apps
Hostnames are 10.244.x.x pod IPs.
Databases created

kubectl exec -it deployment/mysql -- mysql -uroot -psystem@123 -e "SHOW DATABASES;"
Shows userdb, restaurantdb, foodcataloguedb.
End-to-end register works

curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "name":"Test User",
    "username":"testuser",
    "email":"test@example.com",
    "password":"test123",
    "mobileNumber":"9876543210"
  }'
Port-forward for local testing
bash
kubectl port-forward svc/api-gateway 8080:8080
(Needed because NodePort 30080 is not reachable from macOS browser on Docker Desktop; port-forward is the reliable path.)

Part 8 — Access Points

Part 9 — Useful Commands Reference
bash
# See all pods
kubectl get pods

# Live logs
kubectl logs -f deployment/<name> --tail=100

# Restart a deployment
kubectl rollout restart deployment/<name>

# Exec into a pod
kubectl exec -it deployment/<name> -- sh

# Port-forward a service
kubectl port-forward svc/<name> <local>:<remote>

# Describe pod (events + status)
kubectl describe pod <pod-name>

# Delete everything in namespace
kubectl delete all --all -n food-app

# Delete the cluster
kind delete cluster --name food-app-cluster
