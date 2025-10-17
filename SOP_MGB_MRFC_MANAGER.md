# STANDARD OPERATING PROCEDURES (SOP)
## MGB MRFC Manager Mobile Application

**Department of Environment and Natural Resources (DENR)**
**Mines and Geosciences Bureau (MGB)**

---

**Document Version:** 1.0
**Date Issued:** October 16, 2025
**Prepared by:** MGB IT Development Team
**Classification:** Internal Use Only

---

## TABLE OF CONTENTS

1. [Introduction](#1-introduction)
2. [Application Overview](#2-application-overview)
3. [User Roles and Access Levels](#3-user-roles-and-access-levels)
4. [System Requirements](#4-system-requirements)
5. [Installation and Setup](#5-installation-and-setup)
6. [Feature-Specific Standard Operating Procedures](#6-feature-specific-standard-operating-procedures)
7. [Security and Data Management](#7-security-and-data-management)
8. [Troubleshooting and Support](#8-troubleshooting-and-support)
9. [Appendices](#9-appendices)

---

## 1. INTRODUCTION

### 1.1 Purpose
This Standard Operating Procedures (SOP) document provides comprehensive guidelines for the proper use, operation, and management of the MGB MRFC Manager mobile application. It is intended for DENR/MGB personnel involved in monitoring and managing Municipal Resource and Finance Committees (MRFCs) across the Philippines.

### 1.2 Scope
This document covers all operational procedures for both Admin and User portals, including:
- User authentication and access control
- Meeting management and agenda creation
- Attendance tracking with face capture
- Document management and viewing
- Compliance monitoring and reporting
- Offline access capabilities

### 1.3 Definitions and Acronyms

| Term | Definition |
|------|------------|
| **MRFC** | Municipal Resource and Finance Committee |
| **MGB** | Mines and Geosciences Bureau |
| **DENR** | Department of Environment and Natural Resources |
| **Proponent** | Mining or quarrying company/permit holder |
| **Admin** | System administrator with full access rights |
| **User** | Regular user with read-only access |
| **SOP** | Standard Operating Procedures |
| **APK** | Android Package Kit (installation file) |

---

## 2. APPLICATION OVERVIEW

### 2.1 Application Purpose
The MGB MRFC Manager is a mobile application designed to:
- Streamline MRFC meeting management
- Track attendance and compliance of mining proponents
- Facilitate document sharing and review
- Generate compliance reports and performance indicators
- Enable real-time collaboration between field officers and central office

### 2.2 Key Features

#### Fully Included Features
1. **Dual User Interface** - Separate Admin and User portals with role-based permissions
2. **Real-Time Agenda Display** - Dynamic meeting agenda viewing and management
3. **Feedback & Notes Section** - Personal note-taking and documentation
4. **Document Viewing & Interaction** - Browse, search, and download documents

#### Partially Included Features (Basic Versions)
1. **Attendance Tracking** - Manual entry with face capture (no facial recognition)
2. **Meeting Management** - Create meetings with voice recording capability
3. **Automatic Report Navigation** - Simple filtering and search functionality
4. **Percentage Compliance/Performance Indicators** - Manual calculation requiring admin review
5. **Offline Access** - View-only mode when internet is unavailable (upload disabled)

### 2.3 Application Architecture
- **Platform:** Android (native application)
- **Minimum Android Version:** Android 7.1 (API 25)
- **Target Android Version:** Android 15 (API 36)
- **User Interface:** Material Design 3
- **Data Storage:** Local device storage (offline) + cloud synchronization (online)

---

## 3. USER ROLES AND ACCESS LEVELS

### 3.1 Admin Portal

#### 3.1.1 Admin Permissions
Administrators have full system access including:

| Permission Category | Capabilities |
|---------------------|--------------|
| **MRFC Management** | Create, read, update, delete MRFC records |
| **Proponent Management** | Add, edit, delete proponent information |
| **Meeting Management** | Schedule meetings, create agendas, set dates/locations |
| **Attendance Tracking** | Mark attendance, capture photos, generate attendance reports |
| **Document Management** | Upload, categorize, delete documents |
| **Compliance Monitoring** | View compliance dashboard, generate reports, export data |
| **Notifications** | Send notifications, mark as read, manage alerts |
| **User Management** | Assign user access to specific MRFCs (future feature) |

#### 3.1.2 Admin Responsibilities
1. Maintain accurate MRFC and proponent records
2. Schedule and manage quarterly MRFC meetings
3. Track attendance and ensure proper documentation
4. Upload required compliance documents
5. Monitor proponent compliance percentages
6. Generate and review quarterly reports
7. Respond to user notifications and feedback

### 3.2 User Portal

#### 3.2.1 User Permissions
Regular users have limited, read-only access:

| Permission Category | Capabilities |
|---------------------|--------------|
| **MRFC Viewing** | View assigned MRFC details (read-only) |
| **Proponent Viewing** | View proponent information for assigned MRFCs |
| **Agenda Viewing** | View meeting agendas, dates, and matters arising |
| **Document Viewing** | Search, filter, and download documents |
| **Personal Notes** | Create, edit, delete personal notes |
| **Notifications** | Receive and view notifications |

#### 3.2.2 User Responsibilities
1. Review meeting agendas before MRFC meetings
2. Take personal notes during meetings
3. Download and review required documents
4. Provide feedback through notes section
5. Report discrepancies to administrators

### 3.3 Access Control Matrix

| Feature | Admin | User |
|---------|-------|------|
| Dashboard Access | ✅ Full | ✅ Limited |
| MRFC Management | ✅ CRUD | ❌ View Only |
| Proponent Management | ✅ CRUD | ❌ View Only |
| Agenda Management | ✅ Create/Edit | ❌ View Only |
| Attendance Tracking | ✅ Mark/Edit | ❌ No Access |
| File Upload | ✅ Upload/Delete | ❌ Download Only |
| Compliance Dashboard | ✅ Full Access | ❌ No Access |
| Notifications | ✅ Send/Manage | ✅ Receive/View |
| Personal Notes | ✅ Yes | ✅ Yes |

---

## 4. SYSTEM REQUIREMENTS

### 4.1 Hardware Requirements

#### Minimum Requirements
- **Device:** Android smartphone or tablet
- **Processor:** Quad-core 1.5 GHz or higher
- **RAM:** 2 GB minimum
- **Storage:** 500 MB free space (1 GB recommended)
- **Camera:** 5 MP rear camera (for attendance photos)
- **Display:** 5-inch screen, 720p resolution minimum

#### Recommended Requirements
- **Processor:** Octa-core 2.0 GHz or higher
- **RAM:** 4 GB or more
- **Storage:** 2 GB free space
- **Camera:** 8 MP or higher with autofocus
- **Display:** 6-inch screen, 1080p resolution or higher

### 4.2 Software Requirements
- **Operating System:** Android 7.1 (Nougat) or higher
- **Internet Connection:** Required for initial setup and data synchronization
  - Wi-Fi or mobile data (3G/4G/5G)
  - Offline mode available for viewing only
- **Permissions Required:**
  - Camera access (for attendance photos)
  - Storage access (for document downloads/uploads)
  - Internet access (for data synchronization)
  - Microphone access (for voice recording during meetings)

### 4.3 Network Requirements
- **Bandwidth:** Minimum 512 kbps (2 Mbps recommended)
- **Latency:** Maximum 500ms
- **Server Connection:** Access to MGB backend server (URL to be provided during deployment)

---

## 5. INSTALLATION AND SETUP

### 5.1 Initial Installation

#### 5.1.1 For IT Administrators (Deployment)
1. **Download APK File**
   - Obtain the latest MGB MRFC Manager APK from the official MGB IT repository
   - Verify file integrity using provided checksum

2. **Device Preparation**
   - Enable "Install from Unknown Sources" in device settings
   - Path: Settings > Security > Unknown Sources (Enable)

3. **Installation Process**
   - Transfer APK to target device via USB, email, or file sharing
   - Locate APK file using device file manager
   - Tap APK file to initiate installation
   - Grant requested permissions during installation
   - Tap "Install" and wait for completion

4. **Post-Installation Verification**
   - Launch application
   - Verify splash screen displays DENR branding
   - Confirm login screen loads successfully

#### 5.1.2 For End Users
1. **Receive Installation Instructions** from IT Administrator
2. **Install APK** following provided instructions
3. **Launch Application** and proceed to login
4. **Contact IT Support** if installation fails

### 5.2 First-Time Login

#### 5.2.1 Admin First Login
```
Step 1: Launch application
Step 2: At login screen, enter provided credentials:
        - Username: [Provided by IT Admin]
        - Password: [Provided by IT Admin]
Step 3: Tap "Login" button
Step 4: System will route to Admin Dashboard
Step 5: Change default password immediately
        (Settings > Change Password - Future Feature)
Step 6: Verify internet connection for data synchronization
Step 7: Allow necessary permissions when prompted:
        - Camera (for attendance tracking)
        - Storage (for document management)
        - Microphone (for voice recording)
```

#### 5.2.2 User First Login
```
Step 1: Launch application
Step 2: Enter provided credentials
Step 3: System will route to User Dashboard
Step 4: Verify assigned MRFCs are visible
Step 5: Test document download functionality
Step 6: Familiarize with interface and available features
```

### 5.3 Permission Configuration

#### 5.3.1 Required Permissions

**Camera Permission** (Admin only)
- **Purpose:** Capture attendance photos
- **How to Enable:**
  1. Go to Settings > Apps > MGB MRFC Manager
  2. Tap "Permissions"
  3. Enable "Camera"

**Storage Permission** (All users)
- **Purpose:** Save and access documents
- **How to Enable:**
  1. Go to Settings > Apps > MGB MRFC Manager
  2. Tap "Permissions"
  3. Enable "Storage" or "Files and Media"

**Microphone Permission** (Admin only)
- **Purpose:** Record meeting audio notes
- **How to Enable:**
  1. Go to Settings > Apps > MGB MRFC Manager
  2. Tap "Permissions"
  3. Enable "Microphone"

---

## 6. FEATURE-SPECIFIC STANDARD OPERATING PROCEDURES

---

## 6.1 DUAL USER INTERFACE

### 6.1.1 Admin Portal Operations

#### Accessing Admin Dashboard
**Frequency:** Daily during active monitoring periods
**Responsible Party:** MRFC Coordinators, MGB Field Officers

**Procedure:**
1. Launch MGB MRFC Manager application
2. Enter admin credentials on login screen
3. System automatically routes to Admin Dashboard
4. Dashboard displays:
   - Navigation drawer (left side)
   - Quick action cards (center)
   - Notification badge (top right)

**Navigation Structure:**
```
Admin Dashboard
├── Dashboard Home
├── Meeting Management
│   ├── MRFC List
│   ├── Proponent List
│   ├── Quarter Selection
│   └── Agenda Management
├── Documents & Files
│   ├── File Upload
│   └── Uploaded Files
├── Monitoring & Reports
│   ├── Attendance Tracking
│   └── Compliance Dashboard
├── Notifications
├── Settings (Future Feature)
└── Logout
```

**Best Practices:**
- Check notifications daily for urgent alerts
- Review dashboard cards for quick overview
- Use navigation drawer for organized access to features
- Log out when not actively using the app

#### Admin Dashboard Components

**Quick Action Cards:**
1. **MRFC Management Card**
   - Purpose: Access MRFC and proponent records
   - Action: Tap to view MRFC list

2. **Meeting Management Card**
   - Purpose: Create and manage meeting agendas
   - Action: Tap to select quarter

3. **Attendance Tracking Card**
   - Purpose: Mark attendance during meetings
   - Action: Tap to mark attendance with photos

4. **File Upload Card**
   - Purpose: Upload compliance documents
   - Action: Tap to upload files

5. **Compliance Dashboard Card**
   - Purpose: View compliance statistics
   - Action: Tap to view charts and reports

6. **Notifications Card**
   - Purpose: View system notifications
   - Action: Tap to view all notifications

### 6.1.2 User Portal Operations

#### Accessing User Dashboard
**Frequency:** As needed for meeting preparation and document review
**Responsible Party:** Field Inspectors, Technical Staff, Support Personnel

**Procedure:**
1. Launch MGB MRFC Manager application
2. Enter user credentials on login screen
3. System automatically routes to User Dashboard
4. Dashboard displays three main cards:
   - View MRFC & Proponents
   - View Agenda & Documents
   - Manage Personal Notes

**Navigation Structure:**
```
User Dashboard
├── MRFC Selection
│   └── Proponent View
│       ├── Agenda View
│       └── Document List
├── Personal Notes
└── Notifications
```

**Best Practices:**
- Review agenda before attending meetings
- Download important documents for offline access
- Take notes during meetings for later reference
- Check notifications regularly

---

## 6.2 REAL-TIME AGENDA DISPLAY

### 6.2.1 Creating Meeting Agenda (Admin)

**Frequency:** Quarterly (Q1, Q2, Q3, Q4)
**Responsible Party:** MRFC Coordinator, Meeting Organizer
**Timeline:** Create agenda at least 7 days before meeting date

#### Procedure for Agenda Creation

**Step 1: Select Quarter**
```
1. From Admin Dashboard, tap "Meeting Management"
2. Select appropriate quarter (Q1 2025, Q2 2025, Q3 2025, Q4 2025)
3. Tap selected quarter card
4. System navigates to Agenda Management screen
```

**Step 2: Set Meeting Details**
```
1. Tap "Meeting Date" field
2. Select date using date picker calendar
3. Confirm selected date
4. Tap "Meeting Location" field
5. Enter complete meeting location/address
6. Verify information is accurate
```

**Step 3: Review Standard Agenda Items**
The system displays 8 standard agenda items:
1. Call to Order
2. Opening Prayer
3. Roll Call
4. Reading and Approval of Previous Minutes
5. Matters Arising from Previous Meeting
6. Reports from Proponents
7. Other Matters
8. Adjournment

**Note:** Standard items cannot be edited. They follow DENR meeting protocols.

**Step 4: Add Matters Arising (if applicable)**
```
1. Scroll to "Matters Arising" section
2. Tap "+ Add Matter" button
3. Fill in the form:
   - Issue: [Description of unresolved matter]
   - Status: Select from dropdown (Pending/In Progress/Resolved)
   - Assigned To: [Name of responsible person]
   - Date Raised: Select date using date picker
   - Remarks: [Optional additional notes]
4. Tap "Save" to add matter
5. Repeat for additional matters arising
```

**Step 5: Save Agenda**
```
1. Review all entered information
2. Verify meeting date and location
3. Check matters arising entries
4. Tap "Save Agenda" button
5. Confirmation toast message appears: "Agenda saved successfully"
6. Agenda is now visible to all users assigned to this MRFC
```

#### Editing Existing Agenda

**Procedure:**
```
1. Navigate to Quarter Selection
2. Select quarter with existing agenda
3. System loads saved agenda data
4. Edit meeting date or location as needed
5. Modify matters arising:
   - Tap matter to edit
   - Update fields
   - Tap "Save"
6. To delete matter: Tap delete icon next to matter
7. Tap "Save Agenda" to commit changes
```

**Important Notes:**
- Each MRFC can have one agenda per quarter
- Editing agenda after meeting date will show warning
- All changes are immediately visible to users with access

### 6.2.2 Viewing Meeting Agenda (User)

**Frequency:** Before each meeting and during meeting
**Responsible Party:** All meeting attendees

#### Procedure for Agenda Viewing

**Step 1: Access Agenda**
```
1. From User Dashboard, tap "View Agenda & Documents"
2. If multiple MRFCs: Select specific MRFC from list
3. Select quarter (Q1, Q2, Q3, Q4)
4. System displays agenda details
```

**Step 2: Review Agenda Information**

**Meeting Details Section:**
- Meeting Date: [Date]
- Meeting Location: [Address]

**Standard Agenda Items:**
- Numbered list of 8 standard items
- Displayed in card format
- Cannot be modified by user

**Matters Arising Section:**
- Table format displaying:
  - Issue description
  - Current status (color-coded)
  - Assigned person
  - Date raised
  - Remarks

**Status Color Codes:**
- 🔴 Red: Pending
- 🟡 Yellow: In Progress
- 🟢 Green: Resolved

**Step 3: Offline Access**
```
1. View agenda while online (automatically cached)
2. Agenda remains visible when offline
3. Offline indicator displays at top of screen
4. Updates only sync when back online
```

**Best Practices:**
- Download agenda at least 1 day before meeting
- Take screenshots for reference if unstable internet expected
- Use personal notes feature to annotate agenda items
- Report discrepancies to admin immediately

---

## 6.3 FEEDBACK & NOTES SECTION

### 6.3.1 Creating Personal Notes (User & Admin)

**Frequency:** As needed
**Responsible Party:** All users

#### Procedure for Adding Notes

**Step 1: Access Notes Section**
```
User Portal:
1. From User Dashboard, tap "Manage Personal Notes"

Admin Portal:
1. From Admin Dashboard, open navigation drawer
2. Tap "Personal Notes" (if available)
3. Or access via meeting screens
```

**Step 2: Create New Note**
```
1. Tap floating "+" button (bottom right)
2. Dialog appears with form fields:
   - Note Title: [Enter descriptive title]
   - Note Content: [Enter detailed notes]
   - MRFC: [Select from dropdown]
   - Quarter: [Select Q1/Q2/Q3/Q4]
3. Fill all required fields
4. Tap "Save Note" button
5. Note appears in notes list with timestamp
```

**Step 3: Organize Notes**
```
1. Notes are automatically sorted by date (newest first)
2. Each note displays:
   - Title (bold)
   - Content preview (first 100 characters)
   - MRFC name
   - Quarter
   - Date created
```

### 6.3.2 Editing and Managing Notes

#### Edit Existing Note
```
1. Tap note from notes list
2. Edit dialog opens with pre-filled information
3. Modify title, content, MRFC, or quarter
4. Tap "Save Changes"
5. Confirmation message appears
```

#### Delete Note
```
1. Tap delete icon next to note
2. Confirmation dialog appears: "Delete this note?"
3. Tap "Yes" to confirm deletion
4. Note is permanently removed
```

#### Search Notes
```
1. Tap search icon at top of notes screen
2. Enter search keywords (searches title and content)
3. Results filter in real-time
4. Clear search to view all notes again
```

**Note Storage:**
- Notes are stored locally on device
- Synced to cloud when internet available
- Accessible offline after initial creation
- Private to individual user account

**Best Practices:**
- Use descriptive titles for easy searching
- Link notes to specific MRFC and quarter
- Take notes during meetings for accurate records
- Review notes before subsequent meetings
- Delete outdated notes to reduce clutter

---

## 6.4 DOCUMENT VIEWING & INTERACTION

### 6.4.1 Uploading Documents (Admin Only)

**Frequency:** As required per compliance schedule
**Responsible Party:** Admin, MRFC Coordinator
**Document Categories:**
- MTF Report (Multi-Partite Monitoring Team Report)
- AEPEP (Annual Environmental Protection and Enhancement Program)
- CMVR (Certified Mine Valuation Report)
- SDMP Report (Social Development Management Program)
- Production Report
- Safety Report
- Other Documents

#### Procedure for File Upload

**Step 1: Access File Upload Screen**
```
1. From Admin Dashboard, tap "Documents & Files" in navigation drawer
2. Tap "File Upload"
3. File Upload screen displays with:
   - Category dropdown
   - Quarter selection
   - "Browse Files" button
   - Upload progress area
   - List of uploaded files
```

**Step 2: Select File Category and Quarter**
```
1. Tap "Select Category" dropdown
2. Choose appropriate document category:
   - MTF Report
   - AEPEP
   - CMVR
   - SDMP Report
   - Production Report
   - Safety Report
   - Other Documents
3. Tap "Select Quarter" dropdown
4. Choose quarter: Q1 2025, Q2 2025, Q3 2025, Q4 2025
```

**Step 3: Browse and Select File**
```
1. Tap "Browse Files" button
2. System opens file picker
3. Navigate to document location
4. Supported file types:
   - PDF (.pdf)
   - Microsoft Word (.doc, .docx)
   - Microsoft Excel (.xls, .xlsx)
   - Images (.jpg, .png) - for scanned documents
5. Tap file to select
6. File name displays in selection area
```

**Step 4: Upload File**
```
1. Verify:
   - Correct category selected
   - Correct quarter selected
   - Correct file chosen
2. Tap "Upload File" button
3. Progress bar displays upload status (0-100%)
4. Upload complete message appears
5. File appears in "Uploaded Files" list below
```

**Step 5: Verify Upload**
```
1. Check uploaded files list
2. Verify file entry shows:
   - File name
   - Category badge
   - Quarter badge
   - Upload date
   - File size
   - File type icon
3. Tap file to preview (if supported)
```

#### Managing Uploaded Files

**View Uploaded Files:**
```
1. Scroll to "Uploaded Files" section
2. Files display in card format
3. Newest uploads appear first
4. Each card shows:
   - File icon (PDF, Excel, Word, Image)
   - File name
   - Category (color-coded badge)
   - Quarter badge
   - Upload date
   - File size
   - Delete button (trash icon)
```

**Delete Uploaded File:**
```
1. Locate file in uploaded files list
2. Tap delete icon (trash bin)
3. Confirmation dialog appears: "Delete this file?"
4. Tap "Yes" to confirm
5. File is permanently removed
6. Success message displays
```

**Important Notes:**
- Maximum file size: 25 MB per file
- Files must be work-related documents only
- Ensure proper categorization for easy retrieval
- Files are immediately visible to all users with access
- Deleted files cannot be recovered

### 6.4.2 Viewing and Downloading Documents (User)

**Frequency:** As needed
**Responsible Party:** All users with document access

#### Procedure for Document Access

**Step 1: Navigate to Document List**
```
1. From User Dashboard, tap "View Agenda & Documents"
2. Select MRFC (if multiple assigned)
3. Tap "View Documents" or navigate via proponent view
4. Document List screen displays
```

**Step 2: Browse Documents**
```
1. Documents display in card format
2. Each card shows:
   - File name
   - File type icon (PDF, Excel, Word)
   - Category badge
   - Quarter badge
   - Upload date
   - File size
   - Download button
```

**Step 3: Filter Documents**

**Filter by File Type:**
```
1. Tap "Filter" icon at top of screen
2. Select file type:
   - All Files
   - PDF Files
   - Excel Files
   - Word Documents
3. List updates to show only selected type
```

**Filter by Quarter:**
```
1. Tap "Quarter" dropdown
2. Select specific quarter (Q1, Q2, Q3, Q4) or "All Quarters"
3. List updates to show only documents from selected quarter
```

**Search Documents:**
```
1. Tap search icon at top
2. Enter file name or keywords
3. Results filter in real-time
4. Clear search to view all documents
```

**Step 4: Download Document**
```
1. Locate desired document
2. Tap "Download" button (download icon)
3. Download progress indicator appears
4. On completion: "Document downloaded successfully" message
5. File saved to device Downloads folder
6. Tap notification to open file
```

**Step 5: View Downloaded Document**
```
1. Open device file manager
2. Navigate to Downloads folder
3. Locate downloaded file
4. Tap to open with default app (PDF reader, Word viewer, etc.)
5. If no app available: "No app found to open this file type"
```

#### Offline Document Access

**Procedure:**
```
1. While online: Download required documents
2. Downloaded files remain accessible offline
3. Offline indicator displays if attempting to download without internet
4. Previously downloaded files open normally
5. Upload and new downloads disabled until online
```

**Best Practices:**
- Download critical documents before field visits
- Use filtering to quickly find specific documents
- Verify document category and quarter before downloading
- Delete old downloads to free device storage
- Report missing or incorrect documents to admin

---

## 6.5 ATTENDANCE TRACKING (Manual Entry + Face Capture)

### 6.5.1 Marking Attendance (Admin Only)

**Frequency:** During each MRFC meeting
**Responsible Party:** Meeting Organizer, Admin
**Required Equipment:** Android device with functioning camera

#### Pre-Meeting Preparation

**Step 1: Verify Proponent List**
```
1-2 days before meeting:
1. Navigate to Admin Dashboard > MRFC Management
2. Select relevant MRFC
3. Tap "View Proponents"
4. Verify all active proponents are listed
5. Add missing proponents if necessary
6. Ensure contact information is current
```

**Step 2: Prepare Attendance Materials**
```
On meeting day, before meeting starts:
1. Ensure device is fully charged (or bring power bank)
2. Test camera functionality
3. Clean camera lens for clear photos
4. Verify adequate lighting at meeting venue
5. Ensure sufficient device storage (minimum 100 MB free)
```

#### During-Meeting Attendance Marking

**Step 1: Access Attendance Screen**
```
1. From Admin Dashboard, tap "Attendance Tracking" card
2. Or navigate via drawer: Monitoring & Reports > Attendance Tracking
3. Attendance Tracking screen displays:
   - Photo capture area (top)
   - "Select All" / "Deselect All" buttons
   - List of all proponents with checkboxes
   - Present count
   - Absent count
   - "Save Attendance" button
```

**Step 2: Capture Attendance Photo**
```
1. Tap "Capture Photo" button in photo area
2. Camera preview opens
3. Position camera to capture:
   - Wide shot of meeting room showing attendees
   - Clear view of faces (if possible)
   - Meeting setup and environment
4. Tap capture button (camera icon)
5. Photo preview displays
6. Options:
   - "Retake" - Capture new photo
   - "Use Photo" - Confirm and save photo
7. Tap "Use Photo"
8. Photo thumbnail displays in attendance screen
```

**Alternative: If Camera Unavailable**
```
1. Proceed without photo
2. Manual attendance marking still functional
3. Photo can be added later via edit function
```

**Step 3: Mark Individual Attendance**

**Method 1: Individual Selection**
```
1. Scroll through proponent list
2. Each proponent card displays:
   - Proponent name
   - Company name
   - "Present" checkbox
   - "Absent" checkbox (auto-selected if Present unchecked)
3. For each proponent present:
   - Tap "Present" checkbox
   - Green checkmark appears
   - Present count increments
4. Leave unchecked for absent proponents
```

**Method 2: Bulk Selection**
```
1. If most/all proponents are present:
   - Tap "Select All" button at top
   - All checkboxes marked as Present
   - Present count shows total proponents
2. Uncheck individuals who are absent
3. Absent count updates automatically
```

**Method 3: Bulk Deselection**
```
1. If most proponents are absent:
   - Tap "Deselect All" button
   - All checkboxes unmarked (all Absent)
   - Absent count shows total proponents
2. Check only those who are present
3. Counts update automatically
```

**Step 4: Verify Attendance Counts**
```
1. Check "Present" count at bottom
2. Check "Absent" count at bottom
3. Verify: Present + Absent = Total Proponents
4. Review proponent list for accuracy
5. Make corrections if needed
```

**Step 5: Save Attendance Record**
```
1. Verify all information is correct:
   - Photo captured (if applicable)
   - All present proponents marked
   - Counts are accurate
2. Tap "Save Attendance" button
3. System validates:
   - At least one attendance marking
   - Data integrity check
4. Success message: "Attendance saved successfully"
5. Return to dashboard or continue with meeting tasks
```

#### Post-Meeting Attendance Review

**Procedure:**
```
1. Navigate to Attendance Tracking screen
2. Tap "View Past Attendance" (if available)
3. Select meeting date
4. Review:
   - Attendance photo
   - Present/Absent counts
   - List of attendees
5. Edit if discrepancies found:
   - Tap "Edit Attendance"
   - Update checkboxes
   - Save changes
```

### 6.5.2 Attendance Data Management

#### Generating Attendance Report

**Procedure:**
```
1. Navigate to Compliance Dashboard
2. Select "Attendance Report" option
3. Choose date range or quarter
4. System generates report showing:
   - Meeting dates
   - Attendance percentages by proponent
   - Overall attendance trends
   - Photos (thumbnail gallery)
5. Tap "Export Report" to save or share
```

**Report Contents:**
- Header: MRFC name, quarter, date range
- Summary statistics:
  - Total meetings held
  - Average attendance rate
  - Most/least attended meetings
- Proponent-level data:
  - Name, company
  - Meetings attended / total meetings
  - Attendance percentage
  - Status indicator (color-coded)
- Photo gallery (thumbnails)

#### Attendance Photo Guidelines

**Photo Quality Standards:**
- Minimum resolution: 1280x720 pixels
- Clear, well-lit image
- Faces visible (if possible, for verification)
- Meeting environment recognizable
- No blurry or dark photos

**Privacy Considerations:**
- Photos are internal DENR/MGB use only
- Do not share publicly without authorization
- Store securely on device and server
- Delete old photos per data retention policy

**Best Practices:**
- Take photo at start of meeting (after roll call)
- Capture entire group if possible
- Use landscape orientation for wider coverage
- Retake if photo is unclear
- Add photo timestamp in photo area (auto-generated)
- Include photo caption with meeting details (future feature)

---

## 6.6 MEETING MANAGEMENT (Create Meeting with Voice Recording)

### 6.6.1 Creating New Meeting (Admin Only)

**Frequency:** Quarterly or as required
**Responsible Party:** MRFC Coordinator, Meeting Organizer
**Timeline:** Schedule at least 14 days in advance

#### Procedure for Meeting Creation

**Step 1: Initiate Meeting Creation**
```
1. From Admin Dashboard, tap "Meeting Management"
2. Select quarter for meeting (Q1, Q2, Q3, Q4)
3. Tap "Create New Meeting" button (if not redirected to Agenda Management)
4. Meeting Creation form displays
```

**Step 2: Enter Basic Meeting Information**
```
1. Meeting Title: [Auto-filled] "MRFC Quarterly Meeting - Q[X] 2025"
   - Can be edited for special meetings
2. Meeting Date:
   - Tap date field
   - Select date from calendar
   - Recommended: Schedule 2-4 weeks ahead
3. Meeting Time:
   - Tap time field
   - Select start time (e.g., 9:00 AM)
   - Select end time (e.g., 12:00 PM)
4. Meeting Location:
   - Enter complete address
   - Include room/venue details
   - Example: "Municipal Hall, Conference Room 2, [Municipality Name]"
```

**Step 3: Create Meeting Agenda**
```
(Follow procedures in Section 6.2.1 - Creating Meeting Agenda)
1. Standard 8 agenda items auto-populate
2. Set meeting date and location
3. Add matters arising from previous meeting
4. Save agenda
```

**Step 4: Notify Participants (Future Feature)**
```
1. Select notification recipients:
   - All MRFC members
   - Specific proponents
   - DENR field officers
2. Compose notification message
3. Include:
   - Meeting date, time, location
   - Agenda summary
   - Required documents to bring
4. Send notification
5. Confirmation: "Notification sent to [X] users"
```

### 6.6.2 Voice Recording During Meeting

**Frequency:** During MRFC meetings
**Responsible Party:** Meeting Secretary, Designated Recorder
**Required Permission:** Microphone access

#### Pre-Meeting Audio Setup

**Step 1: Verify Audio Permissions**
```
1-2 days before meeting:
1. Open device Settings > Apps > MGB MRFC Manager
2. Tap "Permissions"
3. Verify "Microphone" is enabled
4. If disabled: Enable microphone access
```

**Step 2: Test Recording Functionality**
```
Before meeting starts:
1. Navigate to Meeting Management screen
2. Tap "Test Audio Recording" (if available)
3. Speak test phrase
4. Playback recording
5. Verify audio quality:
   - Clear voice
   - Minimal background noise
   - Adequate volume
6. If poor quality: Adjust device position or use external microphone
```

**Step 3: Position Recording Device**
```
Best practices for audio capture:
- Place device in center of meeting table
- Ensure microphone is not obstructed
- Distance: Maximum 6-8 feet from speakers
- Avoid placement near:
  - Air conditioning vents
  - Windows with traffic noise
  - Electrical equipment (fans, projectors)
- Test positioning with sample recording
```

#### During-Meeting Recording Procedure

**Step 1: Start Recording**
```
When meeting begins (after call to order):
1. Navigate to Agenda Management screen
2. Locate "Voice Recording" section (top or bottom of screen)
3. Tap "Start Recording" button (microphone icon)
4. Recording indicator appears:
   - Red pulsing dot
   - Timer showing duration (00:00:00)
   - Recording status: "Recording in progress"
5. Device captures audio continuously
6. Place device securely (don't move during recording)
```

**Step 2: Monitor Recording**
```
During meeting:
1. Periodically check recording indicator (still pulsing)
2. Check timer to ensure recording is active
3. Monitor device battery level (minimum 30% recommended)
4. Check available storage (recording requires ~1 MB per minute)
5. If issue detected:
   - Tap "Pause Recording" if needed
   - Resume when ready
```

**Step 3: Pause Recording (Optional)**
```
During breaks or off-record discussions:
1. Tap "Pause Recording" button
2. Timer pauses, pulsing indicator stops
3. Recording status: "Recording paused"
4. When ready to resume:
   - Tap "Resume Recording" button
   - Timer continues from pause point
   - Recording resumes
```

**Step 4: Stop and Save Recording**
```
When meeting concludes (after adjournment):
1. Tap "Stop Recording" button
2. Confirmation dialog appears: "Save recording?"
3. Enter recording details:
   - Recording Name: [Auto-filled] "MRFC Q[X] Meeting - [Date]"
   - Notes: [Optional] Brief description or key topics
4. Tap "Save Recording"
5. System processes and saves audio file
6. Progress indicator shows "Saving recording..."
7. Success message: "Recording saved successfully"
8. Recording duration displayed: "Total time: [HH:MM:SS]"
```

**Step 5: Verify Saved Recording**
```
After saving:
1. Navigate to "Saved Recordings" section
2. Locate newly saved recording
3. Recording card displays:
   - Recording name
   - Date recorded
   - Duration
   - File size
   - Play button
4. Tap "Play" to verify audio quality
5. If unsatisfactory: Delete and use backup recording method
```

#### Post-Meeting Audio Management

**Playing Back Recordings**
```
1. Navigate to Meeting Management or Agenda screen
2. Tap "Saved Recordings"
3. Select recording from list
4. Tap "Play" button
5. Audio player displays:
   - Play/Pause button
   - Progress bar
   - Current time / Total duration
   - Volume control
   - Playback speed (1x, 1.5x, 2x)
6. Tap progress bar to skip to specific time
7. Tap "Close" when finished
```

**Downloading Recording for Archive**
```
1. Locate recording in Saved Recordings list
2. Tap "Download" or "Export" icon
3. Select export format:
   - MP3 (recommended for smaller file size)
   - WAV (for higher quality)
4. Choose save location:
   - Device Downloads folder
   - External storage
   - Cloud storage (if integrated)
5. Tap "Export"
6. Confirmation: "Recording exported to [location]"
```

**Deleting Old Recordings**
```
1. Locate recording to delete
2. Tap delete icon (trash bin)
3. Confirmation dialog: "Delete recording? This cannot be undone."
4. Tap "Yes" to confirm
5. Recording permanently removed
6. Storage space freed
```

**Important Notes:**
- Recordings are large files (approximately 60 MB per hour)
- Regularly export and delete old recordings to free space
- Keep device plugged in during long meetings (recording drains battery)
- Inform meeting participants that recording is in progress
- Follow DENR data retention policy for audio archives

#### Voice Recording Best Practices

**Audio Quality Tips:**
1. Use quiet meeting room with minimal echo
2. Close windows and doors to reduce external noise
3. Silence mobile phones and other devices
4. Speak clearly and project voice toward recording device
5. Avoid shuffling papers directly on table with device
6. Use external microphone for rooms larger than 20 people

**Recording Management:**
1. Label recordings with clear, descriptive names
2. Include date and MRFC name in recording title
3. Export important recordings to backup storage within 7 days
4. Delete recordings older than 1 year (per policy)
5. Do not share recordings externally without authorization

**Troubleshooting Recording Issues:**
| Issue | Solution |
|-------|----------|
| No recording indicator appears | Check microphone permission; restart app |
| Recording stops unexpectedly | Check device storage; check battery level |
| Audio is very quiet | Position device closer; increase device volume |
| Excessive background noise | Reposition device; close doors/windows |
| Recording file too large | Export as MP3; reduce recording quality setting |

---

## 6.7 AUTOMATIC REPORT NAVIGATION (Simple Filtering)

### 6.7.1 Accessing Reports (Admin)

**Frequency:** Monthly or quarterly as required
**Responsible Party:** MRFC Coordinator, MGB Supervisor

#### Procedure for Report Access

**Step 1: Navigate to Compliance Dashboard**
```
1. From Admin Dashboard, tap "Monitoring & Reports" in drawer
2. Select "Compliance Dashboard"
3. Dashboard displays with:
   - Compliance pie chart (top)
   - Summary statistics (middle)
   - Proponent list with percentages (bottom)
   - Filter options (top right)
```

**Step 2: View Compliance Summary**
```
Pie Chart displays:
- Green slice: Compliant proponents (%)
- Yellow slice: Partially Compliant (%)
- Red slice: Non-Compliant (%)

Tap pie chart slices to view:
- Percentage value
- Count of proponents in category
- Category definition
```

**Summary Cards show:**
- Total Proponents: [Count]
- Compliant: [Count] ([%])
- Partially Compliant: [Count] ([%])
- Non-Compliant: [Count] ([%])
- Last Updated: [Date/Time]

**Step 3: Filter Report Data**

**Filter by Quarter:**
```
1. Tap "Quarter" filter dropdown at top
2. Select specific quarter:
   - Q1 2025
   - Q2 2025
   - Q3 2025
   - Q4 2025
   - All Quarters (default)
3. Dashboard updates to show data for selected period
4. Pie chart and statistics recalculate
```

**Filter by MRFC:**
```
1. Tap "MRFC" filter dropdown
2. Select specific MRFC from list
3. Dashboard shows data only for selected MRFC
4. Useful for focused monitoring
```

**Filter by Compliance Status:**
```
1. Tap "Status" filter dropdown
2. Select status to display:
   - All Status (default)
   - Compliant Only
   - Partially Compliant Only
   - Non-Compliant Only
3. Proponent list updates to show only selected status
```

**Filter by Date Range:**
```
1. Tap "Date Range" filter
2. Select "Start Date" from calendar
3. Select "End Date" from calendar
4. Tap "Apply"
5. Dashboard shows compliance data within date range
```

**Step 4: Clear Filters**
```
1. Tap "Clear Filters" button (or refresh icon)
2. All filters reset to default (All Quarters, All MRFCs, All Status)
3. Dashboard displays complete data set
```

### 6.7.2 Viewing Proponent Compliance Details

**Procedure:**
```
1. Scroll to "Proponent Compliance" list (below pie chart)
2. Each proponent card displays:
   - Proponent name
   - Company name
   - Compliance percentage (0-100%)
   - Progress bar (color-coded):
     - Green: 80-100% (Compliant)
     - Yellow: 50-79% (Partially Compliant)
     - Red: 0-49% (Non-Compliant)
   - Status badge
3. Tap proponent card for detailed view:
   - Required documents list
   - Submitted documents (checkmarks)
   - Missing documents (X marks)
   - Submission dates
   - Document categories
4. Tap "View Documents" to access files
5. Tap "Back" to return to list
```

### 6.7.3 Generating and Exporting Reports

#### Generate Compliance Report

**Procedure:**
```
1. From Compliance Dashboard, apply desired filters
2. Tap "Generate Report" button (bottom of screen)
3. Report generation dialog appears
4. Select report type:
   - Summary Report (statistics only)
   - Detailed Report (includes proponent details)
   - Full Report (includes document lists)
5. Select format:
   - PDF (recommended for viewing)
   - Excel (recommended for further analysis)
6. Tap "Generate"
7. Progress indicator: "Generating report..."
8. Completion: "Report generated successfully"
9. Report saved to device Downloads folder
```

#### Export Data for Analysis

**Procedure:**
```
1. From Compliance Dashboard, tap "Export Data" button
2. Select data to export:
   - Compliance statistics
   - Proponent list
   - Document checklist
   - Attendance records (if applicable)
3. Select format:
   - CSV (for Excel/database import)
   - JSON (for system integration)
   - PDF (for presentation)
4. Tap "Export"
5. File saved to Downloads folder
6. Confirmation with file name and location
```

**Exported Data Includes:**
- Report generation date
- Filter parameters applied
- MRFC details
- Proponent list with compliance scores
- Summary statistics
- Document submission status
- Color-coded indicators
- Charts (if PDF format)

### 6.7.4 Report Navigation Features

**Search Functionality:**
```
1. Tap search icon at top of Compliance Dashboard
2. Enter proponent name or company name
3. Results filter in real-time
4. Matching proponents highlighted in list
5. Clear search to view all proponents
```

**Sort Options:**
```
1. Tap "Sort" icon at top of proponent list
2. Select sort criteria:
   - Compliance % (High to Low)
   - Compliance % (Low to High)
   - Proponent Name (A-Z)
   - Company Name (A-Z)
   - Recently Updated
3. List reorders immediately
```

**Refresh Data:**
```
1. Pull down on dashboard screen to refresh
2. Or tap "Refresh" icon at top
3. System syncs with latest data
4. Updated timestamp displays
```

### 6.7.5 Report Best Practices

**Regular Monitoring Schedule:**
- Review compliance dashboard weekly
- Generate monthly summary reports
- Conduct quarterly detailed reviews
- Compare quarter-over-quarter trends

**Data Accuracy:**
- Verify proponent list is current before generating reports
- Ensure all documents are properly uploaded and categorized
- Cross-check manual entries with physical records
- Update compliance percentages after document review

**Report Distribution:**
- Generate reports in PDF for official submission
- Export data to Excel for internal analysis
- Share reports only with authorized DENR/MGB personnel
- Include report generation date and filter parameters in file name

**Archival:**
- Save quarterly reports to designated folder
- Use consistent naming convention: "MRFC_[Name]_Q[X]_2025_Compliance_Report.pdf"
- Back up reports to secure storage
- Maintain archive per data retention policy (minimum 5 years)

---

## 6.8 PERCENTAGE COMPLIANCE / PERFORMANCE INDICATORS

### 6.8.1 Understanding Compliance Calculation

**Compliance Percentage Formula:**
```
Compliance % = (Submitted Required Documents / Total Required Documents) × 100
```

**Document Categories and Requirements:**
Each proponent must submit these documents quarterly:
1. MTF Report (Multi-Partite Monitoring Team Report)
2. AEPEP (Annual Environmental Protection and Enhancement Program)
3. CMVR (Certified Mine Valuation Report)
4. SDMP Report (Social Development Management Program)
5. Production Report
6. Safety Report

**Total Required Documents per Quarter:** 6

**Example Calculation:**
- Proponent submits 5 out of 6 required documents
- Compliance % = (5 / 6) × 100 = 83.33%
- Status: Compliant (above 80% threshold)

**Compliance Status Thresholds:**
- **Compliant:** 80-100% (Green indicator)
- **Partially Compliant:** 50-79% (Yellow indicator)
- **Non-Compliant:** 0-49% (Red indicator)

### 6.8.2 Manual Compliance Review (Admin)

**Frequency:** Monthly or after document submission deadline
**Responsible Party:** MRFC Coordinator, Compliance Officer

#### Procedure for Compliance Assessment

**Step 1: Generate Document Checklist**
```
1. Navigate to Compliance Dashboard
2. Select quarter to review (e.g., Q2 2025)
3. Tap "Detailed View" to see proponent-by-proponent breakdown
4. System displays checklist for each proponent:
   - Document name
   - Required: Yes/No
   - Status: Submitted / Pending / Late
   - Submission date
   - Document link (if submitted)
```

**Step 2: Review Submitted Documents**
```
For each proponent:
1. Tap proponent name to expand details
2. Review document checklist:
   - Green checkmark: Document submitted and accepted
   - Yellow clock: Document submitted, pending review
   - Red X: Document not submitted
3. Tap document name to open and review:
   - Verify content is relevant and complete
   - Check date ranges match reporting period
   - Ensure proper signatures and certifications
4. Mark document as:
   - ✅ Accepted (counts toward compliance)
   - ⚠️ Needs Revision (does not count until resubmitted)
   - ❌ Rejected (does not count)
```

**Step 3: Update Compliance Status**
```
1. After reviewing all documents for a proponent:
2. Tap "Update Compliance" button
3. System recalculates compliance percentage
4. If needed, manually override calculation:
   - Tap "Manual Override" option
   - Enter adjusted percentage with justification
   - Example: "Waived CMVR requirement due to operational suspension"
5. Add review notes in "Compliance Notes" field
6. Tap "Save Changes"
7. Updated compliance % displays in dashboard
```

**Step 4: Flag Non-Compliant Proponents**
```
1. Filter dashboard to show "Non-Compliant Only"
2. Review list of proponents below 50% compliance
3. For each non-compliant proponent:
   - Tap "Send Reminder" button
   - Compose notification:
     - Subject: "Compliance Requirement - Action Required"
     - Body: List missing documents and deadline
   - Tap "Send"
4. Add flag icon to proponent for priority follow-up
5. Set follow-up reminder date
```

### 6.8.3 Performance Indicators Dashboard

**Key Performance Indicators (KPIs) Displayed:**

**1. Overall Compliance Rate**
```
Calculation: (Total Compliant Proponents / Total Active Proponents) × 100
Display: Large percentage at top of dashboard
Target: 85% or higher
Color Coding:
- Green: 85-100% (Meeting target)
- Yellow: 70-84% (Below target)
- Red: Below 70% (Critical)
```

**2. Average Compliance Percentage**
```
Calculation: Sum of all proponent compliance % / Number of proponents
Display: Card below pie chart
Example: "Average Compliance: 76.5%"
```

**3. Document Submission Rate**
```
Calculation: (Total Documents Submitted / Total Documents Required) × 100
Display: Progress bar with percentage
Example: "Document Submission: 142/180 (78.9%)"
```

**4. Trend Indicator**
```
Display: Arrow icon with percentage change from previous quarter
Example: "↑ 5.2% from Q1" (improvement)
Example: "↓ 3.1% from Q1" (decline)
Color: Green for improvement, Red for decline
```

**5. Meeting Attendance Rate**
```
Calculation: (Total Proponents Present / Total Proponents) × 100
Display: Below compliance statistics
Example: "Attendance Rate: 80% (8/10 present at last meeting)"
```

**6. Top Performers**
```
Display: List of top 3 proponents with highest compliance
Shows:
- Proponent name
- Compliance %
- Badge: "Excellent Compliance" (95-100%)
```

**7. At-Risk Proponents**
```
Display: List of proponents below 60% compliance
Shows:
- Proponent name
- Compliance %
- Missing document count
- Alert icon
```

### 6.8.4 Admin Review and Approval Process

**Frequency:** End of each quarter
**Responsible Party:** MGB Regional Director, MRFC Chairperson

#### Quarterly Compliance Review Procedure

**Step 1: Compile Quarterly Data**
```
1. Navigate to Compliance Dashboard
2. Set filter to completed quarter (e.g., Q2 2025)
3. Tap "Refresh" to ensure latest data
4. Verify all proponents are listed
5. Check that all documents are accounted for (submitted or marked pending)
```

**Step 2: Generate Draft Report**
```
1. Tap "Generate Report" button
2. Select "Detailed Report" type
3. Select PDF format
4. Tap "Generate"
5. Review draft report for accuracy:
   - Proponent list complete
   - Percentages calculated correctly
   - Charts display properly
   - Notes and flags are visible
```

**Step 3: Conduct Admin Review Meeting**
```
Agenda:
1. Present compliance dashboard on screen/projector
2. Discuss overall compliance rate and trends
3. Review each non-compliant proponent:
   - Reasons for non-compliance
   - Corrective actions taken
   - Deadline for resubmission
4. Highlight top performers
5. Identify systemic issues (e.g., repeated late submissions of specific document type)
6. Decide on:
   - Approval of current compliance standings
   - Need for follow-up actions
   - Sanctions for non-compliant proponents (per DENR policy)
```

**Step 4: Approve and Finalize Compliance Report**
```
1. After review meeting, return to dashboard
2. Make any final adjustments:
   - Update manual override percentages (if justified)
   - Add admin review notes
   - Attach meeting minutes (if applicable)
3. Tap "Finalize Report" button
4. Confirmation dialog: "This will lock the report. Continue?"
5. Tap "Yes" to finalize
6. Report status changes to "Approved - [Date]"
7. Generate final PDF for official records
8. Distribute report to stakeholders
```

**Step 5: Initiate Follow-Up Actions**
```
For non-compliant proponents:
1. Generate "Notice of Non-Compliance" letters (external to app)
2. Schedule follow-up meetings
3. Set deadlines for document resubmission
4. In app, add follow-up tasks:
   - Tap proponent card
   - Tap "Add Action Item"
   - Enter task: "Resubmit MTF Report by [Date]"
   - Set reminder
   - Assign to responsible officer
   - Save
5. Track action items in next review cycle
```

### 6.8.5 Performance Indicator Best Practices

**Data Integrity:**
- Conduct monthly data quality checks
- Verify document uploads are categorized correctly
- Cross-reference app data with physical records
- Correct errors immediately and document changes

**Objective Assessment:**
- Use consistent criteria for document acceptance
- Document reasons for manual overrides
- Avoid favoritism in compliance scoring
- Maintain audit trail of review decisions

**Continuous Improvement:**
- Analyze trends over multiple quarters
- Identify common compliance challenges
- Provide training to proponents on requirements
- Simplify document submission process based on feedback

**Reporting Standards:**
- Generate reports on consistent schedule (monthly/quarterly)
- Use standard templates for consistency
- Include narrative explanations for significant changes
- Archive all reports for future reference

**Stakeholder Communication:**
- Share compliance summaries with proponents monthly
- Recognize top performers publicly (with permission)
- Provide constructive feedback to underperforming proponents
- Hold quarterly compliance workshops

---

## 6.9 OFFLINE ACCESS (View Only)

### 6.9.1 Understanding Offline Mode

**What is Available Offline:**
✅ Previously viewed MRFC details
✅ Cached proponent information
✅ Downloaded meeting agendas
✅ Personal notes (created while online)
✅ Downloaded documents
✅ Previously viewed compliance data

**What is NOT Available Offline:**
❌ Uploading new documents
❌ Creating new agendas
❌ Marking attendance
❌ Syncing new data
❌ Downloading new documents
❌ Sending notifications
❌ Generating live reports

**Offline Mode Indicator:**
- Banner at top of screen: "You are offline - View only mode"
- Orange/yellow background color
- Sync icon with slash through it

### 6.9.2 Preparing for Offline Use

**Pre-Field Visit Checklist (Admin & User):**

**24-48 Hours Before Field Visit:**
```
1. Connect to stable Wi-Fi network
2. Open MGB MRFC Manager app
3. Navigate to all screens you'll need:
   - Admin: MRFC list, proponent details, agendas, compliance dashboard
   - User: Assigned MRFCs, agendas, document list
4. Allow app to cache all data (keep app open for 5-10 minutes)
5. Download critical documents:
   - Tap each document in Document List
   - Wait for "Download complete" message
   - Verify file saved (check Downloads folder)
6. Create any personal notes you'll need offline
7. Take screenshots of important dashboard screens (backup)
```

**Document Download Priority:**
```
High Priority (Download First):
- Meeting agendas for upcoming meetings
- Compliance reports for site visits
- Proponent production reports
- Safety reports

Medium Priority:
- AEPEP documents
- SDMP reports
- Previous meeting minutes

Low Priority:
- Reference documents
- Historical reports
```

**Verify Offline Readiness:**
```
1. Enable airplane mode on device
2. Open app (should still function)
3. Navigate to key screens:
   - MRFC list loads
   - Proponent details display
   - Agendas visible
   - Downloaded documents open
4. If any screen fails to load: Reconnect to internet, view that screen, then retry offline test
5. Disable airplane mode when testing complete
```

### 6.9.3 Using the App Offline

#### Offline Access for Admin Users

**Viewing MRFC and Proponent Data:**
```
1. Launch app (works offline after initial login)
2. Navigate to MRFC List
3. Previously viewed MRFCs display normally
4. Tap MRFC to view details
5. View proponent list (cached data)
6. Tap proponent to view details
7. All previously cached information is available
```

**Viewing Agendas Offline:**
```
1. Navigate to Quarter Selection
2. Select quarter
3. Agenda displays if previously viewed online
4. Matters arising table visible
5. Meeting date and location shown
6. Standard agenda items list available
```

**Accessing Downloaded Documents:**
```
1. Navigate to File Upload screen or Document List
2. Uploaded files list displays (cached)
3. Tap document to open
4. If previously downloaded: Document opens normally
5. If not downloaded: Error message: "Cannot download while offline"
6. Use device file manager to access Downloads folder as alternative
```

**Offline Limitations for Admin:**
```
Cannot perform:
- Upload new files (button disabled, shows "Offline - Upload unavailable")
- Mark attendance (camera function disabled)
- Create/edit agendas (save button disabled)
- Send notifications
- Generate live compliance reports (can view cached dashboard)
- Sync changes made by other users
```

#### Offline Access for User Portal

**Viewing MRFCs and Proponents:**
```
1. Launch app
2. Navigate to MRFC Selection
3. Assigned MRFCs display (cached)
4. Tap MRFC to view proponent details
5. All previously viewed information available
```

**Viewing Agendas:**
```
1. Navigate to Agenda View
2. Select quarter
3. Agenda displays if previously cached
4. Can read all agenda items and matters arising
5. Meeting details visible
```

**Accessing Personal Notes:**
```
1. Navigate to Personal Notes
2. All notes created while online are available
3. Can read notes
4. Can create new notes (saved locally)
5. New notes sync when back online
```

**Opening Downloaded Documents:**
```
1. Navigate to Document List
2. Previously downloaded documents display
3. Tap document to open
4. Document opens from local storage
5. Cannot download new documents (download button shows "Offline")
```

**Offline Work Capture:**
```
Important: Notes and observations made offline
1. Create personal notes to capture field observations
2. Take device photos (outside app) for reference
3. Make written notes in physical notebook as backup
4. When back online:
   - App syncs personal notes automatically
   - Upload photos via File Upload screen
   - Create formal agenda notes or compliance comments
```

### 6.9.4 Returning Online and Syncing Data

**Automatic Sync Procedure:**
```
When device reconnects to internet:
1. App detects network connection
2. Offline indicator banner disappears
3. Sync icon animates at top of screen
4. Background sync begins automatically:
   - Personal notes upload to server
   - Cached data refreshes with latest from server
   - Notification badge updates
   - Compliance dashboard refreshes
5. Sync completion: "Data synced successfully" toast message
6. All features return to normal operation
```

**Manual Sync:**
```
If automatic sync doesn't trigger:
1. Pull down on main dashboard screen (swipe down gesture)
2. Or tap "Sync Now" in settings (if available)
3. Sync progress indicator displays
4. Wait for completion message
5. Verify latest data is visible (check timestamps)
```

**Conflict Resolution:**
```
If data conflicts detected (rare):
1. App displays "Sync Conflict" dialog
2. Shows conflicting items:
   - Your offline changes
   - Server changes by other users
3. Choose resolution:
   - Keep your changes (override server)
   - Accept server changes (discard your changes)
   - Merge both (if possible)
4. Tap "Resolve" to apply selection
5. Sync completes
```

### 6.9.5 Offline Mode Best Practices

**Before Going Offline:**
- Download all required documents
- View all screens you'll need
- Create templates for notes you'll take
- Charge device fully (offline mode still uses battery)
- Ensure sufficient storage space

**While Offline:**
- Use personal notes feature to capture information
- Take device photos outside app (upload later)
- Don't attempt restricted actions (grayed-out buttons)
- Conserve battery (close other apps)
- Keep app open to prevent cache clearing

**After Returning Online:**
- Allow sync to complete before making new changes
- Verify synced data appears correctly
- Upload any photos or documents collected offline
- Review notification badge for messages received while offline
- Generate any reports needed from fresh data

**Troubleshooting Offline Issues:**
| Issue | Solution |
|-------|----------|
| Screen shows "No data" | Content was not viewed online first; wait until online to access |
| Document won't open | Document was not downloaded; use file manager to check Downloads folder |
| Can't create notes | Check device storage space; restart app |
| Offline indicator won't disappear | Check internet connection; toggle airplane mode; restart app |
| Sync fails after reconnecting | Check internet stability; logout and login again; contact IT support |

---

## 7. SECURITY AND DATA MANAGEMENT

### 7.1 User Authentication and Account Security

#### 7.1.1 Password Policy

**Current Implementation:**
- Username and password authentication (hardcoded for demo)
- Test credentials provided by IT administrator
- No password complexity requirements enforced (demo phase)

**Future Implementation (Production):**
```
Password Requirements:
- Minimum 8 characters
- At least 1 uppercase letter
- At least 1 lowercase letter
- At least 1 number
- At least 1 special character (!@#$%^&*)
- Cannot contain username
- Cannot be one of last 5 passwords used
- Must be changed every 90 days
```

#### 7.1.2 Login Security Best Practices

**For Users:**
1. **Protect Login Credentials**
   - Do not share username and password with anyone
   - Do not write password on paper or sticky notes
   - Use device password manager if available
   - Change password immediately if compromised

2. **Secure Device Access**
   - Enable device screen lock (PIN, pattern, or biometric)
   - Lock device when unattended
   - Do not save password in public or shared devices
   - Report lost or stolen devices immediately to IT

3. **Logout Procedures**
   - Always logout when finished using app
   - Do not stay logged in on shared devices
   - Automatic logout after 30 minutes of inactivity (future feature)

4. **Recognize Security Threats**
   - Be cautious of phishing attempts (fake login requests)
   - Verify app source before installation
   - Do not enter credentials on suspicious screens
   - Report security concerns to IT immediately

#### 7.1.3 Account Lockout and Recovery

**Account Lockout Policy (Future Feature):**
- Account locks after 5 failed login attempts
- Lockout duration: 30 minutes
- Admin can manually unlock user accounts
- Notification sent to user email upon lockout

**Password Reset Procedure (Future Feature):**
```
1. Tap "Forgot Password?" on login screen
2. Enter registered email address
3. Receive password reset link via email
4. Click link (valid for 1 hour)
5. Create new password following policy
6. Confirm new password
7. Login with new credentials
```

### 7.2 Data Privacy and Confidentiality

#### 7.2.1 Data Classification

**Confidential Data (Highest Protection):**
- User credentials (usernames, passwords)
- Proponent financial information
- Compliance scores and reports
- Internal DENR communications
- Unreleased audit findings

**Internal Use Data (Moderate Protection):**
- MRFC meeting agendas
- Proponent company information
- Document metadata
- Attendance records
- Performance indicators

**Public Data (Low Protection):**
- DENR contact information
- General MRFC schedules
- Public compliance reports (after official release)

#### 7.2.2 Data Handling Procedures

**General Principles:**
1. **Need-to-Know Basis:** Access data only as required for job duties
2. **Minimum Necessary:** Request/view only minimum data needed
3. **Confidentiality:** Do not discuss confidential data in public
4. **Secure Communication:** Use official DENR channels for data sharing

**Prohibited Actions:**
- Sharing login credentials with unauthorized persons
- Copying confidential data to personal devices
- Emailing sensitive reports to personal email accounts
- Discussing proponent compliance details publicly
- Sharing attendance photos on social media

**Permitted Actions:**
- Viewing data within assigned role permissions
- Downloading documents for official work purposes
- Sharing reports with authorized DENR personnel
- Discussing compliance in official meetings
- Creating personal work notes (stored securely)

#### 7.2.3 Data Retention and Disposal

**Retention Periods:**
| Data Type | Retention Period |
|-----------|------------------|
| Meeting agendas | 5 years |
| Attendance records | 5 years |
| Compliance reports | 7 years |
| Uploaded documents | 7 years or until permit expires |
| Voice recordings | 3 years |
| Personal notes | Until user deletes (recommend annual review) |
| System logs | 2 years |

**Data Disposal Procedure:**
```
When retention period expires:
1. Admin receives notification: "[Data Type] eligible for deletion"
2. Review data for ongoing relevance
3. If no longer needed:
   - Mark for deletion in system
   - Obtain supervisor approval
   - System permanently deletes after 30-day grace period
4. Deletion log created for audit trail
```

**User Responsibility:**
- Regularly review and delete old personal notes
- Delete downloaded documents from device when no longer needed
- Clear device Downloads folder monthly
- Uninstall app properly if leaving DENR (admin assistance required)

### 7.3 Device Security

#### 7.3.1 Secure Device Configuration

**Minimum Security Requirements:**
```
Required:
- Device screen lock enabled (PIN, pattern, password, or biometric)
- Device encryption enabled (default on Android 7.0+)
- Automatic screen lock after 5 minutes of inactivity
- Unknown sources disabled (except during app installation)
- Device OS updated to latest security patches

Recommended:
- Biometric authentication (fingerprint or face unlock)
- Two-factor authentication (future feature)
- Remote wipe capability enabled
- Find My Device enabled (Google/Samsung)
- Full device backup to secure cloud storage
```

**Device Security Checklist:**
```
☑ Screen lock enabled
☑ Automatic lock set to 5 minutes or less
☑ App permissions reviewed and appropriate
☑ Device location services enabled (for find my device)
☑ Google Play Protect enabled (for malware scanning)
☑ Bluetooth disabled when not in use
☑ Wi-Fi set to "Ask to join networks" (not automatic)
☑ USB debugging disabled
```

#### 7.3.2 Lost or Stolen Device Protocol

**Immediate Actions (Within 1 hour):**
```
1. Attempt to call/locate device using Find My Device
2. If not found within 15 minutes:
   - Contact IT support immediately: [IT Support Phone]
   - Report: User name, device model, last known location
   - IT will remotely lock user account to prevent unauthorized access
3. If device contains highly sensitive data:
   - IT initiates remote wipe (erases all data)
4. File incident report with DENR security office
5. IT issues temporary credentials for replacement device
```

**After Device Recovery or Replacement:**
```
If device found:
1. Verify device has not been tampered with
2. Run security scan using IT support
3. Change password immediately
4. IT re-enables account access
5. Review recent app activity for suspicious behavior

If device not recovered:
1. IT provides replacement device (if DENR-issued)
2. Install MGB MRFC Manager on new device
3. Login with new credentials (provided by IT)
4. Re-download required documents
5. Old device access remains permanently blocked
```

### 7.4 Data Backup and Recovery

#### 7.4.1 Automatic Backup (Server-Side)

**Backend System (Future Implementation):**
- All data automatically backed up to MGB server daily
- Incremental backups every 6 hours
- Full backups weekly
- Backup retention: 90 days rolling
- Offsite backup to disaster recovery site
- Encrypted backup storage

**User Data Backup:**
- Personal notes sync to server when online
- Compliance data backed up automatically
- Documents stored redundantly on server
- Meeting agendas backed up after each save

#### 7.4.2 Local Device Backup

**User Responsibility:**
```
Important downloaded documents:
1. Periodically back up device to Google Drive or Samsung Cloud
2. Device backup includes:
   - Downloaded documents (in Downloads folder)
   - App data (partial)
   - Device photos (attendance photos outside app)
3. Set backup to occur automatically on Wi-Fi
4. Verify backup completed successfully monthly
```

**Manual Backup Procedure:**
```
1. Connect device to Wi-Fi
2. Open device Settings > Backup & Reset
3. Enable "Back up my data"
4. Select backup account (Google/Samsung)
5. Tap "Back up now"
6. Wait for completion (may take 10-30 minutes)
7. Verify: Settings > Backup > View backup details
```

#### 7.4.3 Data Recovery Procedures

**Recovering from Device Failure:**
```
1. Install MGB MRFC Manager on new device
2. Login with existing credentials
3. App syncs data from server automatically:
   - Personal notes restored
   - MRFC/proponent list reloaded
   - Agendas downloaded
4. Re-download required documents manually
5. Device-only data (photos outside app) must be restored from device backup
```

**Recovering Deleted Data:**
```
Within 30 days of deletion:
1. Contact IT support immediately
2. Provide: Username, data type, approximate deletion date
3. IT searches backup archives
4. If found: IT restores data to user account
5. User verifies restored data is correct

After 30 days:
- Data may be permanently deleted from backups
- Recovery not guaranteed
- Emphasizes importance of careful deletion
```

### 7.5 Incident Reporting

#### 7.5.1 Security Incident Types

**Report Immediately:**
- Suspected unauthorized access to account
- Lost or stolen device containing app data
- Data breach (accidental sharing of confidential info)
- Malware or virus detected on device
- Suspicious app behavior or error messages
- Phishing attempts or suspicious emails
- Password compromise
- Unintended document upload or deletion

**Report Within 24 Hours:**
- App crashes or technical errors preventing work
- Sync failures or data inconsistencies
- Permission errors (cannot access expected data)
- Unusual performance issues

#### 7.5.2 Incident Reporting Procedure

**Step-by-Step:**
```
1. Stop using app immediately if security incident suspected
2. Do not attempt to "fix" the problem yourself
3. Contact IT Support:
   - Phone: [IT Support Phone Number]
   - Email: [IT Support Email]
   - In-person: [Office Location]
4. Provide the following information:
   - Your name and position
   - Device model and Android version
   - Date and time of incident
   - Description of what happened
   - Actions you took
   - Screenshot of error message (if applicable)
5. Do not share incident details with unauthorized persons
6. Follow IT instructions for remediation
7. Document incident in DENR incident log (if required)
8. IT will investigate and provide resolution
```

**Incident Reporting Template:**
```
Subject: MGB MRFC Manager Security Incident - [Your Name]

Date/Time of Incident: [Date] at [Time]
User: [Your Full Name]
Position: [Your Job Title]
Device: [Brand and Model]
Android Version: [e.g., Android 12]

Description of Incident:
[Detailed description of what occurred]

Actions Taken:
[What you did in response]

Impact:
[Effect on work or data]

Screenshot Attached: Yes/No

Contact Number: [Your Phone]
```

---

## 8. TROUBLESHOOTING AND SUPPORT

### 8.1 Common Issues and Solutions

#### 8.1.1 Login and Authentication Issues

**Issue 1: "Invalid username or password" error**

**Possible Causes:**
- Incorrect credentials entered
- Caps Lock enabled
- Account locked due to failed attempts
- Account not yet created in system

**Solutions:**
```
1. Verify credentials:
   - Check username spelling (case-sensitive)
   - Verify password (case-sensitive)
   - Ensure no extra spaces
2. Check keyboard:
   - Disable Caps Lock
   - Switch to English keyboard if using multilingual
3. Wait 30 minutes if account locked
4. Contact IT if password forgotten
5. Verify account creation with IT admin
```

**Issue 2: App closes immediately after login**

**Possible Causes:**
- Insufficient device storage
- App cache corruption
- Incompatible Android version
- App crash due to backend error

**Solutions:**
```
1. Check device storage:
   - Settings > Storage
   - Ensure at least 500 MB free
   - Delete unnecessary files if needed
2. Clear app cache:
   - Settings > Apps > MGB MRFC Manager
   - Tap "Storage"
   - Tap "Clear Cache" (not Clear Data)
3. Restart device
4. Reinstall app if problem persists
5. Contact IT support if issue continues
```

#### 8.1.2 Data Loading and Sync Issues

**Issue 3: Data not loading / Endless loading spinner**

**Possible Causes:**
- No internet connection
- Slow internet speed
- Server downtime
- Data not cached for offline use

**Solutions:**
```
1. Check internet connection:
   - Open web browser, test connection
   - Try different network (switch to mobile data or Wi-Fi)
2. Verify app permissions:
   - Settings > Apps > MGB MRFC Manager > Permissions
   - Ensure "Internet" permission granted (usually automatic)
3. Force sync:
   - Pull down on screen to refresh
   - Or tap refresh icon if available
4. Close and reopen app
5. If offline: Switch to online mode to load data
6. Check DENR IT status page for server status
7. Contact IT if server appears down
```

**Issue 4: "Sync failed" error after going back online**

**Possible Causes:**
- Server unreachable
- Data conflicts
- Authentication token expired
- Insufficient server storage

**Solutions:**
```
1. Check internet connection stability
2. Logout and login again to refresh authentication
3. Try manual sync:
   - Pull down on dashboard
   - Wait for sync completion
4. If conflict dialog appears:
   - Review conflicting changes
   - Choose appropriate resolution
5. Clear app cache and retry
6. Contact IT if error persists
```

#### 8.1.3 Document and File Issues

**Issue 5: Cannot upload file / "Upload failed" error**

**Possible Causes:**
- File size exceeds limit (25 MB)
- Unsupported file type
- No internet connection
- Insufficient server storage
- Missing storage permission

**Solutions:**
```
1. Check file size:
   - Files > Find file > Check file size
   - If over 25 MB: Compress file or split into parts
2. Verify file type:
   - Supported: PDF, DOC, DOCX, XLS, XLSX, JPG, PNG
   - Convert unsupported types using online converter
3. Check internet connection:
   - Must be online to upload
   - Use stable Wi-Fi for large files
4. Verify storage permission:
   - Settings > Apps > MGB MRFC Manager > Permissions
   - Enable "Storage" or "Files and Media"
5. Try different file
6. Restart app and retry
7. Contact IT if all files fail to upload
```

**Issue 6: Downloaded document won't open**

**Possible Causes:**
- No app installed to open file type
- Corrupted download
- File damaged before upload
- Insufficient device storage

**Solutions:**
```
1. Install appropriate viewer app:
   - PDF: Adobe Acrobat Reader, Google PDF Viewer
   - Word: Microsoft Word, Google Docs
   - Excel: Microsoft Excel, Google Sheets
2. Re-download file:
   - Delete previous download
   - Download again from Document List
3. Try opening from file manager:
   - Open device file manager
   - Navigate to Downloads
   - Tap file to open
4. Verify file is not corrupted:
   - Check file size (should not be 0 KB)
5. Contact admin to re-upload document if damaged
```

#### 8.1.4 Camera and Attendance Issues

**Issue 7: Camera not working for attendance photos**

**Possible Causes:**
- Camera permission denied
- Camera in use by another app
- Hardware camera issue
- Insufficient storage for photo

**Solutions:**
```
1. Grant camera permission:
   - Settings > Apps > MGB MRFC Manager > Permissions
   - Enable "Camera"
2. Close other camera apps:
   - Recent apps button
   - Close all camera-related apps
3. Restart device to release camera
4. Check available storage (need ~5 MB per photo)
5. Test camera with default camera app:
   - If camera doesn't work there either: Hardware issue
6. Skip photo if urgent, add later via edit function
7. Contact IT support if camera permission issue persists
```

**Issue 8: Attendance not saving / "Save failed" error**

**Possible Causes:**
- No internet connection (attendance requires sync)
- No proponent marked as present or absent
- Photo capture failed
- Server timeout

**Solutions:**
```
1. Verify internet connection (attendance save requires sync)
2. Ensure at least one checkbox marked
3. Re-capture photo if it appears as gray box
4. Try saving without photo (photo optional in emergency)
5. Take screenshot of attendance list as backup
6. Retry save after checking connection
7. Contact IT if issue persists
```

#### 8.1.5 Performance and App Behavior Issues

**Issue 9: App is slow / Laggy performance**

**Possible Causes:**
- Low device memory (RAM)
- Too many apps running
- Large cache size
- Old device with insufficient specs
- Poor internet connection

**Solutions:**
```
1. Close background apps:
   - Recent apps button
   - Swipe away unused apps
2. Clear app cache:
   - Settings > Apps > MGB MRFC Manager > Storage > Clear Cache
3. Restart device to free memory
4. Check device storage:
   - Delete unnecessary files
   - Move photos/videos to cloud storage
5. Uninstall unused apps to free resources
6. Use Wi-Fi instead of mobile data if slow
7. Consider device upgrade if specs below minimum
```

**Issue 10: App crashes frequently**

**Possible Causes:**
- App bug or compatibility issue
- Corrupted app data
- Device OS issue
- Conflicting app

**Solutions:**
```
1. Update to latest app version (if available)
2. Clear app cache (see Issue 9)
3. Update device Android OS:
   - Settings > System > System Update
4. Uninstall recently installed apps (may conflict)
5. Reinstall MGB MRFC Manager:
   - Uninstall app (data may be lost locally)
   - Reinstall from APK
   - Login to restore synced data
6. Report crash details to IT support:
   - What action triggered crash
   - Frequency of crashes
   - Error message (if any)
```

### 8.2 Error Messages and Meanings

| Error Message | Meaning | Action |
|---------------|---------|--------|
| "Invalid username or password" | Login credentials incorrect | Verify credentials; contact IT if forgotten |
| "No internet connection" | Device not connected to network | Check Wi-Fi/mobile data; reconnect |
| "Sync failed" | Data synchronization error | Check connection; retry; logout/login |
| "Upload failed" | File upload unsuccessful | Check file size/type; verify connection |
| "Permission denied" | App lacks required permission | Grant permission in device settings |
| "File too large" | File exceeds 25 MB limit | Compress file or split into parts |
| "Storage full" | Device storage insufficient | Free up space; delete old files |
| "Camera unavailable" | Camera permission denied or in use | Grant permission; close other camera apps |
| "Account locked" | Too many failed login attempts | Wait 30 minutes; contact IT to unlock |
| "Server error" | Backend server issue | Retry later; check server status; contact IT |
| "Session expired" | Authentication timeout | Logout and login again |
| "Unsupported file type" | File format not allowed | Convert to PDF, DOC, XLS, or image |

### 8.3 IT Support Contact Information

**MGB IT Support Desk**

**Phone Support:**
- Main Line: [Phone Number]
- Mobile/SMS: [Mobile Number]
- Operating Hours: Monday-Friday, 8:00 AM - 5:00 PM

**Email Support:**
- Primary: [IT Support Email]
- Emergency: [Emergency IT Email]
- Response Time: Within 24 hours (business days)

**In-Person Support:**
- Location: [MGB Office Address, Room Number]
- Walk-in Hours: Monday-Friday, 9:00 AM - 4:00 PM
- Appointment: Recommended for complex issues

**Online Support:**
- Help Desk Portal: [URL if available]
- Knowledge Base: [URL if available]
- User Manual: [URL or file location]

**Emergency Contact (After Hours):**
- Critical security incidents only
- Emergency Hotline: [24/7 Emergency Number]
- Email: [Emergency Email]

**When Contacting Support, Provide:**
1. Your full name and position
2. Username (do not provide password)
3. Device brand and model
4. Android version
5. Description of issue
6. Steps to reproduce problem
7. Error message (screenshot if possible)
8. When issue first occurred
9. Actions already attempted

---

## 9. APPENDICES

### Appendix A: Quick Reference Guide

**ADMIN QUICK ACTIONS:**
| Task | Navigation Path |
|------|-----------------|
| View MRFCs | Dashboard > MRFC Management |
| Add Proponent | MRFC List > Select MRFC > Proponents > + Button |
| Create Agenda | Dashboard > Quarter Selection > Select Quarter |
| Mark Attendance | Dashboard > Attendance Tracking |
| Upload Document | Dashboard > Navigation Drawer > File Upload |
| View Compliance | Dashboard > Compliance Dashboard |
| Check Notifications | Dashboard > Notification Icon (top right) |

**USER QUICK ACTIONS:**
| Task | Navigation Path |
|------|-----------------|
| View Agenda | Dashboard > View Agenda & Documents > Select MRFC > Select Quarter |
| Download Document | Dashboard > View Agenda & Documents > View Documents > Download |
| Add Note | Dashboard > Manage Personal Notes > + Button |
| View Proponent | Dashboard > View MRFC & Proponents > Select MRFC > Select Proponent |

### Appendix B: Glossary of Terms

| Term | Definition |
|------|------------|
| **AEPEP** | Annual Environmental Protection and Enhancement Program - required document detailing environmental compliance plans |
| **Agenda** | List of topics to be discussed in MRFC meeting, following standard 8-item format |
| **Attendance Tracking** | Process of recording which proponents attended meeting, including photo documentation |
| **CMVR** | Certified Mine Valuation Report - document assessing mining operation value |
| **Compliance Percentage** | Calculated score (0-100%) indicating proponent's submission of required documents |
| **DENR** | Department of Environment and Natural Resources - parent agency of MGB |
| **Matter Arising** | Unresolved issue from previous meeting requiring follow-up |
| **MGB** | Mines and Geosciences Bureau - government agency overseeing mining operations |
| **MRFC** | Municipal Resource and Finance Committee - local body monitoring mining compliance |
| **MTF Report** | Multi-Partite Monitoring Team Report - collaborative monitoring document |
| **Offline Mode** | App state when no internet connection; allows viewing cached data only |
| **Proponent** | Mining or quarrying company/permit holder subject to MRFC oversight |
| **Quarter** | Three-month period (Q1: Jan-Mar, Q2: Apr-Jun, Q3: Jul-Sep, Q4: Oct-Dec) |
| **SDMP** | Social Development Management Program - community engagement and development plan |
| **Sync** | Process of uploading local changes to server and downloading updates from server |

### Appendix C: Document Checklist Template

**Quarterly Document Requirements per Proponent:**

| Document Category | Q1 | Q2 | Q3 | Q4 | Notes |
|-------------------|----|----|----|----|-------|
| MTF Report | ☐ | ☐ | ☐ | ☐ | Due 15th of following month |
| AEPEP | ☐ | ☐ | ☐ | ☐ | Annual submission in Q1 |
| CMVR | ☐ | ☐ | ☐ | ☐ | Annual submission in Q4 |
| SDMP Report | ☐ | ☐ | ☐ | ☐ | Due 15th of following month |
| Production Report | ☐ | ☐ | ☐ | ☐ | Due 15th of following month |
| Safety Report | ☐ | ☐ | ☐ | ☐ | Due 15th of following month |

**Compliance Calculation:**
- Documents Submitted: _____ / 6
- Compliance Percentage: _____ %
- Status: Compliant / Partially Compliant / Non-Compliant

### Appendix D: Meeting Agenda Template

**[MRFC Name] Quarterly Meeting**
**Quarter [X], Year [YYYY]**

**Date:** [Meeting Date]
**Time:** [Start Time] - [End Time]
**Location:** [Complete Address]

**STANDARD AGENDA:**

1. **Call to Order**
2. **Opening Prayer**
3. **Roll Call**
   - Total Proponents: _____
   - Present: _____
   - Absent: _____
4. **Reading and Approval of Previous Minutes**
5. **Matters Arising from Previous Meeting**
   - [List matters arising with status]
6. **Reports from Proponents**
   - [Each proponent presents compliance reports]
7. **Other Matters**
   - [Additional topics for discussion]
8. **Adjournment**

**MATTERS ARISING TABLE:**

| Issue | Status | Assigned To | Date Raised | Remarks |
|-------|--------|-------------|-------------|---------|
| | | | | |
| | | | | |

**ATTENDANCE RECORD:**
- Attached: Attendance photo
- Present Proponents: [List names]
- Absent Proponents: [List names]

### Appendix E: Compliance Status Definitions

**Compliant (80-100%)**
- Definition: Proponent has submitted at least 5 of 6 required quarterly documents
- Indicator: Green badge/progress bar
- Action: Recognition; maintain monitoring
- Reporting: Include in "Top Performers" section

**Partially Compliant (50-79%)**
- Definition: Proponent has submitted 3-4 of 6 required quarterly documents
- Indicator: Yellow badge/progress bar
- Action: Send reminder notification; schedule follow-up; identify barriers
- Reporting: Flag for increased monitoring

**Non-Compliant (0-49%)**
- Definition: Proponent has submitted 2 or fewer of 6 required quarterly documents
- Indicator: Red badge/progress bar
- Action: Issue formal notice; require corrective action plan; escalate to management; possible sanctions per DENR policy
- Reporting: Mandatory reporting to MGB regional director

### Appendix F: Revision History

| Version | Date | Author | Changes |
|---------|------|--------|---------|
| 1.0 | October 16, 2025 | MGB IT Development Team | Initial SOP release |

---

## END OF STANDARD OPERATING PROCEDURES

**For questions, clarifications, or suggestions regarding this SOP, please contact:**

**MGB IT Development Team**
Email: [IT Email]
Phone: [IT Phone]

**Document Control:**
**Classification:** Internal Use Only
**Distribution:** All DENR/MGB personnel with access to MGB MRFC Manager
**Review Cycle:** Annual (next review due October 2026)
**Approval:** [Name, Title] - MGB Regional Director

---

*This SOP is a living document and will be updated as the application evolves and new features are implemented.*
