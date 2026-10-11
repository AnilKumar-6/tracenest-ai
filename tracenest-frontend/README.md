<!-- # React + Vite

This template provides a minimal setup to get React working in Vite with HMR and some Oxlint rules.

Currently, two official plugins are available:

- [@vitejs/plugin-react](https://github.com/vitejs/vite-plugin-react/blob/main/packages/plugin-react) uses [Oxc](https://oxc.rs)
- [@vitejs/plugin-react-swc](https://github.com/vitejs/vite-plugin-react/blob/main/packages/plugin-react-swc) uses [SWC](https://swc.rs/)

## React Compiler

The React Compiler is not enabled on this template because of its impact on dev & build performances. To add it, see [this documentation](https://react.dev/learn/react-compiler/installation).

## Expanding the Oxlint configuration

If you are developing a production application, we recommend using TypeScript with type-aware lint rules enabled. Check out the [TS template](https://github.com/vitejs/vite/tree/main/packages/create-vite/template-react-ts) for information on how to integrate TypeScript and Oxlint's TypeScript related rules in your project. -->

# TraceNest AI — Decision Management System

TraceNest AI is a full-stack web application for creating, managing, tracking, and reviewing decisions through a centralized dashboard. It demonstrates REST API development, database integration, server-side pagination, filtering, and a responsive React frontend.

## Live Demo

- **Frontend:** https://tracenest-ai.vercel.app
- **Backend API:** https://tracenest-ai.onrender.com/api/decisions

## Features

- Create and manage decisions
- Edit decision details
- Update decision statuses
- View decision history
- Search decisions by text
- Filter decisions by status
- Server-side pagination and sorting
- Dashboard summary counters
- Persistent data storage using PostgreSQL

## Technology Stack

### Frontend
- React
- Vite
- JavaScript
- HTML and CSS

### Backend
- Java
- Spring Boot
- Spring Data JPA / Hibernate
- REST APIs
- Maven

### Database and Deployment
- PostgreSQL
- Neon PostgreSQL
- Docker
- Render — backend deployment
- Vercel — frontend deployment
- Git and GitHub

## Architecture

The React frontend communicates with the Spring Boot backend through REST APIs. The backend uses Spring Data JPA to interact with the PostgreSQL database.

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
- Java Development Kit compatible with the project configuration
- Maven or the included Maven Wrapper
- Node.js and npm
- PostgreSQL database

### 1. Clone the repository

```bash
git clone https://github.com/AnilKumar-6/tracenest-ai.git
cd tracenest-ai
```

### 2. Run the backend

Configure your database connection using environment variables or your local application configuration. Do not commit database passwords or secret keys.

Run the Spring Boot application using the Maven Wrapper if available:

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

Open the local URL printed by Vite in your terminal.

## Testing

The backend includes automated tests. Run the test suite with:

```bash
mvn test
```

## Future Improvements

- Authentication and role-based access control
- More comprehensive error handling and validation
- Additional automated integration tests
- Improved monitoring and application logging
- Enhanced dashboard analytics

## Author

**Anil Kumar Yerragolla**

GitHub: https://github.com/AnilKumar-6
