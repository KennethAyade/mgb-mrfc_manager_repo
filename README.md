# MGB MRFC Manager - Production-Ready System

**Mines and Geosciences Bureau - Multi-Partite Monitoring Team Management System**

A comprehensive tablet-based management system for MGB Region 7 to manage Multi-Partite Monitoring Teams (MRFCs), proponents, meetings, compliance monitoring, and documentation. Built with enterprise-grade architecture and AI-powered compliance analysis.

**Version:** 2.0.36 | **Status:** 🚀 Production Live (Railway) | **Last Updated:** December 11, 2025

---

## 🎯 Project Overview

**Target Platform:** Android Tablet (10-inch, landscape-optimized)
**Purpose:** Streamline MRFC operations, automate compliance analysis, and enable offline meeting management
**AI Engine:** Anthropic Claude Haiku 4.5 (optional)
**Deployment:** Railway (Backend) + APK Distribution (Android)

### Project Scale
- **Backend**: 19 models, 15 controllers, 18 route files (~15,000 lines of TypeScript)
- **Android**: 47+ Activities, 28 ViewModels, 18 Repositories (~25,000 lines of Kotlin)
- **Database**: 14 tables, 40+ indexes, 7 triggers
- **Features**: 20+ major features across authentication, management, compliance, offline support
- **Documentation**: 2,100+ lines in PROJECT_STATUS.md, 20+ supplementary docs

