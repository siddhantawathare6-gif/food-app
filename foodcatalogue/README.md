
PART 1: CONTROLLER

FoodCatalogueController (/foodCatalogue)

Class-level
@RestController → JSON responses.
@RequestMapping("/foodCatalogue") → base path.
Commented @CrossOrigin → CORS handled by gateway.

POST /addFoodItem
Annotations :
@PostMapping("/addFoodItem").
@RequestBody FoodItemDTO foodItemDTO → parses JSON body.
What the method does :
Logs item name, price, restaurantId, quantity.
Calls foodCatalogueService.addFoodItem(dto) → returns saved FoodItemDTO.
Returns 201 CREATED.

GET /fetchRestaurantAndFoodItemsById/{restaurantId}
Annotations :
@GetMapping("/fetchRestaurantAndFoodItemsById/{restaurantId}").
@PathVariable Integer restaurantId.
What the method does :
Calls foodCatalogueService.fetchFoodCataloguePageDetails(restaurantId) → returns FoodCataloguePage (restaurant info + its food items list).
Returns 200 OK.

POST /addRestaurantWithFoodItems
Annotations :
@PostMapping("/addRestaurantWithFoodItems").
@RequestBody RestaurantWithFoodItemsDTO → contains restaurant + foodItems list.
What the method does :
Calls foodCatalogueService.addRestaurantWithFoodItems(request) → returns same shape with ids populated.
Returns 201 CREATED.

PUT /updateRestaurantWithFoodItems/{restaurantId}
Annotations :
@PutMapping("/updateRestaurantWithFoodItems/{restaurantId}").
@PathVariable Integer restaurantId + @RequestBody RestaurantWithFoodItemsDTO.
What the method does :
Calls foodCatalogueService.updateRestaurantWithFoodItems(restaurantId, request) → returns updated DTO.
Returns 200 OK.

DELETE /deleteRestaurantWithFoodItems/{restaurantId}
Annotations :
@DeleteMapping("/deleteRestaurantWithFoodItems/{restaurantId}").
@PathVariable Integer restaurantId.
What the method does :
Calls foodCatalogueService.deleteRestaurantWithFoodItems(restaurantId).
Returns 204 NO CONTENT.

PART 2: SERVICE LAYER

FoodCatalogueService

Class-level
@Service → Spring bean.
Injected: FoodItemRepo (DB), CachedRestaurantService (restaurant fetch with caching + retry), RestaurantService (direct restaurant calls).
addFoodItem(FoodItemDTO)
Annotations : none.
What it does :
Map FoodItemDTO → FoodItem entity via FoodItemMapper.
foodItemRepo.save(entity) → INSERT.
Map saved entity → DTO, return.

fetchFoodCataloguePageDetails(Integer restaurantId)
Annotations: none (caching is on the downstream CachedRestaurantService).
What it does :
fetchFoodItemList(restaurantId) → foodItemRepo.findByRestaurantId(restaurantId) → list of FoodItem for that restaurant.
cachedRestaurantService.getRestaurantDetails(restaurantId) → calls restaurant-service through a cache (@Cacheable("foodCatalogueRestaurant")) + retry.
If restaurant is null → throw FoodCatalogueServiceException(404, "Restaurant not found with ID: ...").
Map food item list → DTO list.
Build FoodCataloguePage (restaurant + foodItemList), return.

addRestaurantWithFoodItems(RestaurantWithFoodItemsDTO)
Annotations :
@Transactional → if saving food items fails, the food-item inserts roll back.
What it does :
Step 1: restaurantService.createRestaurant(request.getRestaurant()) → POST to restaurant-service /addRestaurant → returns Restaurant with new id.
Step 2: If food items exist:
For each item → set restaurantId = createdRestaurant.getId().
Map DTOs → FoodItem entities.
foodItemRepo.saveAll(...) → batch INSERT.
Set the created restaurant + saved items back into the request DTO.
Return it.

⚠️ Note: The restaurant creation is a remote call. If saving food items later fails and rolls back, the remote restaurant is not rolled back — a classic distributed-transaction problem. In practice this is often acceptable or handled by a saga.

