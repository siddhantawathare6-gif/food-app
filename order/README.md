
PART 1: CONTROLLER

OrderController (/order)

Class-level
@RestController → JSON responses.
@RequestMapping("/order") → base path.
Commented @CrossOrigin → CORS handled by gateway.

POST /saveOrder
Annotations :
@PostMapping("/saveOrder").
@RequestBody OrderDTOFromFE orderDetails → request from frontend contains: userId, restaurant, foodItemsList, deliveryAddress, paymentMethod, totalAmount, deliveryInstructions.
What the method does :
Calls orderService.saveOrderInDb(orderDetails) → returns OrderDTO with generated orderId.
Returns 201 CREATED.

GET /history/{userId}
Annotations :
@GetMapping("/history/{userId}").
@PathVariable Integer userId.
What the method does :
Calls orderService.getOrderHistory(userId) → list of OrderDTO.
Returns 200 OK.

GET /{orderId}
Annotations :
@GetMapping("/{orderId}").
@PathVariable Integer orderId.
What the method does :
Calls orderService.getOrderById(orderId) → single OrderDTO.
Returns 200 OK.

PUT /cancel/{orderId}
Annotations :
@PutMapping("/cancel/{orderId}").
@PathVariable Integer orderId.
What the method does :
Calls orderService.cancelOrder(orderId) → cancelled OrderDTO.
Returns 200 OK.

GET /status/{status}
Annotations :
@GetMapping("/status/{status}").
@PathVariable OrderStatus status → Spring converts string to enum (e.g. PENDING, DELIVERED).
What the method does :
Calls orderService.getOrdersByStatus(status) → list of OrderDTO.
Returns 200 OK.

PART 2: SERVICE LAYER

OrderService

Class-level
@Service → Spring bean.
Injected: OrderRepository (MongoDB), SequenceGenerator (auto-increment orderId), CachedUserService (fetch user with caching + retry).

saveOrderInDb(OrderDTOFromFE)
Annotations: none (the caching lives on CachedUserService).
What it does :
sequenceGenerator.generateNextOrderId() → Mongo findAndModify on the sequence collection → atomically increments and returns the next integer.
cachedUserService.getUserDetails(userId) → fetch user via cache/HTTP (see below).
Build Order entity:
orderId, foodItemsList, restaurant, userDTO
status = PENDING
deliveryAddress, paymentMethod, totalAmount, deliveryInstructions
createdAt = updatedAt = LocalDateTime.now()
orderRepo.save(order) → INSERT into MongoDB order collection.
Map → DTO, return.

updateOrderStatus(Integer orderId, OrderStatus newStatus)
Annotations : none.
What it does :
orderRepo.findByOrderId(orderId) → or throw OrderNotFoundException.
validateStatusTransition(current, newStatus) → enforces allowed state machine (see below).
Set new status; updatedAt = now.
setStatusTimestamp(order, newStatus) → stamps the matching timestamp field (confirmedAt, preparingAt, readyAt, outForDeliveryAt, deliveredAt, cancelledAt).
Save. Map → DTO, return.
Status transition rules (in validateStatusTransition):
From DELIVERED or CANCELLED → no further changes allowed.
PENDING → CONFIRMED or CANCELLED.
CONFIRMED → PREPARING or CANCELLED.
PREPARING → READY or CANCELLED.
READY → OUT_FOR_DELIVERY or CANCELLED.
OUT_FOR_DELIVERY → DELIVERED or CANCELLED.
Anything else → RuntimeException("Invalid status transition ...").

getOrderHistory(Integer userId)
Annotations : none.
What it does :
orderRepo.findByUserDTO_Id(userId) → Spring Data Mongo query on nested field userDTO.id.
Map each Order → DTO, return list.

getOrderById(Integer orderId)
Annotations : none.
What it does :
orderRepo.findByOrderId(orderId) → or throw OrderNotFoundException.
Map → DTO, return.

