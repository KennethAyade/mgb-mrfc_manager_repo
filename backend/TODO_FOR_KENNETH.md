# Backend Setup - TODO for Kenneth

## Overview
The backend infrastructure has been set up with all necessary files. You need to complete the integration and test the setup.

---

## ✅ Already Completed

- ✅ Project structure created
- ✅ TypeScript configuration (tsconfig.json)
- ✅ Package.json with all dependencies
- ✅ Database schema (database/schema.sql)
- ✅ Environment variables template (.env.example)
- ✅ All 14 Sequelize models created (src/models/)
- ✅ All 13 controllers created (src/controllers/)
- ✅ All 13 route files created with detailed documentation (src/routes/)
- ✅ Middleware (auth, auditLog) created
- ✅ Utilities (jwt, password, validation) created

---

## 🔧 Tasks to Complete

### Task 1: Update Route Files to Use Controllers
**Priority: HIGH**

Update all route files in `backend/src/routes/` to connect them with their controllers.

**Files to Update:**
1. `auth.routes.ts` → use `../controllers/auth.controller`
2. `user.routes.ts` → use `../controllers/user.controller`
3. `mrfc.routes.ts` → use `../controllers/mrfc.controller`
4. `proponent.routes.ts` → use `../controllers/proponent.controller`
5. `quarter.routes.ts` → use `../controllers/quarter.controller`
6. `agenda.routes.ts` → use `../controllers/agenda.controller`
7. `document.routes.ts` → use `../controllers/document.controller`
8. `attendance.routes.ts` → use `../controllers/attendance.controller`
9. `compliance.routes.ts` → use `../controllers/compliance.controller`
10. `note.routes.ts` → use `../controllers/note.controller`
11. `notification.routes.ts` → use `../controllers/notification.controller`
12. `auditLog.routes.ts` → use `../controllers/auditLog.controller`
13. `statistics.routes.ts` → use `../controllers/statistics.controller`

**How to do it:**

For each route file, follow this pattern:

```typescript
// Step 1: Add import at the top
import * as authController from '../controllers/auth.controller';

// Step 2: Replace the stub async function with controller call
// BEFORE:
router.post('/register', validate(registerSchema, 'body'), async (req: Request, res: Response) => {
  try {
    // TODO: IMPLEMENT...
    res.status(501).json({ /* stub response */ });
  } catch (error) {
    // error handling
  }
});

// AFTER:
router.post('/register', validate(registerSchema, 'body'), authController.register);
```

**Important:**
- Keep ALL the detailed JSDoc comments above each endpoint
- Keep ALL middleware (authenticate, authorize, validate)
- Keep ALL validation schemas
- Only replace the async function handler with the controller function

---

### Task 2: Install Backend Dependencies
**Priority: HIGH**

Navigate to the backend directory and install all packages.

```bash
cd backend
npm install
```

**Expected output:**
- All packages from package.json should install successfully
- Node modules folder created
- package-lock.json created

**If you get errors:**
- Make sure you have Node.js v18+ installed: `node --version`
- Make sure you have npm v9+ installed: `npm --version`
- Try clearing npm cache: `npm cache clean --force`

---

### Task 3: Create and Configure .env File
**Priority: HIGH**

Create your environment configuration file.

```bash
# In the backend directory
cp .env.example .env
```

**Edit the .env file and configure these critical variables:**

#### 1. Database Configuration
Choose one option:

**Option A: Neon (Recommended - 3GB free)**
1. Go to https://neon.tech
2. Create free account
3. Create new project
4. Copy connection string
5. Paste into .env:
```env
DATABASE_URL=postgresql://username:password@host:5432/database?sslmode=require
```

**Option B: Supabase (500MB free)**
1. Go to https://supabase.com
2. Create free account
3. Create new project
4. Go to Settings > Database
5. Copy connection string (Pooler mode)
6. Paste into .env

#### 2. JWT Secrets (IMPORTANT!)
Generate secure random secrets:

```bash
# Run these commands and copy the output to .env
node -e "console.log('JWT_SECRET=' + require('crypto').randomBytes(64).toString('hex'))"
node -e "console.log('JWT_REFRESH_SECRET=' + require('crypto').randomBytes(64).toString('hex'))"
```

#### 3. Cloudinary (For file uploads)
1. Go to https://cloudinary.com
2. Sign up for free account (25GB free)
3. Go to Dashboard > API Keys
4. Copy and paste to .env:
```env
CLOUDINARY_CLOUD_NAME=your-cloud-name
CLOUDINARY_API_KEY=your-api-key
CLOUDINARY_API_SECRET=your-api-secret
```

#### 4. Other Variables
Update these if needed:
```env
PORT=3000
NODE_ENV=development
SUPER_ADMIN_USERNAME=superadmin
SUPER_ADMIN_PASSWORD=Change@Me#2025
SUPER_ADMIN_EMAIL=admin@mgb.gov.ph
```

---

### Task 4: Setup Database Schema
**Priority: HIGH**

After configuring database connection, run the schema to create all tables.