updateRestaurantWithFoodItems(Integer restaurantId, RestaurantWithFoodItemsDTO)
Annotations :
@Transactional → DB changes are atomic (food item delete + insert).
What it does :
Step 1: restaurantService.updateRestaurant(restaurantId, request.getRestaurant()) → PUT to restaurant-service → returns updated restaurant.
Step 2: foodItemRepo.deleteByRestaurantId(restaurantId) → remove ALL existing food items for that restaurant.
Step 3: For each incoming food item:
set restaurantId.
set id = null (so JPA treats it as new and does INSERT, not UPDATE).
map → entity.
foodItemRepo.saveAll(...) → batch INSERT.
Set saved items back into the request DTO.
Return request.
Simple "replace-all" strategy for updates: delete then re-insert.

deleteRestaurantWithFoodItems(Integer restaurantId)
Annotations :
@Transactional → DB deletes are atomic.
What it does :
foodItemRepo.deleteByRestaurantId(restaurantId) → removes all food items for that restaurant from local DB.
restaurantService.deleteRestaurant(restaurantId) → DELETE to restaurant-service /deleteRestaurant/{id}.
No return value.

RestaurantService (this is the HTTP client to restaurant-service)

Class-level
@Service → Spring bean.
@Autowired RestTemplate (the one from AppConfig — @LoadBalanced + interceptor).
URL used: http://RESTAURANT-SERVICE/restaurant — RESTAURANT-SERVICE is resolved by Eureka through the load-balanced RestTemplate.
fetchRestaurantDetailsFromRestaurantMS(Integer restaurantId)
Annotations :
@Retry(name = "restaurantServiceRetry", fallbackMethod = "fetchRestaurantFallback") → uses Resilience4j. If the call throws, it retries according to the restaurantServiceRetry config (defined in application.yml). After all retries fail → fetchRestaurantFallback(...) is called.
What it does :
restTemplate.getForObject(.../fetchById/{id}, Restaurant.class) → GET restaurant-service.
Returns Restaurant DTO.

createRestaurant(Restaurant)
Annotations : none.
What it does :
Build HttpHeaders with Content-Type: application/json.
Wrap body + headers in HttpEntity.
restTemplate.exchange(... /addRestaurant, POST, entity, Restaurant.class) → POST to restaurant-service.
Return the created Restaurant (with new id).

updateRestaurant(Integer id, Restaurant)
Annotations : none.
What it does :
Build HttpEntity with the new restaurant data.
restTemplate.exchange(... /updateRestaurant/{id}, PUT, entity, Restaurant.class).
Return updated Restaurant.

deleteRestaurant(Integer id)
Annotations : none.
What it does :
restTemplate.delete(... /deleteRestaurant/{id}) → DELETE on restaurant-service.

fetchRestaurantFallback(Integer, Exception)
Annotations : none (called automatically by @Retry).
What it does :
Logs error.
Throws FoodCatalogueServiceException(503, "Restaurant service is currently unavailable. Please try again shortly.") → controller returns 503.

CachedRestaurantService

Class-level
@Service → Spring bean.
Injected: RestaurantService.

getRestaurantDetails(Integer restaurantId)
Annotations :
@Cacheable(value = "foodCatalogueRestaurant", key = "#restaurantId") → checks Redis cache foodCatalogueRestaurant for this id. Hit → return cached Restaurant. Miss → run the method, store result. TTL 20 min (from CacheConfig).
What it does :
Delegates to restaurantService.fetchRestaurantDetailsFromRestaurantMS(id) — which has retry + fallback.
Returns Restaurant.
Why this wrapper class? To keep the @Cacheable boundary clean. If you cached RestaurantService.fetchRestaurantDetailsFromRestaurantMS directly, a fallback exception would also be considered by the cache proxy, which gets messy. Wrapping it in another bean is a common pattern.

PART 3: FILTERS
CorrelationIdFilter (@Component, @Order(1))
Same as other services.
Read/generate X-Correlation-Id → MDC.put("correlationId", id).
Chain continues.
finally → MDC.remove("correlationId").