cancelOrder(Integer orderId)
Annotations : none.
What it does :
orderRepo.findByOrderId(orderId) → or throw OrderNotFoundException.
If !order.getStatus().isCancellable() → RuntimeException("Order cannot be cancelled. Current status: ...").
Set status CANCELLED, updatedAt = now, cancelledAt = now.
Save. Map → DTO, return.

getOrdersByStatus(OrderStatus status)
Annotations : none.
What it does :
orderRepo.findByStatus(status).
Map each → DTO, return list.

CachedUserService

Class-level
@Service → Spring bean.

getUserDetails(Integer userId)
Annotations :
@Cacheable(value = "orderServiceUser", key = "#userId") → check Redis cache orderServiceUser for the userId. Hit → return cached UserDTO. Miss → run method, store result (TTL 15 min from CacheConfig).
What it does :
Delegates to userService.fetchUserDetailsFromUserId(userId) (which has retry + fallback).
Why wrapper: keeps @Cacheable separate from @Retry. Same pattern as foodcatalogue.

UserService (HTTP client to userinfo-service)

Class-level
@Service → Spring bean.
Injected: RestTemplate (@LoadBalanced + interceptor).
URL: http://USER-SERVICE/user/fetchUserById/{id} — USER-SERVICE is the logical name registered in Eureka.

fetchUserDetailsFromUserId(Integer)
Annotations :
@Retry(name = "userServiceRetry", fallbackMethod = "fetchUserDetailsFallback") → Resilience4j retries on failure; after all attempts → fallback.
What it does :
restTemplate.getForObject("http://USER-SERVICE/user/fetchUserById/" + id, UserDTO.class).
Return UserDTO.

fetchUserDetailsFallback(Integer, Exception)
Annotations : none (invoked by @Retry).
What it does :
Log error.
Throw OrderServiceException(503, "User service is currently unavailable. Please try again shortly.").

SequenceGenerator

Class-level
@Service.

generateNextOrderId()
Annotations : none.
What it does :
Uses MongoOperations.findAndModify(...):
Query: _id = "sequence".
Update: $inc: { sequence: 1 }.
Options: returnNew(true) (return updated doc) + upsert(true) (create if not exists).
Returns counter.getSequence() → an int.
Atomic — safe under concurrent requests because Mongo's findAndModify is atomic.

PART 3: SCHEDULER

OrderStatusScheduler

Class-level
@Component → Spring bean.
@EnableScheduling → turns on @Scheduled support in this app.

Fields
@Value("${scheduler.order.status.delay:60000}") → delay between runs in ms; default 60s.
@Value("${scheduler.order.status.enabled:true}") → on/off switch.

updateOrderStatus() — runs every delayInMilliseconds
Annotations :
@Scheduled(fixedDelayString = "${scheduler.order.status.delay:60000}") → runs periodically with a fixed delay between completions.
@Transactional → the whole batch runs in one transaction (safe save).
What it does :
If disabled → log & return.
orderRepository.findByStatusNotIn([CANCELLED, DELIVERED]) → get all active orders.
For each active order :
Skip if createdAt null, status null.
Compute minutes since createdAt via ChronoUnit.MINUTES.between(...).
getStatusBasedOnTime(minutes) → simulate progression:
< 2 min → PENDING
< 4 min → CONFIRMED
< 7 min → PREPARING
< 10 min → READY
< 14 min → OUT_FOR_DELIVERY
else → DELIVERED
If new status differs from current → update status, updatedAt, stamp the matching timestamp field, orderRepository.save(order).
Purpose: simulates a restaurant kitchen / delivery pipeline moving orders through states automatically (since this demo project has no real staff dashboard).

PART 4: FILTERS

CorrelationIdFilter (@Component, @Order(1))
Same as other services: read/generate X-Correlation-Id, put in MDC, chain, finally remove.

CorrelationIdRestTemplateInterceptor (@Component)
Same as foodcatalogue: reads correlationId from MDC and sets it as header on outgoing RestTemplate calls to USER-SERVICE (and any other service).
Ensures logs across order-service and userinfo-service share the same correlation id.

PART 5: CONFIG

CacheConfig
Annotations : 
@Configuration, @EnableCaching.

