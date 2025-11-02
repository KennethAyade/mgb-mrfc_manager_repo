# Pre-Testing Checklist - MRFC & Document Management

## ✅ **COMPLETED - Backend & Routing**

### 1. **AndroidManifest.xml - FIXED**
Added the following activity registrations:
```xml
<activity android:name=".ui.admin.CreateMRFCActivity" />
<activity android:name=".ui.admin.EditMRFCActivity" />
<activity android:name=".ui.admin.DocumentReviewActivity" />
```
**Location:** [AndroidManifest.xml](app/src/main/AndroidManifest.xml:78-91)

### 2. **Navigation Menu - FIXED**
Added "Review Documents" menu item to navigation drawer:
```xml
<item
    android:id="@+id/nav_document_review"
    android:icon="@drawable/ic_check"
    android:title="Review Documents" />
```
**Location:** [nav_drawer_menu.xml](app/src/main/res/menu/nav_drawer_menu.xml:43-46)

### 3. **Navigation Handler - FIXED**
Added navigation case in AdminDashboardActivity:
```kotlin
R.id.nav_document_review -> {
    startActivity(Intent(this, DocumentReviewActivity::class.java))
}
```
**Location:** [AdminDashboardActivity.kt](app/src/main/java/com/mgb/mrfcmanager/ui/admin/AdminDashboardActivity.kt:144-146)

### 4. **Backend Server - VERIFIED**
- ✅ Server running on http://localhost:3000
- ✅ Health endpoint responding: `/api/v1/health`
- ✅ Database migration completed successfully
- ✅ All compliance fields added to mrfcs table

---

## ✅ **COMPLETED - Layout XML Files**

All layout files have been created successfully:

### **Priority 1: Activity Layouts**

#### 1. **activity_create_mrfc.xml**
**Required Views:**
- Toolbar
- ScrollView with form fields:
  - TextInputEditText for: etMRFCName, etMrfcCode, etMunicipality, etProvince, etRegion
  - TextInputEditText for: etContactPerson, etContactNumber, etEmail, etAddress
  - AutoCompleteTextView: actvCategory (dropdown)
  - MaterialButton: btnCreate, btnCancel
  - ProgressBar: progressBar

**Reference:** Similar to `activity_create_user.xml`

#### 2. **activity_edit_mrfc.xml**
**Required Views:**
- Toolbar
- ScrollView with TWO sections:

  **Section A - Basic MRFC Fields:**
  - Same as Create MRFC above
  - Plus: Switch for isActive (switchActive)

  **Section B - Compliance Management:**
  - TextInputEditText: etCompliancePercentage (numeric input)
  - AutoCompleteTextView: actvComplianceStatus
  - TextInputEditText: etComplianceRemarks (multiline)
  - TextView: tvComplianceUpdatedAt (timestamp display)
  - ChipGroup with 4 chips:
    - chipCompliant, chipPartial, chipNonCompliant, chipNotAssessed
  - MaterialButton: btnSave, btnUpdateCompliance, btnCancel
  - ProgressBar: progressBar

#### 3. **activity_document_review.xml**
**Required Views:**
- Toolbar
- ChipGroup for filters (horizontal scroll):
  - chipAll, chipPending, chipAccepted, chipRejected
- RecyclerView: rvDocuments
- TextView: tvEmpty (empty state message)
- ProgressBar: progressBar

**Reference:** Similar to `activity_user_management.xml` with filters

### **Priority 2: Item Layouts**

#### 4. **item_document_review.xml**
**Required for:** DocumentReviewActivity adapter
**Required Views:**
- MaterialCardView containing:
  - TextView: tvFileName, tvCategory, tvFileSize, tvUploadDate, tvStatus
  - TextView: tvDescription (optional, visibility=GONE if empty)
  - TextView: tvRejectionReason (red color, visibility=GONE unless rejected)
  - MaterialButton: btnApprove, btnReject (only visible for pending docs)
  - MaterialButton: btnDownload (always visible)

**Design Pattern:** Card with icon + text + action buttons (3-button layout)

### **Priority 3: Dialog Layouts**

#### 5. **dialog_reject_document.xml**
**Required for:** Reject document dialog in DocumentReviewActivity
**Required Views:**
- TextInputLayout + TextInputEditText: etRejectionReason (multiline, required)

**Simple Layout:** Just one multiline text input for rejection reason

---

## 📋 **Navigation Flow Map**

```
AdminDashboard
├─ [Sidebar] "Review Documents" → DocumentReviewActivity
│   └─ Filter tabs (All, Pending, Accepted, Rejected)
│   └─ Document list with approve/reject buttons
│
├─ [Card/Sidebar] "MRFCs" → MRFCListActivity
│   ├─ [FAB Button] → CreateMRFCActivity (NEW)
│   └─ [Click Item] → MRFCDetailActivity
│       └─ [Edit Button] → EditMRFCActivity (NEW)
│
└─ [Card/Sidebar] "Upload Files" → FileUploadActivity (UPDATED)
    └─ Category dropdown with DocumentCategory enum
    └─ Description field
    └─ Upload progress
```

---

## ✅ **COMPLETED - Updates to Existing Files**

