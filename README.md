# **Eron Back-End Developer Challenge**

In this challenge, the REST API contains information about a collection of movie released after the year 2010, 
directed by acclaimed directors. Given the threshold value, the goal is to use the API to get the list of the names of the directors with
most movies directed. Specially, the list of names of directors with movie count strictly greater than the given threshold.

The list of names must be returned in alphabetical order.

Decisions Taken
As the challenge required an aggregation on an external API with no DB


Resumen
- Microservicio Spring Boot (WebFlux) que consulta el API de películas paginado y expone `GET /api/directors?threshold=X` devolviendo directores con más películas que X, ordenados alfabéticamente.

Requisitos
- JDK 17+ (recomendado 17 u 21)
- Maven Wrapper incluido (`mvnw`/`mvnw.cmd`)

Config (src/main/resources/application.yml)
- `movies.base-url`: URL base del API externo
- `movies.connect-timeout-ms`, `movies.response-timeout-ms`, `movies.max-retries`

Ejecución local
- Tests: `./mvnw -q test`
- Run: `./mvnw spring-boot:run`
- Endpoint: `http://localhost:8080/api/directors?threshold=4`

Estructura
- `controller/`: `DirectorsController`, manejo de errores con `RestExceptionHandler`
- `service/`: `DirectorsService` (agregación reactiva por director)
- `client/`: `MoviesClient` (WebClient + paginación + retries)
- `model/`: DTOs externos e internos
- `config/`: `MoviesProperties`, `HttpClientConfig`

Pruebas
- Servicio: lógica de conteo/orden (`DirectorsServiceTest`)
- Cliente: paginación y reintentos con MockWebServer (`MoviesClientTest`)
- Controlador: contrato y validación con WebTestClient (`DirectorsControllerTest`)

Empaquetado Docker
1) Construir JAR: `./mvnw -q -DskipTests package`
2) Construir imagen: `docker build -t eron-challenge:latest .`
3) Ejecutar: `docker run --rm -p 8080:8080 eron-challenge:latest`

Perfil Docker y variables de entorno
- El contenedor activa el perfil `docker` por defecto (`SPRING_PROFILES_ACTIVE=docker`).
- Puedes parametrizar la URL del API y timeouts con variables de entorno:
  - `MOVIES_BASE_URL` (default: `https://wiremock.dev.eroninternational.com`)
  - `MOVIES_CONNECT_TIMEOUT_MS` (default: `2000`)
  - `MOVIES_RESPONSE_TIMEOUT_MS` (default: `4000`)
  - `MOVIES_MAX_RETRIES` (default: `2`)

Ejemplos
- Ejecutar con una URL distinta:
  `docker run --rm -p 8080:8080 -e MOVIES_BASE_URL=https://mi.api/ eron-challenge:latest`
- Ejecutar local sin Docker, usando el perfil docker:
  `./mvnw spring-boot:run -Dspring-boot.run.profiles=docker -Dspring-boot.run.jvmArguments="-DMOVIES_BASE_URL=https://mi.api/"`

Docker Compose
- Archivo: `compose.yaml`. Variables pueden definirse en `.env` (usa `.env.example` como base).
- Levantar en segundo plano:
  `docker compose up -d --build`
- Ver logs:
  `docker compose logs -f`
- Probar endpoint:
  `curl "http://localhost:8080/api/directors?threshold=4"`
- Detener y limpiar:
  `docker compose down`

Observabilidad (Actuator + Prometheus + Grafana)
- Endpoints Actuator (perfil local y docker):
  - Health: `GET /actuator/health`, liveness/readiness: `/actuator/health/liveness`, `/actuator/health/readiness`
  - Info: `GET /actuator/info`
  - Prometheus: `GET /actuator/prometheus`
- Compose incluye servicios opcionales:
  - Prometheus: http://localhost:9090 (scrapea `api:8080/actuator/prometheus`)
  - Grafana: http://localhost:3000 (user: admin / pass: admin)
- Añadir Prometheus como datasource en Grafana:
  - URL: `http://prometheus:9090`
  - Explorar métricas: `http_server_requests_seconds_count`, `jvm_memory_used_bytes`, etc.


Decisiones técnicas
- WebFlux end-to-end, backpressure y concurrencia controlada al paginar
- DTOs `record` inmutables y mapeo explícito de campos externos
- Propiedades externalizadas y validadas con `@ConfigurationProperties`
- Resiliencia: timeouts + retry exponencial ante errores transitorios (5xx, conexión)
- Errores: `ProblemDetail` (400 validación, 502 upstream)

Mejoras futuras
- Observabilidad: Actuator + Micrometer/Prometheus, logs JSON
- Circuit Breaker con Resilience4j
- CI/CD, escaneo SAST/DAST, Docker multi-arch
