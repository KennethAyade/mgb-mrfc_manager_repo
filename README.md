# MRFC Manager - Android Application

**Municipal Resource and Finance Committee Meeting Management System**

A dual-portal Android application for managing MRFC meetings, compliance monitoring, and documentation with separate interfaces for administrators and regular users.

---

## 🎨 Design Philosophy

This UI-only implementation follows the **DENR/MGB visual aesthetic**:
- **Primary Color**: DENR Green (#2E7D32)
- **Design Pattern**: Card-based Material Design 3
- **Layout Style**: Clean, spacious with generous padding
- **Navigation**: Simple and intuitive hierarchical structure

---

## ✅ What's Implemented

### Core Infrastructure
- ✅ **Project Setup**: Gradle configuration with all necessary dependencies
- ✅ **Resource Files**: Complete colors, themes, dimensions, and strings
- ✅ **Data Models**: User, MRFC, Proponent, Agenda, Document
- ✅ **Demo Data**: Hardcoded test data (`DemoData.kt`)

### Completed Screens

#### 1. **Splash Screen** (`SplashActivity`)
- DENR green background with branding
- Auto-navigation to login after 2 seconds
- **Location**: `app/src/main/java/com/mgb/mrfcmanager/ui/auth/SplashActivity.kt`

#### 2. **Login Screen** (`LoginActivity`)
- Gradient green background
- Username/password authentication (hardcoded)
- Routes to appropriate dashboard based on role
- **Test Credentials**:
  - Admin: `admin` / `admin123`
  - User: `user1` / `user123`
- **Location**: `app/src/main/java/com/mgb/mrfcmanager/ui/auth/LoginActivity.kt`

#### 3. **Admin Dashboard** (`AdminDashboardActivity`)
- 3x2 grid layout with icon cards
- Six main functions:
  1. Select MRFC (✅ functional)
  2. Attendance (placeholder)
  3. Notifications (placeholder)
  4. File Upload (placeholder)
  5. Settings (placeholder)
  6. Minutes of Meetings (placeholder)
- **Location**: `app/src/main/java/com/mgb/mrfcmanager/ui/admin/AdminDashboardActivity.kt`

#### 4. **MRFC List Screen** (`MRFCListActivity`)
- RecyclerView displaying all MRFCs
- Uses hardcoded data from `DemoData.mrfcList`
- Green button list items
- Floating Action Button (FAB) for adding new MRFC (placeholder)
- Back navigation to dashboard
- **Location**: `app/src/main/java/com/mgb/mrfcmanager/ui/admin/MRFCListActivity.kt`

#### 5. **User Dashboard** (`UserDashboardActivity`)
- Placeholder screen for regular users
- Shows "Coming Soon" message
- **Location**: `app/src/main/java/com/mgb/mrfcmanager/ui/user/UserDashboardActivity.kt`

---

## 📱 How to Run the App

### Prerequisites
- Android Studio (latest version recommended)
- JDK 11 or higher
- Android SDK with API 25+ support

### Steps
1. **Open Project**
   ```bash
   cd MGB
   # Open in Android Studio
   ```

2. **Sync Gradle**
   - Wait for Gradle sync to complete
   - All dependencies will be downloaded automatically

3. **Run the App**
   - Connect an Android device or start an emulator
   - Click the "Run" button (green triangle) in Android Studio
   - Select your target device

4. **Test Login**
   - Wait 2 seconds for splash screen
   - Login with:
     - **Admin**: `admin` / `admin123`
     - **User**: `user1` / `user123`

---

## 🗂️ Project Structure

```
app/src/main/
├── java/com/mgb/mrfcmanager/
│   ├── ui/
│   │   ├── auth/
│   │   │   ├── SplashActivity.kt          ✅
│   │   │   └── LoginActivity.kt           ✅
│   │   ├── admin/
│   │   │   ├── AdminDashboardActivity.kt  ✅
│   │   │   └── MRFCListActivity.kt        ✅
│   │   └── user/
│   │       └── UserDashboardActivity.kt   ✅
│   ├── data/model/
│   │   ├── User.kt                        ✅
│   │   ├── MRFC.kt                        ✅
│   │   ├── Proponent.kt                   ✅
│   │   ├── Agenda.kt                      ✅
│   │   └── Document.kt                    ✅
│   └── utils/
│       └── DemoData.kt                    ✅
└── res/
    ├── layout/                             ✅ (5 layouts created)
    ├── drawable/                           ✅ (6 icons created)
    ├── values/
    │   ├── colors.xml                     ✅
    │   ├── strings.xml                    ✅
    │   ├── themes.xml                     ✅
    │   └── dimens.xml                     ✅
    └── AndroidManifest.xml                ✅
```

---

## 🚧 What Remains to be Built

### Admin Portal Screens (TODO)

#### Priority 1 - Core Meeting Management
1. **Proponent List Screen**
   - Display proponents for selected MRFC
   - Add/Edit/Delete proponent functionality
   - Status indicators (Active/Inactive)

2. **Quarter Selection Screen**
   - 2x2 grid for quarters
   - Navigate to Agenda Management

3. **Agenda Management Screen**
   - Meeting date/location pickers
   - Standard agenda items (8 items from spec)
   - "Matters Arising" table
   - Save functionality

#### Priority 2 - Supporting Features
4. **Attendance Screen**
   - List of attendees
   - Camera integration for photos
   - Mark present/absent

5. **File Upload Screen**
   - File picker integration
   - Category selection
   - Upload progress indicator

6. **Compliance Dashboard**
   - MPAndroidChart integration
   - Donut chart for compliance rate
   - Bar charts for breakdown
   - Generate report functionality

7. **Notifications Screen**
   - List of notifications
   - Mark as read functionality

### User Portal Screens (TODO)

1. **User Dashboard**
   - Grid layout similar to admin
   - Access to: Agenda View, Documents, Notes

2. **MRFC Selection (User)**
   - Select MRFC to view

3. **Proponent View (User)**
   - View-only proponent list

4. **Agenda View (User)**
   - Read-only agenda display
   - View meeting details

5. **Document List (User)**
   - Browse and download documents

6. **Notes Screen**
   - Add/edit personal notes
   - Attach to agenda items

---

## 🔧 Backend Integration Guide

When you're ready to add a backend, follow these steps:

### 1. Add Room Database

**Update `build.gradle.kts`:**
```kotlin
dependencies {
    // Room
    implementation("androidx.room:room-runtime:2.6.1")
    implementation("androidx.room:room-ktx:2.6.1")
    kapt("androidx.room:room-compiler:2.6.1")
}
```

**Convert Data Models to Entities:**
```kotlin
@Entity(tableName = "mrfcs")
data class MRFC(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val municipality: String,
    val contactPerson: String,
    val contactNumber: String
)
```

### 2. Create DAO Interfaces

```kotlin
@Dao
interface MRFCDao {
    @Query("SELECT * FROM mrfcs")
    fun getAllMRFCs(): Flow<List<MRFC>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(mrfc: MRFC)

    @Delete
    suspend fun delete(mrfc: MRFC)
}
```

### 3. Create Database Class

```kotlin
@Database(entities = [MRFC::class, Proponent::class, /* ... */], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun mrfcDao(): MRFCDao
    // ...
}
```

### 4. Add ViewModels

```kotlin
class MRFCListViewModel(private val repository: MRFCRepository) : ViewModel() {
    val mrfcs: LiveData<List<MRFC>> = repository.getAllMRFCs().asLiveData()

    fun insert(mrfc: MRFC) = viewModelScope.launch {
        repository.insert(mrfc)
    }
}
```

### 5. Update Activities

**Replace this:**
```kotlin
val mrfcs = DemoData.mrfcList
adapter.submitList(mrfcs)
```

**With this:**
```kotlin
viewModel.mrfcs.observe(this) { mrfcs ->
    adapter.submitList(mrfcs)
}
```

### 6. Remove Demo Data

- Delete `DemoData.kt`
- Remove all hardcoded data references
- Remove TODOcomments

---

## 🎯 Key Features Overview

### Authentication
- Hardcoded credentials (admin/user)
- Role-based routing
- **TODO**: Add proper authentication service

### Admin Features
- ✅ Dashboard with 6 function cards
- ✅ MRFC list with RecyclerView
- 🚧 Full CRUD operations for MRFC/Proponents
- 🚧 Agenda management with standard items
- 🚧 Attendance tracking with camera
- 🚧 File upload and categorization
- 🚧 Compliance monitoring with charts

### User Features
- 🚧 Read-only access to agendas
- 🚧 Document viewing
- 🚧 Personal notes functionality

### Technical Highlights
- Material Design 3 components
- RecyclerView with ViewHolder pattern
- Hardcoded data ready for backend swap
- TODO comments marking backend integration points
- Clean architecture preparation (models, UI separation)

---

## 📊 Sample Data

The app includes demo data for:
- **5 MRFCs** (Dingras, Laoag, Batac, Bangui, Pagudpud)
- **10 Proponents** (Mining and quarrying companies)
- **8 Standard Agenda Items** (as per MRFC spec)
- **5 Sample Documents** (MTF reports, AEPEP, CMVR, Minutes)
- **4 Quarters** (2025)

**Location**: `app/src/main/java/com/mgb/mrfcmanager/utils/DemoData.kt`

---

## 🎨 UI Components Reference

### Colors (`colors.xml`)
- `denr_green` (#2E7D32) - Primary
- `denr_green_dark` (#1B5E20) - Dark variant
- `denr_green_light` (#4CAF50) - Light variant
- Status colors: compliant, partial, non-compliant, pending

### Dimensions (`dimens.xml`)
- Spacing: 4dp, 8dp, 16dp, 24dp, 32dp
- Text sizes: 12sp to 28sp
- Button height: 56dp
- Card corner radius: 12dp

### Styles (`themes.xml`)
- `GreenButton` - Standard green button
- `GreenButtonFull` - Full-width green button
- `CardStyle` - Elevated card with rounded corners
- `TextInputStyle` - Outlined text input

---

## 📝 Development Workflow

### Adding a New Screen

1. **Create Layout XML**
   ```xml
   <!-- res/layout/activity_my_screen.xml -->
   <androidx.coordinatorlayout.widget.CoordinatorLayout>
       <!-- Your UI -->
   </androidx.coordinatorlayout.widget.CoordinatorLayout>
   ```

2. **Create Activity Class**
   ```kotlin
   class MyScreenActivity : AppCompatActivity() {
       override fun onCreate(savedInstanceState: Bundle?) {
           super.onCreate(savedInstanceState)
           setContentView(R.layout.activity_my_screen)
       }
   }
   ```

3. **Add to AndroidManifest.xml**
   ```xml
   <activity
       android:name=".ui.admin.MyScreenActivity"
       android:exported="false" />
   ```

4. **Navigate to Screen**
   ```kotlin
   startActivity(Intent(this, MyScreenActivity::class.java))
   ```

---

## 🐛 Known Issues & Limitations

1. **Authentication**: Hardcoded credentials only
2. **Data Persistence**: No database - data resets on app restart
3. **Icons**: Using placeholder vector drawables (replace with proper icons)
4. **Camera**: CameraX dependency added but not implemented
5. **Charts**: MPAndroidChart dependency added but not implemented
6. **File Handling**: No actual file upload/download functionality yet
7. **Placeholder Screens**: Many screens show "Coming Soon" messages

---

## 🚀 Next Steps (Recommended Order)

### Phase 1: Complete Admin Core (Week 1-2)
1. Implement Proponent List with full CRUD
2. Add Quarter Selection screen
3. Build Agenda Management with standard items
4. Test complete flow: MRFC → Proponents → Quarter → Agenda

### Phase 2: Supporting Features (Week 3-4)
1. Implement Attendance with CameraX
2. Add File Upload functionality
3. Create Compliance Dashboard with charts
4. Build Notifications system

### Phase 3: User Portal (Week 5-6)
1. Build User Dashboard
2. Implement read-only views
3. Add Notes functionality
4. Test user workflows

### Phase 4: Backend Integration (Week 7-8)
1. Set up Room database
2. Create DAOs and repositories
3. Add ViewModels
4. Replace hardcoded data
5. Add proper authentication

---

## 📚 Resources & Documentation

### Dependencies Used
- **AndroidX Core**: Core Kotlin extensions
- **Material Design 3**: UI components
- **RecyclerView**: List displays
- **Navigation**: Screen navigation (added, not yet used)
- **ViewModel/LiveData**: MVVM support (added for future use)
- **CameraX**: Camera functionality (added, not implemented)
- **MPAndroidChart**: Charts and graphs (added, not implemented)
- **Coil**: Image loading (added, not implemented)

### Key Files to Review
- `DemoData.kt` - All hardcoded test data
- `themes.xml` - App styling and colors
- `strings.xml` - All text strings
- `AndroidManifest.xml` - App configuration

---

## 💡 Tips for Developers

1. **Search for "TODO: BACKEND"** in code to find all integration points
2. **Use DemoData patterns** when adding new screens
3. **Follow Material Design 3** guidelines for consistency
4. **Test with both admin and user roles** regularly
5. **Keep hardcoded data** until backend is fully ready
6. **Document new features** in this README

---

## 📞 Test Credentials

### Admin Access
- **Username**: `admin`
- **Password**: `admin123`
- **Features**: Full CRUD access, dashboard, all admin screens

### User Access
- **Username**: `user1`
- **Password**: `user123`
- **Features**: Read-only access, limited functionality

---

## 📄 License

This project is developed for MGB-DENR Philippines.

---

## 🎉 Summary

**This UI-only implementation provides:**
- ✅ Complete visual foundation following DENR design aesthetic
- ✅ Working authentication and navigation
- ✅ Functional Admin Dashboard and MRFC List
- ✅ Hardcoded demo data for testing
- ✅ Backend-ready architecture with TODO markers
- ✅ Comprehensive documentation

**Ready for:**
- 🚀 Immediate visual testing and feedback
- 🚀 Additional screen implementations
- 🚀 Backend integration when needed
- 🚀 Stakeholder demonstrations

The app is fully buildable and runnable right now using hardcoded data!
