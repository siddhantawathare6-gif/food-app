
PART 1: FILTERS (this is the core of the gateway)

CorrelationIdGlobalFilter (@Component)
Annotations : 
@Component → registered as a Spring bean; picked up by Spring Cloud Gateway automatically.
Implements GlobalFilter, Ordered → runs for every request that passes through the gateway.

filter(exchange, chain)
What it does :
Reads header X-Correlation-Id from the incoming request.
If missing/blank → generate UUID.randomUUID().
Mutates the incoming request to include the header (using exchange.getRequest().mutate().header(...)).
This is crucial: the header now travels to the downstream microservice, so all services share the same correlation id.
Sets the same header on the outgoing response (so the client sees it too).
Calls chain.filter(...) with the mutated exchange.

getOrder()
Annotations : none (just an override).
Returns Ordered.HIGHEST_PRECEDENCE → runs before routing, i.e. before any other gateway filter including JwtAuthenticationFilter (-50).

Why this ordering matters
CorrelationId first → any log emitted by the JWT filter (or anything downstream) already carries the correlation id.

JwtAuthenticationFilter (@Component)
Annotations : 
@Component → Spring bean; picked up as a global filter.
Implements GlobalFilter, Ordered → runs on every gateway request.

Whitelisted paths (defined as constants)

PUBLIC_PATHS (no auth needed, any method):
/api/auth/login, /api/auth/register, /api/auth/signin, /api/auth/signup.
PUBLIC_GET_PATHS (auth-free only for GET — browsing):
/restaurant/fetchAllRestaurant, /restaurant/fetchById, /restaurant/image, /foodCatalogue, /user/image.

ADMIN_ONLY_ROUTES (needs valid JWT + specific role):
/restaurant/addRestaurant → ROLE_ADMIN
/restaurant/uploadImage/ → ROLE_ADMIN

filter(exchange, chain)
What it does (step by step):
Read path and method from the incoming request.
If path matches a PUBLIC_PATHS entry OR (GET + matches PUBLIC_GET_PATHS) → skip auth, call chain.filter(exchange).
Otherwise treat as protected:
Read Authorization header.
If missing or doesn't start with Bearer → respond 401 with JSON body.
Extract token (substring(7)).
If !jwtUtils.isTokenValid(token) → respond 401 "Invalid or expired token".
If path matches an ADMIN_ONLY_ROUTES entry:
jwtUtils.extractRoles(token) → get list of role strings.
If the required role isn't present → respond 403 "You do not have permission...".
If all checks pass → chain.filter(exchange) → request is forwarded to the downstream service.

getOrder()
Returns -50 → runs after CorrelationIdGlobalFilter but before other lower-priority filters.

unauthorized(...) helper
Sets status code (401 or 403).
Sets Content-Type: application/json.
Builds a small JSON body: {timestamp, message, details: uri=...}.
Writes it to the response using a DataBuffer.
Returns the reactive Mono<Void>.

PART 2: UTILITIES
GatewayJwtUtils (@Component)
Annotations : 
@Component → Spring bean.
@Value("${app.jwt.secret}") → injects the JWT secret (Base64-encoded) from application.yml.

Methods
extractUsername(token)
Parse claims; return subject.
On any exception → return null.

isTokenValid(token)
Try to parse claims.
Success → true.
JwtException or IllegalArgumentException → false.

getSignKey() (private)

Keys.hmacShaKeyFor(Decoders.BASE64.decode(secret)) → HMAC signing key from the Base64 secret.
Used to verify the signature of incoming tokens.
parseClaims(token) (private)
Jwts.parser().verifyWith(getSignKey()).build().parseSignedClaims(token).getPayload().
Throws if signature invalid or token expired.

extractRoles(token)

Parse claims. Look for roles claim.
If it's a List, map each element:
If element is a Map with an authority key → use that value.
Otherwise use the element as-is.
Return list of role strings (e.g. ["ROLE_USER", "ROLE_ADMIN"]).
On exception → return empty list.

PART 3: CONFIG

RateLimiterConfig (@Configuration)
Annotations :
@Configuration → declares beans.
@Autowired GatewayJwtUtils jwtUtils → inject util for parsing tokens.

Bean ipKeyResolver()
Annotations :

@Primary → when two KeyResolver beans exist, this one is the default used by any RequestRateLimiter filter that doesn't explicitly name a resolver.
@Bean → registers bean.

What it does:
Returns a KeyResolver lambda:
Extract clientIp = exchange.getRequest().getRemoteAddress().getAddress().getHostAddress().
Log it.
Return Mono.just(clientIp).
Purpose: rate-limit public routes by IP (since there's no user identity to bucket on).

Bean userKeyResolver()
Annotations :
@Bean → registers bean.
What it does :
Returns a KeyResolver lambda:
Read Authorization header.
If Bearer <token>:
Extract token.
jwtUtils.extractUsername(token) → username.
If username present → return Mono.just(username).
Fallback: no valid token → use "anonymous:" + clientIp.
Purpose: rate-limit authenticated routes per-user (so one user can't hammer the API; each user has their own bucket).


PART 4: FLOW DIAGRAM

<img width="4462" height="3335" alt="Flow Diagram" src="https://github.com/user-attachments/assets/f69d1b70-60c4-4dce-8944-84f69e2c8572" />


PART 5: SEQUENCE DIAGRAMS

1. Public request (login)

<img width="5419" height="2841" alt="Public-login" src="https://github.com/user-attachments/assets/e864bc62-531c-4f30-b421-67c990c956b2" />

2. Protected request (order history)

<img width="6738" height="2907" alt="Protected-order" src="https://github.com/user-attachments/assets/7d5cc45d-7acd-4fb5-b1cf-95c52f1bb44a" />


3. Missing token → 401

<img width="2940" height="1587" alt="Missing-token" src="https://github.com/user-attachments/assets/6cfb1658-6059-426e-b453-27f7c4715153" />

4. Admin route without required role → 403

<img width="4111" height="2082" alt="Admin-route" src="https://github.com/user-attachments/assets/b70434f7-92eb-4d8a-ab94-97b69f2924f0" />

5. Rate limiting key resolution

<img width="2885" height="2735" alt="Rate-limit" src="https://github.com/user-attachments/assets/05a834b4-bcac-4034-a571-e9046aa47a80" />

<img width="3143" height="4713" alt="Key-resolution" src="https://github.com/user-attachments/assets/2f9eff93-dca0-4a73-9632-a7e2aef57fee" />

6. Route Matching

<img width="4787" height="2630" alt="Route-Matching" src="https://github.com/user-attachments/assets/cbe2a917-7e9a-45c7-8629-c02247601b42" />


PART 6: CLASS DIAGRAM

<img width="4359" height="2118" alt="CLASS-DIAGRAM" src="https://github.com/user-attachments/assets/1faa414a-7cd0-4e4f-bb71-84211c6bbb62" />



