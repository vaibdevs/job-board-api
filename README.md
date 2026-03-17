# Job Board API

![Java](https://img.shields.io/badge/Java-17-orange?style=flat-square)
![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.2-green?style=flat-square)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-15-blue?style=flat-square)
![Docker](https://img.shields.io/badge/Docker-Compose-2496ED?style=flat-square)

A REST API that connects employers and jobseekers. Built with Java 17 and Spring Boot 3.2.

---

## Tech Stack

| Layer          | Technology                  |
|----------------|-----------------------------|
| Language       | Java 17                     |
| Framework      | Spring Boot 3.2             |
| Auth           | Spring Security + JWT       |
| Database       | PostgreSQL 15               |
| ORM            | Spring Data JPA + Hibernate |
| Search         | JPA Specifications          |
| Migrations     | Flyway                      |
| Containerisation | Docker + Docker Compose   |
| Testing        | Spring Boot Test + MockMvc  |

---

## Getting Started

### Prerequisites

- Docker Desktop installed and running
- That is it. Java and PostgreSQL do not need to be installed locally.

### Run the project
```bash
# Clone the repo
git clone https://github.com/vaibdevs/jobboard-api.git
cd jobboard-api

# Start the full stack (app + database)
docker compose up --build
```

The API will be available at `http://localhost:8080`

### Other commands
```bash
# Run in background
docker compose up -d --build

# View logs
docker compose logs -f app

# Stop containers
docker compose down

# Stop and wipe database (fresh start)
docker compose down -v
```

### Run without Docker
```bash
# Start PostgreSQL on port 5432 manually, then:
mvn spring-boot:run
```

---

## API Reference

### Auth
```
POST   /auth/register     Body: { email, password, role }    → returns JWT
POST   /auth/login        Body: { email, password }          → returns JWT
```

### Jobs
```
GET    /jobs                           Public   → list OPEN jobs
                                                  supports: ?title= ?location= ?page= ?size= ?sort=
GET    /jobs/{id}                      Public   → single job detail
POST   /jobs                           EMPLOYER → create job
PUT    /jobs/{id}                      EMPLOYER → update own job
DELETE /jobs/{id}                      EMPLOYER → delete own job
```

### Companies
```
POST   /companies                      EMPLOYER → create company
GET    /companies/{id}                 Public   → company detail
```

### Applications
```
POST   /jobs/{id}/apply                JOBSEEKER → apply with resume PDF
GET    /applications/mine              JOBSEEKER → my applications
DELETE /applications/{id}              JOBSEEKER → withdraw application
GET    /jobs/{id}/applications         EMPLOYER  → view applicants for own job
PATCH  /applications/{id}/status       EMPLOYER  → update applicant status
```

### Admin
```
GET    /admin/users                    ADMIN → all users
DELETE /admin/users/{id}               ADMIN → delete user
PUT    /admin/jobs/{id}/status         ADMIN → force-update any job
GET    /admin/stats                    ADMIN → platform statistics
```

### Files
```
GET    /files/{filename}               Public → download uploaded resume PDF
```

---

## Example Requests

**Register**
```bash
curl -X POST http://localhost:8080/auth/register \
  -H "Content-Type: application/json" \
  -d '{"email": "hr@infosys.com", "password": "secret123", "role": "EMPLOYER"}'
```

**Post a job**
```bash
curl -X POST http://localhost:8080/jobs \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <your-jwt>" \
  -d '{
    "companyId": "c-001",
    "title": "Backend Engineer",
    "description": "Java developer with Spring Boot experience.",
    "location": "Bengaluru"
  }'
```

**Search jobs**
```bash
curl "http://localhost:8080/jobs?title=engineer&location=Bengaluru&page=0&size=10&sort=postedAt,desc"
```

**Apply with resume**
```bash
curl -X POST http://localhost:8080/jobs/j-001/apply \
  -H "Authorization: Bearer <your-jwt>" \
  -F "resume=@/path/to/resume.pdf"
```

---

## Error Responses

All errors return this shape:
```json
{
  "timestamp": "2024-03-14T10:30:00",
  "status": 403,
  "error": "Forbidden",
  "message": "You do not own this job",
  "path": "/jobs/j-001"
}
```

| Status | When                                               |
|--------|----------------------------------------------------|
| `400`  | Validation failed — missing or malformed fields    |
| `401`  | No JWT or token is expired                         |
| `403`  | Wrong role, or ownership check failed              |
| `404`  | Resource does not exist                            |
| `409`  | Jobseeker applies to the same job twice            |

---

## Project Structure
```
src/
├── main/
│   ├── java/com/jobboard/
│   │   ├── auth/              AuthController, AuthService, JwtUtil, JwtAuthFilter
│   │   ├── config/            SecurityConfig
│   │   ├── user/              User, UserRepository, UserDetailsServiceImpl
│   │   ├── company/           Company, CompanyController, CompanyService, dto/
│   │   ├── job/               Job, JobController, JobService, JobSpecification, dto/
│   │   ├── application/       Application, ApplicationController, ApplicationService, dto/
│   │   ├── file/              FileStorageService
│   │   └── exception/         GlobalExceptionHandler, ResourceNotFoundException
│   └── resources/
│       ├── db/migration/
│       │   ├── V1__create_users.sql
│       │   ├── V2__create_companies.sql
│       │   ├── V3__create_jobs.sql
│       │   └── V4__create_applications.sql
│       └── application.yml
└── test/
    └── java/com/jobboard/
        ├── auth/              AuthControllerTest
        ├── job/               JobControllerTest
        └── application/       ApplicationControllerTest
```

---

## Security Model

Every request passes through three layers:
```
1. JwtAuthFilter     → is the token valid?          No → 401
2. Spring Security   → is the role allowed?         No → 403
3. Service layer     → do you own this resource?    No → 403
                                                       ↓
                                               Repository (DB)
```

---

## Design Decisions

**Flyway over `ddl-auto=create`**
Versioned SQL files that run once in order. Schema history is tracked in `flyway_schema_history`. `ddl-auto` is set to `validate` — Hibernate checks but never modifies the schema.

**JPA Specifications for search**
Composable predicates built at runtime. Each filter is one method. Any combination runs as a single SQL query. Adding a new filter never requires rewriting existing queries.

**`UNIQUE(job_id, applicant_id)` at DB level**
A Java-only check can fail under concurrent requests. The DB constraint is atomic — it is the guaranteed safety net regardless of race conditions.

**`FetchType.LAZY` on all relationships**
Only loads related entities when explicitly accessed. Prevents accidental N+1 queries and keeps every list endpoint fast.

**Multi-stage Dockerfile**
Stage 1 uses Maven to build the JAR. Stage 2 copies only the JAR into a slim JRE Alpine image. Final image is under 200 MB with no build tools included.

---

## ERD: 
  ### Table-1: Users
  <img width="970" height="840" alt="Image" src="https://github.com/user-attachments/assets/fc7d45fc-43eb-4761-b62b-fb722c7459aa" />

  ### Table-2: Comapnies
  <img width="736" height="1120" alt="Image" src="https://github.com/user-attachments/assets/31dace75-ecb6-4339-ad3c-740c87ae3b13" />

  ### Table-3: Jobs
  <img width="606" height="1106" alt="Image" src="https://github.com/user-attachments/assets/13786e38-abae-42f5-98ae-5870bb70914d" />
  
  ### Table-4: Applications
  <img width="1224" height="1130" alt="Image" src="https://github.com/user-attachments/assets/377d9a6b-d93a-44b3-a89b-a9611e7b8cd1" />

  ### Full schema — all 4 tables together
  <img width="470" height="1510" alt="Image" src="https://github.com/user-attachments/assets/12abe7e7-4e06-4abb-a4c8-527efe52f8db" />


---

## HLD
<img width="830" height="698" alt="Image" src="https://github.com/user-attachments/assets/52bd7ab1-3404-45ff-9375-66bbfb75d579" />

---

## LLD
 ### Flow 1 — User Registration
<img width="1416" height="1174" alt="Image" src="https://github.com/user-attachments/assets/86891889-211e-4c21-b08a-ceee04d4f9d4" />


### Flow 2 — Login & JWT Validation (every protected request)
<img width="1354" height="1200" alt="Image" src="https://github.com/user-attachments/assets/c6adfb67-3a72-4b16-b760-ba270ccd1c92" />

### Flow 3 — Employer posts a job
<img width="1636" height="1346" alt="Image" src="https://github.com/user-attachments/assets/47c00a1f-d187-4ca1-967d-416db5dbf3bd" />

### Flow 4 — Jobseeker searches and applies
<img width="1744" height="1324" alt="Image" src="https://github.com/user-attachments/assets/617af8b5-b8e6-4edb-96a5-5c222e7c782a" />

### Flow 5 — Employer reviews and updates application status
<img width="1394" height="1320" alt="Image" src="https://github.com/user-attachments/assets/07fb2ca6-0ce5-45de-ac41-a8efd0795f32" />

---

## Running Tests
```bash
# Run all tests
mvn test

# Run specific test class
mvn test -Dtest=JobControllerTest

# Run with coverage report
mvn test jacoco:report
```

---

## SET OF TASKS:
### Task 1: Environment Setup

### Task 2: Project Bootstrap

### Task 3: Database Setup

### Task 4: JPA Entities

### Task 5: JWT Authentication

### Task 6: Company & Job Endpoints

### Task 7: Application Endpoints + File Upload

### Task 8: Testing

### Task 9: Docker + Docker Compose

---

## License

MIT License — free to use, modify, and distribute.
