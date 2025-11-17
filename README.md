# MRFC Manager - Full Stack Application

**Municipal Resource and Finance Committee Meeting Management System**

A complete Android application with Node.js backend for managing MRFC meetings, compliance monitoring, and documentation with separate interfaces for administrators and regular users.

---

## 🎨 Design Philosophy

This application follows the **DENR/MGB visual aesthetic**:
- **Primary Color**: DENR Green (#2E7D32)
- **Design Pattern**: Card-based Material Design 3
- **Layout Style**: Clean, spacious with generous padding
- **Navigation**: Simple and intuitive hierarchical structure

---

## ✅ What's Implemented

### Backend (Node.js + TypeScript)
- ✅ **REST API**: Complete API with authentication
- ✅ **PostgreSQL Database**: Full data persistence
- ✅ **AWS S3 Storage**: File uploads/downloads (up to 100MB)
- ✅ **JWT Authentication**: Secure token-based auth
- ✅ **Auto Compliance Analysis**: Automatic CMVR analysis with real OCR
- ✅ **AI-Powered Analysis**: Anthropic Claude (Haiku 4.5) for intelligent compliance detection
- ✅ **Real OCR**: pdfjs-dist + Tesseract.js for scanned PDFs
- ✅ **Smart Fallback**: Keyword analysis if AI unavailable
- ✅ **Error Handling**: Failed analyses marked as "Pending Manual Review"
- ✅ **Audit Logging**: Track all important actions

### Android App (Kotlin)
- ✅ **Full Backend Integration**: No hardcoded data
- ✅ **Authentication**: JWT token management
- ✅ **Document Management**: Upload, view, download, delete
- ✅ **Compliance Analysis**: Auto-trigger, real-time progress, results display
- ✅ **MRFC Management**: List, create, update, delete
- ✅ **Proponent Management**: Full CRUD operations
- ✅ **Agenda Management**: Meeting planning and tracking
- ✅ **Responsive UI**: Material Design 3 components

### Compliance Analysis Features
- ✅ **Auto-Trigger**: Analyzes documents automatically when viewed
- ✅ **AI-Powered**: Anthropic Claude (Haiku 4.5) for intelligent analysis
- ✅ **Real OCR**: Extracts text from scanned PDFs
- ✅ **Digital PDF Support**: Fast text extraction for searchable PDFs
- ✅ **Smart Analysis**: Context-aware compliance detection with AI
- ✅ **Fallback Strategy**: Keyword analysis if AI unavailable
- ✅ **Section Breakdown**: ECC, EPEP, Water Quality, Air Quality, etc.
- ✅ **Non-Compliant Items**: Detailed issue tracking with severity
- ✅ **Progress Tracking**: Real-time OCR progress updates
- ✅ **Error Handling**: "Pending Manual Review" for failed analyses

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
   Create `backend/.env`:
   ```env
   # Database
   DATABASE_URL=postgresql://user:password@localhost:5432/mrfc_db
   
   # JWT
   JWT_SECRET=your-secret-key-here
   
   # AWS S3
   S3_BUCKET_NAME=adhub-s3-demo
   AWS_ACCESS_KEY_ID=AKIA...
   AWS_SECRET_ACCESS_KEY=...
   AWS_REGION=us-east-1

   # Anthropic Claude AI (Optional - for intelligent compliance analysis)
   ANTHROPIC_API_KEY=sk-ant-...
   
   # Server
   PORT=3000
   NODE_ENV=development
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
- **Database**: PostgreSQL with Sequelize ORM
- **File Storage**: AWS S3
- **OCR**: Tesseract.js + pdfjs-dist + canvas
- **Authentication**: JWT tokens
- **Security**: Helmet, CORS, rate limiting

### Android
- **Language**: Kotlin
- **Min SDK**: API 25 (Android 7.1)
- **Target SDK**: API 36
- **Architecture**: MVVM
- **Networking**: Retrofit + Moshi
- **UI**: Material Design 3
- **Image Loading**: Coil
- **Async**: Kotlin Coroutines

### Free OCR Libraries
- **pdfjs-dist**: PDF rendering (Mozilla PDF.js)
- **canvas**: Image rendering
- **Tesseract.js**: OCR text extraction
- **pdf.js-extract**: Quick text extraction

---

## 🚀 Recent Updates (November 2025)

### Latest Changes
1. **S3 Migration**: Migrated from Cloudinary to AWS S3
   - All file operations now use S3
   - Increased file size limit to 100MB
   - Better cost efficiency

2. **Auto Compliance Analysis**: 
   - Viewing CMVR document automatically triggers analysis
   - No manual "Analyze" button needed
   - Seamless user experience

3. **Real OCR Implementation**:
   - Replaced pdf2pic with pdfjs-dist + canvas
   - Works cross-platform (Windows, Mac, Linux)
   - No external dependencies (GraphicsMagick/ImageMagick)
   - Handles scanned PDFs properly

4. **Proper Error Handling**:
   - Failed analyses saved as "Pending Manual Review"
   - No mock data fallback
   - Clear error messages in UI

5. **Removed Hardcoded Data**:
   - Deleted DemoData.kt
   - 100% backend-integrated
   - All data from real database

6. **Fixed Infinite Polling**:
   - Progress polling stops correctly
   - Handles cached results properly

7. **Fixed JSON Parsing**:
   - ApiResponse wrapper handling
   - Proper data extraction

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

## 🐛 Known Issues & Limitations

### Current Limitations
1. **OCR Performance**: Scanned PDFs take 2-3 minutes for 25 pages
2. **OCR on Windows**: Works but slower than Linux
3. **File Size**: Limited to 100MB by Multer (S3 supports up to 5TB)

### Resolved Issues
- ✅ JSON parsing errors - Fixed
- ✅ Infinite polling loops - Fixed
- ✅ Hardcoded demo data - Removed
- ✅ Cloudinary to S3 migration - Complete
- ✅ Auto-trigger analysis - Implemented
- ✅ OCR cross-platform support - Fixed

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

### Backend (Render.com)
```bash
cd backend
npm run build
npm start
```

### Android (APK)
1. Build → Generate Signed Bundle/APK
2. Select APK
3. Create/select keystore
4. Build release APK

---

## 📄 License

This project is developed for MGB-DENR Philippines.

---

## 🎉 Current Status

**Backend**: ✅ Production-ready with real OCR  
**Android App**: ✅ Fully integrated with backend  
**File Storage**: ✅ AWS S3  
**Compliance Analysis**: ✅ Automatic with real OCR  
**Demo Data**: ✅ Removed (100% real data)  

**The application is complete and ready for production deployment!**

---

## 📖 Additional Documentation

- `S3_MIGRATION_COMPLETE.md` - AWS S3 integration details
- `AUTO_ANALYSIS_IMPLEMENTATION_COMPLETE.md` - Compliance analysis implementation
- `ANDROID_JSON_PARSING_FIX.md` - JSON parsing fixes
- `ANDROID_INFINITE_POLLING_FIX.md` - Polling loop fixes
- `REAL_ANALYSIS_TESTING_COMPLETE.md` - OCR testing guide
- `S3_BUCKET_SETUP_GUIDE.md` - S3 bucket configuration

---

## 🆘 Troubleshooting

### Backend won't start
- Check `.env` file has all required variables
- Verify PostgreSQL is running
- Check AWS credentials are valid

### Android app can't connect
- Verify backend is running on `http://localhost:3000`
- For emulator, backend URL should be `http://10.0.2.2:3000`
- Check firewall settings

### File upload fails
- Check S3 bucket permissions
- Verify AWS credentials in `.env`
- Check file size (max 100MB)

### OCR takes too long
- Normal for scanned PDFs (2-3 minutes for 25 pages)
- Digital PDFs with text are instant
- Consider using higher quality scans for better OCR accuracy

---

## 📞 Support

For issues or questions, check the documentation files in the root directory or contact the development team.