### Design Philosophy
- **Primary Color**: DENR Green (#388E3C)
- **Design Pattern**: Material Design 3 with card-based layouts
- **Layout Style**: Tablet-optimized landscape interface
- **Navigation**: Hybrid drawer menu + floating home button
- **Architecture**: Enterprise-grade MVVM with offline-first approach

---

## ✨ Key Features (v2.0.35)

### 🔐 Authentication & Authorization
- JWT token-based authentication
- Role-based access control (Super Admin, Admin, User)
- Encrypted token storage with DataStore
- Session expiration handling

### 👥 User & MRFC Management
- Complete user CRUD with role assignment
- MRFC management with compliance tracking
- Proponent management with status monitoring
- User-to-MRFC access control

### 📅 Meeting Management
- Quarter-based meeting organization (Q1-Q4)
- Meeting scheduling with start/end times
- **Agenda item management** with highlighting
- **Other Matters tab** for post-agenda items
- Meeting minutes and matters arising tracking
- Real-time meeting status updates

### 📄 Document Management
- AWS S3 storage (up to 100MB per file)
- Document categories: MTF, AEPEP, CMVR, SDMP, Research, Production, Safety
- PDF viewer integration
- Quarter-based filtering
- Document review workflow

### 🤖 AI-Powered Compliance Analysis
- **Auto-trigger analysis** when viewing CMVR documents
- **Anthropic Claude Haiku 4.5** for intelligent compliance detection
- **Vision API** for scanned PDFs (30-60 seconds)
- **Real OCR** with Tesseract.js + pdfjs-dist (2-3 minutes for scanned docs)
- **Digital PDF support** with instant text extraction (< 1 second)
- Compliance percentage calculation (0-100%)
- Section-wise breakdown (ECC, EPEP, Water/Air Quality)
- Non-compliant items with severity levels
- Admin adjustment capability
- "Pending Manual Review" for failed analyses

### ✅ Attendance Tracking
- **Attendance type**: ONSITE/ONLINE support
- Photo capture with camera integration
- S3 photo upload
- Tablet-based workflow (one-time attendance per user)
- **User-selectable tablet number (1-15)** with backend validation
- **Full attendance editing**: name, position, department, type, tablet number
- Edit dialog with dropdowns for attendance type and tablet number
- Summary statistics (present/absent/rate)

### 📝 Notes & Voice Recording
- Personal notes with pinning capability
- Meeting-linked notes with **backend validation**
- **Voice recording** with description support
- Audio playback with authentication
- Duration and file size tracking
- **WakeLock** for stable recording (no sleep interruption)
- **Activity-scoped ViewModel** for persistent recording state across tab switches

### 📬 Notifications
- In-app notification center
- Read/unread status tracking
- Notification types: Meeting, Compliance, Alert, General
- Delete and mark as read functionality

### 🔄 Offline Support
- **Room database** with 5 entity types (Meeting, AgendaItem, Note, CachedFile, PendingSync)
- **SyncWorker** for background synchronization with WorkManager
- **NetworkConnectivityManager** for real-time connectivity monitoring
- **FileCacheManager** for intelligent file caching
- **OfflineIndicator** UI component
- Offline-first notes with sync status tracking

### 📊 Dashboard & Reports
- Admin dashboard with real-time statistics
- Compliance dashboard with aggregated data
- Donut charts for compliance distribution
- Quick action shortcuts

### 🔍 Audit & Security
- Comprehensive audit logging (CREATE, UPDATE, DELETE)
- Rate limiting and CORS protection
- Helmet.js security headers
- Input validation with Joi
- bcrypt password hashing (10 rounds)

---

## 📱 How to Run the Application

### Prerequisites
- **Android Studio** (latest version)
- **JDK 11 or higher**
- **Node.js 18+** and npm
- **PostgreSQL** database
- **AWS S3** bucket (configured)

### Backend Setup

1. **Install Dependencies**
   ```bash
   cd backend
   npm install
   ```

2. **Configure Environment**
   Create `backend/.env` from the template:
   ```env
   # Database (PostgreSQL)
   DATABASE_URL=postgresql://user:password@localhost:5432/mrfc_db
   # OR for Neon/Supabase
   # DATABASE_URL=postgresql://user:password@host.neon.tech:5432/dbname?sslmode=require

   # JWT Authentication
   JWT_SECRET=your-secret-key-here-min-32-chars
   JWT_EXPIRES_IN=24h
   JWT_REFRESH_EXPIRES_IN=7d

   # AWS S3 (Required for file storage)
   S3_BUCKET_NAME=adhub-s3-demo
   AWS_ACCESS_KEY_ID=AKIA...
   AWS_SECRET_ACCESS_KEY=...
   AWS_REGION=us-east-1

   # Anthropic Claude AI (Optional - for intelligent compliance analysis)
   # If not provided, falls back to keyword-based analysis
   ANTHROPIC_API_KEY=sk-ant-...

   # Server
   PORT=3000
   NODE_ENV=development

   # Security
   BCRYPT_ROUNDS=10
   ALLOWED_ORIGINS=http://localhost:3000,http://10.0.2.2:3000
   ```

3. **Setup Database**
   ```bash
   npm run db:migrate
   npm run db:seed
   ```

4. **Start Backend**
   ```bash
   npm run dev
   ```
   Server runs on `http://localhost:3000`

### Android App Setup

1. **Open Project in Android Studio**
   - Open the root directory in Android Studio
   - Wait for Gradle sync to complete

2. **Configure API Base URL**
   For emulator, the backend URL is already set to `http://10.0.2.2:3000`

3. **Run the App**
   - Connect Android device or start emulator (API 25+)
   - Click Run button (▶️)
   - App will install and launch

4. **Login**
   - **Admin**: `superadmin` / `Admin@123`
   - **User**: Create via admin panel

---

## 🗂️ Project Structure

```
mgb-mrfc_manager_repo/
├── backend/                    # Node.js Backend
│   ├── src/
│   │   ├── config/
│   │   │   ├── database.ts    # PostgreSQL config
│   │   │   └── s3.ts          # AWS S3 config
│   │   ├── controllers/       # API logic
│   │   │   ├── document.controller.ts
│   │   │   ├── complianceAnalysis.controller.ts
│   │   │   └── ...
│   │   ├── models/            # Database models
│   │   ├── routes/            # API routes
│   │   ├── middleware/        # Auth, upload, etc.
│   │   └── server.ts          # Entry point
│   ├── database/
│   │   └── schema.sql         # Database schema
│   └── package.json
│
└── app/                        # Android Application
    ├── src/main/
    │   ├── java/com/mgb/mrfcmanager/
    │   │   ├── ui/            # Activities & UI
    │   │   │   ├── auth/      # Login, Splash
    │   │   │   ├── admin/     # Admin screens
    │   │   │   └── user/      # User screens
    │   │   ├── data/
    │   │   │   ├── remote/    # API services, DTOs
    │   │   │   └── repository/# Data repositories
    │   │   └── viewmodel/     # ViewModels
    │   └── res/               # Resources
    └── build.gradle.kts
```

---

## 🎯 Key Features

### Document Management
- Upload documents (PDF, DOC, XLS) up to 100MB
- Automatic categorization (MTF, AEPEP, CMVR, etc.)
- Download and view documents
- Delete with audit logging
- AWS S3 storage integration

### CMVR Compliance Analysis
- **Automatic Analysis**: Triggers when viewing document
- **Real OCR**: Extracts text from scanned PDFs using Tesseract.js
- **Digital PDF Support**: Fast text extraction for searchable PDFs
- **Compliance Calculation**: 
  - Searches for keywords: "complied", "not complied", "satisfied", etc.
  - Calculates percentages per section
  - Determines rating (Fully/Partially/Non-Compliant)
- **Section Breakdown**: ECC Compliance, EPEP, Water Quality, Air Quality, Noise, Waste
- **Non-Compliant Items**: Lists specific issues with severity
- **Progress Tracking**: Real-time updates during OCR
- **Error Handling**: Failed analyses marked as "Pending Manual Review"
- **Caching**: Analyzed documents return results instantly

### MRFC Management
- Create, read, update, delete MRFCs
- Track compliance status
- Assign administrators
- View associated proponents

### Proponent Management
- Full CRUD operations
- Link to MRFCs
- Track permit information
- Contact management

### Agenda Management
- Create meeting agendas
- Standard agenda items
- Matters arising tracking
- Quarter-based organization

---

## 🔧 Technical Stack

### Backend
- **Runtime**: Node.js 18+
- **Language**: TypeScript
- **Framework**: Express.js
- **Database**: PostgreSQL 14+ with Sequelize ORM
- **File Storage**: AWS S3 (100MB limit per file)
- **AI Engine**: Anthropic Claude Haiku 4.5 (optional, `@anthropic-ai/sdk`)
- **OCR Libraries**:
  - pdfjs-dist 4.9.155 (PDF rendering, secure)
  - Tesseract.js (OCR text extraction)
  - canvas (image processing)
  - pdf.js-extract (quick text extraction)
- **Authentication**: JWT tokens (jsonwebtoken)
- **Validation**: Joi
- **Security**: Helmet, CORS, express-rate-limit
- **Testing**: Jest + Supertest
- **API Documentation**: Swagger (swagger-ui-express)

### Android Frontend
- **Language**: Kotlin with coroutines
- **Min SDK**: API 25 (Android 7.1)
- **Target SDK**: API 36 (Android 14+)
- **Architecture**: MVVM (Model-View-ViewModel)
- **Networking**: Retrofit 2 + OkHttp (5-minute timeouts for OCR)
- **JSON Parsing**: Moshi with Kotlin reflection
- **Async**: Kotlin Coroutines + LiveData
- **UI**: Material Design 3
- **Image Loading**: Coil 2.5.0 (with S3 signed URLs)
- **Local Storage**:
  - DataStore (encrypted token management)
  - Room Database (offline support)
- **Background Work**: WorkManager for sync
- **Media**: CameraX for photo capture
- **Charts**: MPAndroidChart v3.1.0

### Database
- **PostgreSQL 14+** with Sequelize ORM
- **14 Core Tables**: users, mrfcs, proponents, quarters, agendas, agenda_items, attendance, documents, voice_recordings, notes, notifications, user_mrfc_access, compliance_logs, audit_logs
- **40+ Performance Indexes**
- **7 Auto-update Triggers**
- **Comprehensive Foreign Keys** with CASCADE delete

### Cloud Services
- **AWS S3**: File storage (documents, photos)
- **Anthropic Claude**: Intelligent compliance analysis (optional, Haiku 4.5)
- **Railway**: Backend hosting and deployment

---

## 🚀 Recent Updates

### December 11, 2025 (v2.0.36) - Attendance & Recording Enhancements
**Key Improvements:**

1. **Tablet Number Feature - Complete** ✅
   - User-selectable tablet number (1-15) for attendance records
   - Backend: Migration `017_add_tablet_number_to_attendance.sql` with validation
   - Frontend: Dropdown in AttendanceActivity for new records
   - **Edit Dialog**: Full CRUD capability for existing attendance records
   - Edit name, position, department, attendance type, and tablet number
   - UpdateAttendanceRequest supports all fields

2. **Voice Recording State Persistence** ✅
   - Changed VoiceRecordingViewModel to Activity scope
   - Recording state now persists when switching tabs
   - No UI changes, just improved stability

3. **Notes Backend Validation** ✅
   - Added validation for title, mrfc_id, and quarter_id
   - Better error messages for invalid data
   - Enhanced note controller with comprehensive checks

4. **Offline Support Integration** ✅
   - Fixed SplashActivity async auth with TokenManager.ensureInitialized()
   - Prevents users from being logged out on app restart
   - FileCacheManager properly integrated

### December 10, 2025 (v2.0.35) - Major Feature Release
**9 New Features Implemented:**

1. **Other Matters Tab** ✅
   - Separate tab for post-agenda discussion items
   - `is_other_matter` field added to AgendaItem model
   - New endpoints for marking and filtering other matters
   - OtherMattersFragment for dedicated UI

2. **Attendance Type (ONSITE/ONLINE)** ✅
   - Dropdown to select attendance type per attendee
   - `attendance_type` enum field (ONSITE, ONLINE)
   - Supports mobile app users attending remotely
   - Migration: `015_add_attendance_type_to_attendance.sql`

3. **Attendance Edit Capability** ✅
   - Users can edit their own attendance records
   - Admins can edit any attendance record
   - Edit dialog in AttendanceActivity

4. **Agenda Highlighting Feature** ✅
   - Admin can mark agenda items as "discussed" with green highlight
   - `is_highlighted`, `highlighted_by`, `highlighted_at` fields added
   - Toggle endpoint: `PUT /agenda-items/:id/toggle-highlight`
   - Green background display for highlighted items in UI

5. **Audio Recording Standby Fix** ✅
   - WakeLock implementation for stable audio recording
   - Prevents device sleep during recording sessions
   - AudioRecorderHelper enhanced with WakeLock management

6. **Comprehensive Offline Support** ✅
   - Room database with 5 entity types (Meeting, AgendaItem, Note, CachedFile, PendingSync)
   - DAOs for all entities with comprehensive query methods
   - OfflineNotesRepository with offline-first architecture
   - SyncWorker for background sync using WorkManager
   - NetworkConnectivityManager for real-time connectivity monitoring
   - FileCacheManager for intelligent file caching
   - OfflineIndicator UI component showing connection status

7. **Fixed MRFC ID Error in Notes** ✅
   - Fixed validation error when creating notes
   - NotesActivity updated with proper MRFC ID handling

8. **File Redirect/Sort Behavior** ✅
   - Improved file organization and sorting in meeting views

9. **Database Migrations** ✅
   - `015_add_attendance_type_to_attendance.sql`
   - `016_add_other_matter_and_highlight_to_agenda_items.sql`

### November 2025 (v2.0.0-2.0.8) - Foundation Updates

1. **Claude AI Migration (v2.0.8)** ✅
   - Migrated from Google Gemini to Anthropic Claude Haiku 4.5
   - Native JSON mode for reliable response parsing
   - Vision API for direct scanned PDF analysis
   - Zero frontend changes required
   - All features preserved

2. **AWS S3 Migration (v2.0.0)** ✅
   - Migrated from Cloudinary to AWS S3
   - Increased file size limit to 100MB (from 10MB)
   - Better cost efficiency (~4x cheaper storage)
   - All file operations now use S3

3. **Auto Compliance Analysis** ✅
   - Viewing CMVR document automatically triggers analysis
   - No manual "Analyze" button needed
   - Seamless user experience

4. **Real OCR Implementation** ✅
   - Replaced pdf2pic with pdfjs-dist + canvas
   - Works cross-platform (Windows, Mac, Linux)
   - No external dependencies (GraphicsMagick/ImageMagick)
   - Handles scanned PDFs properly

5. **Removed Hardcoded Data** ✅
   - Deleted DemoData.kt
   - 100% backend-integrated
   - All data from real database

---

## 📊 Compliance Analysis Workflow

### For Digital PDFs (with selectable text):
```
1. User views CMVR document
2. Backend checks for existing analysis
3. If none exists, downloads PDF from S3
4. Extracts text directly (< 1 second)
5. Analyzes compliance keywords
6. Saves results to database
7. Returns to Android app
8. Displays compliance percentage
```

### For Scanned PDFs (images only):
```
1. User views CMVR document
2. Backend checks for existing analysis
3. If none exists, downloads PDF from S3
4. Detects no selectable text
5. Loads PDF with pdfjs-dist
6. Renders each page to canvas (2-3 minutes for 25 pages)
7. Runs Tesseract OCR on each page
8. Extracts text from all pages
9. Analyzes compliance keywords
10. Saves results to database
11. Returns to Android app
12. Displays compliance percentage
```

### If Analysis Fails:
```
1. Backend attempts analysis
2. OCR fails (quality too low, corrupted, etc.)
3. Saves analysis with status='FAILED'
4. Returns failed analysis to app
5. Android shows "Pending Manual Review"
6. Admin can manually adjust later
```

---

## 🏗️ System Architecture

```
┌─────────────────────────────────────────────────────────────┐
│                     Android Tablet App                      │
│  ┌──────────┐  ┌──────────┐  ┌──────────┐  ┌──────────┐  │
│  │  UI      │  │ViewModel │  │Repository│  │  Room    │  │
│  │Activities│←→│ LiveData │←→│  API     │←→│ Database │  │
│  └──────────┘  └──────────┘  └──────────┘  └──────────┘  │
└─────────────────────────┬───────────────────────────────────┘
                          │ HTTPS/REST API
                          ↓
┌─────────────────────────────────────────────────────────────┐
│                   Backend API (Railway)                      │
│  ┌──────────┐  ┌──────────┐  ┌──────────┐  ┌──────────┐  │
│  │  Routes  │→│Controller│→│  Models  │→│PostgreSQL│  │
│  │  + Auth  │  │  Logic   │  │Sequelize │  │ Database │  │
│  └──────────┘  └──────────┘  └──────────┘  └──────────┘  │
└─────────────────────────┬───────────┬───────────────────────┘
                          │           │
            ┌─────────────┘           └─────────────┐
            ↓                                       ↓
    ┌──────────────┐                       ┌──────────────┐
    │   AWS S3     │                       │ Claude API   │
    │ File Storage │                       │  (Haiku 4.5) │
    │ 100MB files  │                       │ AI Analysis  │
    └──────────────┘                       └──────────────┘
```

**Data Flow:**
1. User interacts with Android UI (Activities)
2. ViewModel manages state and business logic
3. Repository handles data operations (API + Room)
4. API calls go through Retrofit with JWT auth
5. Backend validates, processes, and stores in PostgreSQL
6. Files stored in AWS S3, AI analysis via Claude API
7. Offline data cached in Room, synced via WorkManager

---

## 🐛 Known Issues & Limitations

### Current Limitations
1. **OCR Performance**: Scanned PDFs take 2-3 minutes for 25 pages (acceptable for quality OCR)
2. **File Size**: Limited to 100MB per file (sufficient for most documents)
3. **Offline Editing**: Currently limited to notes; full offline editing planned
4. **Push Notifications**: Firebase integration pending (in-app notifications work)
5. **Reports Generation**: Attendance and compliance reports pending
6. **Data Export**: CSV/Excel export not yet implemented

### Recently Resolved Issues (v2.0.0-2.0.35)
- ✅ **Railway deployment crash loop** - Fixed idempotent migrations (v2.0.6)
- ✅ **Android UI consistency** - Fixed toolbar positioning, FAB, system insets (v2.0.7)
- ✅ **Gemini to Claude migration** - Seamless AI provider switch (v2.0.8)
- ✅ **JSON parsing errors** - Fixed ApiResponse wrapper handling
- ✅ **Infinite polling loops** - Progress polling stops correctly
- ✅ **Hardcoded demo data** - Removed completely, 100% backend data
- ✅ **Cloudinary 401 errors** - Migrated to AWS S3
- ✅ **OCR EPIPE errors on Windows** - Fixed with pdfjs-dist
- ✅ **PDF viewer back navigation** - Migrated to OnBackPressedDispatcher

---

## 📚 API Documentation

### Base URL
- Development: `http://localhost:3000/api/v1`
- Production: `https://your-domain.com/api/v1`

### Key Endpoints

#### Authentication
- `POST /auth/login` - User login
- `POST /auth/register` - User registration (admin only)

#### Documents
- `GET /documents` - List documents
- `POST /documents/upload` - Upload document
- `GET /documents/:id` - Get document metadata
- `GET /documents/:id/download` - Download document
- `DELETE /documents/:id` - Delete document

#### Compliance Analysis
- `GET /compliance/document/:documentId` - Get analysis (auto-triggers if not exists)
- `POST /compliance/analyze` - Manually trigger analysis
- `PUT /compliance/document/:documentId` - Update analysis (admin adjustments)
- `GET /compliance/progress/:documentId` - Get real-time OCR progress

#### MRFCs
- `GET /mrfcs` - List MRFCs
- `POST /mrfcs` - Create MRFC
- `PUT /mrfcs/:id` - Update MRFC
- `DELETE /mrfcs/:id` - Delete MRFC

#### Proponents
- `GET /proponents` - List proponents
- `POST /proponents` - Create proponent
- `PUT /proponents/:id` - Update proponent
- `DELETE /proponents/:id` - Delete proponent

---

## 💡 Tips for Developers

1. **Backend Development**: Check `backend/src/controllers/` for API logic
2. **Android Development**: Check `app/src/main/java/com/mgb/mrfcmanager/ui/` for screens
3. **Database Schema**: See `backend/database/schema.sql`
4. **API Testing**: Use Postman or `backend/test-api.sh`
5. **Logs**: Backend logs show detailed OCR progress

---

## 📞 Test Credentials

### Admin Access
- **Username**: `superadmin`
- **Password**: `Admin@123`
- **Features**: Full system access, compliance analysis, document management

### Creating Users
- Login as admin
- Navigate to User Management
- Create new users with specific roles

---

## 🚀 Deployment

### Backend (Railway)
**Production URL**: Railway deployment (auto-deploy from main branch)

```bash
# Local build and test
cd backend
npm run build
npm start

# Railway deployment
# Push to main branch triggers auto-deployment
# Environment variables configured in Railway dashboard
```

**Required Environment Variables** (set in Railway):
- `DATABASE_URL` - PostgreSQL connection string
- `JWT_SECRET` - JWT signing secret
- `S3_BUCKET_NAME`, `AWS_ACCESS_KEY_ID`, `AWS_SECRET_ACCESS_KEY`, `AWS_REGION` - AWS S3 credentials
- `ANTHROPIC_API_KEY` - Claude AI API key (optional)
- `PORT` - Server port (default: 3000)
- `NODE_ENV` - Environment (production)

### Android (APK Distribution)
1. Build → Generate Signed Bundle/APK
2. Select APK
3. Create/select keystore
4. Build release APK
5. Distribute APK to tablets

**Production Build:**
```bash
# In Android Studio
Build > Generate Signed Bundle/APK > APK
Select release variant
Sign with production keystore
```

---

## 📄 License

This project is developed for MGB-DENR Philippines.

---

## 🎉 Current Status (v2.0.36)

**Deployment**: 🚀 Production Live on Railway
**Backend**: ✅ 19 models, 15 controllers, 18 routes - Production-ready
**Android App**: ✅ 47+ Activities, 28 ViewModels - Fully integrated MVVM
**File Storage**: ✅ AWS S3 (100MB per file)
**AI Analysis**: ✅ Claude Haiku 4.5 + OCR (Tesseract.js + pdfjs-dist)
**Database**: ✅ PostgreSQL with 14 tables, 40+ indexes, 17 migrations
**Offline Support**: ✅ Room database with WorkManager sync
**Demo Data**: ✅ Removed (100% real backend data)
**Attendance**: ✅ Full CRUD with tablet number selection (1-15)
**Recording**: ✅ Persistent state across tab navigation

**The application is production-ready and actively deployed!**

---

## 🗺️ Features Roadmap

### High Priority (Next Phase)
- [ ] **Attendance Reports** - Generate PDF/Excel attendance reports
- [ ] **Compliance Reports** - Automated compliance summary reports
- [ ] **Push Notifications** - Firebase Cloud Messaging integration
- [ ] **Full Offline Editing** - Extend offline support to all entities
- [ ] **Data Export** - CSV/Excel export for all major entities

### Medium Priority (Future)
- [ ] **Advanced Search & Filters** - Multi-field search with date ranges
- [ ] **Photo Upload for Proponents** - Company logos and profile photos
- [ ] **Meeting Recording Transcription** - Auto-transcribe voice recordings
- [ ] **Analytics Dashboard** - Advanced charts and trend analysis
- [ ] **Email Notifications** - Automated email alerts for meetings

### Low Priority (Nice to Have)
- [ ] **Mobile Phone Support** - Responsive layout for phones (currently tablet-only)
- [ ] **Dark Mode** - System-wide dark theme support
- [ ] **Multi-language** - English + Filipino language support
- [ ] **Calendar Integration** - Sync meetings with device calendar
- [ ] **Biometric Authentication** - Fingerprint/face unlock

---

## 📖 Additional Documentation

### Feature Documentation
- `PROJECT_STATUS.md` - **Complete project status tracker** (2,100+ lines, verified accurate)
- `CHANGELOG_NOV_2025.md` - Complete changelog for November updates
- `WHATS_NEW_SUMMARY.md` - Quick summary of recent changes

### AI & Compliance
- `CHATGPT_SETUP_GUIDE.md` - Claude AI setup guide (v2.0.8)
- `CHATGPT_MIGRATION_SUMMARY.md` - Gemini to Claude migration details
- `GEMINI_AI_INTEGRATION.md` - Legacy: Original Gemini implementation (preserved)
- `AUTO_ANALYSIS_IMPLEMENTATION_COMPLETE.md` - Auto-trigger compliance analysis
- `REAL_ANALYSIS_TESTING_COMPLETE.md` - OCR testing and verification guide

### AWS S3 Integration
- `S3_MIGRATION_COMPLETE.md` - Complete AWS S3 migration guide
- `S3_BUCKET_SETUP_GUIDE.md` - S3 bucket configuration steps
- `S3_MIGRATION_ALL_COMPONENTS.md` - Technical migration details

### Bug Fixes & Improvements
- `ANDROID_JSON_PARSING_FIX.md` - JSON parsing fixes
- `ANDROID_INFINITE_POLLING_FIX.md` - Polling loop fixes
- `RAILWAY_MIGRATION_FIX.md` - Railway deployment troubleshooting
- `RAILWAY_FIX_SUMMARY.md` - Quick Railway fix reference
- `HARDCODED_DATA_REMOVED.md` - Demo data removal documentation

### Deployment Guides
- `RAILWAY_DEPLOYMENT_GUIDE.md` - Complete Railway deployment guide
- `RAILWAY_QUICK_START.md` - Quick start for Railway
- `RAILWAY_DEPLOYMENT_CHECKLIST.md` - Pre-deployment checklist
- `RAILWAY_ENV_TEMPLATE.txt` - Environment variable template

---

## 🆘 Troubleshooting

### Backend Issues

**Backend won't start**
```bash
# Check if PostgreSQL is running
psql --version
# Verify .env has all required variables
cat backend/.env
# Check node version (must be 18+)
node --version
# Clear node_modules and reinstall
rm -rf node_modules package-lock.json && npm install
```

**Database connection fails**
- Verify `DATABASE_URL` format: `postgresql://user:password@host:5432/dbname`
- For SSL connections (Neon/Supabase), add `?sslmode=require`
- Check PostgreSQL service is running: `sudo service postgresql status`
- Test connection: `psql $DATABASE_URL`

**S3 upload fails**
- Verify AWS credentials in `.env` are correct
- Check S3 bucket permissions (public read, bucket policy configured)
- Test with: `aws s3 ls s3://your-bucket-name`
- Verify bucket region matches `AWS_REGION` in `.env`

### Android Issues

**App can't connect to backend**
- **Emulator**: URL should be `http://10.0.2.2:3000` (not localhost)
- **Physical device**: Use computer's local IP (e.g., `http://192.168.1.100:3000`)
- Check backend is running: `curl http://localhost:3000/api/v1/health`
- Verify firewall allows port 3000
- Check Android Network Security Config allows cleartext HTTP for development

**Authentication fails**
- Clear app data: Settings > Apps > MRFC Manager > Storage > Clear Data
- Check JWT_SECRET matches between app sessions
- Verify backend returns valid token format in `/auth/login` response
- Check token expiration (24h default)

**File upload/download fails**
- Check file size (max 100MB)
- Verify S3 bucket is accessible from Android device
- Check internet connectivity on device
- Review Retrofit timeout settings (should be 300s for OCR)

**Offline sync not working**
- Verify WorkManager is not battery-optimized: Settings > Battery > Battery Optimization
- Check Room database is created: `adb shell ls /data/data/com.mgb.mrfcmanager/databases/`
- Force sync by toggling airplane mode off
- Check sync worker logs in Android Studio Logcat

### Compliance Analysis Issues

**OCR takes too long (>5 minutes)**
- Normal range: 2-3 minutes for 25-page scanned PDFs
- Digital PDFs should complete in < 10 seconds
- Check backend logs for actual progress
- Verify Claude API key if using AI analysis
- Consider increasing OkHttp timeout if needed

**Analysis shows "Pending Manual Review"**
- OCR failed due to poor scan quality or corrupted PDF
- Admin can manually adjust compliance percentage in UI
- Try re-scanning document at higher quality
- Check backend logs for specific error details

**Claude AI not working**
- Verify `ANTHROPIC_API_KEY` in backend `.env`
- System automatically falls back to keyword analysis if API unavailable
- Check API quota/credits at console.anthropic.com
- Review backend logs for API error messages

### Railway Deployment Issues

**Deployment fails**
- Check all environment variables are set in Railway dashboard
- Verify database migrations are idempotent (use `IF NOT EXISTS`)
- Check build logs for TypeScript compilation errors
- Ensure `package.json` has correct start script: `"start": "node dist/server.js"`

**Database migrations fail**
- Manually run migrations: `npm run db:migrate`
- Check migration files don't have nested transactions
- Verify all foreign key references exist
- Use DO blocks for conditional DDL statements

---

## 📞 Support & Resources

- **Documentation**: See `PROJECT_STATUS.md` for complete feature list
- **Deployment**: See `RAILWAY_DEPLOYMENT_GUIDE.md` for Railway setup
- **API Setup**: See `CHATGPT_SETUP_GUIDE.md` for Claude AI configuration
- **Issues**: Check resolved issues in `RAILWAY_FIX_SUMMARY.md` and other fix docs
- **Development**: Contact the development team for access and questions