### MRFCListActivity - FAB Button Added ✅
**Location:** [MRFCListActivity.kt](app/src/main/java/com/mgb/mrfcmanager/ui/admin/MRFCListActivity.kt:129-146)
- FAB button now launches CreateMRFCActivity with startActivityForResult
- onActivityResult refreshes list after successful creation

### MRFCDetailActivity - Edit Menu Added ✅
**Location:** [MRFCDetailActivity.kt](app/src/main/java/com/mgb/mrfcmanager/ui/admin/MRFCDetailActivity.kt:232-261)
- Created menu_mrfc_detail.xml with Edit action
- Added onCreateOptionsMenu and onOptionsItemSelected
- Edit button launches EditMRFCActivity with startActivityForResult
- onActivityResult refreshes data after successful edit

### EditMRFCActivity - Result Handling Added ✅
**Location:** [EditMRFCActivity.kt](app/src/main/java/com/mgb/mrfcmanager/ui/admin/EditMRFCActivity.kt:164-165)
- Calls setResult(RESULT_OK) and finish() on successful update
- Added REQUEST_CODE_EDIT_MRFC constant

---

## 🎨 **Layout Creation Guidelines**

### Using Android Studio:
1. Right-click on `res/layout` folder
2. New → Layout Resource File
3. Enter file name (e.g., `activity_create_mrfc`)
4. Root element: `androidx.constraintlayout.widget.ConstraintLayout`
5. Use the design editor to drag and drop components

### Material Design Components to Use:
- **TextInputLayout** + **TextInputEditText** (for form fields)
- **MaterialButton** (for actions)
- **MaterialCardView** (for list items)
- **Chip** and **ChipGroup** (for filters/tags)
- **AutoCompleteTextView** (for dropdowns)

### Colors for Status Indicators:
```xml
<!-- Document Status Colors -->
<color name="status_pending">#FF9800</color>  <!-- Orange -->
<color name="status_accepted">#4CAF50</color> <!-- Green -->
<color name="status_rejected">#F44336</color> <!-- Red -->

<!-- Compliance Status Colors -->
<color name="compliance_compliant">#4CAF50</color>     <!-- Green -->
<color name="compliance_partial">#FF9800</color>       <!-- Orange -->
<color name="compliance_non_compliant">#F44336</color> <!-- Red -->
<color name="compliance_not_assessed">#9E9E9E</color>  <!-- Gray -->
```

---

## 🧪 **Testing Checklist (After Layouts Created)**

### Backend API Testing:
```bash
# Test MRFC endpoints
curl http://localhost:3000/api/v1/mrfcs
curl http://localhost:3000/api/v1/mrfcs/1

# Test Document endpoints
curl http://localhost:3000/api/v1/documents/mrfc/1
```

### Frontend Flow Testing:
1. **MRFC Management:**
   - ✅ Navigate to MRFC List
   - ⏳ Click FAB to create new MRFC
   - ⏳ Fill form and submit
   - ⏳ Open MRFC detail and click edit
   - ⏳ Update compliance percentage and status
   - ⏳ Save changes

2. **Document Upload:**
   - ✅ Navigate to File Upload
   - ⏳ Select category from dropdown
   - ⏳ Choose file
   - ⏳ Add description
   - ⏳ Upload and verify success

3. **Document Review:**
   - ✅ Navigate to Review Documents from sidebar
   - ⏳ Filter by status (Pending/Accepted/Rejected)
   - ⏳ Approve a pending document
   - ⏳ Reject a document with reason
   - ⏳ Download a document

---

## 📊 **Current Code Status**

### ✅ Fully Implemented (Kotlin Activities):
- CreateMRFCActivity.kt - Complete with validation
- EditMRFCActivity.kt - Complete with compliance management
- FileUploadActivity.kt - Updated with enum categories
- DocumentReviewActivity.kt - Complete with filters and actions

### ✅ Backend Ready:
- MRFC Controller - Full CRUD + compliance endpoints
- Document Controller - Upload, review, approve/reject
- Database - Migration applied successfully
- DTOs - All frontend DTOs match backend schema

### ✅ All Implementation Complete:
- ✅ 5 layout XML files created
- ✅ FAB button added to MRFCListActivity
- ✅ Edit menu added to MRFCDetailActivity
- ⏳ Ready for end-to-end testing

---

## 🚀 **Next Steps - READY FOR TESTING**

All implementation is complete! You can now:

1. **Build the Android app** - Ensure no compilation errors
2. **Run the app** on emulator or device
3. **Test the workflows** below:
   - MRFC Management (Create, Edit, View)
   - Document Upload with categories
   - Document Review and Approval
   - Compliance tracking updates

---

## 📝 **Notes**

- **Icon Requirements:** Ensure these drawable icons exist:
  - `ic_check` (for Review Documents menu item)
  - `ic_file` (for document items)
  - Standard Material icons for forms

- **Permissions:** Already set in AndroidManifest.xml
  - INTERNET ✅
  - READ_EXTERNAL_STORAGE ✅
  - WRITE_EXTERNAL_STORAGE ✅

- **Dependencies:** All required (Retrofit, Moshi, Coroutines, LiveData, Material Design)

---

**Generated:** 2025-11-02
**Status:** Ready for layout creation and testing
