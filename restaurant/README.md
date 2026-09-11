
PART 1: SERVICE LAYER
RestaurantServiceImpl
Class-level
@Service → marks this class as a Spring-managed bean in the business layer; Spring instantiates it once and can inject it into any other bean.

Fields injected via constructor: restaurantRepository (MySQL access), fileStorageService (disk access).

@Value("${app.image.base-url:/restaurant/image}") on baseImageUrl → pulls the value from application.yml at startup; if missing, uses /restaurant/image. This is used to build image URLs for DTOs.

featchAllRestaurant(pageNo, pageSize, sortBy, sortDir)
Annotations:

@Cacheable(value = "restaurantPage", key = "#pageNo + '-' + #pageSize + '-' + #sortBy + '-' + #sortDir") → before the method runs, Spring builds a key like "0-10-name-asc" and checks Redis cache restaurantPage. Hit → returns cached DTO, method body skipped. Miss → runs the method, stores result in Redis under that key (TTL 5 min from CacheConfig).

What the method does:

Build Sort from sortDir (asc → Sort.by(sortBy).ascending(), else descending).

Build Pageable = PageRequest.of(pageNo, pageSize, sort).

Call restaurantRepository.findAll(pageable) → returns Page<Restaurant> (content + totalElements + totalPages + isLast).

For each entity → map to RestaurantDTO via RestaurantMapper, then set dto.imageUrl = baseImageUrl + "/" + id.

Build RestaurantPageDto with: restaurantList, pageNo, pageSize, totalElement, totalPage, isLast.

Return → Spring caches it.

addRestaurant(dto)
Annotations:

@CacheEvict(value = "restaurantPage", allEntries = true) → after this method finishes, wipe the entire restaurantPage cache (because the list just changed, cached pages are stale).

What the method does:

Map RestaurantDTO → Restaurant entity via RestaurantMapper.

restaurantRepository.save(entity) → INSERT (id is null → Spring Data treats it as new; auto-generated id is populated).

Map saved entity → DTO, set dto.imageUrl = baseImageUrl + "/" + id.

Return DTO.

fetchRestaurantById(id)
Annotations:

@Cacheable(value = "restaurant", key = "#id") → checks Redis cache restaurant with the id as key. Hit → returns cached DTO, body skipped. Miss → runs method, stores result (TTL 30 min).

What the method does:

restaurantRepository.findById(id) → Optional<Restaurant>.

If empty → throw RestaurantNotFoundException (→ 404 upstream).

Map entity → DTO, set imageUrl.

Return.

uploadRestaurantImage(id, file)
Annotations:

@CacheEvict(value = {"restaurant", "restaurantPage"}, allEntries = true) → after upload, wipe both caches (single-by-id cache is stale because image URL changed; page cache because every list DTO carries an imageUrl).

What the method does:

findById(id) → or throw RestaurantNotFoundException.

If restaurant.imageUrl != null → fileStorageService.deleteFile(oldFilename) (delete old image file to prevent orphans with different extensions).

fileStorageService.storeFile(id, file) → returns new filename like "5.png".

Set restaurant.imageUrl = filename, then save() → UPDATE.

Return URL: baseImageUrl + "/" + id.

getRestaurantImage(id)
Annotations: none — returns raw bytes, so caching is skipped.

What the method does:

findById(id) → or throw.

If imageUrl == null → return getDefaultImage().

Else → fileStorageService.readFile(imageUrl) → return byte[].

getDefaultImage()
Annotations: none.

What the method does:

fileStorageService.readFile("default.jpg") → return byte[] (empty if file missing).

updateRestaurant(id, dto)
Annotations:

@CacheEvict(value = {"restaurant", "restaurantPage"}, allEntries = true) → after update, wipe both caches (data changed for this id and every page containing it).

What the method does:

findById(id) → or throw.

Copy ONLY these 4 fields from DTO: name, address, city, restaurantDescription.

Skip: imageUrl (handled by upload endpoint), rating / reviewCount (owned by order service).

save() → UPDATE.

Map to DTO, set imageUrl, return.

deleteRestaurant(id)
Annotations:

@CacheEvict(value = {"restaurant", "restaurantPage"}, allEntries = true) → after delete, wipe both caches (id no longer exists; every page that showed it is stale).

What the method does:

findById(id) → or throw (so a non-existent id triggers 404, not a silent no-op).

If imageUrl != null → delete image file from disk.

deleteById(id) → DELETE row.

FileStorageServiceImpl
Class-level
@Service → marks as Spring bean.

@Value("${app.image.upload-dir:uploads/restaurants}") on uploadDir → pulls folder path from config with a fallback.

init() — runs once at startup
Annotations:

@PostConstruct → Spring calls this method automatically right after the bean is created and dependencies are injected (before any request is served).

What the method does:

Check if uploadDir exists; if not, Files.createDirectories(uploadPath).

On IOException → throw RestaurantServiceException(500, "Failed to initialize file storage").

Guarantees the folder exists before any upload endpoint is called.

storeFile(id, file)
Annotations: none.

What the method does:

Validate:

file.isEmpty() → throw RestaurantServiceException(400, "Uploaded file is empty").

