# Public Holidays Service

A Spring Boot REST API that provides public holiday information for countries using ISO 3166-1 alpha-2 country codes.

The service supports retrieving:
- Last celebrated public holidays
- Weekday holiday counts per country
- Common public holidays between two countries

---

## Running the Application

### Prerequisites
- Java 17+
- Maven 3.8+

### Build
```bash
mvn clean install
```

### Run
```bash
mvn spring-boot:run
```

The application will start at:

```
http://localhost:8080
```

---

## API Endpoints

### Get Last Celebrated Holidays
```
GET /api/holidays/last/{country}
```

Query Parameters:
- `count` (optional, default: 3, max: 3)

Example:
```
GET /api/holidays/last/DE?count=2
```

---

### Get Weekday Holiday Counts
```
GET /api/holidays/weekday-counts
```

Query Parameters:
- `year` (required)
- `countries` (required, repeatable)
- `weekend` (optional, default: true)
- `sort` (optional: ASC | DESC)

Example:
```
GET /api/holidays/weekday-counts?year=2024&countries=DE&countries=FR&weekend=false
```

---

### Get Common Holidays Between Two Countries
```
GET /api/holidays/common
```

Query Parameters:
- `year` (required)
- `country1` (required)
- `country2` (required)

Example:
```
GET /api/holidays/common?year=2024&country1=DE&country2=NL
```

---

## Running Tests

```bash
mvn test
```

---

## Error Handling

Example error response:
```json
{
  "code": "INVALID_YEAR",
  "message": "Year cannot be in the future.",
  "timestamp": "2025-06-01T10:17:45Z",
  "path": "/api/holidays/common",
  "details": {
    "year": "XXXX"
  }
}
```

---

## Validation Rules

- Country codes must be ISO 3166-1 alpha-2
- Year must be between 1900 and the current year
- Count must be between 0 and 3
- Null or invalid inputs are rejected

---

## Project Structure

```
src
 ├── main
 │   └── java/com/github/tessdev/holidayservice
 │       ├── config 
 │       ├── controller
 │       ├── service
 │       ├── validation
 │       ├── exception
 │       └── model
 └── test
     └── java/com/github/tessdev/holidayservice
```
