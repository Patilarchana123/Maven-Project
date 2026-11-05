# PeopleDesk Employee Management

A Java 17 / Spring Boot REST API with an H2 database and a standalone HTML, CSS, JavaScript, and Bootstrap frontend. The backend and frontend are separate processes; the frontend reads its API endpoint from `API_BASE_URL` when it starts.

## Requirements

- Java 17 or newer
- Maven 3.8+
- Node.js 18+ (no npm packages are required)

## Run the application

Open two terminals in the project root.

1. Start the backend:

   ```powershell
   mvn spring-boot:run
   ```

   The REST API listens on port `8080` by default.

2. Start the frontend in the second terminal:

   ```powershell
   node .\frontend\server.js
   ```

   Open <http://localhost:3000>. Bootstrap is loaded from jsDelivr, so the browser needs internet access for Bootstrap styling.

The in-memory H2 database starts with four sample employees and is reset whenever the backend restarts. The H2 console is available at <http://localhost:8080/h2-console> with JDBC URL `jdbc:h2:mem:employees`, username `sa`, and an empty password.

## Configuration

Set environment variables before starting each process. For example, in PowerShell:

```powershell
$env:SERVER_PORT = "8081"
$env:CORS_ALLOWED_ORIGINS = "http://localhost:3001,http://127.0.0.1:3001"
mvn spring-boot:run
```

In the frontend terminal:

```powershell
$env:FRONTEND_PORT = "3001"
$env:API_BASE_URL = "http://localhost:8081/api"
node .\frontend\server.js
```

| Variable | Process | Default |
| --- | --- | --- |
| `SERVER_PORT` | Backend | `8080` |
| `CORS_ALLOWED_ORIGINS` | Backend | `http://localhost:3000,http://127.0.0.1:3000` |
| `FRONTEND_PORT` | Frontend | `3000` |
| `API_BASE_URL` | Frontend | `http://localhost:8080/api` |

`API_BASE_URL` should include the API prefix (`/api`). The frontend serves this value as runtime configuration; the browser JavaScript does not hardcode a backend address.

## REST API

All employee responses use JSON fields `id`, `name`, `email`, `department`, `designation`, `salary`, and `active`. The numeric employee ID is supplied by the caller and is the resource key. Employee IDs and email addresses must be unique. Employee creation defaults to active if `active` is omitted; the dashboard lets you set the status. Invalid input returns `400`, duplicate IDs/emails return `409`, missing employee IDs return `404`, and a successful deletion returns `204`.

| Method | Path | Purpose |
| --- | --- | --- |
| `GET` | `/api/employees` | List employees; optional `name` and `department` query parameters can be combined |
| `GET` | `/api/employees/{id}` | Get one employee |
| `POST` | `/api/employees` | Create an employee |
| `PUT` | `/api/employees/{id}` | Update an employee's details |
| `DELETE` | `/api/employees/{id}` | Delete an employee |
| `GET` | `/api/employees/count` | Get total employee count as a JSON number |
| `GET` | `/api/employees/department/{department}` | List employees in a department (case-insensitive) |

### Curl examples for every API

The examples use the default backend port. Change `8080` to match `SERVER_PORT` if configured. Run the create request first; the remaining examples use its employee ID `2001`.

```sh
# List all employees
curl http://localhost:8080/api/employees

# Get one employee
curl http://localhost:8080/api/employees/1001

# Create an employee
curl -X POST http://localhost:8080/api/employees \
  -H "Content-Type: application/json" \
  -d '{"id":2001,"name":"Jordan Lee","email":"jordan.lee@example.com","department":"Engineering","designation":"Developer","salary":85000,"active":true}'

# Update an employee
curl -X PUT http://localhost:8080/api/employees/2001 \
  -H "Content-Type: application/json" \
  -d '{"id":2001,"name":"Jordan Lee","email":"jordan.lee@example.com","department":"Product","designation":"Senior Developer","salary":92000,"active":true}'

# Delete an employee
curl -i -X DELETE http://localhost:8080/api/employees/2001

# Get the total employee count
curl http://localhost:8080/api/employees/count

# List employees in a department
curl http://localhost:8080/api/employees/department/Engineering

# Search by name and filter by department (the filters can also be used separately)
curl -G http://localhost:8080/api/employees --data-urlencode "name=ava" --data-urlencode "department=Engineering"
```

## Run tests

```powershell
mvn test
```

The backend tests cover validation and employee create, read, update, delete, name/department filtering, and count behavior.