Bean cacheManager
Same JSON serializer setup as other services.
Default TTL 10 min; disableCachingNullValues().
Named cache: orderServiceUser → TTL 15 min.

RestTemplateConfig
Annotations : 
@Configuration.

Bean restTemplate: 
@Bean, @LoadBalanced → resolves http://USER-SERVICE/... via Eureka + client-side LB.
Attaches CorrelationIdRestTemplateInterceptor.

PART 6: ENTITY / DTOs / REPO

Entities
Order — @Document("order") (MongoDB)
Fields:
id → Mongo document id (String)
orderId → business id (Integer)
foodItemsList → List<FoodItemsDTO>
restaurant → Restaurant (DTO snapshot)
userDTO → UserDTO (snapshot)
status → OrderStatus enum
deliveryAddress, paymentMethod, totalAmount, deliveryInstructions
createdAt, updatedAt
confirmedAt, preparingAt, readyAt, outForDeliveryAt, deliveredAt, cancelledAt

Sequence — @Document(collection="sequence")
id (String, e.g. "sequence")
sequence (int) — the counter used by SequenceGenerator.

OrderStatus (Enum)
Values: PENDING, CONFIRMED, PREPARING, READY, OUT_FOR_DELIVERY, DELIVERED, CANCELLED.
Each has a displayName.
isCancellable() → true except for DELIVERED / CANCELLED.
isEditable() → true only for PENDING / CONFIRMED.

DTOs
OrderDTOFromFE → incoming order from frontend: foodItemsList, userId, restaurant, deliveryAddress, paymentMethod, totalAmount, deliveryInstructions.
OrderDTO → outgoing order: orderId, foodItemsList, restaurant, userDTO, status (string), plus the same order fields as OrderDTOFromFE.
FoodItemsDTO → id, itemName, itemDescription, isVeg, price, restaurantId, quantity (same as foodcatalogue's).
Restaurant → client-side copy: id, name, address, city, restaurantDescription.
UserDTO → client-side copy from userinfo: id, name, password, city, address + nested AddressDTO.
AddressDTO → addressLine1, addressLine2, city, state, pincode, country.

Repository
OrderRepository extends MongoRepository<Order, String>:
Optional<Order> findByOrderId(Integer orderId)
List<Order> findByUserDTO_Id(Integer userId) → query on nested field
List<Order> findByStatus(OrderStatus status)

List<Order> findByStatusNotIn(List<OrderStatus> statuses) → used by scheduler


PART 7: FLOW DIAGRAM

<img width="2439" height="4476" alt="FLOW-DIAGRAM" src="https://github.com/user-attachments/assets/2c12ba9f-0cc9-4875-8810-817a79747bbd" />


PART 8: SEQUENCE DIAGRAMS

1. POST /saveOrder

<img width="7672" height="3633" alt="saveOrder" src="https://github.com/user-attachments/assets/a279f7b7-c6d5-489d-8ecf-86893efc7e93" />

2. GET /order/history/{userId}

<img width="2892" height="1554" alt="order-history" src="https://github.com/user-attachments/assets/3e0c1eed-fa32-4b52-967a-de9330fe2a03" />

3. PUT /order/cancel/{orderId}

<img width="2913" height="2412" alt="order-cancel" src="https://github.com/user-attachments/assets/ac2c0a9b-b1be-46c7-b8da-a47a7f4f496c" />

4. Scheduler auto-progressing orders

<img width="3309" height="2296" alt="auto-progressing" src="https://github.com/user-attachments/assets/b8b3430e-9131-4fae-9acf-91058aa2c9d7" />

5. Retry + fallback for user call

<img width="3421" height="1818" alt="Retry-fallback" src="https://github.com/user-attachments/assets/da80e53d-efb8-4147-a21e-25ba82c0c085" />


PART 9: CLASS DIAGRAM

<img width="8558" height="6811" alt="CLASS-DIAGRAM" src="https://github.com/user-attachments/assets/93907ca8-7d4f-4f6f-bdd0-85657c406e29" />
