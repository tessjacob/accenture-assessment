# Copilot Instructions for Holiday Service

## Project Overview
**Holiday Service** is a Spring Boot 3.5.9 REST API (Java 21) that integrates with the Nager.Date API to retrieve holiday information. The project follows a standard layered architecture with controller → service → model separation.

## Architecture & Key Components

### Package Structure
- `controller/`: REST endpoints (currently `HolidayController` with `/holidays` endpoint)
- `service/`: Business logic layer (`HolidayService` - currently returns empty list)
- `model/`: Data models (`Holiday` with `name` and `date` fields)
- `config/`: Spring configuration beans (`AppConfig` for custom configurations)

### Data Flow
1. HTTP GET requests hit `HolidayController.list()` at `/holidays`
2. Controller delegates to `HolidayService.getAll()`
3. Service fetches/transforms data and returns `List<Holiday>`
4. Response serialized as JSON via Spring REST

## Build & Test Commands

### Maven (Primary Build Tool)
- **Build**: `mvn clean package` (from project root)
- **Run**: `mvn spring-boot:run` or run `HolidayServiceApplication.main()`
- **Test**: `mvn test`
- **Install Dependencies**: Dependencies auto-managed by Maven; pom.xml declares Spring Boot starters

### Key Dependencies
- `spring-boot-starter-web`: REST controller support
- `spring-boot-starter-validation`: Bean validation
- `spring-boot-starter-actuator`: Health checks & metrics
- `spring-boot-starter-test`: JUnit & testing utilities

## Project Patterns & Conventions

### Spring Bean Naming
- **Controllers**: `@RestController` with `@RequestMapping` (e.g., `/holidays`)
- **Services**: `@Service` class with business logic, injected via constructor
- **Configuration**: `@Configuration` classes in `config/` package for Spring beans

### Constructor Injection Pattern
Used exclusively (not field injection). Example from `HolidayController`:
```java
public HolidayController(HolidayService holidayService) {
    this.holidayService = holidayService;
}
```

### Data Models
- Simple POJOs with getters/setters
- `Holiday` uses `LocalDate` for temporal data (Java 8+ Time API)
- No JPA/Hibernate currently (suggests potential future DB integration)

## Integration Points

### Nager.Date API
- **Purpose**: External source for holiday data
- **Status**: Integration stub - service currently returns empty list
- **Next Step**: Implement HTTP client (RestTemplate or WebClient) to call Nager API

### Expected Flow
Service should:
1. Accept country code parameter
2. Call Nager API endpoint (e.g., `/holidays/2024/[country]`)
3. Map API response to internal `Holiday` model
4. Return list to controller

## Common Tasks

### Adding New Endpoint
1. Create method in `HolidayController` with `@GetMapping` or `@PostMapping`
2. Add corresponding method in `HolidayService` if logic needed
3. Follow existing injection pattern: constructor-inject `HolidayService`

### Extending Holiday Model
- Add fields to `Holiday.java` with getter/setters
- Update Spring will auto-serialize to JSON
- Update service methods to populate new fields

### Testing
- Add tests in `src/test/java/com/github/tessdev/nager_holiday_client/`
- Use Spring Test utilities from `spring-boot-starter-test`
- Mock `HolidayService` in controller tests

## Key Files Reference
- **Entry Point**: [HolidayServiceApplication.java](../src/main/java/com/github/tessdev/nager_holiday_client/HolidayServiceApplication.java)
- **REST Layer**: [HolidayController.java](../src/main/java/com/github/tessdev/holidayservice/controller/HolidayController.java)
- **Business Logic**: [HolidayService.java](../src/main/java/com/github/tessdev/holidayservice/service/HolidayService.java)
- **Data Model**: [Holiday.java](../src/main/java/com/github/tessdev/holidayservice/model/Holiday.java)
- **Build Config**: [pom.xml](../pom.xml)
- **App Config**: [application.properties](../src/main/resources/application.properties)
