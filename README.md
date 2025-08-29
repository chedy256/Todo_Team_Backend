# TodoTeam Backend

A Spring Boot REST API for a collaborative todo/task management application with user authentication and task assignment features.

## Features

- **User Authentication**: JWT-based authentication with registration and login
- **Task Management**: Create, read, update, and delete tasks
- **Task Assignment**: Assign tasks to users and manage ownership
- **Self-Assignment**: Users can assign themselves to unassigned tasks
- **Task Completion**: Track task completion status with completion updates
- **Access Control**: Users can only see:
  - Unassigned tasks (assignedId = null)
  - Tasks they own (ownerId = current user)
  - Tasks assigned to them (assignedId = current user)
- **Completion Control**: Only task owners or assignees can update completion status
- **Priority Management**: Support for LOW, NORMAL, and HIGH priority tasks
- **Due Date Tracking**: Set and manage task due dates
- **Database Integration**: PostgreSQL database with JPA/Hibernate

## Technology Stack

- **Java 21**
- **Spring Boot 3.4.9**
- **Spring Security** (JWT Authentication)
- **Spring Data JPA** (Database Access)
- **PostgreSQL 15** (Database)
- **Docker & Docker Compose** (Database Setup)
- **Maven** (Build Tool)

## Prerequisites

- Java 21 or higher
- Maven 3.6+
- Docker and Docker Compose

## Quick Start

### 1. Clone the Repository
```bash
git clone <repository-url>
cd TodoTeam
```

### 2. Start the Database
```bash
docker compose up -d
```

This will start a PostgreSQL database with:
- **Database**: TodoTeamDB
- **Username**: postgres
- **Password**: postgres
- **Port**: 5432

### 3. Run the Application
```bash
mvn spring-boot:run
```

The application will start on `http://localhost:8080`

## Configuration

### Database Configuration
The application is configured to use PostgreSQL with the following default settings in `application.yml`:

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/TodoTeamDB
    username: postgres
    password: postgres
```

### Customizing Database Credentials
To use different database credentials:

1. **Update `application.yml`**:
   ```yaml
   spring:
     datasource:
       username: your_username
       password: your_password
   ```

2. **Update `docker-compose.yml`**:
   ```yaml
   environment:
     POSTGRES_USER: your_username
     POSTGRES_PASSWORD: your_password
   ```

3. **Restart the database**:
   ```bash
   docker compose down -v
   docker compose up -d
   ```

### Environment Variables (Recommended for Production)
You can use environment variables for sensitive configuration:

```yaml
spring:
  datasource:
    url: ${DATABASE_URL:jdbc:postgresql://localhost:5432/TodoTeamDB}
    username: ${DB_USERNAME:postgres}
    password: ${DB_PASSWORD:postgres}
```

Then set the environment variables:
```bash
export DATABASE_URL=jdbc:postgresql://your-host:5432/your-database
export DB_USERNAME=your_username
export DB_PASSWORD=your_secure_password
```

## API Endpoints

### Authentication
- `POST /auth/register` - Register a new user
- `POST /auth/login` - Login and get JWT token

### Tasks
- `GET /tasks` - Get accessible tasks for current user
- `GET /tasks/{id}` - Get specific task (if accessible)
- `POST /tasks` - Create a new task
- `PUT /tasks/{id}` - Update a task (owner only, except for completion and self-assignment)
- `DELETE /tasks/{id}` - Delete a task (owner only)

### Users
- `GET /users/me` - Get current user information

### Status
- `GET /status` - Health check endpoint

## Task Access Rules

The application implements strict access control for tasks:

1. **Unassigned Tasks**: Visible to all users (assignedId = null)
2. **Owned Tasks**: Users can see tasks they created (ownerId = current user)
3. **Assigned Tasks**: Users can see tasks assigned to them (assignedId = current user)

Users cannot see tasks that don't meet any of these criteria.

## Task Update Permissions

The application has different permission levels for updating tasks:

1. **Task Owners**: Can update all task properties (description, priority, dueDate, assigneeId, completed)
2. **Task Assignees**: Can only update the completion status (`completed` field)
3. **Self-Assignment**: Any user can assign themselves to an unassigned task (assignedId = null)
4. **Completion Updates**: Both owners and assignees can mark tasks as completed or incomplete

## Request Examples

### Register a User
```bash
curl -X POST http://localhost:8080/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "email": "user@example.com",
    "username": "user",
    "password": "password123"
  }'
```

### Login
```bash
curl -X POST http://localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "user@example.com",
    "password": "password123"
  }'
```

### Create a Task
```bash
curl -X POST http://localhost:8080/tasks \
  -H "Authorization: Bearer <your-jwt-token>" \
  -H "Content-Type: application/json" \
  -d '{
    "title": "Complete project",
    "description": "Finish the TodoTeam backend",
    "priority": "HIGH",
    "dueDate": 1693872000000,
    "assigneeId": 2
  }'
```

### Get Tasks
```bash
curl -H "Authorization: Bearer <your-jwt-token>" \
  http://localhost:8080/tasks
```

### Update Task (Owner - All Properties)
```bash
curl -X PUT http://localhost:8080/tasks/1 \
  -H "Authorization: Bearer <owner-jwt-token>" \
  -H "Content-Type: application/json" \
  -d '{
    "description": "Updated description",
    "priority": "HIGH",
    "dueDate": 1693872000000,
    "assigneeId": 2,
    "completed": true
  }'
```

### Update Task Completion (Assignee or Owner)
```bash
curl -X PUT http://localhost:8080/tasks/1 \
  -H "Authorization: Bearer <assignee-jwt-token>" \
  -H "Content-Type: application/json" \
  -d '{
    "completed": true
  }'
```

### Self-Assign to Unassigned Task
```bash
curl -X PUT http://localhost:8080/tasks/1 \
  -H "Authorization: Bearer <your-jwt-token>" \
  -H "Content-Type: application/json" \
  -d '{
    "assigneeId": <your-user-id>
  }'
```

## Development

### Running Tests
```bash
mvn test
```

### Building for Production
```bash
mvn clean package
java -jar target/TodoTeam-0.0.1-SNAPSHOT.jar
```

### Database Schema
The application uses Hibernate with `create-drop` strategy for development, which recreates the database schema on each restart. For production, change this to `update` or `validate` in `application.yml`:

```yaml
spring:
  jpa:
    hibernate:
      ddl-auto: update  # or 'validate' for production
```

## Troubleshooting

### Database Connection Issues
1. Ensure Docker is running and the PostgreSQL container is healthy:
   ```bash
   docker compose ps
   ```

2. Check database logs:
   ```bash
   docker compose logs postgres
   ```

3. Verify database credentials match between `application.yml` and `docker-compose.yml`

### Application Won't Start
1. Check if port 8080 is available
2. Verify Java 21 is installed: `java --version`
3. Ensure Maven dependencies are downloaded: `mvn clean compile`

### Authentication Issues
- Ensure JWT tokens are included in the `Authorization: Bearer <token>` header
- Check token expiration (default: 12 hours)

## Contributing

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add some amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

## License

This project is licensed under the MIT License - see the LICENSE file for details.