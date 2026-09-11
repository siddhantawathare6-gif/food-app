
PART 1: CONTROLLERS
AuthController (/api/auth)

Class-level
@RestController → marks class as REST controller; every method returns JSON.
@RequestMapping("/api/auth") → base path for auth endpoints.
Commented @CrossOrigin → CORS is handled centrally by the API Gateway.

POST /login and /signin
Annotations:

@PostMapping(value = {"/login", "/signin"}) → maps both /api/auth/login and /api/auth/signin to this method (both work, same handler).
@Valid on LoginDTO login → triggers Bean Validation on the DTO (@NotBlank on emailOrUsername and password).
@RequestBody → parses JSON body into LoginDTO.

What the method does:

Logs the attempt with the email/username masked via maskEmailOrUsername(...) (never logs the raw identifier).
Calls authService.login(login) → returns JwtAuthResponse (contains access token, token type Bearer, userId).

Returns 200 OK with the JWT response.

POST /register and /signup
Annotations:

@PostMapping(value = {"/register", "/signup"}) → both URLs map here.
@Valid @RequestBody RegisterDTO register → validates name, username, email, password, mobileNumber using constraints inside RegisterDTO.

What the method does:
Logs the attempt (username + masked email).
Calls authService.register(register) → returns a success string.

Returns 201 CREATED with the message.

UserController (/user)
Class-level
@RestController → JSON responses.
@RequestMapping("/user") → base path.

POST /addUser
Annotations:

@PreAuthorize("hasRole('ADMIN')") → only users with ROLE_ADMIN can call this. If not admin → 403.
@PostMapping("/addUser") → maps endpoint.
@RequestBody UserDTO userDTO → JSON → UserDTO.

What the method does:

Calls userService.addUser(userDTO) → returns saved UserDTO.
Returns 200 OK.

GET /fetchUserById/{userId}
Annotations:

@GetMapping("/fetchUserById/{userId}").
@PathVariable Long userId → binds URL segment into the method arg.

What the method does:

Calls userService.fetchUserDetailsById(userId) → returns UserDTO.
Returns 200 OK.

GET /profile
Annotations:

@GetMapping("/profile").

Authentication authentication (Spring injects it) → represents the currently authenticated user extracted from JWT.

What the method does:

authentication.getName() → username from SecurityContext (set by JwtAuthFilter).
Calls userService.getUserProfileByUsername(username) → returns UserDTO.
Returns 200 OK.

PUT /profile
Annotations:

@PutMapping("/profile").

@Valid @RequestBody UpdateUserDTO → validates the update payload (name size, alternate mobile pattern via @OptionalPattern).

Authentication → currently logged-in user.

What the method does:

Gets username from Authentication.
Calls userService.updateUserProfile(username, dto) → returns updated UserDTO.
Returns 200 OK.

GET /admin
Annotations:

@PreAuthorize("hasRole('ADMIN')") → admin-only.
@GetMapping("/admin").

What the method does:

Returns a simple string "Admin only can access" — a demo protected endpoint to verify role-based access.

POST /uploadImage/{id}
Annotations:

@PostMapping("/uploadImage/{id}").
@PathVariable Long id.
@RequestParam("image") MultipartFile file → binds uploaded file.

What the method does:

Calls userService.uploadUserImage(id, file) → returns image URL.
Returns 200 OK with URL string.

GET /image/{id}
Annotations:

@GetMapping(value = "/image/{id}", produces = MediaType.IMAGE_JPEG_VALUE) → response is raw JPEG bytes, not JSON.
@PathVariable Long id.

What the method does:

Calls userService.getUserImage(id) → byte[].

If empty → 404. Else → 200 OK with bytes.

PART 2: SERVICE LAYER
AuthServiceImpl
Class-level
@Service → Spring bean.

Injected: AuthenticationManager, UserRepository, RoleRepository, PasswordEncoder, JwtUtils.

login(LoginDTO)
Annotations: none (no caching for login).

What the method does:

authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(emailOrUsername, password)) → triggers Spring Security authentication flow → internally calls CustomUserDetailService.loadUserByUsername(...) to fetch user and compares password (BCrypt).
On failure → Spring throws BadCredentialsException (handled upstream → 401).
On success → puts the Authentication into SecurityContextHolder.

Casts principal to UserDetails.

jwtUtils.generatToken(principal) → returns signed JWT string.

Looks up the actual User entity (by username, then by email as fallback) to get the numeric id.

Builds JwtAuthResponse with: accessToken, tokenType = "Bearer", userId.

Returns.

register(RegisterDTO)
Annotations: none (registration isn't cached).

What the method does:

Check userRepository.existsByEmail(...) → if yes → throw UserAlreadyRegisterException(400, "email already exists!.").
Check existsByUsername(...) → throw if exists.
Check existsByMobileNumber(...) → throw if exists.

Create User entity, set name/email/username/password/mobileNumber.

passwordEncoder.encode(password) → hashes the raw password (BCrypt).

Fetch ROLE_USER from RoleRepository → if missing → throw UserinfoApiException(500, "System configuration error...").
Assign roles set to user.
userRepository.save(user) → INSERT.

Return "user register successfully".

UserServiceImpl
Class-level
@Service → Spring bean.

@Value("${app.image.upload-dir:uploads/users}") on uploadDir → folder path from config.

@Value("${app.image.base-url:/user/image}") on baseImageUrl → URL prefix for image URLs.

addUser(UserDTO)
Annotations: none.

What the method does:

Check existsByUsername and existsByEmail → throw UserinfoApiException(400) if either exists.
Map UserDTO → User via UserMapper.
userRepository.save(user) → INSERT.
Map back → DTO, set imageUrl = baseImageUrl + "/" + id.
Return DTO.

fetchUserDetailsById(Long userId)
Annotations:

@Cacheable(value = "user", key = "#userId") → before running, check Redis cache user with the id as key. Hit → return cached DTO. Miss → run, store result.

What the method does:

userRepository.findById(userId) → or throw UserinfoApiException(404).
Map entity → DTO, return.
updateUserProfile(String username, UpdateUserDTO dto)
Annotations:

@Transactional → the whole method runs in one DB transaction. If any exception is thrown, changes are rolled back.
@CacheEvict(value = "user", key = "#result.id") → after the method returns the updated DTO, evict the cache entry for that user's id. #result refers to the returned value.

What the method does:

findByUsername(username) → or throw 404.
If dto.name not null → update name.
If dto.alternateMobileNumber not null:
Trim it.

If not empty → check findByAlternateMobileNumber(...) → if it belongs to another user → throw 400 "Alternate mobile number is already registered to another user.". Else set it.
If empty string → set alternateMobileNumber to null.
If dto.address not null:
Get or create Address object.
Copy each non-null field (addressLine1, addressLine2, city, state, pincode, country).
Set address on user.

userRepository.save(user) → UPDATE.
Map to DTO, set imageUrl, return.
getUserProfileByUsername(String username)
Annotations: none (uses findByUsername — could be cached but isn't).

What the method does:

findByUsername(username) → or throw 404.

Map entity → DTO, set imageUrl, return.

uploadUserImage(Long userId, MultipartFile file)
Annotations:

@Transactional → DB update is atomic.

@CacheEvict(value = "user", key = "#userId") → evict the cached DTO for this user (its imageUrl just changed).

What the method does:

findById(userId) → or throw 404.

Validate: file not empty; content type starts with image/. Else → 400.

Ensure uploadDir exists → Files.createDirectories(...).

Extract extension from originalFilename (fallback .jpg).

File name: {userId}{extension}, e.g. 12.jpg.

If user already had an image with a different filename → Files.deleteIfExists(uploadPath.resolve(oldName)).

Files.copy(stream, filePath, REPLACE_EXISTING) → saves the file.

Set user.imageUrl = fileName, save() → UPDATE.

Return baseImageUrl + "/" + userId.

On IOException → throw UserinfoApiException(500, "Failed to store uploaded file").

getUserImage(Long userId)
Annotations: none (raw bytes, no caching).

What the method does:

findById(userId) → or throw 404.

If user.imageUrl != null:

Build path uploadDir/{imageUrl}.

If file exists → return Files.readAllBytes(...).

Else → log warning and fall through.

Fallback → read uploadDir/avatar.jpg. If exists → return bytes.

If even fallback missing → return new byte[0] (controller → 404).

On IOException → throw 500.

PART 3: SECURITY COMPONENTS
JwtAuthFilter (@Component, @Order(2))
Annotations
@Component → registered as a Spring bean.

@Order(2) → runs AFTER CorrelationIdFilter (@Order(1)) but before the rest of the filter chain.

What it does
Reads Authorization header via getTokenFromRequest(...).

Expects format Bearer <token>.

Returns the token string (after substring(7)), or null.

If token present and jwtUtils.validateToken(token) → true:

jwtUtils.extractUsername(token) → get username.

If username present AND SecurityContextHolder has no auth yet:

userDetailsService.loadUserByUsername(username) → UserDetails with authorities.

Build UsernamePasswordAuthenticationToken(userDetails, null, authorities).

Attach request details (IP, session id, etc.).

SecurityContextHolder.getContext().setAuthentication(authToken) → marks this request as authenticated.

Always calls filterChain.doFilter(request, response) → continues chain.

CustomUserDetailService
Annotations
@Service → Spring bean; Spring Security uses it automatically as the UserDetailsService.

What it does
Implements loadUserByUsername(usernameOrEmail).

userRepository.findByUsernameOrEmail(usernameOrEmail, usernameOrEmail) → or throw UsernameNotFoundException.

Map each Role → SimpleGrantedAuthority(role.getName()) → gives ROLE_USER, ROLE_ADMIN, etc.

Return Spring Security's User(username, password, authorities).

JwtAuthenticationEntryPoint
Annotations
@Component → Spring bean; registered in SecurityConfig (not shown) as the entry point.

What it does
Implements commence(...) — called when an unauthenticated request hits a protected endpoint.

Sends 401 UNAUTHORIZED with the exception message.

Prevents Spring's default HTML login page from being returned.

PART 4: FILTER
CorrelationIdFilter (@Component, @Order(1))
Annotations
@Component → Spring bean.

@Order(1) → runs before JwtAuthFilter so even auth-related logs carry the correlation id.

What it does
Read X-Correlation-Id header.

If null/blank → UUID.randomUUID().

MDC.put("correlationId", value) → every log line includes it.

Continue chain.

finally → MDC.remove("correlationId") (prevent leaks across pooled threads).

PART 5: ENTITIES / DTOS / VALIDATION
Entities
User (@Entity) → id, name, username (unique), password, email (unique), mobileNumber (unique), alternateMobileNumber (unique), embedded Address, createdAt, updatedAt, imageUrl, Set<Role> (ManyToMany via user_roles join table).

@PrePersist onCreate() → sets createdAt & updatedAt on insert.

@PreUpdate onUpdate() → refreshes updatedAt on update.

Role (@Entity) → id, name (ROLE_USER, ROLE_ADMIN, ...).

Address (@Embeddable) → embedded inside User; no separate table.

DTOs
RegisterDTO → name, username, email, password, mobileNumber, roles; each validated with @NotBlank, @Size, @Email, @Pattern.

LoginDTO → emailOrUsername, password (@NotBlank on both).

JwtAuthResponse → accessToken, tokenType (Bearer), userId.

UserDTO → full user payload returned to clients (imageUrl already built).

UpdateUserDTO → name, alternateMobileNumber (validated via @OptionalPattern), nested @Valid AddressDTO.

AddressDTO → mirrors Address.

Custom Validation
@OptionalPattern → custom annotation; only validates the pattern if the field is non-null and non-empty.

OptionalPatternValidator → implements ConstraintValidator<OptionalPattern, String>:

null/empty → valid (skip).

else → value.matches(regexp).
