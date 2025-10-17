# BACKEND DEVELOPMENT TASKS
## MGB MRFC Manager Application

**Document Version:** 1.0
**Date Created:** October 16, 2025
**Target Completion:** [To be scheduled]

---

## TABLE OF CONTENTS

1. [Overview](#overview)
2. [Technology Stack Recommendations](#technology-stack-recommendations)
3. [Database Design](#database-design)
4. [API Endpoints Specification](#api-endpoints-specification)
5. [Implementation Phases](#implementation-phases)
6. [Task Breakdown by Priority](#task-breakdown-by-priority)
7. [Testing Requirements](#testing-requirements)
8. [Deployment Checklist](#deployment-checklist)

---

## OVERVIEW

### Project Goals
Develop a robust backend system to replace hardcoded demo data in the MGB MRFC Manager Android application with a scalable, secure, cloud-based solution.

### Current State
- ✅ Fully functional Android UI with hardcoded data
- ✅ 27 Kotlin activities and adapters
- ✅ 6 data models defined
- ❌ No backend API integration
- ❌ No persistent database
- ❌ No user authentication system
- ❌ No file storage system

### Target State
- ✅ RESTful API with complete CRUD operations
- ✅ Relational database with proper relationships
- ✅ JWT-based authentication system
- ✅ Cloud file storage for documents
- ✅ Real-time sync capabilities
- ✅ Admin dashboard (web-based)
- ✅ API documentation (Swagger/OpenAPI)

---

## TECHNOLOGY STACK RECOMMENDATIONS

### Option 1: Node.js Stack (Recommended)
**Pros:** Fast development, large ecosystem, good for real-time features
```
Backend Framework: Express.js / NestJS
Database: PostgreSQL / MySQL
ORM: Sequelize / TypeORM / Prisma
Authentication: Passport.js with JWT
File Storage: AWS S3 / Google Cloud Storage / Azure Blob
Real-time: Socket.io (for future features)
API Documentation: Swagger
Testing: Jest / Mocha
Hosting: AWS EC2 / Google Cloud Run / Heroku
```

### Option 2: Python Stack
**Pros:** Strong for data analysis, excellent for future AI/ML features
```
Backend Framework: Django / FastAPI
Database: PostgreSQL
ORM: Django ORM / SQLAlchemy
Authentication: Django REST Framework Token / JWT
File Storage: AWS S3 / Google Cloud Storage
API Documentation: Django REST Swagger / FastAPI auto-docs
Testing: Pytest
Hosting: AWS EC2 / Google Cloud Run / PythonAnywhere
```

### Option 3: PHP Stack (Laravel)
**Pros:** Mature ecosystem, good for DENR existing infrastructure
```
Backend Framework: Laravel
Database: MySQL / PostgreSQL
ORM: Eloquent ORM
Authentication: Laravel Sanctum / Passport
File Storage: Laravel Storage (S3 driver)
API Documentation: Laravel Swagger
Testing: PHPUnit
Hosting: AWS EC2 / Shared hosting
```

### Option 4: Java Spring Boot
**Pros:** Enterprise-grade, excellent for government systems
```
Backend Framework: Spring Boot
Database: PostgreSQL / MySQL
ORM: Hibernate / Spring Data JPA
Authentication: Spring Security with JWT
File Storage: AWS S3 SDK
API Documentation: SpringDoc OpenAPI
Testing: JUnit / Mockito
Hosting: AWS EC2 / Google Cloud
```

**RECOMMENDATION:** Node.js with Express.js/NestJS + PostgreSQL for rapid development and scalability.

---

## DATABASE DESIGN

### Entity Relationship Diagram

```
┌─────────────────┐
│     Users       │
├─────────────────┤
│ PK id           │
│    username     │
│    password_hash│
│    full_name    │
│    email        │
│    role         │ (enum: ADMIN, USER)
│    created_at   │
│    updated_at   │
│    last_login   │
│    is_active    │
└────────┬────────┘
         │
         │ 1:N (created_by)
         ▼
┌─────────────────────┐
│      MRFCs          │
├─────────────────────┤
│ PK id               │
│    name             │
│    municipality     │
│    contact_person   │
│    contact_number   │
│    email            │
│    address          │
│ FK created_by       │
│    created_at       │
│    updated_at       │
│    is_active        │
└────────┬────────────┘
         │
         │ 1:N
         ▼
┌─────────────────────┐
│    Proponents       │
├─────────────────────┤
│ PK id               │
│ FK mrfc_id          │
│    name             │
│    company_name     │
│    permit_number    │
│    permit_type      │
│    status           │ (enum: ACTIVE, INACTIVE, SUSPENDED)
│    contact_person   │
│    contact_number   │
│    email            │
│    address          │
│    created_at       │
│    updated_at       │
└────────┬────────────┘
         │
         │ 1:N
         ▼
┌─────────────────────┐         ┌──────────────────┐
│     Agendas         │         │    Quarters      │
├─────────────────────┤         ├──────────────────┤
│ PK id               │         │ PK id            │
│ FK mrfc_id          │         │    name          │ (Q1 2025, Q2 2025, etc.)
│ FK quarter_id       │◄────────│    year          │
│    meeting_date     │         │    start_date    │
│    meeting_time     │         │    end_date      │
│    location         │         │    is_current    │
│    status           │         └──────────────────┘
│    created_at       │
│    updated_at       │
└────────┬────────────┘
         │
         │ 1:N
         ▼
┌─────────────────────┐
│  Matters_Arising    │
├─────────────────────┤
│ PK id               │
│ FK agenda_id        │
│    issue            │
│    status           │ (enum: PENDING, IN_PROGRESS, RESOLVED)
│    assigned_to      │
│    date_raised      │
│    date_resolved    │
│    remarks          │
│    created_at       │
│    updated_at       │
└─────────────────────┘

┌─────────────────────┐
│    Attendance       │
├─────────────────────┤
│ PK id               │
│ FK agenda_id        │
│ FK proponent_id     │
│    is_present       │
│    photo_url        │
│    marked_at        │
│    marked_by        │
│    remarks          │
└─────────────────────┘

┌─────────────────────┐
│     Documents       │
├─────────────────────┤
│ PK id               │
│ FK proponent_id     │
│ FK quarter_id       │
│ FK uploaded_by      │
│    file_name        │
│    original_name    │
│    file_type        │
│    file_size        │
│    category         │ (enum: MTF_REPORT, AEPEP, CMVR, SDMP, PRODUCTION, SAFETY, OTHER)
│    file_path        │
│    file_url         │
│    upload_date      │
│    status           │ (enum: PENDING, ACCEPTED, REJECTED)
│    reviewed_by      │
│    reviewed_at      │
│    remarks          │
│    created_at       │
│    updated_at       │
└─────────────────────┘

┌─────────────────────┐
│   Voice_Recordings  │
├─────────────────────┤
│ PK id               │
│ FK agenda_id        │
│    recording_name   │
│    file_name        │
│    file_path        │
│    file_url         │
│    duration         │ (seconds)
│    file_size        │
│    recorded_by      │
│    recorded_at      │
│    created_at       │
└─────────────────────┘

┌─────────────────────┐
│       Notes         │
├─────────────────────┤
│ PK id               │
│ FK user_id          │
│ FK mrfc_id          │
│ FK quarter_id       │
│    title            │
│    content          │
│    created_at       │
│    updated_at       │
└─────────────────────┘

┌─────────────────────┐
│   Notifications     │
├─────────────────────┤
│ PK id               │
│ FK user_id          │
│    type             │ (enum: MEETING, COMPLIANCE, ALERT, GENERAL)
│    title            │
│    message          │
│    is_read          │
│    read_at          │
│    created_at       │
│    sent_at          │
└─────────────────────┘

┌─────────────────────┐
│  User_MRFC_Access   │ (Many-to-Many relationship)
├─────────────────────┤
│ PK id               │
│ FK user_id          │
│ FK mrfc_id          │
│    granted_by       │
│    granted_at       │
│    is_active        │
└─────────────────────┘

┌─────────────────────┐
│   Compliance_Logs   │ (For tracking compliance changes)
├─────────────────────┤
│ PK id               │
│ FK proponent_id     │
│ FK quarter_id       │
│    compliance_pct   │
│    documents_submitted │
│    documents_required  │
│    status           │
│    calculated_at    │
│    calculated_by    │
└─────────────────────┘

┌─────────────────────┐
│    Audit_Logs       │ (For tracking all system changes)
├─────────────────────┤
│ PK id               │
│ FK user_id          │
│    entity_type      │ (table name)
│    entity_id        │
│    action           │ (CREATE, UPDATE, DELETE)
│    old_values       │ (JSON)
│    new_values       │ (JSON)
│    ip_address       │
│    user_agent       │
│    created_at       │
└─────────────────────┘
```

### Database Tables Summary

| Table | Purpose | Relationships |
|-------|---------|---------------|
| `users` | Store user accounts | 1:N to Notes, Documents, Audit Logs |
| `mrfcs` | Store MRFC details | 1:N to Proponents, Agendas |
| `proponents` | Store mining companies | N:1 to MRFCs, 1:N to Documents, Attendance |
| `quarters` | Store quarter periods | 1:N to Agendas, Documents, Notes |
| `agendas` | Store meeting agendas | N:1 to MRFCs, Quarters; 1:N to Matters Arising, Attendance |
| `matters_arising` | Store unresolved issues | N:1 to Agendas |
| `attendance` | Store attendance records | N:1 to Agendas, Proponents |
| `documents` | Store file metadata | N:1 to Proponents, Quarters |
| `voice_recordings` | Store meeting recordings | N:1 to Agendas |
| `notes` | Store personal notes | N:1 to Users, MRFCs, Quarters |
| `notifications` | Store user notifications | N:1 to Users |
| `user_mrfc_access` | Map users to MRFCs | N:N relationship table |
| `compliance_logs` | Track compliance history | N:1 to Proponents, Quarters |
| `audit_logs` | Track all system changes | N:1 to Users |

### SQL Schema (PostgreSQL)

```sql
-- Create ENUM types
CREATE TYPE user_role AS ENUM ('ADMIN', 'USER');
CREATE TYPE proponent_status AS ENUM ('ACTIVE', 'INACTIVE', 'SUSPENDED');
CREATE TYPE matter_status AS ENUM ('PENDING', 'IN_PROGRESS', 'RESOLVED');
CREATE TYPE document_category AS ENUM ('MTF_REPORT', 'AEPEP', 'CMVR', 'SDMP', 'PRODUCTION', 'SAFETY', 'OTHER');
CREATE TYPE document_status AS ENUM ('PENDING', 'ACCEPTED', 'REJECTED');
CREATE TYPE notification_type AS ENUM ('MEETING', 'COMPLIANCE', 'ALERT', 'GENERAL');
CREATE TYPE audit_action AS ENUM ('CREATE', 'UPDATE', 'DELETE');
CREATE TYPE agenda_status AS ENUM ('DRAFT', 'PUBLISHED', 'COMPLETED', 'CANCELLED');

-- Users table
CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    full_name VARCHAR(100) NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    role user_role NOT NULL DEFAULT 'USER',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    last_login TIMESTAMP,
    is_active BOOLEAN DEFAULT TRUE
);

-- MRFCs table
CREATE TABLE mrfcs (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(200) NOT NULL,
    municipality VARCHAR(100) NOT NULL,
    contact_person VARCHAR(100),
    contact_number VARCHAR(20),
    email VARCHAR(100),
    address TEXT,
    created_by BIGINT REFERENCES users(id),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    is_active BOOLEAN DEFAULT TRUE
);

-- Proponents table
CREATE TABLE proponents (
    id BIGSERIAL PRIMARY KEY,
    mrfc_id BIGINT NOT NULL REFERENCES mrfcs(id) ON DELETE CASCADE,
    name VARCHAR(200) NOT NULL,
    company_name VARCHAR(200) NOT NULL,
    permit_number VARCHAR(50),
    permit_type VARCHAR(50),
    status proponent_status DEFAULT 'ACTIVE',
    contact_person VARCHAR(100),
    contact_number VARCHAR(20),
    email VARCHAR(100),
    address TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Quarters table
CREATE TABLE quarters (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(20) NOT NULL, -- 'Q1 2025'
    year INTEGER NOT NULL,
    quarter_number INTEGER NOT NULL CHECK (quarter_number BETWEEN 1 AND 4),
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    is_current BOOLEAN DEFAULT FALSE,
    UNIQUE(year, quarter_number)
);

-- Agendas table
CREATE TABLE agendas (
    id BIGSERIAL PRIMARY KEY,
    mrfc_id BIGINT NOT NULL REFERENCES mrfcs(id) ON DELETE CASCADE,
    quarter_id BIGINT NOT NULL REFERENCES quarters(id),
    meeting_date DATE NOT NULL,
    meeting_time TIME,
    location TEXT,
    status agenda_status DEFAULT 'DRAFT',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(mrfc_id, quarter_id)
);

-- Matters Arising table
CREATE TABLE matters_arising (
    id BIGSERIAL PRIMARY KEY,
    agenda_id BIGINT NOT NULL REFERENCES agendas(id) ON DELETE CASCADE,
    issue TEXT NOT NULL,
    status matter_status DEFAULT 'PENDING',
    assigned_to VARCHAR(100),
    date_raised DATE NOT NULL,
    date_resolved DATE,
    remarks TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Attendance table
CREATE TABLE attendance (
    id BIGSERIAL PRIMARY KEY,
    agenda_id BIGINT NOT NULL REFERENCES agendas(id) ON DELETE CASCADE,
    proponent_id BIGINT NOT NULL REFERENCES proponents(id) ON DELETE CASCADE,
    is_present BOOLEAN DEFAULT FALSE,
    photo_url TEXT,
    marked_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    marked_by BIGINT REFERENCES users(id),
    remarks TEXT,
    UNIQUE(agenda_id, proponent_id)
);

-- Documents table
CREATE TABLE documents (
    id BIGSERIAL PRIMARY KEY,
    proponent_id BIGINT NOT NULL REFERENCES proponents(id) ON DELETE CASCADE,
    quarter_id BIGINT NOT NULL REFERENCES quarters(id),
    uploaded_by BIGINT REFERENCES users(id),
    file_name VARCHAR(255) NOT NULL,
    original_name VARCHAR(255) NOT NULL,
    file_type VARCHAR(50),
    file_size BIGINT,
    category document_category NOT NULL,
    file_path TEXT NOT NULL,
    file_url TEXT,
    upload_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    status document_status DEFAULT 'PENDING',
    reviewed_by BIGINT REFERENCES users(id),
    reviewed_at TIMESTAMP,
    remarks TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Voice Recordings table
CREATE TABLE voice_recordings (
    id BIGSERIAL PRIMARY KEY,
    agenda_id BIGINT NOT NULL REFERENCES agendas(id) ON DELETE CASCADE,
    recording_name VARCHAR(255) NOT NULL,
    file_name VARCHAR(255) NOT NULL,
    file_path TEXT NOT NULL,
    file_url TEXT,
    duration INTEGER, -- in seconds
    file_size BIGINT,
    recorded_by BIGINT REFERENCES users(id),
    recorded_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Notes table
CREATE TABLE notes (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    mrfc_id BIGINT REFERENCES mrfcs(id) ON DELETE CASCADE,
    quarter_id BIGINT REFERENCES quarters(id),
    title VARCHAR(200) NOT NULL,
    content TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Notifications table
CREATE TABLE notifications (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT REFERENCES users(id) ON DELETE CASCADE,
    type notification_type NOT NULL,
    title VARCHAR(200) NOT NULL,
    message TEXT,
    is_read BOOLEAN DEFAULT FALSE,
    read_at TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    sent_at TIMESTAMP
);

-- User MRFC Access (Many-to-Many)
CREATE TABLE user_mrfc_access (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    mrfc_id BIGINT NOT NULL REFERENCES mrfcs(id) ON DELETE CASCADE,
    granted_by BIGINT REFERENCES users(id),
    granted_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    is_active BOOLEAN DEFAULT TRUE,
    UNIQUE(user_id, mrfc_id)
);

-- Compliance Logs table
CREATE TABLE compliance_logs (
    id BIGSERIAL PRIMARY KEY,
    proponent_id BIGINT NOT NULL REFERENCES proponents(id) ON DELETE CASCADE,
    quarter_id BIGINT NOT NULL REFERENCES quarters(id),
    compliance_pct DECIMAL(5,2),
    documents_submitted INTEGER,
    documents_required INTEGER,
    status VARCHAR(50),
    calculated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    calculated_by BIGINT REFERENCES users(id)
);

-- Audit Logs table
CREATE TABLE audit_logs (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT REFERENCES users(id),
    entity_type VARCHAR(50) NOT NULL,
    entity_id BIGINT NOT NULL,
    action audit_action NOT NULL,
    old_values JSONB,
    new_values JSONB,
    ip_address VARCHAR(45),
    user_agent TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Create indexes for performance
CREATE INDEX idx_proponents_mrfc ON proponents(mrfc_id);
CREATE INDEX idx_agendas_mrfc ON agendas(mrfc_id);
CREATE INDEX idx_agendas_quarter ON agendas(quarter_id);
CREATE INDEX idx_matters_arising_agenda ON matters_arising(agenda_id);
CREATE INDEX idx_attendance_agenda ON attendance(agenda_id);
CREATE INDEX idx_attendance_proponent ON attendance(proponent_id);
CREATE INDEX idx_documents_proponent ON documents(proponent_id);
CREATE INDEX idx_documents_quarter ON documents(quarter_id);
CREATE INDEX idx_voice_recordings_agenda ON voice_recordings(agenda_id);
CREATE INDEX idx_notes_user ON notes(user_id);
CREATE INDEX idx_notifications_user ON notifications(user_id);
CREATE INDEX idx_user_mrfc_access_user ON user_mrfc_access(user_id);
CREATE INDEX idx_user_mrfc_access_mrfc ON user_mrfc_access(mrfc_id);
CREATE INDEX idx_compliance_logs_proponent ON compliance_logs(proponent_id);
CREATE INDEX idx_audit_logs_user ON audit_logs(user_id);
CREATE INDEX idx_audit_logs_entity ON audit_logs(entity_type, entity_id);

-- Create updated_at trigger function
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ language 'plpgsql';

-- Apply updated_at trigger to relevant tables
CREATE TRIGGER update_users_updated_at BEFORE UPDATE ON users
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
CREATE TRIGGER update_mrfcs_updated_at BEFORE UPDATE ON mrfcs
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
CREATE TRIGGER update_proponents_updated_at BEFORE UPDATE ON proponents
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
CREATE TRIGGER update_agendas_updated_at BEFORE UPDATE ON agendas
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
CREATE TRIGGER update_matters_arising_updated_at BEFORE UPDATE ON matters_arising
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
CREATE TRIGGER update_documents_updated_at BEFORE UPDATE ON documents
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
CREATE TRIGGER update_notes_updated_at BEFORE UPDATE ON notes
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
```

---

## API ENDPOINTS SPECIFICATION

### Base URL
```
Production: https://api.mgb-mrfc.denr.gov.ph/v1
Development: http://localhost:3000/api/v1
```

### Authentication
All endpoints (except login/register) require JWT token in header:
```
Authorization: Bearer <jwt_token>
```

---

### 1. AUTHENTICATION ENDPOINTS

#### POST `/auth/login`
Login user and return JWT token

**Request Body:**
```json
{
  "username": "admin",
  "password": "admin123"
}
```

**Response (200):**
```json
{
  "success": true,
  "data": {
    "user": {
      "id": 1,
      "username": "admin",
      "full_name": "John Doe",
      "email": "admin@mgb.gov.ph",
      "role": "ADMIN"
    },
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "expiresIn": "24h"
  }
}
```

**Response (401):**
```json
{
  "success": false,
  "error": {
    "code": "INVALID_CREDENTIALS",
    "message": "Invalid username or password"
  }
}
```

---

#### POST `/auth/refresh`
Refresh JWT token

**Request Body:**
```json
{
  "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
}
```

**Response (200):**
```json
{
  "success": true,
  "data": {
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "expiresIn": "24h"
  }
}
```

---

#### POST `/auth/logout`
Logout user (invalidate token)

**Response (200):**
```json
{
  "success": true,
  "message": "Logged out successfully"
}
```

---

#### POST `/auth/change-password`
Change user password (authenticated)

**Request Body:**
```json
{
  "currentPassword": "oldpass123",
  "newPassword": "newpass456"
}
```

**Response (200):**
```json
{
  "success": true,
  "message": "Password changed successfully"
}
```

---

### 2. USER ENDPOINTS

#### GET `/users/me`
Get current authenticated user profile

**Response (200):**
```json
{
  "success": true,
  "data": {
    "id": 1,
    "username": "admin",
    "full_name": "John Doe",
    "email": "admin@mgb.gov.ph",
    "role": "ADMIN",
    "last_login": "2025-10-16T10:30:00Z",
    "created_at": "2025-01-01T00:00:00Z"
  }
}
```

---

#### GET `/users` (Admin only)
Get list of all users

**Query Parameters:**
- `page` (default: 1)
- `limit` (default: 20)
- `role` (filter by role: ADMIN, USER)
- `search` (search by name, username, email)

**Response (200):**
```json
{
  "success": true,
  "data": {
    "users": [
      {
        "id": 1,
        "username": "admin",
        "full_name": "John Doe",
        "email": "admin@mgb.gov.ph",
        "role": "ADMIN",
        "is_active": true,
        "created_at": "2025-01-01T00:00:00Z"
      }
    ],
    "pagination": {
      "page": 1,
      "limit": 20,
      "total": 45,
      "totalPages": 3
    }
  }
}
```

---

#### POST `/users` (Admin only)
Create new user

**Request Body:**
```json
{
  "username": "user1",
  "password": "user123",
  "full_name": "Jane Smith",
  "email": "jane@mgb.gov.ph",
  "role": "USER"
}
```

**Response (201):**
```json
{
  "success": true,
  "data": {
    "id": 2,
    "username": "user1",
    "full_name": "Jane Smith",
    "email": "jane@mgb.gov.ph",
    "role": "USER",
    "created_at": "2025-10-16T10:30:00Z"
  }
}
```

---

#### PUT `/users/:id` (Admin only)
Update user

**Request Body:**
```json
{
  "full_name": "Jane Doe Smith",
  "email": "jane.smith@mgb.gov.ph",
  "role": "ADMIN",
  "is_active": true
}
```

**Response (200):**
```json
{
  "success": true,
  "data": {
    "id": 2,
    "username": "user1",
    "full_name": "Jane Doe Smith",
    "email": "jane.smith@mgb.gov.ph",
    "role": "ADMIN",
    "is_active": true,
    "updated_at": "2025-10-16T11:00:00Z"
  }
}
```

---

#### DELETE `/users/:id` (Admin only)
Deactivate user (soft delete)

**Response (200):**
```json
{
  "success": true,
  "message": "User deactivated successfully"
}
```

---

### 3. MRFC ENDPOINTS

#### GET `/mrfcs`
Get list of MRFCs (Admin: all, User: assigned only)

**Query Parameters:**
- `page` (default: 1)
- `limit` (default: 20)
- `search` (search by name, municipality)
- `is_active` (filter by active status)

**Response (200):**
```json
{
  "success": true,
  "data": {
    "mrfcs": [
      {
        "id": 1,
        "name": "Camarines Norte MRFC",
        "municipality": "Daet",
        "contact_person": "Juan Dela Cruz",
        "contact_number": "09171234567",
        "email": "camarinesnorte.mrfc@denr.gov.ph",
        "is_active": true,
        "proponent_count": 10,
        "created_at": "2025-01-01T00:00:00Z"
      }
    ],
    "pagination": {
      "page": 1,
      "limit": 20,
      "total": 5,
      "totalPages": 1
    }
  }
}
```

---

#### GET `/mrfcs/:id`
Get MRFC details

**Response (200):**
```json
{
  "success": true,
  "data": {
    "id": 1,
    "name": "Camarines Norte MRFC",
    "municipality": "Daet",
    "contact_person": "Juan Dela Cruz",
    "contact_number": "09171234567",
    "email": "camarinesnorte.mrfc@denr.gov.ph",
    "address": "DENR-PENRO Building, Daet, Camarines Norte",
    "is_active": true,
    "proponent_count": 10,
    "created_at": "2025-01-01T00:00:00Z",
    "updated_at": "2025-10-01T00:00:00Z"
  }
}
```

---

#### POST `/mrfcs` (Admin only)
Create new MRFC

**Request Body:**
```json
{
  "name": "Albay MRFC",
  "municipality": "Legazpi City",
  "contact_person": "Maria Santos",
  "contact_number": "09181234567",
  "email": "albay.mrfc@denr.gov.ph",
  "address": "DENR-PENRO Building, Legazpi City, Albay"
}
```

**Response (201):**
```json
{
  "success": true,
  "data": {
    "id": 6,
    "name": "Albay MRFC",
    "municipality": "Legazpi City",
    "contact_person": "Maria Santos",
    "contact_number": "09181234567",
    "email": "albay.mrfc@denr.gov.ph",
    "address": "DENR-PENRO Building, Legazpi City, Albay",
    "is_active": true,
    "created_at": "2025-10-16T10:30:00Z"
  }
}
```

---

#### PUT `/mrfcs/:id` (Admin only)
Update MRFC

**Request Body:**
```json
{
  "contact_person": "Updated Name",
  "contact_number": "09191234567"
}
```

**Response (200):**
```json
{
  "success": true,
  "data": {
    "id": 1,
    "name": "Camarines Norte MRFC",
    "municipality": "Daet",
    "contact_person": "Updated Name",
    "contact_number": "09191234567",
    "updated_at": "2025-10-16T11:00:00Z"
  }
}
```

---

#### DELETE `/mrfcs/:id` (Admin only)
Deactivate MRFC (soft delete)

**Response (200):**
```json
{
  "success": true,
  "message": "MRFC deactivated successfully"
}
```

---

### 4. PROPONENT ENDPOINTS

#### GET `/mrfcs/:mrfcId/proponents`
Get proponents for specific MRFC

**Query Parameters:**
- `page` (default: 1)
- `limit` (default: 20)
- `status` (filter by status: ACTIVE, INACTIVE, SUSPENDED)
- `search` (search by name, company_name)

**Response (200):**
```json
{
  "success": true,
  "data": {
    "proponents": [
      {
        "id": 1,
        "mrfc_id": 1,
        "name": "ABC Mining Corp",
        "company_name": "ABC Mining Corporation",
        "permit_number": "QP-001-2023",
        "permit_type": "Quarry Permit",
        "status": "ACTIVE",
        "contact_person": "Pedro Santos",
        "contact_number": "09171234567",
        "email": "abc@mining.com",
        "compliance_percentage": 85.5,
        "created_at": "2025-01-01T00:00:00Z"
      }
    ],
    "pagination": {
      "page": 1,
      "limit": 20,
      "total": 10,
      "totalPages": 1
    }
  }
}
```

---

#### GET `/proponents/:id`
Get proponent details

**Response (200):**
```json
{
  "success": true,
  "data": {
    "id": 1,
    "mrfc_id": 1,
    "mrfc_name": "Camarines Norte MRFC",
    "name": "ABC Mining Corp",
    "company_name": "ABC Mining Corporation",
    "permit_number": "QP-001-2023",
    "permit_type": "Quarry Permit",
    "status": "ACTIVE",
    "contact_person": "Pedro Santos",
    "contact_number": "09171234567",
    "email": "abc@mining.com",
    "address": "Brgy. Mining Site, Daet, Camarines Norte",
    "compliance_percentage": 85.5,
    "document_count": 24,
    "created_at": "2025-01-01T00:00:00Z",
    "updated_at": "2025-10-01T00:00:00Z"
  }
}
```

---

#### POST `/mrfcs/:mrfcId/proponents` (Admin only)
Create new proponent

**Request Body:**
```json
{
  "name": "XYZ Mining",
  "company_name": "XYZ Mining Company Inc.",
  "permit_number": "QP-010-2025",
  "permit_type": "Quarry Permit",
  "status": "ACTIVE",
  "contact_person": "Anna Reyes",
  "contact_number": "09181234567",
  "email": "xyz@mining.com",
  "address": "Brgy. New Site, Daet, Camarines Norte"
}
```

**Response (201):**
```json
{
  "success": true,
  "data": {
    "id": 11,
    "mrfc_id": 1,
    "name": "XYZ Mining",
    "company_name": "XYZ Mining Company Inc.",
    "permit_number": "QP-010-2025",
    "permit_type": "Quarry Permit",
    "status": "ACTIVE",
    "created_at": "2025-10-16T10:30:00Z"
  }
}
```

---

#### PUT `/proponents/:id` (Admin only)
Update proponent

**Request Body:**
```json
{
  "status": "SUSPENDED",
  "contact_number": "09191234567"
}
```

**Response (200):**
```json
{
  "success": true,
  "data": {
    "id": 1,
    "name": "ABC Mining Corp",
    "status": "SUSPENDED",
    "contact_number": "09191234567",
    "updated_at": "2025-10-16T11:00:00Z"
  }
}
```

---

#### DELETE `/proponents/:id` (Admin only)
Delete proponent (hard delete - use with caution)

**Response (200):**
```json
{
  "success": true,
  "message": "Proponent deleted successfully"
}
```

---

### 5. QUARTER ENDPOINTS

#### GET `/quarters`
Get list of quarters

**Query Parameters:**
- `year` (filter by year)
- `is_current` (filter current quarter)

**Response (200):**
```json
{
  "success": true,
  "data": {
    "quarters": [
      {
        "id": 1,
        "name": "Q1 2025",
        "year": 2025,
        "quarter_number": 1,
        "start_date": "2025-01-01",
        "end_date": "2025-03-31",
        "is_current": false
      },
      {
        "id": 4,
        "name": "Q4 2025",
        "year": 2025,
        "quarter_number": 4,
        "start_date": "2025-10-01",
        "end_date": "2025-12-31",
        "is_current": true
      }
    ]
  }
}
```

---

#### POST `/quarters` (Admin only)
Create new quarter

**Request Body:**
```json
{
  "year": 2026,
  "quarter_number": 1,
  "start_date": "2026-01-01",
  "end_date": "2026-03-31"
}
```

**Response (201):**
```json
{
  "success": true,
  "data": {
    "id": 5,
    "name": "Q1 2026",
    "year": 2026,
    "quarter_number": 1,
    "start_date": "2026-01-01",
    "end_date": "2026-03-31",
    "is_current": false
  }
}
```

---

### 6. AGENDA ENDPOINTS

#### GET `/mrfcs/:mrfcId/agendas`
Get agendas for specific MRFC

**Query Parameters:**
- `quarter_id` (filter by quarter)
- `status` (filter by status)

**Response (200):**
```json
{
  "success": true,
  "data": {
    "agendas": [
      {
        "id": 1,
        "mrfc_id": 1,
        "mrfc_name": "Camarines Norte MRFC",
        "quarter_id": 4,
        "quarter_name": "Q4 2025",
        "meeting_date": "2025-10-25",
        "meeting_time": "09:00:00",
        "location": "Municipal Hall, Conference Room 2, Daet",
        "status": "PUBLISHED",
        "matters_arising_count": 3,
        "created_at": "2025-10-10T00:00:00Z"
      }
    ]
  }
}
```

---

#### GET `/agendas/:id`
Get agenda details with matters arising

**Response (200):**
```json
{
  "success": true,
  "data": {
    "id": 1,
    "mrfc_id": 1,
    "mrfc_name": "Camarines Norte MRFC",
    "quarter_id": 4,
    "quarter_name": "Q4 2025",
    "meeting_date": "2025-10-25",
    "meeting_time": "09:00:00",
    "location": "Municipal Hall, Conference Room 2, Daet",
    "status": "PUBLISHED",
    "standard_items": [
      {
        "id": 1,
        "title": "Call to Order"
      },
      {
        "id": 2,
        "title": "Opening Prayer"
      },
      {
        "id": 3,
        "title": "Roll Call"
      },
      {
        "id": 4,
        "title": "Reading and Approval of Previous Minutes"
      },
      {
        "id": 5,
        "title": "Matters Arising from Previous Meeting"
      },
      {
        "id": 6,
        "title": "Reports from Proponents"
      },
      {
        "id": 7,
        "title": "Other Matters"
      },
      {
        "id": 8,
        "title": "Adjournment"
      }
    ],
    "matters_arising": [
      {
        "id": 1,
        "issue": "Incomplete AEPEP submission from ABC Mining",
        "status": "IN_PROGRESS",
        "assigned_to": "Juan Dela Cruz",
        "date_raised": "2025-09-15",
        "remarks": "Follow-up scheduled for October 20"
      }
    ],
    "created_at": "2025-10-10T00:00:00Z",
    "updated_at": "2025-10-12T00:00:00Z"
  }
}
```

---

#### POST `/mrfcs/:mrfcId/agendas` (Admin only)
Create new agenda

**Request Body:**
```json
{
  "quarter_id": 4,
  "meeting_date": "2025-10-25",
  "meeting_time": "09:00:00",
  "location": "Municipal Hall, Conference Room 2, Daet",
  "matters_arising": [
    {
      "issue": "Incomplete AEPEP submission from ABC Mining",
      "status": "PENDING",
      "assigned_to": "Juan Dela Cruz",
      "date_raised": "2025-09-15",
      "remarks": "Follow-up needed"
    }
  ]
}
```

**Response (201):**
```json
{
  "success": true,
  "data": {
    "id": 2,
    "mrfc_id": 1,
    "quarter_id": 4,
    "meeting_date": "2025-10-25",
    "meeting_time": "09:00:00",
    "location": "Municipal Hall, Conference Room 2, Daet",
    "status": "DRAFT",
    "matters_arising_count": 1,
    "created_at": "2025-10-16T10:30:00Z"
  }
}
```

---

#### PUT `/agendas/:id` (Admin only)
Update agenda

**Request Body:**
```json
{
  "meeting_date": "2025-10-26",
  "location": "Updated location",
  "status": "PUBLISHED"
}
```

**Response (200):**
```json
{
  "success": true,
  "data": {
    "id": 1,
    "meeting_date": "2025-10-26",
    "location": "Updated location",
    "status": "PUBLISHED",
    "updated_at": "2025-10-16T11:00:00Z"
  }
}
```

---

#### DELETE `/agendas/:id` (Admin only)
Delete agenda

**Response (200):**
```json
{
  "success": true,
  "message": "Agenda deleted successfully"
}
```

---

### 7. MATTERS ARISING ENDPOINTS

#### POST `/agendas/:agendaId/matters-arising` (Admin only)
Add matter arising to agenda

**Request Body:**
```json
{
  "issue": "Safety report pending from XYZ Mining",
  "status": "PENDING",
  "assigned_to": "Maria Santos",
  "date_raised": "2025-10-16",
  "remarks": "Urgent follow-up required"
}
```

**Response (201):**
```json
{
  "success": true,
  "data": {
    "id": 4,
    "agenda_id": 1,
    "issue": "Safety report pending from XYZ Mining",
    "status": "PENDING",
    "assigned_to": "Maria Santos",
    "date_raised": "2025-10-16",
    "remarks": "Urgent follow-up required",
    "created_at": "2025-10-16T10:30:00Z"
  }
}
```

---

#### PUT `/matters-arising/:id` (Admin only)
Update matter arising

**Request Body:**
```json
{
  "status": "RESOLVED",
  "date_resolved": "2025-10-20",
  "remarks": "Report submitted and accepted"
}
```

**Response (200):**
```json
{
  "success": true,
  "data": {
    "id": 4,
    "status": "RESOLVED",
    "date_resolved": "2025-10-20",
    "remarks": "Report submitted and accepted",
    "updated_at": "2025-10-20T10:00:00Z"
  }
}
```

---

#### DELETE `/matters-arising/:id` (Admin only)
Delete matter arising

**Response (200):**
```json
{
  "success": true,
  "message": "Matter arising deleted successfully"
}
```

---

### 8. ATTENDANCE ENDPOINTS

#### GET `/agendas/:agendaId/attendance`
Get attendance records for specific agenda/meeting

**Response (200):**
```json
{
  "success": true,
  "data": {
    "agenda_id": 1,
    "meeting_date": "2025-10-25",
    "photo_url": "https://storage.mgb.gov.ph/attendance/2025/Q4/meeting-1-photo.jpg",
    "summary": {
      "total_proponents": 10,
      "present": 8,
      "absent": 2,
      "attendance_rate": 80.0
    },
    "attendance": [
      {
        "id": 1,
        "proponent_id": 1,
        "proponent_name": "ABC Mining Corp",
        "company_name": "ABC Mining Corporation",
        "is_present": true,
        "marked_at": "2025-10-25T09:15:00Z",
        "marked_by": "admin",
        "remarks": null
      },
      {
        "id": 2,
        "proponent_id": 2,
        "proponent_name": "DEF Quarry",
        "company_name": "DEF Quarrying Services",
        "is_present": false,
        "marked_at": "2025-10-25T09:15:00Z",
        "marked_by": "admin",
        "remarks": "Notified absence due to equipment issue"
      }
    ]
  }
}
```

---

#### POST `/agendas/:agendaId/attendance` (Admin only)
Mark attendance for meeting (bulk operation)

**Request Body:**
```json
{
  "photo": "base64_encoded_image_string",
  "attendance": [
    {
      "proponent_id": 1,
      "is_present": true
    },
    {
      "proponent_id": 2,
      "is_present": false,
      "remarks": "Notified absence"
    }
  ]
}
```

**Response (201):**
```json
{
  "success": true,
  "data": {
    "agenda_id": 1,
    "photo_url": "https://storage.mgb.gov.ph/attendance/2025/Q4/meeting-1-photo.jpg",
    "summary": {
      "total_proponents": 10,
      "present": 8,
      "absent": 2,
      "attendance_rate": 80.0
    },
    "created_at": "2025-10-25T09:15:00Z"
  }
}
```

---

#### PUT `/attendance/:id` (Admin only)
Update individual attendance record

**Request Body:**
```json
{
  "is_present": true,
  "remarks": "Arrived late"
}
```

**Response (200):**
```json
{
  "success": true,
  "data": {
    "id": 2,
    "proponent_id": 2,
    "is_present": true,
    "remarks": "Arrived late",
    "updated_at": "2025-10-25T10:00:00Z"
  }
}
```

---

### 9. DOCUMENT ENDPOINTS

#### GET `/documents`
Get documents (Admin: all, User: accessible only)

**Query Parameters:**
- `proponent_id` (filter by proponent)
- `quarter_id` (filter by quarter)
- `category` (filter by category)
- `status` (filter by status)
- `search` (search by file name)
- `page` (default: 1)
- `limit` (default: 20)

**Response (200):**
```json
{
  "success": true,
  "data": {
    "documents": [
      {
        "id": 1,
        "proponent_id": 1,
        "proponent_name": "ABC Mining Corp",
        "quarter_id": 4,
        "quarter_name": "Q4 2025",
        "file_name": "abc_mining_mtf_q4_2025.pdf",
        "original_name": "MTF Report Q4 2025.pdf",
        "file_type": "application/pdf",
        "file_size": 2458624,
        "category": "MTF_REPORT",
        "file_url": "https://storage.mgb.gov.ph/documents/2025/Q4/abc_mining_mtf_q4_2025.pdf",
        "upload_date": "2025-10-15T14:30:00Z",
        "status": "ACCEPTED",
        "uploaded_by": "admin"
      }
    ],
    "pagination": {
      "page": 1,
      "limit": 20,
      "total": 142,
      "totalPages": 8
    }
  }
}
```

---

#### GET `/documents/:id`
Get document details

**Response (200):**
```json
{
  "success": true,
  "data": {
    "id": 1,
    "proponent_id": 1,
    "proponent_name": "ABC Mining Corp",
    "quarter_id": 4,
    "quarter_name": "Q4 2025",
    "file_name": "abc_mining_mtf_q4_2025.pdf",
    "original_name": "MTF Report Q4 2025.pdf",
    "file_type": "application/pdf",
    "file_size": 2458624,
    "category": "MTF_REPORT",
    "file_url": "https://storage.mgb.gov.ph/documents/2025/Q4/abc_mining_mtf_q4_2025.pdf",
    "upload_date": "2025-10-15T14:30:00Z",
    "status": "ACCEPTED",
    "uploaded_by": "admin",
    "reviewed_by": "supervisor1",
    "reviewed_at": "2025-10-16T09:00:00Z",
    "remarks": "Document complete and acceptable"
  }
}
```

---

#### POST `/documents/upload` (Admin only)
Upload new document

**Request (multipart/form-data):**
```
file: [binary file]
proponent_id: 1
quarter_id: 4
category: MTF_REPORT
remarks: Q4 2025 submission
```

**Response (201):**
```json
{
  "success": true,
  "data": {
    "id": 143,
    "proponent_id": 1,
    "quarter_id": 4,
    "file_name": "abc_mining_mtf_q4_2025.pdf",
    "original_name": "MTF Report Q4 2025.pdf",
    "file_type": "application/pdf",
    "file_size": 2458624,
    "category": "MTF_REPORT",
    "file_url": "https://storage.mgb.gov.ph/documents/2025/Q4/abc_mining_mtf_q4_2025.pdf",
    "status": "PENDING",
    "upload_date": "2025-10-16T10:30:00Z"
  }
}
```

---

#### GET `/documents/:id/download`
Download document file

**Response (200):**
```
Content-Type: application/pdf
Content-Disposition: attachment; filename="MTF Report Q4 2025.pdf"
[Binary file content]
```

---

#### PUT `/documents/:id` (Admin only)
Update document metadata (not file itself)

**Request Body:**
```json
{
  "status": "ACCEPTED",
  "remarks": "Document reviewed and approved"
}
```

**Response (200):**
```json
{
  "success": true,
  "data": {
    "id": 1,
    "status": "ACCEPTED",
    "reviewed_by": "admin",
    "reviewed_at": "2025-10-16T11:00:00Z",
    "remarks": "Document reviewed and approved"
  }
}
```

---

#### DELETE `/documents/:id` (Admin only)
Delete document (removes file from storage)

**Response (200):**
```json
{
  "success": true,
  "message": "Document deleted successfully"
}
```

---

### 10. VOICE RECORDING ENDPOINTS

#### GET `/agendas/:agendaId/recordings`
Get voice recordings for specific agenda

**Response (200):**
```json
{
  "success": true,
  "data": {
    "recordings": [
      {
        "id": 1,
        "agenda_id": 1,
        "recording_name": "MRFC Q4 Meeting - 2025-10-25",
        "file_name": "meeting_1_recording.mp3",
        "file_url": "https://storage.mgb.gov.ph/recordings/2025/Q4/meeting_1_recording.mp3",
        "duration": 7245,
        "file_size": 57860000,
        "recorded_by": "admin",
        "recorded_at": "2025-10-25T09:00:00Z"
      }
    ]
  }
}
```

---

#### POST `/agendas/:agendaId/recordings/upload` (Admin only)
Upload voice recording

**Request (multipart/form-data):**
```
file: [binary audio file]
recording_name: MRFC Q4 Meeting - 2025-10-25
notes: Full meeting audio
```

**Response (201):**
```json
{
  "success": true,
  "data": {
    "id": 2,
    "agenda_id": 1,
    "recording_name": "MRFC Q4 Meeting - 2025-10-25",
    "file_name": "meeting_1_recording.mp3",
    "file_url": "https://storage.mgb.gov.ph/recordings/2025/Q4/meeting_1_recording.mp3",
    "duration": 7245,
    "file_size": 57860000,
    "recorded_at": "2025-10-25T09:00:00Z"
  }
}
```

---

#### GET `/recordings/:id/download`
Download voice recording

**Response (200):**
```
Content-Type: audio/mpeg
Content-Disposition: attachment; filename="meeting_1_recording.mp3"
[Binary audio content]
```

---

#### DELETE `/recordings/:id` (Admin only)
Delete voice recording

**Response (200):**
```json
{
  "success": true,
  "message": "Recording deleted successfully"
}
```

---

### 11. NOTES ENDPOINTS

#### GET `/notes`
Get user's personal notes

**Query Parameters:**
- `mrfc_id` (filter by MRFC)
- `quarter_id` (filter by quarter)
- `search` (search in title and content)
- `page` (default: 1)
- `limit` (default: 50)

**Response (200):**
```json
{
  "success": true,
  "data": {
    "notes": [
      {
        "id": 1,
        "user_id": 2,
        "mrfc_id": 1,
        "mrfc_name": "Camarines Norte MRFC",
        "quarter_id": 4,
        "quarter_name": "Q4 2025",
        "title": "Meeting Observations",
        "content": "ABC Mining showed improvement in safety protocols...",
        "created_at": "2025-10-25T11:30:00Z",
        "updated_at": "2025-10-25T11:30:00Z"
      }
    ],
    "pagination": {
      "page": 1,
      "limit": 50,
      "total": 23,
      "totalPages": 1
    }
  }
}
```

---

#### POST `/notes`
Create new note

**Request Body:**
```json
{
  "mrfc_id": 1,
  "quarter_id": 4,
  "title": "Follow-up Items",
  "content": "Need to verify ABC Mining's production report figures..."
}
```

**Response (201):**
```json
{
  "success": true,
  "data": {
    "id": 24,
    "user_id": 2,
    "mrfc_id": 1,
    "quarter_id": 4,
    "title": "Follow-up Items",
    "content": "Need to verify ABC Mining's production report figures...",
    "created_at": "2025-10-16T10:30:00Z"
  }
}
```

---

#### PUT `/notes/:id`
Update note (own notes only)

**Request Body:**
```json
{
  "title": "Updated title",
  "content": "Updated content..."
}
```

**Response (200):**
```json
{
  "success": true,
  "data": {
    "id": 24,
    "title": "Updated title",
    "content": "Updated content...",
    "updated_at": "2025-10-16T11:00:00Z"
  }
}
```

---

#### DELETE `/notes/:id`
Delete note (own notes only)

**Response (200):**
```json
{
  "success": true,
  "message": "Note deleted successfully"
}
```

---

### 12. NOTIFICATIONS ENDPOINTS

#### GET `/notifications`
Get user's notifications

**Query Parameters:**
- `type` (filter by type)
- `is_read` (filter by read status)
- `page` (default: 1)
- `limit` (default: 50)

**Response (200):**
```json
{
  "success": true,
  "data": {
    "notifications": [
      {
        "id": 1,
        "type": "MEETING",
        "title": "Upcoming MRFC Meeting",
        "message": "MRFC Quarterly Meeting scheduled for October 25, 2025 at 9:00 AM",
        "is_read": false,
        "created_at": "2025-10-15T08:00:00Z"
      },
      {
        "id": 2,
        "type": "COMPLIANCE",
        "title": "Compliance Report Due",
        "message": "Q4 compliance documents due by October 31, 2025",
        "is_read": true,
        "read_at": "2025-10-16T09:00:00Z",
        "created_at": "2025-10-01T08:00:00Z"
      }
    ],
    "unread_count": 5,
    "pagination": {
      "page": 1,
      "limit": 50,
      "total": 45,
      "totalPages": 1
    }
  }
}
```

---

#### POST `/notifications` (Admin only)
Create notification (send to users)

**Request Body:**
```json
{
  "user_ids": [1, 2, 3],
  "type": "MEETING",
  "title": "Meeting Reschedule",
  "message": "MRFC meeting rescheduled to October 26, 2025"
}
```

**Response (201):**
```json
{
  "success": true,
  "message": "Notification sent to 3 users"
}
```

---

#### PUT `/notifications/:id/read`
Mark notification as read

**Response (200):**
```json
{
  "success": true,
  "data": {
    "id": 1,
    "is_read": true,
    "read_at": "2025-10-16T10:30:00Z"
  }
}
```

---

#### PUT `/notifications/mark-all-read`
Mark all notifications as read

**Response (200):**
```json
{
  "success": true,
  "message": "All notifications marked as read",
  "updated_count": 5
}
```

---

### 13. COMPLIANCE ENDPOINTS

#### GET `/compliance/dashboard`
Get compliance dashboard data (Admin: all, User: assigned MRFCs)

**Query Parameters:**
- `mrfc_id` (filter by MRFC)
- `quarter_id` (filter by quarter)

**Response (200):**
```json
{
  "success": true,
  "data": {
    "summary": {
      "total_proponents": 50,
      "compliant": 35,
      "partially_compliant": 10,
      "non_compliant": 5,
      "compliance_rate": 70.0,
      "average_compliance_percentage": 78.5
    },
    "by_status": {
      "compliant": {
        "count": 35,
        "percentage": 70.0
      },
      "partially_compliant": {
        "count": 10,
        "percentage": 20.0
      },
      "non_compliant": {
        "count": 5,
        "percentage": 10.0
      }
    },
    "proponents": [
      {
        "id": 1,
        "name": "ABC Mining Corp",
        "company_name": "ABC Mining Corporation",
        "mrfc_name": "Camarines Norte MRFC",
        "compliance_percentage": 100.0,
        "documents_submitted": 6,
        "documents_required": 6,
        "status": "COMPLIANT",
        "last_submission": "2025-10-15T14:30:00Z"
      },
      {
        "id": 2,
        "name": "DEF Quarry",
        "company_name": "DEF Quarrying Services",
        "mrfc_name": "Camarines Norte MRFC",
        "compliance_percentage": 66.7,
        "documents_submitted": 4,
        "documents_required": 6,
        "status": "PARTIALLY_COMPLIANT",
        "last_submission": "2025-10-10T10:00:00Z"
      }
    ]
  }
}
```

---

#### GET `/compliance/proponent/:proponentId`
Get detailed compliance for specific proponent

**Query Parameters:**
- `quarter_id` (filter by quarter)

**Response (200):**
```json
{
  "success": true,
  "data": {
    "proponent_id": 1,
    "proponent_name": "ABC Mining Corp",
    "company_name": "ABC Mining Corporation",
    "mrfc_name": "Camarines Norte MRFC",
    "quarter_name": "Q4 2025",
    "compliance_percentage": 83.3,
    "documents_submitted": 5,
    "documents_required": 6,
    "status": "COMPLIANT",
    "documents": [
      {
        "category": "MTF_REPORT",
        "required": true,
        "submitted": true,
        "status": "ACCEPTED",
        "submission_date": "2025-10-15T14:30:00Z",
        "document_id": 1
      },
      {
        "category": "AEPEP",
        "required": true,
        "submitted": true,
        "status": "ACCEPTED",
        "submission_date": "2025-10-12T10:00:00Z",
        "document_id": 2
      },
      {
        "category": "CMVR",
        "required": true,
        "submitted": false,
        "status": null,
        "submission_date": null,
        "document_id": null
      }
    ],
    "compliance_history": [
      {
        "quarter_name": "Q3 2025",
        "compliance_percentage": 100.0,
        "status": "COMPLIANT"
      },
      {
        "quarter_name": "Q2 2025",
        "compliance_percentage": 83.3,
        "status": "COMPLIANT"
      }
    ]
  }
}
```

---

#### POST `/compliance/calculate` (Admin only)
Manually trigger compliance calculation

**Request Body:**
```json
{
  "quarter_id": 4,
  "mrfc_id": 1
}
```

**Response (200):**
```json
{
  "success": true,
  "message": "Compliance calculated for 10 proponents",
  "data": {
    "quarter_id": 4,
    "mrfc_id": 1,
    "calculated_at": "2025-10-16T10:30:00Z"
  }
}
```

---

#### GET `/compliance/export`
Export compliance report

**Query Parameters:**
- `mrfc_id` (optional)
- `quarter_id` (required)
- `format` (pdf, excel, csv)

**Response (200):**
```
Content-Type: application/pdf (or application/vnd.ms-excel, text/csv)
Content-Disposition: attachment; filename="compliance_report_Q4_2025.pdf"
[Binary file content]
```

---

### 14. USER-MRFC ACCESS ENDPOINTS

#### GET `/users/:userId/mrfcs` (Admin only)
Get MRFCs assigned to specific user

**Response (200):**
```json
{
  "success": true,
  "data": {
    "user_id": 2,
    "username": "user1",
    "full_name": "Jane Smith",
    "assigned_mrfcs": [
      {
        "mrfc_id": 1,
        "mrfc_name": "Camarines Norte MRFC",
        "granted_at": "2025-01-01T00:00:00Z",
        "granted_by": "admin"
      }
    ]
  }
}
```

---

#### POST `/users/:userId/mrfcs` (Admin only)
Grant user access to MRFC

**Request Body:**
```json
{
  "mrfc_id": 2
}
```

**Response (201):**
```json
{
  "success": true,
  "message": "Access granted successfully",
  "data": {
    "user_id": 2,
    "mrfc_id": 2,
    "granted_at": "2025-10-16T10:30:00Z"
  }
}
```

---

#### DELETE `/users/:userId/mrfcs/:mrfcId` (Admin only)
Revoke user access to MRFC

**Response (200):**
```json
{
  "success": true,
  "message": "Access revoked successfully"
}
```

---

### 15. STATISTICS & REPORTS ENDPOINTS

#### GET `/statistics/overview` (Admin only)
Get system-wide statistics

**Response (200):**
```json
{
  "success": true,
  "data": {
    "total_mrfcs": 5,
    "total_proponents": 50,
    "total_users": 45,
    "total_documents": 856,
    "meetings_this_quarter": 5,
    "average_attendance_rate": 82.5,
    "overall_compliance_rate": 78.0,
    "recent_activity": {
      "last_document_upload": "2025-10-16T09:45:00Z",
      "last_meeting": "2025-10-15T09:00:00Z",
      "last_user_login": "2025-10-16T10:30:00Z"
    }
  }
}
```

---

#### GET `/statistics/mrfc/:mrfcId`
Get statistics for specific MRFC

**Response (200):**
```json
{
  "success": true,
  "data": {
    "mrfc_id": 1,
    "mrfc_name": "Camarines Norte MRFC",
    "total_proponents": 10,
    "active_proponents": 8,
    "inactive_proponents": 2,
    "meetings_this_year": 4,
    "average_attendance_rate": 85.0,
    "compliance_rate": 80.0,
    "documents_this_quarter": 48,
    "pending_documents": 12,
    "top_performers": [
      {
        "proponent_name": "ABC Mining Corp",
        "compliance_percentage": 100.0
      }
    ],
    "at_risk_proponents": [
      {
        "proponent_name": "XYZ Mining",
        "compliance_percentage": 33.3
      }
    ]
  }
}
```

---

### 16. AUDIT LOG ENDPOINTS

#### GET `/audit-logs` (Admin only)
Get audit logs

**Query Parameters:**
- `user_id` (filter by user)
- `entity_type` (filter by table name)
- `action` (filter by action: CREATE, UPDATE, DELETE)
- `start_date` (filter from date)
- `end_date` (filter to date)
- `page` (default: 1)
- `limit` (default: 50)

**Response (200):**
```json
{
  "success": true,
  "data": {
    "logs": [
      {
        "id": 1234,
        "user_id": 1,
        "username": "admin",
        "entity_type": "proponents",
        "entity_id": 5,
        "action": "UPDATE",
        "old_values": {
          "status": "ACTIVE"
        },
        "new_values": {
          "status": "SUSPENDED"
        },
        "ip_address": "192.168.1.100",
        "created_at": "2025-10-16T10:30:00Z"
      }
    ],
    "pagination": {
      "page": 1,
      "limit": 50,
      "total": 5678,
      "totalPages": 114
    }
  }
}
```

---

## IMPLEMENTATION PHASES

### Phase 1: Foundation (Weeks 1-2)
**Priority: CRITICAL**

**Tasks:**
1. ✅ Set up development environment
   - Install Node.js, PostgreSQL, code editor
   - Initialize Git repository
   - Set up project structure

2. ✅ Initialize backend project
   - Create Express.js/NestJS project
   - Configure TypeScript (if using)
   - Set up environment variables (.env)
   - Configure linting and formatting

3. ✅ Set up database
   - Install PostgreSQL
   - Create database
   - Run SQL schema script
   - Create seed data script
   - Set up database migrations tool (e.g., Sequelize migrations, TypeORM migrations)

4. ✅ Implement basic authentication
   - User model and table
   - Password hashing (bcrypt)
   - JWT token generation and validation
   - Login endpoint
   - JWT middleware for route protection

5. ✅ Set up project documentation
   - API documentation framework (Swagger/OpenAPI)
   - README.md with setup instructions
   - Environment variable documentation

**Deliverables:**
- Running backend server
- Database with schema
- Working login endpoint
- JWT authentication middleware
- Basic API documentation

---

### Phase 2: Core CRUD Operations (Weeks 3-4)
**Priority: HIGH**

**Tasks:**
1. ✅ Implement User endpoints
   - GET /users/me
   - GET /users (admin)
   - POST /users (admin)
   - PUT /users/:id (admin)
   - DELETE /users/:id (admin)

2. ✅ Implement MRFC endpoints
   - GET /mrfcs
   - GET /mrfcs/:id
   - POST /mrfcs (admin)
   - PUT /mrfcs/:id (admin)
   - DELETE /mrfcs/:id (admin)

3. ✅ Implement Proponent endpoints
   - GET /mrfcs/:mrfcId/proponents
   - GET /proponents/:id
   - POST /mrfcs/:mrfcId/proponents (admin)
   - PUT /proponents/:id (admin)
   - DELETE /proponents/:id (admin)

4. ✅ Implement Quarter endpoints
   - GET /quarters
   - POST /quarters (admin)

5. ✅ Implement role-based access control
   - Admin vs User middleware
   - MRFC access control for users
   - User-MRFC relationship endpoints

**Deliverables:**
- All user, MRFC, proponent, quarter CRUD endpoints
- Role-based access control
- Updated API documentation

---

### Phase 3: Meeting Management (Weeks 5-6)
**Priority: HIGH**

**Tasks:**
1. ✅ Implement Agenda endpoints
   - GET /mrfcs/:mrfcId/agendas
   - GET /agendas/:id
   - POST /mrfcs/:mrfcId/agendas (admin)
   - PUT /agendas/:id (admin)
   - DELETE /agendas/:id (admin)

2. ✅ Implement Matters Arising endpoints
   - POST /agendas/:agendaId/matters-arising (admin)
   - PUT /matters-arising/:id (admin)
   - DELETE /matters-arising/:id (admin)

3. ✅ Implement standard agenda items
   - Hardcoded 8 standard items
   - Return with agenda details

4. ✅ Implement business logic
   - One agenda per MRFC per quarter validation
   - Cascade delete for matters arising
   - Status management (DRAFT, PUBLISHED, COMPLETED)

**Deliverables:**
- All agenda and matters arising endpoints
- Business logic validation
- Updated API documentation

---

### Phase 4: File Storage System (Weeks 7-8)
**Priority: HIGH**

**Tasks:**
1. ✅ Set up cloud storage
   - Choose provider (AWS S3 / Google Cloud Storage / Azure Blob)
   - Create storage buckets
   - Configure access keys
   - Set up folder structure (by year/quarter)

2. ✅ Implement file upload middleware
   - Multer configuration for multipart/form-data
   - File type validation (PDF, DOC, XLS, JPG, PNG)
   - File size validation (max 25 MB)
   - Virus scanning (optional but recommended)

3. ✅ Implement Document endpoints
   - POST /documents/upload (admin)
   - GET /documents
   - GET /documents/:id
   - GET /documents/:id/download
   - PUT /documents/:id (admin)
   - DELETE /documents/:id (admin)

4. ✅ Implement Voice Recording endpoints
   - POST /agendas/:agendaId/recordings/upload (admin)
   - GET /agendas/:agendaId/recordings
   - GET /recordings/:id/download
   - DELETE /recordings/:id (admin)

5. ✅ Implement file cleanup
   - Delete files from storage when database record deleted
   - Scheduled cleanup for orphaned files

**Deliverables:**
- Cloud storage integration
- Document upload/download functionality
- Voice recording upload/download functionality
- File management endpoints

---

### Phase 5: Attendance & Compliance (Weeks 9-10)
**Priority: HIGH**

**Tasks:**
1. ✅ Implement Attendance endpoints
   - POST /agendas/:agendaId/attendance (admin)
   - GET /agendas/:agendaId/attendance
   - PUT /attendance/:id (admin)
   - Photo upload with attendance

2. ✅ Implement Compliance calculation logic
   - Calculate compliance percentage per proponent per quarter
   - Required documents checklist by category
   - Compliance status determination (COMPLIANT, PARTIALLY_COMPLIANT, NON_COMPLIANT)
   - Store in compliance_logs table

3. ✅ Implement Compliance endpoints
   - GET /compliance/dashboard
   - GET /compliance/proponent/:proponentId
   - POST /compliance/calculate (admin)
   - GET /compliance/export

4. ✅ Implement scheduled compliance calculation
   - Cron job to calculate compliance daily
   - Notification triggers for non-compliance

**Deliverables:**
- Attendance tracking functionality
- Compliance calculation system
- Compliance dashboard endpoints
- Export functionality

---

### Phase 6: Notes & Notifications (Weeks 11-12)
**Priority: MEDIUM**

**Tasks:**
1. ✅ Implement Notes endpoints
   - GET /notes
   - POST /notes
   - PUT /notes/:id
   - DELETE /notes/:id
   - Search functionality

2. ✅ Implement Notifications endpoints
   - GET /notifications
   - POST /notifications (admin)
   - PUT /notifications/:id/read
   - PUT /notifications/mark-all-read

3. ✅ Implement notification triggers
   - New meeting scheduled
   - Document submission deadline approaching
   - Non-compliance alert
   - Manual notifications from admin

4. ✅ Implement push notification service (optional)
   - Firebase Cloud Messaging integration
   - Device token registration
   - Push notification sending

**Deliverables:**
- Personal notes functionality
- Notification system
- Push notifications (optional)

---

### Phase 7: Reporting & Analytics (Weeks 13-14)
**Priority: MEDIUM**

**Tasks:**
1. ✅ Implement Statistics endpoints
   - GET /statistics/overview (admin)
   - GET /statistics/mrfc/:mrfcId

2. ✅ Implement report generation
   - PDF generation library (e.g., PDFKit, Puppeteer)
   - Excel generation library (e.g., ExcelJS)
   - CSV generation
   - Report templates

3. ✅ Implement Audit Log endpoints
   - GET /audit-logs (admin)
   - Automatic audit logging middleware

4. ✅ Implement audit logging system
   - Trigger on all CREATE, UPDATE, DELETE operations
   - Capture old/new values
   - Store user IP and user agent

**Deliverables:**
- Statistics and analytics endpoints
- Report generation (PDF, Excel, CSV)
- Audit logging system

---

### Phase 8: Testing & Optimization (Weeks 15-16)
**Priority: HIGH**

**Tasks:**
1. ✅ Write unit tests
   - Test all endpoints
   - Test authentication middleware
   - Test business logic functions
   - Target: 80%+ code coverage

2. ✅ Write integration tests
   - Test complete user workflows
   - Test file uploads
   - Test compliance calculations

3. ✅ Performance optimization
   - Database query optimization
   - Add database indexes
   - Implement caching (Redis)
   - Image/file optimization

4. ✅ Security audit
   - SQL injection prevention
   - XSS prevention
   - CSRF protection
   - Rate limiting
   - Input validation
   - Dependency vulnerability scan

5. ✅ Load testing
   - Test with 100+ concurrent users
   - Test file upload under load
   - Identify bottlenecks

**Deliverables:**
- Comprehensive test suite
- Performance optimization report
- Security audit report
- Load testing results

---

### Phase 9: Deployment & DevOps (Weeks 17-18)
**Priority: HIGH**

**Tasks:**
1. ✅ Set up production environment
   - Provision server (AWS EC2 / Google Cloud / Azure)
   - Install Node.js, PostgreSQL, Nginx
   - Configure firewall and security groups
   - Set up SSL certificate (Let's Encrypt)

2. ✅ Configure CI/CD pipeline
   - GitHub Actions / GitLab CI / Jenkins
   - Automated testing on push
   - Automated deployment on merge to main
   - Environment-specific configs

3. ✅ Set up monitoring and logging
   - Application logging (Winston, Bunyan)
   - Error tracking (Sentry)
   - Performance monitoring (New Relic, Datadog)
   - Uptime monitoring

4. ✅ Database backup automation
   - Automated daily backups
   - Backup retention policy
   - Restore testing

5. ✅ Create deployment documentation
   - Server setup guide
   - Deployment procedure
   - Rollback procedure
   - Troubleshooting guide

**Deliverables:**
- Production server setup
- CI/CD pipeline
- Monitoring and logging
- Deployment documentation

---

### Phase 10: Android Integration (Weeks 19-20)
**Priority: CRITICAL**

**Tasks:**
1. ✅ Update Android data models
   - Add API response parsing
   - Update existing data classes
   - Add error handling models

2. ✅ Implement API client in Android
   - Retrofit setup
   - Base URL configuration
   - JWT token interceptor
   - Error interceptor

3. ✅ Replace hardcoded data with API calls
   - Update all activities to use API
   - Implement loading states
   - Implement error handling
   - Implement retry logic

4. ✅ Implement local caching
   - Room database setup
   - Cache API responses
   - Offline-first architecture
   - Sync when online

5. ✅ Update UI for real-time data
   - Pull-to-refresh
   - Loading indicators
   - Empty states
   - Error states

6. ✅ Testing Android integration
   - Test all user flows
   - Test offline mode
   - Test sync functionality
   - Fix bugs

**Deliverables:**
- Fully integrated Android app
- API client library
- Offline support
- Updated Android app ready for testing

---

## TASK BREAKDOWN BY PRIORITY

### CRITICAL PRIORITY (Must have for MVP)
- [ ] Phase 1: Foundation
- [ ] Phase 2: Core CRUD Operations
- [ ] Phase 3: Meeting Management
- [ ] Phase 4: File Storage System (Documents only)
- [ ] Phase 5: Attendance & Compliance
- [ ] Phase 9: Deployment & DevOps
- [ ] Phase 10: Android Integration

### HIGH PRIORITY (Important for full functionality)
- [ ] Phase 4: Voice Recording (remaining)
- [ ] Phase 8: Testing & Optimization

### MEDIUM PRIORITY (Nice to have, can be added later)
- [ ] Phase 6: Notes & Notifications
- [ ] Phase 7: Reporting & Analytics
- [ ] Push notifications
- [ ] Advanced reporting (PDF, Excel)

### LOW PRIORITY (Future enhancements)
- [ ] Web admin dashboard
- [ ] Real-time collaboration (Socket.io)
- [ ] Advanced analytics
- [ ] Mobile app (iOS)
- [ ] Facial recognition for attendance

---

## TESTING REQUIREMENTS

### Unit Tests
- Test all API endpoints (request/response)
- Test authentication middleware
- Test authorization logic
- Test business logic functions
- Test file upload/download
- Test compliance calculation
- Target: 80%+ code coverage

### Integration Tests
- Test complete user workflows:
  - Admin creates MRFC, proponents, agenda
  - Admin uploads documents
  - Admin marks attendance
  - User views data
  - User creates notes
- Test file operations
- Test compliance calculation end-to-end

### API Tests
- Test all endpoints with Postman/Insomnia
- Create Postman collection for all endpoints
- Test authentication flow
- Test authorization (role-based access)
- Test error responses
- Test pagination
- Test filtering and search

### Performance Tests
- Load test with 100+ concurrent users
- Stress test file uploads
- Test database query performance
- Identify and fix slow endpoints

### Security Tests
- SQL injection testing
- XSS testing
- CSRF testing
- Authentication bypass testing
- Authorization bypass testing
- File upload security testing
- Rate limiting testing

---

## DEPLOYMENT CHECKLIST

### Pre-Deployment
- [ ] All tests passing (unit, integration, API)
- [ ] Code reviewed and approved
- [ ] API documentation up to date
- [ ] Environment variables documented
- [ ] Database migrations ready
- [ ] Seed data script ready (optional)
- [ ] Backup strategy in place

### Production Server Setup
- [ ] Server provisioned and accessible
- [ ] Node.js installed (correct version)
- [ ] PostgreSQL installed and configured
- [ ] Nginx installed and configured
- [ ] SSL certificate installed
- [ ] Firewall configured
- [ ] Cloud storage configured
- [ ] Domain name configured

### Deployment Process
- [ ] Clone repository to server
- [ ] Install dependencies (npm install --production)
- [ ] Set environment variables
- [ ] Run database migrations
- [ ] Run seed data (if needed)
- [ ] Start application (PM2 or systemd)
- [ ] Verify application running
- [ ] Test all endpoints
- [ ] Configure Nginx reverse proxy
- [ ] Test via domain name

### Post-Deployment
- [ ] Verify all features working
- [ ] Test Android app with production API
- [ ] Set up monitoring and alerts
- [ ] Set up automated backups
- [ ] Create rollback plan
- [ ] Train admin users
- [ ] Provide documentation to users

### Monitoring & Maintenance
- [ ] Monitor application logs
- [ ] Monitor error rates
- [ ] Monitor performance metrics
- [ ] Monitor database performance
- [ ] Monitor disk space
- [ ] Review and rotate logs
- [ ] Apply security updates
- [ ] Backup verification

---

## ESTIMATED TIMELINE

| Phase | Duration | Dependencies |
|-------|----------|--------------|
| Phase 1: Foundation | 2 weeks | None |
| Phase 2: Core CRUD | 2 weeks | Phase 1 |
| Phase 3: Meeting Management | 2 weeks | Phase 2 |
| Phase 4: File Storage | 2 weeks | Phase 3 |
| Phase 5: Attendance & Compliance | 2 weeks | Phase 4 |
| Phase 6: Notes & Notifications | 2 weeks | Phase 5 |
| Phase 7: Reporting & Analytics | 2 weeks | Phase 5 |
| Phase 8: Testing & Optimization | 2 weeks | Phase 7 |
| Phase 9: Deployment & DevOps | 2 weeks | Phase 8 |
| Phase 10: Android Integration | 2 weeks | Phase 9 |

**Total Estimated Time:** 20 weeks (5 months)

**Critical Path (MVP):** 14 weeks (3.5 months)
- Phase 1, 2, 3, 4 (docs only), 5, 9, 10

---

## TEAM RECOMMENDATIONS

### Minimum Team
- 1 Backend Developer (Node.js/Python/PHP/Java)
- 1 Android Developer (Kotlin)
- 1 Database Administrator (part-time)
- 1 DevOps Engineer (part-time)
- 1 QA Tester

### Ideal Team
- 2 Backend Developers
- 1 Android Developer
- 1 Database Administrator
- 1 DevOps Engineer
- 1 QA Tester
- 1 UI/UX Designer (part-time)
- 1 Technical Writer (part-time)
- 1 Project Manager

---

## RISKS AND MITIGATION

| Risk | Impact | Likelihood | Mitigation |
|------|--------|------------|------------|
| Delayed backend development | High | Medium | Use established framework; follow phases strictly |
| File storage costs exceed budget | Medium | Low | Implement file size limits; use compression; choose cost-effective provider |
| Database performance issues | High | Medium | Proper indexing; query optimization; caching layer |
| Security vulnerabilities | High | Low | Regular security audits; dependency updates; follow security best practices |
| Android integration issues | High | Medium | Early testing; clear API documentation; versioning |
| Server downtime | Medium | Low | Use reliable hosting; implement monitoring; have rollback plan |
| Data loss | High | Very Low | Automated backups; test restore procedures regularly |

---

## SUCCESS CRITERIA

### Technical
- [ ] All critical endpoints implemented and tested
- [ ] 80%+ test coverage
- [ ] API response time < 500ms (95th percentile)
- [ ] Zero critical security vulnerabilities
- [ ] 99.5% uptime
- [ ] Successful Android integration

### Functional
- [ ] Admin can manage MRFCs, proponents, agendas
- [ ] Admin can upload documents and mark attendance
- [ ] Users can view assigned data and create notes
- [ ] Compliance dashboard shows accurate data
- [ ] Offline mode works correctly
- [ ] Reports generate successfully

### User Acceptance
- [ ] DENR/MGB staff successfully trained
- [ ] Users can complete common tasks without help
- [ ] Positive feedback from pilot users
- [ ] No critical bugs reported in first month

---

## NEXT STEPS

1. **Review this document** with project stakeholders
2. **Choose technology stack** (recommendation: Node.js + PostgreSQL)
3. **Assemble development team**
4. **Set up development environment** (Phase 1)
5. **Begin Phase 1: Foundation** (Weeks 1-2)
6. **Schedule weekly progress reviews**
7. **Create project board** (Jira, Trello, GitHub Projects)
8. **Define sprint schedule** (2-week sprints recommended)

---

## CONTACT FOR BACKEND DEVELOPMENT

**For questions, clarifications, or to begin development:**

**MGB IT Development Team**
Email: [Backend Team Email]
Phone: [Phone Number]

**Project Manager:** [Name]
**Lead Backend Developer:** [Name]
**Lead Android Developer:** [Name]

---

**Document prepared by:** MGB IT Development Team
**Date:** October 16, 2025
**Version:** 1.0

---

*This document is a living document and will be updated as development progresses and requirements evolve.*
