# MGB MRFC Manager - API Testing Guide

## Quick Start

### 1. Prerequisites
- Server running on `http://localhost:3000`
- Postman installed (or use curl/Thunder Client)
- Test credentials ready

### 2. Test Credentials

**Super Admin Account:**
```
Username: superadmin
Password: Change@Me#2025
Role: SUPER_ADMIN
```

---

## Testing Methods

### Option 1: Using Postman (Recommended)
1. Download and install [Postman](https://www.postman.com/downloads/)
2. Import the collection file: `MGB_MRFC_API.postman_collection.json`
3. Set environment variables
4. Run each request

### Option 2: Using curl (Command Line)
See curl examples below for each endpoint

### Option 3: Using Thunder Client (VS Code Extension)
Import curl commands directly into Thunder Client

---

## Health Check (Start Here)

**GET** `http://localhost:3000/api/v1/health`

```bash
curl http://localhost:3000/api/v1/health
```

**Expected Response:**
```json
{
  "success": true,
  "message": "MGB MRFC Manager API is running",
  "version": "1.0.0",
  "timestamp": "2025-10-25T10:30:00.000Z"
}
```

---

## Phase 1: Authentication Testing

### 1. Login (Get JWT Token)

**POST** `http://localhost:3000/api/v1/auth/login`

**Headers:**
```
Content-Type: application/json
```

**Body:**
```json
{
  "username": "superadmin",
  "password": "Change@Me#2025"
}
```

**curl:**
```bash
curl -X POST http://localhost:3000/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"superadmin","password":"Change@Me#2025"}'
```

**Expected Response:**
```json
{
  "success": true,
  "data": {
    "user": {
      "id": "1",
      "username": "superadmin",
      "full_name": "System Administrator",
      "email": "admin@mgb.gov.ph",
      "role": "SUPER_ADMIN"
    },
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "expiresIn": "24h"
  }
}
```

**⚠️ IMPORTANT:** Copy the `token` value - you'll need it for all other requests!

---

## Phase 2: User Management Testing

⚠️ **For all requests below**, add this header:
```
Authorization: Bearer YOUR_TOKEN_HERE
```

Replace `YOUR_TOKEN_HERE` with the token from the login response.

### 1. List All Users

**GET** `http://localhost:3000/api/v1/users`

**Query Parameters (optional):**
- `page=1` - Page number
- `limit=20` - Results per page
- `search=admin` - Search by username, name, or email
- `role=SUPER_ADMIN` - Filter by role (SUPER_ADMIN, ADMIN, USER)
- `is_active=true` - Filter by active status
- `sort_by=created_at` - Sort field
- `sort_order=DESC` - ASC or DESC

**curl:**
```bash
curl -X GET "http://localhost:3000/api/v1/users" \
  -H "Authorization: Bearer YOUR_TOKEN_HERE"
```

**curl with filters:**
```bash
curl -X GET "http://localhost:3000/api/v1/users?page=1&limit=10&search=super" \
  -H "Authorization: Bearer YOUR_TOKEN_HERE"
```

**Expected Response:**
```json
{
  "success": true,
  "data": {
    "users": [
      {
        "id": "1",
        "username": "superadmin",
        "full_name": "System Administrator",
        "email": "admin@mgb.gov.ph",
        "role": "SUPER_ADMIN",
        "is_active": true,
        "created_at": "2025-10-25T10:00:00.000Z"
      }
    ],
    "pagination": {
      "page": 1,
      "limit": 20,
      "total": 1,
      "totalPages": 1,
      "hasNext": false,
      "hasPrev": false
    }
  }
}
```

### 2. Create New User (Admin Creates ADMIN, User Creates USER)

**POST** `http://localhost:3000/api/v1/users`

**Body:**
```json
{
  "username": "testadmin",
  "email": "testadmin@mgb.gov.ph",
  "full_name": "Test Administrator",
  "password": "SecurePass@123",
  "role": "ADMIN"
}
```

**curl:**
```bash
curl -X POST http://localhost:3000/api/v1/users \
  -H "Authorization: Bearer YOUR_TOKEN_HERE" \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testadmin",
    "email": "testadmin@mgb.gov.ph",
    "full_name": "Test Administrator",
    "password": "SecurePass@123",
    "role": "ADMIN"
  }'
```

**Expected Response:**
```json
{
  "success": true,
  "message": "User created successfully",
  "data": {
    "id": "2",
    "username": "testadmin",
    "full_name": "Test Administrator",
    "email": "testadmin@mgb.gov.ph",
    "role": "ADMIN",
    "is_active": true,
    "created_at": "2025-10-25T10:15:00.000Z"
  }
}
```

### 3. Get User by ID

**GET** `http://localhost:3000/api/v1/users/1`

**curl:**
```bash
curl -X GET http://localhost:3000/api/v1/users/1 \
  -H "Authorization: Bearer YOUR_TOKEN_HERE"
```

### 4. Update User

**PUT** `http://localhost:3000/api/v1/users/2`

**Body (can update any of these):**
```json
{
  "full_name": "Updated Name",
  "email": "newemail@mgb.gov.ph",
  "is_active": true
}
```

**curl:**
```bash
curl -X PUT http://localhost:3000/api/v1/users/2 \
  -H "Authorization: Bearer YOUR_TOKEN_HERE" \
  -H "Content-Type: application/json" \
  -d '{
    "full_name": "Updated Administrator",
    "email": "updated@mgb.gov.ph"
  }'
```

### 5. Grant MRFC Access to User

**POST** `http://localhost:3000/api/v1/users/2/grant-mrfc-access`

**Body:**
```json
{
  "mrfc_ids": [1, 2, 3]
}
```

**curl:**
```bash
curl -X POST http://localhost:3000/api/v1/users/2/grant-mrfc-access \
  -H "Authorization: Bearer YOUR_TOKEN_HERE" \
  -H "Content-Type: application/json" \
  -d '{"mrfc_ids": [1, 2, 3]}'
```

### 6. Delete User (Soft Delete)

**DELETE** `http://localhost:3000/api/v1/users/2`

**curl:**
```bash
curl -X DELETE http://localhost:3000/api/v1/users/2 \
  -H "Authorization: Bearer YOUR_TOKEN_HERE"
```

---

## Phase 3: MRFC Management Testing

### 1. List MRFCs

**GET** `http://localhost:3000/api/v1/mrfcs`

**Query Parameters:**
- `page=1` - Page number
- `limit=20` - Results per page
- `search=text` - Search by name or location
- `municipality=` - Filter by municipality
- `province=` - Filter by province
- `is_active=true` - Filter by active status

**curl:**
```bash
curl -X GET "http://localhost:3000/api/v1/mrfcs?page=1&limit=10" \
  -H "Authorization: Bearer YOUR_TOKEN_HERE"
```

### 2. Get MRFC by ID

**GET** `http://localhost:3000/api/v1/mrfcs/1`

**curl:**
```bash
curl -X GET http://localhost:3000/api/v1/mrfcs/1 \
  -H "Authorization: Bearer YOUR_TOKEN_HERE"
```

### 3. Create MRFC

**POST** `http://localhost:3000/api/v1/mrfcs`

**Body:**
```json
{
  "name": "Municipal Finance Board",
  "municipality": "Manila",
  "province": "NCR",
  "description": "Regular finance review meetings",
  "user_ids": [1, 2]
}
```

**curl:**
```bash
curl -X POST http://localhost:3000/api/v1/mrfcs \
  -H "Authorization: Bearer YOUR_TOKEN_HERE" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Municipal Finance Board",
    "municipality": "Manila",
    "province": "NCR",
    "description": "Regular finance review meetings",
    "user_ids": [1, 2]
  }'
```

### 4. Update MRFC

**PUT** `http://localhost:3000/api/v1/mrfcs/1`

**Body:**
```json
{
  "description": "Updated description"
}
```

### 5. Delete MRFC (Soft Delete)

**DELETE** `http://localhost:3000/api/v1/mrfcs/1`

---

## Error Handling

### Common Error Responses

**Invalid Token:**
```json
{
  "success": false,
  "error": {
    "code": "UNAUTHORIZED",
    "message": "Invalid or expired token"
  }
}
```

**Missing Token:**
```json
{
  "success": false,
  "error": {
    "code": "MISSING_TOKEN",
    "message": "Authorization token is required"
  }
}
```

**Not Found:**
```json
{
  "success": false,
  "error": {
    "code": "USER_NOT_FOUND",
    "message": "User not found"
  }
}
```

**Validation Error:**
```json
{
  "success": false,
  "error": {
    "code": "VALIDATION_ERROR",
    "message": "Invalid input",
    "details": {
      "field": "email",
      "message": "Email is not valid"
    }
  }
}
```

**Insufficient Permissions:**
```json
{
  "success": false,
  "error": {
    "code": "INSUFFICIENT_PERMISSIONS",
    "message": "You don't have permission to perform this action"
  }
}
```

---

## Testing Workflow

### Step 1: Health Check
```bash
curl http://localhost:3000/api/v1/health
```

### Step 2: Login
```bash
curl -X POST http://localhost:3000/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"superadmin","password":"Change@Me#2025"}'
```
Copy the token from the response.

### Step 3: Use Token in Headers
Add to all subsequent requests:
```
Authorization: Bearer YOUR_TOKEN_HERE
```

### Step 4: Test Endpoints
Follow the curl examples above for each endpoint.

---

## Tips for Testing

1. **In Postman:**
   - Create a variable `token` to store JWT
   - Use `{{token}}` in Authorization header
   - Add a script to auto-extract token from login response

2. **In curl:**
   - Save token to environment variable: `TOKEN=$(curl ... | jq '.data.token')`
   - Use in requests: `curl ... -H "Authorization: Bearer $TOKEN"`

3. **Password Requirements:**
   - Minimum 8 characters
   - At least one uppercase letter
   - At least one lowercase letter
   - At least one number
   - At least one special character

4. **Testing Tips:**
   - Always start with health check
   - Always login to get token
   - Test list endpoints with filters
   - Test create, then update, then delete
   - Check error responses for invalid data

---

## Postman Collection

To use Postman efficiently:

1. **Set environment variable for base URL:**
   - Click "Environments"
   - Create new environment "Development"
   - Add variable: `base_url = http://localhost:3000`

2. **Set token variable:**
   - In login request, go to "Tests" tab
   - Add: `pm.environment.set("token", pm.response.json().data.token);`
   - Now `{{token}}` works in all other requests

3. **Use authorization header:**
   - In each request, go to "Authorization" tab
   - Choose "Bearer Token"
   - Value: `{{token}}`

---

## What's Next?

After testing User Management:
1. Test MRFC Management endpoints
2. Implement Proponent Management (5 endpoints)
3. Implement Quarter Management (2 endpoints)
4. Implement remaining modules

---

**Last Updated:** October 25, 2025
**Status:** Ready for testing
