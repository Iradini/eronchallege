# **Eron Back-End Developer Challenge**

In this challenge, the REST API contains information about a collection of movie released after the year 2010, 
directed by acclaimed directors. Given the threshold value, the goal is to use the API to get the list of the names of the directors with
most movies directed. Specially, the list of names of directors with movie count strictly greater than the given threshold.

The list of names must be returned in alphabetical order.

Technical Decisions
As the challenge involves retrieving data from an external API, transforming that data, and presenting it as a single response I used the aggregation pattern. 

API Gateway/Aggregation Service: 
Was not included since there is one internal service to add the data.

- End-to-end WebFlux, backpressure, and controlled concurrency when paginating
- Immutable 'record' DTOs and explicit mapping of external fields
- Externalized and validated properties with '@ConfigurationProperties'
- Resilence: timeouts + exponential retries for transient error(5xx, connection)
- Errors: 'ProblemDetail' (400 validation, 502 upstream)
- Observability: Micrometer/Prometheus
- Circuit Breaker with Resilence4j

Future Improvements:
- CI/CD, SAST/DAST scanning, multi-arch Docker

### **Author**
Maria Iradini Tablante
- [GitHub](https://github.com/Iradini/)
- [LinkedIn](https://www.linkedin.com/in/mariairadini/)


### Summary
- Spring Boot microservice (WebFlux) that queries the paginated movie API and exposes `GET /api/directors?threshold=X` returning directors with more than X movies, sorted alphabetically.

### Requirements
- JDK 17+ (recommended 17 u 21)
- Maven Wrapper included (`mvnw`/`mvnw.cmd`)

### Config 
- `movies.base-url`: URL base del API externo
- `movies.connect-timeout-ms`, `movies.response-timeout-ms`, `movies.max-retries`

### Local Execution
- Tests: `./mvnw -q test`
- Run: `./mvnw spring-boot:run`
- Endpoint: `http://localhost:8080/api/directors?threshold=4`

### Structure
- `controller/`: `DirectorsController`, errors handling with `RestExceptionHandler`
- `service/`: `DirectorsService` (reactive aggregation  by director)
- `client/`: `MoviesClient` (WebClient + pagination + retries)
- `model/`: external and internal DTOs
- `config/`: `MoviesProperties`, `HttpClientConfig`

### Tests
- Service: counting/ordering logic (`DirectorsServiceTest`)
- Client:  paging and retries with MockWebServer (`MoviesClientTest`)
- Controller: contract and validation with WebTestClient (`DirectorsControllerTest`)

### Docker Packaging
1) Build JAR: `./mvnw -q -DskipTests package`
2) Build imagen: `docker build -t eron-challenge:latest .`
3) Execute: `docker run --rm -p 8080:8080 eron-challenge:latest`

- Run with a different URL:
  `docker run --rm -p 8080:8080 -e MOVIES_BASE_URL=https://mi.api/ eron-challenge:latest`
- Run locally without Docker, using the docker profile:
  `./mvnw spring-boot:run -Dspring-boot.run.profiles=docker -Dspring-boot.run.jvmArguments="-DMOVIES_BASE_URL=https://mi.api/"`