**For Neon:**
```bash
# Connect using psql (if installed)
psql $DATABASE_URL < database/schema.sql
```

**For Supabase:**
1. Go to your Supabase project
2. Click "SQL Editor"
3. Open `database/schema.sql` file
4. Copy all contents
5. Paste into Supabase SQL Editor
6. Click "Run"

**This will create:**
- 14 tables (users, mrfcs, proponents, agendas, etc.)
- All relationships and foreign keys
- All indexes for performance
- Default quarters for 2025
- All ENUM types

---

### Task 5: Test Backend Server Startup
**Priority: HIGH**

Start the development server and verify everything works.

```bash
cd backend
npm run dev
```

**Expected output:**
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

**Test the server:**
Open browser or use curl:
```bash
curl http://localhost:3000/api/v1/health
```

**Expected response:**
```json
{
  "success": true,
  "message": "MGB MRFC Manager API is running",
  "version": "1.0.0",
  "timestamp": "2025-10-19T..."
}
```

---

## 🐛 Troubleshooting

### Error: Cannot find module 'xyz'
**Solution:** Run `npm install` again

### Error: Database connection failed
**Solution:**
- Check your DATABASE_URL in .env
- Make sure database is running
- Verify credentials are correct
- Check if your IP is allowed (for Neon/Supabase)

### Error: Port 3000 already in use
**Solution:**
- Stop other services on port 3000
- Or change PORT in .env to 3001 or another port

### Error: JWT_SECRET is not defined
**Solution:**
- Make sure you generated JWT secrets (see Task 3)
- Verify .env file exists and is in backend/ directory
- Restart the server after updating .env

### TypeScript compilation errors
**Solution:**
- Run `npm run build` to see detailed errors
- Check if all imports are correct
- Verify tsconfig.json exists

---

## 📚 Additional Resources

### API Documentation
- See `backend/README.md` for detailed API documentation
- Each route file has extensive JSDoc comments
- Each controller has implementation details

### Testing Endpoints
Use Postman or curl to test:

1. **Register user:**
```bash
curl -X POST http://localhost:3000/api/v1/auth/register \
  -H "Content-Type: application/json" \
  -d '{"username":"testuser","password":"Test123!","full_name":"Test User","email":"test@example.com"}'
```

2. **Login:**
```bash
curl -X POST http://localhost:3000/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"superadmin","password":"Change@Me#2025"}'
```

3. **Protected endpoint (use token from login):**
```bash
curl http://localhost:3000/api/v1/mrfcs \
  -H "Authorization: Bearer YOUR_TOKEN_HERE"
```

---

## 📁 Project Structure Reference

```
backend/
├── src/
│   ├── config/
│   │   ├── database.ts          ✅ Database connection
│   │   └── cloudinary.ts        ✅ File storage config
│   ├── middleware/
│   │   ├── auth.ts              ✅ JWT authentication
│   │   └── auditLog.ts          ✅ Audit logging
│   ├── models/                  ✅ All 14 models created
│   │   ├── User.ts
│   │   ├── Mrfc.ts
│   │   ├── Proponent.ts
│   │   └── ... (11 more)
│   ├── controllers/             ✅ All 13 controllers created
│   │   ├── auth.controller.ts
│   │   ├── user.controller.ts
│   │   └── ... (11 more)
│   ├── routes/                  ⚠️ Need to connect to controllers
│   │   ├── auth.routes.ts
│   │   ├── user.routes.ts
│   │   └── ... (11 more)
│   ├── utils/
│   │   ├── jwt.ts               ✅ JWT utilities
│   │   ├── password.ts          ✅ Password hashing
│   │   └── validation.ts        ✅ Joi schemas
│   └── server.ts                ✅ Main entry point
├── database/
│   └── schema.sql               ✅ PostgreSQL schema
├── .env.example                 ✅ Environment template
├── package.json                 ✅ Dependencies defined
├── tsconfig.json               ✅ TypeScript config
└── README.md                    ✅ Documentation
```

---

## ✅ Completion Checklist

- [ ] Task 1: Updated all 13 route files to use controllers
- [ ] Task 2: Ran `npm install` successfully
- [ ] Task 3: Created .env file with all credentials
- [ ] Task 4: Ran database schema (all tables created)
- [ ] Task 5: Server starts without errors
- [ ] Tested health check endpoint
- [ ] Tested registration endpoint
- [ ] Tested login endpoint
- [ ] Reviewed README.md for additional info

---

## 🎯 Next Steps After Completion

Once all tasks are done:

1. **Test all endpoints** with Postman
2. **Review API documentation** in README.md
3. **Connect mobile app** to the backend
4. **Deploy to Render** (see README.md deployment section)
5. **Monitor logs** for any issues

---

## 📞 Need Help?

If you encounter issues:
1. Check the error message carefully
2. Review the troubleshooting section above
3. Check backend/README.md for more details
4. Review the JSDoc comments in route/controller files
5. Check the database schema in database/schema.sql

---

**Good luck! The backend is 90% complete - you just need to wire everything together! 🚀**
