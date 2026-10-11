# TraceNest AI — Decision Intelligence Workspace

TraceNest AI is a full-stack decision management application for recording decisions, tracking their progress, and reviewing decision history through a centralized dashboard.

## Live Demo

- **Frontend:** https://tracenest-ai.vercel.app
- **Backend API:** https://tracenest-ai.onrender.com/api/decisions
- **API Documentation:** https://tracenest-ai.onrender.com/swagger-ui.html
- **GitHub Repository:** https://github.com/AnilKumar-6/tracenest-ai

## Features

- Create and manage decisions
- Edit decision details and update statuses
- Track Pending, In Progress, and Completed decisions
- Review decision history
- Search decisions by text
- Filter decisions by status
- Server-side pagination and sorting
- Dashboard summary statistics
- Persistent database storage

## Technology Stack

**Frontend**
- React
- Vite
- JavaScript
- HTML and CSS

**Backend**
- Java 17
- Spring Boot
- Spring Data JPA and Hibernate
- REST APIs
- Maven

**Database and Deployment**
- PostgreSQL with Neon
- Docker
- Render for backend hosting
- Vercel for frontend hosting
- Git and GitHub

## Architecture

The React frontend communicates with the Spring Boot backend through REST APIs. The backend uses Spring Data JPA to access the database.

```text
React + Vite Frontend
        |
        | HTTP / REST API
        v
Spring Boot Backend
        |
        | Spring Data JPA
        v
PostgreSQL Database
```

## API Endpoints

| Method | Endpoint | Purpose |
|---|---|---|
| GET | `/api/decisions` | Retrieve decisions |
| GET | `/api/decisions/dashboard` | Retrieve dashboard statistics |
| GET | `/api/decisions/page` | Retrieve paginated decisions with optional search and status filters |

Example request:

```text
/api/decisions/page?page=0&size=5&sortBy=id&direction=desc
```

The paginated endpoint supports optional `search` and `status` parameters.

## Run Locally

### Prerequisites

- Java 17 or the Java version required by the project
- Maven or the included Maven Wrapper
- Node.js and npm
- A configured database connection

### Start the backend

From the repository root, configure your database connection and run:

```powershell
.\mvnw.cmd spring-boot:run
```

### Start the frontend

Open a second terminal:

```powershell
cd .\tracenest-frontend
npm.cmd install
npm.cmd run dev
```

Open the local URL printed by Vite.

## Testing

Run the backend tests from the repository root:

```powershell
.\mvnw.cmd test
```

The backend test suite has passed locally.

## Security

- Keep database credentials and API secrets out of Git.
- Use environment variables for sensitive configuration.
- Do not commit private logs or local database files.

## Future Improvements

- Authentication and role-based access control
- Expanded validation and error handling
- Additional integration tests
- Monitoring and application logging
- Enhanced dashboard analytics

## Author

Anil Kumar Yerragolla

GitHub: https://github.com/AnilKumar-6