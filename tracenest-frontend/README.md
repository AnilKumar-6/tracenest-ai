# TraceNest AI — Decision Management System

TraceNest AI is a full-stack web application for creating, managing, tracking, and reviewing decisions through a centralized dashboard. It demonstrates REST API development, database integration, server-side pagination, filtering, and sorting.

## Live Demo

- **Frontend:** https://tracenest-ai.vercel.app
- **Backend API:** https://tracenest-ai.onrender.com/api/decisions
- **GitHub:** https://github.com/AnilKumar-6/tracenest-ai

## Features

- Create and manage decisions
- Edit decision details
- Update decision statuses
- View decision history
- Search decisions by text
- Filter decisions by status
- Server-side pagination and sorting
- Dashboard summary statistics
- Persistent PostgreSQL storage

## Technology Stack

### Frontend
- React
- Vite
- JavaScript
- HTML and CSS

### Backend
- Java
- Spring Boot
- Spring Data JPA and Hibernate
- REST APIs
- Maven

### Database and Deployment
- PostgreSQL
- Neon PostgreSQL
- Docker
- Render for backend deployment
- Vercel for frontend deployment
- Git and GitHub

## Architecture

The React frontend communicates with the Spring Boot backend through REST APIs. The backend uses Spring Data JPA to interact with PostgreSQL.

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

Example paginated request:

```text
/api/decisions/page?page=0&size=5&sortBy=id&direction=desc
```

Optional query parameters include `search` and `status`.

## Local Setup

### Prerequisites

- Java Development Kit compatible with the project
- Maven or the Maven Wrapper
- Node.js and npm
- PostgreSQL

### 1. Clone the repository

```bash
git clone https://github.com/AnilKumar-6/tracenest-ai.git
cd tracenest-ai
```

### 2. Run the backend

Configure your local database connection using the project's application configuration or environment variables. Never commit database passwords or secret keys.

```bash
mvn spring-boot:run
```

### 3. Run the frontend

Open a second terminal:

```bash
cd tracenest-frontend
npm install
npm run dev
```

Open the local URL printed by Vite.

## Testing

Run the backend test suite from the repository root:

```bash
mvn test
```

## Future Improvements

- Authentication and role-based access control
- Expanded error handling and validation
- Additional automated integration tests
- Improved monitoring and logging
- Enhanced dashboard analytics

## Author

Anil Kumar Yerragolla

GitHub: https://github.com/AnilKumar-6
