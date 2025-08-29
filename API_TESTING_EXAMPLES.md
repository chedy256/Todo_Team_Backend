# TodoTeam API Testing Examples

This document provides comprehensive examples for testing the TodoTeam API, including the new completion tracking and self-assignment features.

## Prerequisites

1. Start the application: `mvn spring-boot:run`
2. The API will be available at `http://localhost:8080`

## Authentication Setup

### 1. Register Users

**Owner User:**
```bash
curl -X POST http://localhost:8080/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "Task Owner",
    "email": "owner@example.com",
    "password": "password123"
  }'
```

**Assignee User:**
```bash
curl -X POST http://localhost:8080/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "Task Assignee", 
    "email": "assignee@example.com",
    "password": "password123"
  }'
```

### 2. Login and Get Tokens

**Login as Owner:**
```bash
curl -X POST http://localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "owner@example.com",
    "password": "password123"
  }'
```

**Login as Assignee:**
```bash
curl -X POST http://localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "assignee@example.com",
    "password": "password123"
  }'
```

**Save the tokens for use in subsequent requests.**

## Task Management Examples

### 1. Create a Task (Owner)

```bash
curl -X POST http://localhost:8080/tasks \
  -H "Authorization: Bearer <OWNER_JWT_TOKEN>" \
  -H "Content-Type: application/json" \
  -d '{
    "title": "Implement new feature",
    "description": "Add completion tracking to tasks",
    "priority": "HIGH",
    "dueDate": 1735500000000
  }'
```

### 2. Get All Tasks

```bash
curl -X GET http://localhost:8080/tasks \
  -H "Authorization: Bearer <OWNER_JWT_TOKEN>"
```

## Self-Assignment Feature

### 3. Self-Assign to Unassigned Task (Any User)

```bash
curl -X PUT http://localhost:8080/tasks/1 \
  -H "Authorization: Bearer <ASSIGNEE_JWT_TOKEN>" \
  -H "Content-Type: application/json" \
  -d '{
    "assigneeId": 2
  }'
```

**Note:** Replace `2` with the actual user ID of the assignee.

## Completion Tracking Features

### 4. Mark Task as Completed (Owner or Assignee)

**As Assignee:**
```bash
curl -X PUT http://localhost:8080/tasks/1 \
  -H "Authorization: Bearer <ASSIGNEE_JWT_TOKEN>" \
  -H "Content-Type: application/json" \
  -d '{
    "completed": true
  }'
```

**As Owner:**
```bash
curl -X PUT http://localhost:8080/tasks/1 \
  -H "Authorization: Bearer <OWNER_JWT_TOKEN>" \
  -H "Content-Type: application/json" \
  -d '{
    "completed": true
  }'
```

### 5. Mark Task as Incomplete

```bash
curl -X PUT http://localhost:8080/tasks/1 \
  -H "Authorization: Bearer <ASSIGNEE_JWT_TOKEN>" \
  -H "Content-Type: application/json" \
  -d '{
    "completed": false
  }'
```

## Permission Testing

### 6. Test Assignee Permissions (Should Work)

**Assignee can update completion only:**
```bash
curl -X PUT http://localhost:8080/tasks/1 \
  -H "Authorization: Bearer <ASSIGNEE_JWT_TOKEN>" \
  -H "Content-Type: application/json" \
  -d '{
    "completed": true
  }'
```

### 7. Test Assignee Restrictions (Should Fail)

**Assignee cannot update other properties:**
```bash
curl -X PUT http://localhost:8080/tasks/1 \
  -H "Authorization: Bearer <ASSIGNEE_JWT_TOKEN>" \
  -H "Content-Type: application/json" \
  -d '{
    "description": "Assignee trying to change description"
  }'
```

**Expected Response:** `{"error": "Only owner can update task details"}`

### 8. Owner Full Update (Should Work)

**Owner can update all properties:**
```bash
curl -X PUT http://localhost:8080/tasks/1 \
  -H "Authorization: Bearer <OWNER_JWT_TOKEN>" \
  -H "Content-Type: application/json" \
  -d '{
    "description": "Updated by owner",
    "priority": "LOW",
    "dueDate": 1735600000000,
    "assigneeId": 2,
    "completed": true
  }'
```

## Complete Workflow Example

### 9. Full Task Lifecycle

1. **Owner creates task:**
```bash
curl -X POST http://localhost:8080/tasks \
  -H "Authorization: Bearer <OWNER_JWT_TOKEN>" \
  -H "Content-Type: application/json" \
  -d '{
    "title": "Complete project documentation",
    "description": "Write comprehensive API documentation",
    "priority": "NORMAL",
    "dueDate": 1735500000000
  }'
```

2. **Assignee self-assigns:**
```bash
curl -X PUT http://localhost:8080/tasks/2 \
  -H "Authorization: Bearer <ASSIGNEE_JWT_TOKEN>" \
  -H "Content-Type: application/json" \
  -d '{
    "assigneeId": 2
  }'
```

3. **Assignee marks as completed:**
```bash
curl -X PUT http://localhost:8080/tasks/2 \
  -H "Authorization: Bearer <ASSIGNEE_JWT_TOKEN>" \
  -H "Content-Type: application/json" \
  -d '{
    "completed": true
  }'
```

4. **Verify final state:**
```bash
curl -X GET http://localhost:8080/tasks/2 \
  -H "Authorization: Bearer <OWNER_JWT_TOKEN>"
```

## Response Examples

### Task Object Structure

```json
{
  "id": 1,
  "title": "Implement new feature",
  "description": "Add completion tracking to tasks",
  "priority": "HIGH",
  "completed": true,
  "dueDate": 1735500000000,
  "ownerId": 1,
  "assignedId": 2,
  "lastUpdate": 1756430156327
}
```

### Success Response

```json
{
  "status": "success"
}
```

### Error Responses

**Forbidden (assignee trying to update non-completion fields):**
```json
{
  "error": "Only owner can update task details"
}
```

**Forbidden (non-owner, non-assignee trying to update completion):**
```json
{
  "error": "Only owner or assignee can update completion status"
}
```

## Notes

1. **Self-Assignment Rules:**
   - Only works on unassigned tasks (assignedId = null)
   - Any user can assign themselves to unassigned tasks
   - Cannot assign someone else unless you're the owner

2. **Completion Rules:**
   - Both owner and assignee can update completion status
   - Only completion can be updated by assignees
   - Owner can update all fields including completion

3. **Access Control:**
   - Users only see tasks they own, are assigned to, or are unassigned
   - Task updates require appropriate permissions based on the field being updated
