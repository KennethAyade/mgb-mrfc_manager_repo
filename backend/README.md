# MGB MRFC MANAGER - BACKEND API

Government MRFC (Municipal Resource and Finance Committee) compliance tracking system backend API.

## 📋 Table of Contents

- [Overview](#overview)
- [Tech Stack](#tech-stack)
- [Prerequisites](#prerequisites)
- [Installation](#installation)
- [Environment Setup](#environment-setup)
- [Database Setup](#database-setup)
- [Running the Server](#running-the-server)
- [API Documentation](#api-documentation)
- [Project Structure](#project-structure)
- [Implementation Guide](#implementation-guide)
- [Deployment](#deployment)
- [Contributing](#contributing)

---

## 🎯 Overview

This backend API powers the MGB MRFC Manager mobile application, providing:

- **Authentication & Authorization** (JWT-based with role-based access control)
- **MRFC Management** (Create, manage, and monitor MRFCs)
- **Compliance Tracking** (Auto-calculated compliance percentages)
- **Document Management** (Upload, store, and retrieve documents via Cloudinary)
- **Attendance Tracking** (Meeting attendance with photo documentation)
- **Audit Logging** (Complete audit trail of all system changes)
- **Real-time Notifications** (Push notifications for users)

---

## 🛠️ Tech Stack

| Technology | Purpose | Free Tier |
|------------|---------|-----------|
| **Node.js + Express** | Backend framework | ✅ Yes |
| **TypeScript** | Type-safe JavaScript | ✅ Yes |
| **PostgreSQL** | Relational database | ✅ Yes (Neon/Supabase) |
| **Sequelize** | ORM for database | ✅ Yes |
| **JWT** | Authentication tokens | ✅ Yes |
| **Bcrypt** | Password hashing | ✅ Yes |
| **Cloudinary** | File storage (25GB free) | ✅ Yes |
| **Joi** | Input validation | ✅ Yes |
| **Helmet** | Security headers | ✅ Yes |
| **Morgan** | HTTP logging | ✅ Yes |

---

## 📦 Prerequisites

Before you begin, ensure you have:

- **Node.js** (v18 or higher) - [Download](https://nodejs.org/)
- **npm** (v9 or higher) - Comes with Node.js
- **PostgreSQL database** - [Neon](https://neon.tech/) or [Supabase](https://supabase.com/)
- **Cloudinary account** - [Sign up](https://cloudinary.com/)
- **Git** - [Download](https://git-scm.com/)
- **Code editor** - VS Code recommended

---

## 🚀 Installation

### Step 1: Clone the repository

```bash
git clone https://github.com/your-org/mgb-mrfc-backend.git
cd mgb-mrfc-backend
```

### Step 2: Install dependencies

```bash
npm install
```

This will install all packages listed in `package.json`.

### Step 3: Verify installation

```bash
node --version  # Should be v18 or higher
npm --version   # Should be v9 or higher
```

---

## ⚙️ Environment Setup

### Step 1: Create `.env` file

Copy the example environment file:

```bash
cp .env.example .env
```

### Step 2: Configure environment variables

Open `.env` and fill in your actual values:

```env
# Server
NODE_ENV=development
PORT=3000

# Database (choose one option)
DATABASE_URL=postgresql://username:password@host:5432/database?sslmode=require

# JWT Secrets (generate new ones!)
JWT_SECRET=your-super-secret-jwt-key-change-this-in-production
JWT_REFRESH_SECRET=your-super-secret-refresh-key-change-this

# Cloudinary
CLOUDINARY_CLOUD_NAME=your-cloud-name
CLOUDINARY_API_KEY=your-api-key
CLOUDINARY_API_SECRET=your-api-secret

# Super Admin (first user created automatically)
SUPER_ADMIN_USERNAME=superadmin
SUPER_ADMIN_PASSWORD=Change@Me#2025
SUPER_ADMIN_EMAIL=admin@mgb.gov.ph
```

### Step 3: Generate JWT secrets

Run this command to generate secure random secrets:

```bash
node -e "console.log('JWT_SECRET=' + require('crypto').randomBytes(64).toString('hex'))"
node -e "console.log('JWT_REFRESH_SECRET=' + require('crypto').randomBytes(64).toString('hex'))"
```

Copy the output to your `.env` file.

---

## 🗄️ Database Setup

### Option 1: Neon (Recommended - 3GB Free)

1. Go to [https://neon.tech](https://neon.tech)
2. Sign up for free account
3. Create new project
4. Copy the connection string
5. Paste into `.env` as `DATABASE_URL`

### Option 2: Supabase (500MB Free)

1. Go to [https://supabase.com](https://supabase.com)
2. Sign up for free account
3. Create new project
4. Go to Settings > Database
5. Copy connection string (Pooler mode)
6. Paste into `.env` as `DATABASE_URL`

### Run Database Schema

After configuring database connection:

```bash
# Connect to your database using psql or Supabase SQL Editor
# Then run:
psql $DATABASE_URL < database/schema.sql
```

Or manually copy/paste the contents of `database/schema.sql` into your database SQL editor.

This will create:
- 14 tables (users, mrfcs, proponents, agendas, etc.)
- All relationships and foreign keys
- Indexes for performance
- Default quarters for 2025

---

## 🏃 Running the Server

### Development Mode (with auto-reload)

```bash
npm run dev
```

Server will start on `http://localhost:3000` and auto-reload on file changes.

### Production Mode

```bash
npm run build  # Compile TypeScript to JavaScript
npm start      # Run compiled code
```

### Expected Output

```
✅ Database connection established successfully
✅ Database initialized successfully

================================================
🚀 MGB MRFC MANAGER API SERVER
================================================
Environment: development
Server running on: http://localhost:3000
API Base URL: http://localhost:3000/api/v1
Health Check: http://localhost:3000/api/v1/health
================================================
```

### Test the Server

Open browser or use curl:

```bash
curl http://localhost:3000/api/v1/health
```

Expected response:

```json
{
  "success": true,
  "message": "MGB MRFC Manager API is running",
  "version": "1.0.0",
  "timestamp": "2025-10-18T10:30:00.000Z"
}
```

---

## 📚 API Documentation

### Base URL

```
Development: http://localhost:3000/api/v1
Production:  https://your-api.onrender.com/api/v1
```

### Authentication

All endpoints (except `/auth/*`) require JWT token in header:

```
Authorization: Bearer <your-jwt-token>
```

### Endpoints Overview

| Category | Endpoints | Authentication Required |
|----------|-----------|------------------------|
| **Authentication** | 5 endpoints | Some |
| **Users** | 6 endpoints | Yes (Admin) |
| **MRFCs** | 5 endpoints | Yes |
| **Proponents** | 5 endpoints | Yes |
| **Quarters** | 2 endpoints | Yes |
| **Agendas** | 5 endpoints | Yes |
| **Documents** | 6 endpoints | Yes |
| **Attendance** | 3 endpoints | Yes |
| **Compliance** | 4 endpoints | Yes |
| **Notes** | 4 endpoints | Yes |
| **Notifications** | 4 endpoints | Yes |
| **Audit Logs** | 1 endpoint | Yes (Admin) |
| **Statistics** | 2 endpoints | Yes |

### Full API Documentation

See [API_DOCUMENTATION.md](./API_DOCUMENTATION.md) for complete endpoint details.

Or run Swagger docs (when implemented):
```
http://localhost:3000/api-docs
```

---

## 📁 Project Structure

```
backend/
├── src/
│   ├── config/              # Configuration files
│   │   ├── database.ts      # Sequelize database config
│   │   └── cloudinary.ts    # Cloudinary file storage config
│   ├── middleware/          # Express middleware
│   │   ├── auth.ts          # JWT authentication & authorization
│   │   ├── auditLog.ts      # Audit logging middleware
│   │   └── errorHandler.ts  # Global error handler
│   ├── models/              # Sequelize models (to be created)
│   │   ├── User.ts
│   │   ├── Mrfc.ts
│   │   ├── Proponent.ts
│   │   └── ...
│   ├── routes/              # API route handlers
│   │   ├── index.ts         # Main router
│   │   ├── auth.routes.ts   # Authentication routes
│   │   ├── user.routes.ts   # User management routes
│   │   └── ...
│   ├── controllers/         # Business logic (to be created)
│   │   ├── auth.controller.ts
│   │   ├── mrfc.controller.ts
│   │   └── ...
│   ├── utils/               # Utility functions
│   │   ├── jwt.ts           # JWT utilities
│   │   ├── password.ts      # Password hashing
│   │   └── validation.ts    # Joi validation schemas
│   └── server.ts            # Express app entry point
├── database/
│   └── schema.sql           # PostgreSQL schema
├── .env.example             # Environment variables template
├── .gitignore              # Git ignore rules
├── package.json            # Dependencies
├── tsconfig.json           # TypeScript config
└── README.md               # This file
```

---

## 🔨 Implementation Guide

### Current Status

✅ **Completed:**
- Project structure setup
- TypeScript configuration
- Database schema (14 tables)
- Environment configuration
- Authentication middleware (JWT)
- Authorization middleware (role-based)
- Audit logging middleware
- Password utilities (bcrypt)
- Input validation (Joi schemas)
- Cloudinary configuration
- Error handling
- Security middleware (Helmet, CORS, rate limiting)

❌ **To Be Implemented:**

#### Phase 1: Models & Authentication (Day 1)
1. Create Sequelize models for all tables
2. Implement authentication endpoints:
   - POST /auth/register
   - POST /auth/login
   - POST /auth/refresh
   - POST /auth/logout
   - POST /auth/change-password

#### Phase 2: Core CRUD (Day 2)
3. Implement User management endpoints (6 endpoints)
4. Implement MRFC endpoints (5 endpoints)
5. Implement Proponent endpoints (5 endpoints)
6. Implement Quarter endpoints (2 endpoints)

#### Phase 3: Meeting Management (Day 3)
7. Implement Agenda endpoints (5 endpoints)
8. Implement Matters Arising endpoints (3 endpoints)
9. Implement Attendance endpoints (3 endpoints)

#### Phase 4: Documents & Compliance (Day 4)
10. Implement Document upload/download (6 endpoints)
11. Implement Compliance calculation logic
12. Implement Compliance dashboard (4 endpoints)

#### Phase 5: Finish & Deploy (Day 5)
13. Implement Notes endpoints (4 endpoints)
14. Implement Notifications endpoints (4 endpoints)
15. Implement Audit Log viewer (1 endpoint)
16. Implement Statistics endpoints (2 endpoints)
17. Setup Swagger documentation
18. Deploy to Render

### How to Implement Each Endpoint

Each route file (`auth.routes.ts`, etc.) contains:

1. **Detailed comments** explaining what the endpoint does
2. **Request/response examples** showing exact data format
3. **Step-by-step implementation instructions** as TODO comments
4. **Error handling guidance** for all failure cases
5. **Security notes** for critical operations

**Example workflow:**

1. Open `src/routes/auth.routes.ts`
2. Find `POST /auth/register` endpoint
3. Read the detailed comments
4. Uncomment the TODO code
5. Replace placeholders with actual database queries
6. Test with Postman/curl
7. Fix bugs and iterate

---

## 🌐 Deployment

### Deploy to Render (Free Tier)

1. Push code to GitHub
2. Go to [https://render.com](https://render.com)
3. Sign up and connect GitHub
4. Create New > Web Service
5. Select your repository
6. Configure:
   - **Name:** mgb-mrfc-api
   - **Environment:** Node
   - **Build Command:** `npm install && npm run build`
   - **Start Command:** `npm start`
   - **Instance Type:** Free
7. Add environment variables from `.env`
8. Click "Create Web Service"
9. Wait for deployment (5-10 minutes)
10. Test your API at `https://mgb-mrfc-api.onrender.com/api/v1/health`

### Important Notes for Render Free Tier

- ⚠️ **Server sleeps after 15 minutes of inactivity**
- ⚠️ **Takes ~30 seconds to wake up on first request**
- ✅ **Good for MVP, not production 24/7**
- 💡 **Use cron-job.org to ping every 10 minutes to keep alive**

---

## 🧪 Testing

### Manual Testing with Postman

1. Download [Postman](https://www.postman.com/)
2. Create new collection "MGB MRFC API"
3. Test endpoints one by one
4. Save requests for reuse

### Example: Test Login

```
POST http://localhost:3000/api/v1/auth/login
Content-Type: application/json

{
  "username": "superadmin",
  "password": "Change@Me#2025"
}
```

### Example: Test Protected Endpoint

```
GET http://localhost:3000/api/v1/mrfcs
Authorization: Bearer <token-from-login-response>
```

---

## 🤝 Contributing

### Development Workflow

1. Create feature branch: `git checkout -b feature/user-management`
2. Make changes and commit: `git commit -m "Implement user endpoints"`
3. Push branch: `git push origin feature/user-management`
4. Create Pull Request on GitHub
5. After review, merge to main

### Coding Standards

- Use TypeScript strict mode
- Follow ESLint rules
- Write comments for complex logic
- Always validate inputs with Joi
- Always use audit logging for CREATE/UPDATE/DELETE
- Never commit `.env` file

---

## 📞 Support

For questions or issues:

- **Email:** mgb-it@example.com
- **GitHub Issues:** [Create issue](https://github.com/your-org/mgb-mrfc-backend/issues)
- **Documentation:** [Wiki](https://github.com/your-org/mgb-mrfc-backend/wiki)

---

## 📄 License

This project is for internal use by DENR/MGB only. All rights reserved.

---

## 🎯 Quick Start Checklist

- [ ] Install Node.js v18+
- [ ] Clone repository
- [ ] Run `npm install`
- [ ] Create `.env` from `.env.example`
- [ ] Setup Neon/Supabase database
- [ ] Run `database/schema.sql`
- [ ] Setup Cloudinary account
- [ ] Configure `.env` with all credentials
- [ ] Run `npm run dev`
- [ ] Test `http://localhost:3000/api/v1/health`
- [ ] Start implementing endpoints from `auth.routes.ts`

---

**Built with ❤️ by MGB IT Development Team**