contentType null or doesn't start with image/ → throw RestaurantServiceException(400, "Only image files are allowed").

Extract extension from originalFilename (from last . to end); if none → fallback ".jpg".

Target path: uploadDir + "/" + id + extension (e.g. uploads/restaurants/5.jpg).

Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING) → overwrites if the same name exists.

On IOException → throw RestaurantServiceException(500, "Failed to store uploaded file").

Return filename (e.g. "5.jpg").

readFile(fileName)
Annotations: none.

What the method does:

If file doesn't exist → return new byte[0] (no exception — caller handles empty as "missing").

Else → Files.readAllBytes(filePath).

On IOException → throw RestaurantServiceException(500, "Failed to read stored file").

deleteFile(fileName)
Annotations: none.

What the method does:

Files.deleteIfExists(filePath) → no error if already gone.

On IOException → only log.warn and continue (never throws — cleanup failure must not break the main operation).

PART 2: FILTER
CorrelationIdFilter
Annotations:

@Component → registers the class as a Spring bean so it's picked up by Spring Boot's filter chain.

@Order(1) → runs first (before other filters).

What the filter does:

Read header X-Correlation-Id from the incoming request.

If null/blank → generate UUID.randomUUID().toString().

MDC.put("correlationId", value) → any log line in this request (assuming logback pattern uses %X{correlationId}) will include this id.

filterChain.doFilter(request, response) → pass the request down the chain.

finally { MDC.remove("correlationId") } → clears MDC so the next request on the same (pooled) Tomcat thread doesn't inherit a stale id.

Does NOT write the header back on the response (the line is commented out).

Purpose: every request gets one trace id; logs from this service (and any service that forwards the same header) can be grepped by that id.

PART 3: CONFIG
CacheConfig
Annotations:

@Configuration → declares this class produces Spring beans.

@EnableCaching → turns on annotation-driven caching support (@Cacheable, @CacheEvict, etc.) for the whole app.

@Bean cacheManager(RedisConnectionFactory) — what it does:

Build an ObjectMapper:

disable(FAIL_ON_EMPTY_BEANS) → don't fail on empty objects.

registerModule(new JavaTimeModule()) → support LocalDate / LocalDateTime.

activateDefaultTyping(..., NON_FINAL) → embed class info in JSON so deserialization from Redis produces the right type (needed for RestaurantPageDto).

Wrap it in GenericJackson2JsonRedisSerializer.

Build RedisCacheConfiguration:

Default TTL 10 min.

Use the JSON serializer for values.

disableCachingNullValues() → don't cache nulls (avoids "cache the miss").

Return a RedisCacheManager with:

Default config for unnamed caches.

restaurant cache → TTL 30 min.

restaurantPage cache → TTL 5 min.

Result: all @Cacheable / @CacheEvict in the service layer automatically use this manager and these TTLs.


 
 1. Flow Diagram (Request → Response)
 
 <img width="3169" height="2810" alt="request-response" src="https://github.com/user-attachments/assets/bc798df7-8c5a-49c6-9973-38a459938373" />


 2. Component / Class Diagram
    
<img width="6903" height="6798" alt="class-diagram" src="https://github.com/user-attachments/assets/292dd031-d273-4608-9671-afa68cb34039" />


3. Sequence Diagram — GET /fetchById/{id} (cache miss → hit)

<img width="4083" height="3201" alt="GET-fetchById" src="https://github.com/user-attachments/assets/897bc8e5-94e9-4c62-8000-6da9c46b37d5" />


4. Sequence Diagram — POST /addRestaurant

<img width="4017" height="1719" alt="POST-addRestaurant" src="https://github.com/user-attachments/assets/562d5d78-b28b-440c-8913-90efad3d3ace" />


5. Sequence Diagram — POST /uploadImage/{id}

<img width="5787" height="2709" alt="POST-uploadImage" src="https://github.com/user-attachments/assets/200c6ab5-dd33-46f8-9a1e-a88938ed186c" />


6. Sequence Diagram — DELETE /deleteRestaurant/{id}

<img width="5343" height="2214" alt="DELETE-deleteRestaurant" src="https://github.com/user-attachments/assets/5c23d82a-3e59-494d-8c07-0420022bb7d8" />


7. Sequence Diagram — GET /fetchAllRestaurant (paginated + cached)
   
<img width="6207" height="2577" alt="GET-fetchAllRestaurant" src="https://github.com/user-attachments/assets/a38341f6-d77b-4e6f-969b-dcf7e640e714" />


8. Cache Interaction Overview

<img width="2092" height="2971" alt="Cache-Interaction" src="https://github.com/user-attachments/assets/92cc1594-8018-4b09-b8b9-1864274d3e6b" />


9. Filter & MDC Flow

<img width="1222" height="3154" alt="Filter-MDC" src="https://github.com/user-attachments/assets/4763d7fc-46cd-4376-8ada-2c7377bb8dd0" />


10. Package Structure

<img width="11235" height="996" alt="Package-Structure" src="https://github.com/user-attachments/assets/b08bf775-1b34-45fd-a45b-d085b55315d2" />