CorrelationIdRestTemplateInterceptor (@Component)
Annotations
@Component → Spring bean.
Implements ClientHttpRequestInterceptor.
What it does : 
Reads correlationId from MDC.get("correlationId").
If present → sets it as a header X-Correlation-Id on the outgoing RestTemplate request.
Executes the request.
Why this matters: When foodcatalogue calls restaurant-service via RestTemplate, the correlation id propagates to restaurant-service. Their logs share the same id → end-to-end tracing.

PART 4: CONFIG
CacheConfig
Annotations : 
@Configuration, @EnableCaching.

Bean cacheManager
Build ObjectMapper (ignore empty beans, JavaTime, default typing NON_FINAL).
Wrap in GenericJackson2JsonRedisSerializer.
Default TTL 10 min; disableCachingNullValues().
Named cache: foodCatalogueRestaurant → 20 min.
Return RedisCacheManager.

AppConfig
Annotations : 
@Configuration → declares beans.

Bean restTemplate
@Bean → registers RestTemplate as a bean.
@LoadBalanced → makes RestTemplate aware of service names (Eureka client). So http://RESTAURANT-SERVICE/... is resolved via Eureka + client-side load balancing (Spring Cloud LoadBalancer).
restTemplate.setInterceptors(List.of(correlationIdInterceptor)) → attaches the correlation-id interceptor so every outgoing call carries the header.

PART 5: ENTITY / DTOS / REPO

FoodItem (Entity → table food_item)
id (AUTO), itemName, itemDescription, isVeg, price, restaurantId (reference only, no FK to restaurant-service — cross-service boundary), quantity (default 0, not null).

DTOs

FoodItemDTO → same as entity. @JsonProperty("isVeg") because Jackson strips is from boolean getters by default. @JsonIgnoreProperties({"veg"}) ignores an unwanted veg key.
Restaurant → mirror of restaurant-service's DTO (id, name, address, city, description, imageUrl, rating, reviewCount). Lives locally as a client-side copy of the remote contract.
FoodCataloguePage → { restaurant, foodItemsList } — combined view.
RestaurantWithFoodItemsDTO → { restaurant, foodItems } — used for create/update.

FoodItemRepo

List<FoodItem> findByRestaurantId(Integer id) → get all food items for a restaurant.
@Transactional void deleteByRestaurantId(Integer id) → bulk delete (needs @Transactional because delete-by-derived-query runs a modifying query).


PART 6: FLOW DIAGRAM

<img width="2738" height="4924" alt="FLOW-DIAGRAM" src="https://github.com/user-attachments/assets/880a29bb-1182-487a-98fe-cd8400841baa" />


PART 7: SEQUENCE DIAGRAMS

1. POST /addFoodItem

<img width="3658" height="1554" alt="addFoodItem" src="https://github.com/user-attachments/assets/5830d289-9879-4bf6-b340-5c967495774e" />

2. GET /fetchRestaurantAndFoodItemsById/{id} (cache miss)

<img width="9592" height="3369" alt="fetchRestaurantAndFoodItemsById" src="https://github.com/user-attachments/assets/639c7ea0-d355-4ef0-928f-6f0b84b6f9b3" />

3. POST /addRestaurantWithFoodItems

<img width="6261" height="2544" alt="addRestaurantWithFoodItems" src="https://github.com/user-attachments/assets/ecc382de-2c9b-4908-be2e-0cac545d60a4" />

4. PUT /updateRestaurantWithFoodItems/{id}

<img width="6435" height="2379" alt="updateRestaurantWithFoodItems" src="https://github.com/user-attachments/assets/1c955672-c022-4c95-8d33-ce5e312f7b1b" />

5. DELETE /deleteRestaurantWithFoodItems/{id}

<img width="6174" height="2214" alt="deleteRestaurantWithFoodItems" src="https://github.com/user-attachments/assets/94cdb3cc-f069-42e8-ad42-edf38bd76f23" />

6. Correlation ID propagation

<img width="6543" height="3006" alt="Correlation-ID" src="https://github.com/user-attachments/assets/dc1b965e-1ffc-4cb7-a888-0df94b36a3af" />

PART 8: CLASS DIAGRAM

<img width="8726" height="5623" alt="CLASS DIAGRAM" src="https://github.com/user-attachments/assets/301ee7ab-8d00-4613-b48c-c8d097bc4e58" />
















