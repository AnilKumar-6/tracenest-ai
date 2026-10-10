\# TraceNest AI — Decision Intelligence Workspace



TraceNest AI is a full-stack decision management application that helps users record decisions, track their progress, and review decision history.



\## Features



\- Create, view, update, and delete decisions

\- Track decision status: Pending, In Progress, and Completed

\- View dashboard summaries

\- Search, filter, sort, and paginate decisions

\- Review decision history and recorded updates

\- Store data in a persistent local H2 database

\- REST APIs built with Spring Boot

\- React-based frontend



\## Technology Stack



\*\*Backend\*\*

\- Java 17

\- Spring Boot

\- Spring Data JPA

\- Hibernate

\- Maven

\- H2 Database



\*\*Frontend\*\*

\- React

\- Vite

\- JavaScript

\- HTML and CSS



\## Run Locally



\### Prerequisites



\- Java 17

\- Node.js and npm



\### Start the backend



From the project root, run:



```powershell

.\\mvnw.cmd spring-boot:run

```



Backend base URL: `http://localhost:8081`



\### Start the frontend



Open a second terminal:



```powershell

cd .\\tracenest-frontend

npm.cmd install

npm.cmd run dev

```



Open the frontend URL printed in the terminal, usually `http://localhost:5173` or another available port.



\## Testing



Run backend tests from the project root:



```powershell

.\\mvnw.cmd test

```



\## Project Structure



```text

tracenest-ai/

├── src/

│   ├── main/java/TraceNest/AI/

│   ├── main/resources/

│   └── test/java/TraceNest/AI/

├── sql/

├── tracenest-frontend/

├── pom.xml

└── README.md

```



\## Data and Security



The local H2 database is stored in the `data/` directory. It is intentionally excluded from Git so local records are not uploaded with the source code.



Do not commit passwords, access tokens, environment secrets, or unsanitized diagnostic logs.



\## Project Status



The core dashboard, decision CRUD operations, status tracking, persistence, and decision history have been implemented. Backend tests have passed locally.



\## Author



Created as a full-stack Java and React portfolio project.



