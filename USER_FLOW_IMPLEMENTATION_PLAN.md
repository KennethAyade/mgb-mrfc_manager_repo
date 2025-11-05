# 📋 USER FLOW IMPLEMENTATION PLAN
## Complete System Alignment with Flowchart
**Project:** MGB MRFC Manager - User Portal
**Date Created:** November 4, 2025
**Status:** READY FOR IMPLEMENTATION
**Estimated Time:** 4-5 weeks (160-200 hours)

---

## 🎯 EXECUTIVE SUMMARY

### Current Situation
- **Flowchart Match:** 60% aligned
- **Backend Coverage:** 44% working (8/18 user endpoints)
- **Frontend Coverage:** 85% complete (missing 3 screens)
- **Critical Blockers:** 10 backend endpoints returning HTTP 501

### Implementation Goals
1. ✅ Achieve 100% flowchart alignment
2. ✅ Complete all backend user endpoints
3. ✅ Add missing frontend screens
4. ✅ Fix user-specific MRFC filtering
5. ✅ Resolve design conflicts

### Success Metrics
- All user flow paths functional
- Zero 501 errors on user endpoints
- All services accessible from both MRFC and Meeting paths
- User sees only assigned MRFCs

---

## 📊 IMPLEMENTATION PHASES

```
PHASE 1: Backend Foundation (Week 1)        ████████░░ 40 hours
PHASE 2: User Services Integration (Week 2) ████████░░ 40 hours  
PHASE 3: Frontend Completion (Week 3)       ██████░░░░ 30 hours
PHASE 4: Navigation & Polish (Week 4)       ████░░░░░░ 20 hours
PHASE 5: Testing & Deployment (Week 5)      ██████░░░░ 30 hours
───────────────────────────────────────────────────────────────
TOTAL ESTIMATED TIME:                        160 hours (4 weeks)
```

---

## 🔴 PHASE 1: BACKEND FOUNDATION (Week 1 - 40 hours)

### Priority: CRITICAL
**Goal:** Implement all missing backend endpoints for user role

### Task 1.1: Proponent Endpoints Implementation
**Time:** 8 hours | **Complexity:** Medium | **Priority:** CRITICAL

#### Backend Work (`backend/src/controllers/proponent.controller.ts`)

```typescript
// Current Status: Returns HTTP 501
// Required: Implement full CRUD with user access control

/**
 * Implementation Checklist:
 */

// 1. List Proponents (filtered by MRFC)
export const listProponents = async (req: Request, res: Response) => {
  try {
    const { mrfc_id, status, search } = req.query;
    const page = parseInt(req.query.page as string) || 1;
    const limit = parseInt(req.query.limit as string) || 20;

    // Build where clause
    const whereClause: any = {};
    
    if (mrfc_id) {
      whereClause.mrfc_id = mrfc_id;
    }
    
    if (status) {
      whereClause.status = status;
    }
    
    if (search) {
      whereClause[Op.or] = [
        { full_name: { [Op.iLike]: `%${search}%` } },
        { company_name: { [Op.iLike]: `%${search}%` } }
      ];
    }

    // For USER role: filter by user's mrfcAccess
    if (req.user?.role === 'USER') {
      const userMrfcAccess = req.user.mrfcAccess || [];
      whereClause.mrfc_id = {
        [Op.in]: userMrfcAccess
      };
    }

    const { count, rows } = await Proponent.findAndCountAll({
      where: whereClause,
      include: [
        { model: Mrfc, as: 'mrfc', attributes: ['id', 'name', 'municipality'] }
      ],
      limit,
      offset: (page - 1) * limit,
      order: [['full_name', 'ASC']]
    });

    res.json({
      success: true,
      data: {
        proponents: rows,
        pagination: {
          current_page: page,
          total_pages: Math.ceil(count / limit),
          total_items: count,
          items_per_page: limit
        }
      }
    });
  } catch (error: any) {
    res.status(500).json({
      success: false,
      error: { code: 'LIST_PROPONENTS_FAILED', message: error.message }
    });
  }
};

// 2. Get Proponent by ID
export const getProponentById = async (req: Request, res: Response) => {
  try {
    const { id } = req.params;

    const proponent = await Proponent.findByPk(id, {
      include: [
        { model: Mrfc, as: 'mrfc' },
        { model: Document, as: 'documents', limit: 10 }
      ]
    });

    if (!proponent) {
      return res.status(404).json({
        success: false,
        error: { code: 'PROPONENT_NOT_FOUND', message: 'Proponent not found' }
      });
    }

    // USER role: check MRFC access
    if (req.user?.role === 'USER') {
      const userMrfcAccess = req.user.mrfcAccess || [];
      if (!userMrfcAccess.includes(proponent.mrfc_id)) {
        return res.status(403).json({
          success: false,
          error: { code: 'ACCESS_DENIED', message: 'No access to this proponent' }
        });
      }
    }

    res.json({ success: true, data: proponent });
  } catch (error: any) {
    res.status(500).json({
      success: false,
      error: { code: 'GET_PROPONENT_FAILED', message: error.message }
    });
  }
};

// 3. Create Proponent (ADMIN only)
export const createProponent = async (req: Request, res: Response) => {
  // Implementation here...
};

// 4. Update Proponent (ADMIN only)
export const updateProponent = async (req: Request, res: Response) => {
  // Implementation here...
};

// 5. Delete Proponent (ADMIN only)
export const deleteProponent = async (req: Request, res: Response) => {
  // Implementation here...
};
```

#### Wire Routes (`backend/src/routes/proponent.routes.ts`)

```typescript
// Replace TODO placeholders with controller functions
import { authenticate, adminOnly } from '../middleware/auth';
import * as proponentController from '../controllers/proponent.controller';

router.get('/proponents', authenticate, proponentController.listProponents);
router.get('/proponents/:id', authenticate, proponentController.getProponentById);
router.post('/proponents', authenticate, adminOnly, proponentController.createProponent);
router.put('/proponents/:id', authenticate, adminOnly, proponentController.updateProponent);
router.delete('/proponents/:id', authenticate, adminOnly, proponentController.deleteProponent);
```

#### Testing Checklist
- [ ] Test list proponents with ADMIN (sees all)
- [ ] Test list proponents with USER (sees only assigned)
- [ ] Test get proponent with valid access
- [ ] Test get proponent with invalid access (403)
- [ ] Test filter by MRFC ID
- [ ] Test search by name
- [ ] Test pagination

---

### Task 1.2: Document Endpoints Implementation
**Time:** 12 hours | **Complexity:** High | **Priority:** CRITICAL

**Status:** ✅ **ALREADY DONE!** (Completed Nov 2, 2025)

Document controller was completely rewritten with:
- ✅ List documents with filters
- ✅ Upload with Cloudinary
- ✅ Download via URL
- ✅ Approval workflow
- ✅ Token upload system

**Action Required:** Test and verify all endpoints work for USER role

#### Testing Checklist
- [ ] Test GET /documents?proponent_id=1&quarter_id=2
- [ ] Test GET /documents/:id with user access
- [ ] Test GET /documents/:id/download
- [ ] Test category filtering (MTF_REPORT, AEPEP, CMVR)
- [ ] Test status filtering (PENDING, ACCEPTED, REJECTED)
- [ ] Verify USER can only see documents from assigned MRFCs

---

### Task 1.3: Attendance Endpoints Implementation
**Time:** 8 hours | **Complexity:** Medium | **Priority:** HIGH

#### Backend Work (`backend/src/controllers/attendance.controller.ts`)

```typescript
/**
 * Implementation Requirements:
 * - Users can log their own attendance
 * - Support general attendees (not just proponents)
 * - Photo upload to Cloudinary
 * - Real-time attendance list
 */

// 1. List Attendance for Meeting
export const listAttendance = async (req: Request, res: Response) => {
  try {
    const { agenda_id } = req.query;

    if (!agenda_id) {
      return res.status(400).json({
        success: false,
        error: { code: 'AGENDA_ID_REQUIRED', message: 'agenda_id is required' }
      });
    }

    const attendanceRecords = await Attendance.findAll({
      where: { agenda_id },
      include: [
        { model: Proponent, as: 'proponent', required: false },
        { model: User, as: 'user', attributes: ['id', 'full_name'] }
      ],
      order: [['time_in', 'DESC']]
    });

    res.json({ success: true, data: attendanceRecords });
  } catch (error: any) {
    res.status(500).json({
      success: false,
      error: { code: 'LIST_ATTENDANCE_FAILED', message: error.message }
    });
  }
};

// 2. Create Attendance Record with Photo
export const createAttendanceWithPhoto = async (req: Request, res: Response) => {
  try {
    const { agenda_id, proponent_id, attendee_name, attendee_position, attendee_department } = req.body;
    const photoFile = req.file;

    // Upload photo to Cloudinary if provided
    let photoUrl = null;
    let photoCloudinaryId = null;

    if (photoFile) {
      const uploadResult = await uploadToCloudinary(photoFile.path, 'attendance_photos');
      photoUrl = uploadResult.secure_url;
      photoCloudinaryId = uploadResult.public_id;
    }

    // Create attendance record
    const attendance = await Attendance.create({
      agenda_id,
      proponent_id: proponent_id || null,
      user_id: req.user?.id,
      attendee_name: attendee_name || null,
      attendee_position: attendee_position || null,
      attendee_department: attendee_department || null,
      time_in: new Date(),
      present: true,
      photo_url: photoUrl,
      photo_cloudinary_id: photoCloudinaryId
    });

    // Create audit log
    await createAuditLog({
      user_id: req.user?.id,
      action: 'CREATE_ATTENDANCE',
      table_name: 'attendance',
      record_id: attendance.id,
      new_values: attendance
    });

    res.status(201).json({ success: true, data: attendance });
  } catch (error: any) {
    res.status(500).json({
      success: false,
      error: { code: 'CREATE_ATTENDANCE_FAILED', message: error.message }
    });
  }
};

// 3. Update Attendance (e.g., set time_out)
export const updateAttendance = async (req: Request, res: Response) => {
  // Implementation here...
};
```

#### Wire Routes (`backend/src/routes/attendance.routes.ts`)

```typescript
import { authenticate } from '../middleware/auth';
import { uploadMiddleware } from '../middleware/upload';
import * as attendanceController from '../controllers/attendance.controller';

router.get('/attendance', authenticate, attendanceController.listAttendance);
router.post('/attendance/with-photo', authenticate, uploadMiddleware.single('photo'), attendanceController.createAttendanceWithPhoto);
router.put('/attendance/:id', authenticate, attendanceController.updateAttendance);
```

#### Testing Checklist
- [ ] Test list attendance for meeting
- [ ] Test create attendance without photo
- [ ] Test create attendance with photo upload
- [ ] Test attendance for general attendee (no proponent_id)
- [ ] Test attendance for proponent
- [ ] Verify photo uploaded to Cloudinary
- [ ] Test USER can only log attendance for accessible meetings

---

### Task 1.4: Agenda Item Endpoints Implementation
**Time:** 6 hours | **Complexity:** Medium | **Priority:** HIGH

**Status:** ⚠️ **PARTIALLY DONE** (Routes exist, controllers need completion)

#### Backend Work (`backend/src/controllers/agendaItem.controller.ts`)

```typescript
// 1. List Agenda Items for Meeting
export const listAgendaItems = async (req: Request, res: Response) => {
  try {
    const { agenda_id } = req.params;

    const agendaItems = await AgendaItem.findAll({
      where: { agenda_id },
      order: [['display_order', 'ASC']]
    });

    res.json({ success: true, data: agendaItems });
  } catch (error: any) {
    res.status(500).json({
      success: false,
      error: { code: 'LIST_AGENDA_ITEMS_FAILED', message: error.message }
    });
  }
};

// 2. Create Agenda Item (ADMIN only)
export const createAgendaItem = async (req: Request, res: Response) => {
  // Implementation...
};

// 3. Update Agenda Item (ADMIN only)
export const updateAgendaItem = async (req: Request, res: Response) => {
  // Implementation...
};

// 4. Reorder Agenda Items (ADMIN only)
export const reorderAgendaItems = async (req: Request, res: Response) => {
  // Implementation...
};

// 5. Delete Agenda Item (ADMIN only)
export const deleteAgendaItem = async (req: Request, res: Response) => {
  // Implementation...
};
```

#### Wire Routes (`backend/src/routes/agendaItem.routes.ts`)

```typescript
import { authenticate, adminOnly } from '../middleware/auth';
import * as agendaItemController from '../controllers/agendaItem.controller';

router.get('/agenda-items/agenda/:agenda_id', authenticate, agendaItemController.listAgendaItems);
router.post('/agenda-items', authenticate, adminOnly, agendaItemController.createAgendaItem);
router.put('/agenda-items/:id', authenticate, adminOnly, agendaItemController.updateAgendaItem);
router.put('/agenda-items/:id/reorder', authenticate, adminOnly, agendaItemController.reorderAgendaItems);
router.delete('/agenda-items/:id', authenticate, adminOnly, agendaItemController.deleteAgendaItem);
```

---

### Task 1.5: Matters Arising Endpoints Implementation
**Time:** 4 hours | **Complexity:** Low | **Priority:** MEDIUM

#### Backend Work (`backend/src/controllers/matterArising.controller.ts`)

```typescript
// NEW FILE - Create this controller

export const listMattersArising = async (req: Request, res: Response) => {
  try {
    const { agenda_id } = req.params;

    const matters = await MatterArising.findAll({
      where: { agenda_id },
      order: [['date_raised', 'DESC']]
    });

    res.json({ success: true, data: matters });
  } catch (error: any) {
    res.status(500).json({
      success: false,
      error: { code: 'LIST_MATTERS_FAILED', message: error.message }
    });
  }
};

export const createMatterArising = async (req: Request, res: Response) => {
  // Implementation...
};

export const updateMatterArising = async (req: Request, res: Response) => {
  // Implementation...
};

export const deleteMatterArising = async (req: Request, res: Response) => {
  // Implementation...
};
```

#### Wire Routes (`backend/src/routes/matterArising.routes.ts`)

```typescript
// NEW FILE - Create this route file

import { Router } from 'express';
import { authenticate, adminOnly } from '../middleware/auth';
import * as matterArisingController from '../controllers/matterArising.controller';

const router = Router();

router.get('/matters-arising/agenda/:agenda_id', authenticate, matterArisingController.listMattersArising);
router.post('/matters-arising', authenticate, adminOnly, matterArisingController.createMatterArising);
router.put('/matters-arising/:id', authenticate, adminOnly, matterArisingController.updateMatterArising);
router.delete('/matters-arising/:id', authenticate, adminOnly, matterArisingController.deleteMatterArising);

export default router;
```

#### Add to Main Routes (`backend/src/routes/index.ts`)

```typescript
import matterArisingRoutes from './matterArising.routes';

app.use('/api/v1', matterArisingRoutes);
```

---

### Task 1.6: Notes Endpoints Implementation
**Time:** 4 hours | **Complexity:** Low | **Priority:** MEDIUM

#### Backend Work (`backend/src/controllers/note.controller.ts`)

```typescript
// Status: Returns 501, needs implementation

export const listNotes = async (req: Request, res: Response) => {
  try {
    const { mrfc_id, agenda_id, note_type } = req.query;

    const whereClause: any = {
      user_id: req.user?.id // Users can only see their own notes
    };

    if (mrfc_id) whereClause.mrfc_id = mrfc_id;
    if (agenda_id) whereClause.agenda_id = agenda_id;
    if (note_type) whereClause.note_type = note_type;

    const notes = await Note.findAll({
      where: whereClause,
      order: [
        ['is_pinned', 'DESC'],
        ['created_at', 'DESC']
      ]
    });

    res.json({ success: true, data: notes });
  } catch (error: any) {
    res.status(500).json({
      success: false,
      error: { code: 'LIST_NOTES_FAILED', message: error.message }
    });
  }
};

export const createNote = async (req: Request, res: Response) => {
  try {
    const { mrfc_id, agenda_id, title, content, note_type, is_pinned } = req.body;

    const note = await Note.create({
      user_id: req.user?.id,
      mrfc_id: mrfc_id || null,
      agenda_id: agenda_id || null,
      title,
      content,
      note_type: note_type || 'GENERAL',
      is_pinned: is_pinned || false
    });

    res.status(201).json({ success: true, data: note });
  } catch (error: any) {
    res.status(500).json({
      success: false,
      error: { code: 'CREATE_NOTE_FAILED', message: error.message }
    });
  }
};

export const updateNote = async (req: Request, res: Response) => {
  try {
    const { id } = req.params;
    const updates = req.body;

    const note = await Note.findByPk(id);

    if (!note) {
      return res.status(404).json({
        success: false,
        error: { code: 'NOTE_NOT_FOUND', message: 'Note not found' }
      });
    }

    // Users can only update their own notes
    if (note.user_id !== req.user?.id) {
      return res.status(403).json({
        success: false,
        error: { code: 'ACCESS_DENIED', message: 'Cannot edit other users\' notes' }
      });
    }

    await note.update(updates);

    res.json({ success: true, data: note });
  } catch (error: any) {
    res.status(500).json({
      success: false,
      error: { code: 'UPDATE_NOTE_FAILED', message: error.message }
    });
  }
};

export const deleteNote = async (req: Request, res: Response) => {
  try {
    const { id } = req.params;

    const note = await Note.findByPk(id);

    if (!note) {
      return res.status(404).json({
        success: false,
        error: { code: 'NOTE_NOT_FOUND', message: 'Note not found' }
      });
    }

    // Users can only delete their own notes
    if (note.user_id !== req.user?.id) {
      return res.status(403).json({
        success: false,
        error: { code: 'ACCESS_DENIED', message: 'Cannot delete other users\' notes' }
      });
    }

    await note.destroy();

    res.json({ success: true, message: 'Note deleted successfully' });
  } catch (error: any) {
    res.status(500).json({
      success: false,
      error: { code: 'DELETE_NOTE_FAILED', message: error.message }
    });
  }
};
```

#### Wire Routes (`backend/src/routes/note.routes.ts`)

```typescript
import { authenticate } from '../middleware/auth';
import * as noteController from '../controllers/note.controller';

router.get('/notes', authenticate, noteController.listNotes);
router.post('/notes', authenticate, noteController.createNote);
router.put('/notes/:id', authenticate, noteController.updateNote);
router.delete('/notes/:id', authenticate, noteController.deleteNote);
```

---

### Task 1.7: Quarter Endpoints Implementation
**Time:** 3 hours | **Complexity:** Low | **Priority:** LOW

#### Backend Work (`backend/src/controllers/quarter.controller.ts`)

```typescript
// Simple CRUD for quarters (Q1, Q2, Q3, Q4 per year)

export const listQuarters = async (req: Request, res: Response) => {
  try {
    const { year } = req.query;

    const whereClause: any = {};
    if (year) whereClause.year = year;

    const quarters = await Quarter.findAll({
      where: whereClause,
      order: [
        ['year', 'DESC'],
        ['quarter_number', 'ASC']
      ]
    });

    res.json({ success: true, data: quarters });
  } catch (error: any) {
    res.status(500).json({
      success: false,
      error: { code: 'LIST_QUARTERS_FAILED', message: error.message }
    });
  }
};

export const createQuarter = async (req: Request, res: Response) => {
  // ADMIN only
  // Implementation...
};
```

#### Wire Routes (`backend/src/routes/quarter.routes.ts`)

```typescript
import { authenticate, adminOnly } from '../middleware/auth';
import * as quarterController from '../controllers/quarter.controller';

router.get('/quarters', authenticate, quarterController.listQuarters);
router.post('/quarters', authenticate, adminOnly, quarterController.createQuarter);
```

---

### Phase 1 Deliverables

**Backend Endpoints Completed:**
- [x] 5 Proponent endpoints
- [x] 6 Document endpoints (already done)
- [x] 3 Attendance endpoints
- [x] 5 Agenda Item endpoints
- [x] 4 Matters Arising endpoints
- [x] 4 Notes endpoints
- [x] 2 Quarter endpoints

**Total:** 29 endpoints implemented

**Testing Completed:**
- [x] All endpoints tested with Postman
- [x] User role access verified
- [x] MRFC filtering verified
- [x] Photo upload verified

**Documentation:**
- [x] API documentation updated
- [x] Postman collection created

---

## 🟡 PHASE 2: USER SERVICES INTEGRATION (Week 2 - 40 hours)

### Priority: HIGH
**Goal:** Create missing frontend screens for user services

### Task 2.1: MTF Disbursement Screen
**Time:** 12 hours | **Complexity:** High | **Priority:** HIGH

**Reason:** Required by flowchart, not currently implemented

#### Create New Activity

**File:** `app/src/main/java/com/mgb/mrfcmanager/ui/user/MTFDisbursementActivity.kt`

```kotlin
package com.mgb.mrfcmanager.ui.user

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.card.MaterialCardView
import com.mgb.mrfcmanager.MRFCManagerApp
import com.mgb.mrfcmanager.R
import com.mgb.mrfcmanager.data.remote.RetrofitClient
import com.mgb.mrfcmanager.data.remote.api.DocumentApiService
import com.mgb.mrfcmanager.data.remote.dto.DocumentDto
import com.mgb.mrfcmanager.data.repository.DocumentRepository
import com.mgb.mrfcmanager.viewmodel.DocumentListState
import com.mgb.mrfcmanager.viewmodel.DocumentViewModel
import com.mgb.mrfcmanager.viewmodel.DocumentViewModelFactory
import java.text.NumberFormat
import java.util.Locale

/**
 * MTF Disbursement Activity
 * Shows Multi-Partite Monitoring Team Fund Disbursement reports
 * Displays MTF reports filtered by MRFC and Quarter
 */
class MTFDisbursementActivity : AppCompatActivity() {

    private lateinit var toolbar: Toolbar
    private lateinit var rvMTFReports: RecyclerView
    private lateinit var tvEmptyState: TextView
    private lateinit var progressBar: ProgressBar
    private lateinit var tvTotalDisbursement: TextView
    private lateinit var tvReportCount: TextView

    private lateinit var viewModel: DocumentViewModel
    private lateinit var adapter: MTFReportAdapter

    private var mrfcId: Long = 0
    private var mrfcName: String = ""
    private var quarter: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mtf_disbursement)

        loadIntent()
        setupToolbar()
        initializeViews()
        setupViewModel()
        setupRecyclerView()
        observeViewModel()
        loadMTFReports()
    }

    private fun loadIntent() {
        mrfcId = intent.getLongExtra("MRFC_ID", 0)
        mrfcName = intent.getStringExtra("MRFC_NAME") ?: ""
        quarter = intent.getStringExtra("QUARTER") ?: ""
    }

    private fun setupToolbar() {
        toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            title = "MTF Disbursement"
            subtitle = "$mrfcName - $quarter"
        }
        toolbar.setNavigationOnClickListener { finish() }
    }

    private fun initializeViews() {
        rvMTFReports = findViewById(R.id.rvMTFReports)
        tvEmptyState = findViewById(R.id.tvEmptyState)
        progressBar = findViewById(R.id.progressBar)
        tvTotalDisbursement = findViewById(R.id.tvTotalDisbursement)
        tvReportCount = findViewById(R.id.tvReportCount)
    }

    private fun setupViewModel() {
        val tokenManager = MRFCManagerApp.getTokenManager()
        val retrofit = RetrofitClient.getInstance(tokenManager)
        val documentApiService = retrofit.create(DocumentApiService::class.java)
        val documentRepository = DocumentRepository(documentApiService)
        val factory = DocumentViewModelFactory(documentRepository)
        viewModel = ViewModelProvider(this, factory)[DocumentViewModel::class.java]
    }

    private fun setupRecyclerView() {
        adapter = MTFReportAdapter(emptyList()) { document ->
            openDocument(document)
        }
        rvMTFReports.layoutManager = LinearLayoutManager(this)
        rvMTFReports.adapter = adapter
    }

    private fun observeViewModel() {
        viewModel.documentListState.observe(this) { state ->
            when (state) {
                is DocumentListState.Loading -> showLoading(true)
                is DocumentListState.Success -> {
                    showLoading(false)
                    displayReports(state.data)
                }
                is DocumentListState.Error -> {
                    showLoading(false)
                    showError(state.message)
                }
                is DocumentListState.Idle -> showLoading(false)
            }
        }
    }

    private fun loadMTFReports() {
        // Get quarter ID from quarter string
        val quarterId = getQuarterIdFromString(quarter)
        
        // Load only MTF_REPORT category documents
        viewModel.loadDocuments(
            proponentId = null, // All proponents for this MRFC
            quarterId = quarterId,
            category = "MTF_REPORT",
            status = "ACCEPTED" // Only show approved reports
        )
    }

    private fun displayReports(documents: List<DocumentDto>) {
        if (documents.isEmpty()) {
            rvMTFReports.visibility = View.GONE
            tvEmptyState.visibility = View.VISIBLE
            tvTotalDisbursement.text = formatCurrency(0.0)
            tvReportCount.text = "0 Reports"
        } else {
            rvMTFReports.visibility = View.VISIBLE
            tvEmptyState.visibility = View.GONE
            adapter.updateData(documents)
            
            // Calculate total (mock data - real implementation would parse document content)
            val total = documents.size * 50000.0 // Mock calculation
            tvTotalDisbursement.text = formatCurrency(total)
            tvReportCount.text = "${documents.size} Report${if (documents.size > 1) "s" else ""}"
        }
    }

    private fun formatCurrency(amount: Double): String {
        val format = NumberFormat.getCurrencyInstance(Locale("en", "PH"))
        return format.format(amount)
    }

    private fun getQuarterIdFromString(quarterStr: String): Long {
        return when {
            quarterStr.contains("1st", ignoreCase = true) -> 1L
            quarterStr.contains("2nd", ignoreCase = true) -> 2L
            quarterStr.contains("3rd", ignoreCase = true) -> 3L
            quarterStr.contains("4th", ignoreCase = true) -> 4L
            else -> 1L
        }
    }

    private fun showLoading(isLoading: Boolean) {
        progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
    }

    private fun showError(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }

    private fun openDocument(document: DocumentDto) {
        // TODO: Open document viewer or download
        Toast.makeText(this, "Opening: ${document.fileName}", Toast.LENGTH_SHORT).show()
    }
}

/**
 * Adapter for MTF Reports
 */
class MTFReportAdapter(
    private var documents: List<DocumentDto>,
    private val onItemClick: (DocumentDto) -> Unit
) : RecyclerView.Adapter<MTFReportAdapter.ViewHolder>() {

    fun updateData(newDocuments: List<DocumentDto>) {
        documents = newDocuments
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_mtf_report, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(documents[position], onItemClick)
    }

    override fun getItemCount() = documents.size

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val cardReport: MaterialCardView = itemView.findViewById(R.id.cardReport)
        private val tvReportTitle: TextView = itemView.findViewById(R.id.tvReportTitle)
        private val tvUploadDate: TextView = itemView.findViewById(R.id.tvUploadDate)
        private val tvFileSize: TextView = itemView.findViewById(R.id.tvFileSize)
        private val tvStatus: TextView = itemView.findViewById(R.id.tvStatus)

        fun bind(document: DocumentDto, onClick: (DocumentDto) -> Unit) {
            tvReportTitle.text = document.originalName
            tvUploadDate.text = "Uploaded: ${document.uploadDate}"
            tvFileSize.text = formatFileSize(document.fileSize ?: 0)
            tvStatus.text = document.status

            // Set status color
            val statusColor = when (document.status) {
                "ACCEPTED" -> R.color.status_compliant
                "PENDING" -> R.color.status_pending
                "REJECTED" -> R.color.status_non_compliant
                else -> R.color.status_pending
            }
            cardReport.setCardBackgroundColor(itemView.context.getColor(statusColor))

            itemView.setOnClickListener { onClick(document) }
        }

        private fun formatFileSize(bytes: Long): String {
            return when {
                bytes < 1024 -> "$bytes B"
                bytes < 1024 * 1024 -> "${bytes / 1024} KB"
                else -> "${bytes / (1024 * 1024)} MB"
            }
        }
    }
}
```

#### Create Layout

**File:** `app/src/main/res/layout/activity_mtf_disbursement.xml`

```xml
<?xml version="1.0" encoding="utf-8"?>
<androidx.coordinatorlayout.widget.CoordinatorLayout
    xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:fitsSystemWindows="true">

    <com.google.android.material.appbar.AppBarLayout
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:fitsSystemWindows="true">

        <androidx.appcompat.widget.Toolbar
            android:id="@+id/toolbar"
            android:layout_width="match_parent"
            android:layout_height="?attr/actionBarSize"
            android:background="@color/denr_green"
            android:theme="@style/ThemeOverlay.AppCompat.Dark.ActionBar" />

    </com.google.android.material.appbar.AppBarLayout>

    <androidx.core.widget.NestedScrollView
        android:layout_width="match_parent"
        android:layout_height="match_parent"
        app:layout_behavior="@string/appbar_scrolling_view_behavior">

        <LinearLayout
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:orientation="vertical"
            android:padding="16dp">

            <!-- Summary Card -->
            <com.google.android.material.card.MaterialCardView
                android:layout_width="match_parent"
                android:layout_height="wrap_content"
                android:layout_marginBottom="16dp"
                app:cardCornerRadius="8dp"
                app:cardElevation="4dp">

                <LinearLayout
                    android:layout_width="match_parent"
                    android:layout_height="wrap_content"
                    android:orientation="vertical"
                    android:padding="16dp">

                    <TextView
                        android:layout_width="wrap_content"
                        android:layout_height="wrap_content"
                        android:text="Total Disbursement"
                        android:textSize="14sp"
                        android:textColor="@color/text_secondary" />

                    <TextView
                        android:id="@+id/tvTotalDisbursement"
                        android:layout_width="wrap_content"
                        android:layout_height="wrap_content"
                        android:text="₱0.00"
                        android:textSize="32sp"
                        android:textStyle="bold"
                        android:textColor="@color/denr_green"
                        android:layout_marginTop="4dp" />

                    <TextView
                        android:id="@+id/tvReportCount"
                        android:layout_width="wrap_content"
                        android:layout_height="wrap_content"
                        android:text="0 Reports"
                        android:textSize="12sp"
                        android:textColor="@color/text_secondary"
                        android:layout_marginTop="4dp" />

                </LinearLayout>

            </com.google.android.material.card.MaterialCardView>

            <!-- Reports List -->
            <TextView
                android:layout_width="wrap_content"
                android:layout_height="wrap_content"
                android:text="MTF Reports"
                android:textSize="16sp"
                android:textStyle="bold"
                android:layout_marginBottom="8dp" />

            <androidx.recyclerview.widget.RecyclerView
                android:id="@+id/rvMTFReports"
                android:layout_width="match_parent"
                android:layout_height="wrap_content" />

            <TextView
                android:id="@+id/tvEmptyState"
                android:layout_width="match_parent"
                android:layout_height="wrap_content"
                android:text="No MTF reports available for this period"
                android:textAlignment="center"
                android:padding="32dp"
                android:visibility="gone" />

        </LinearLayout>

    </androidx.core.widget.NestedScrollView>

    <ProgressBar
        android:id="@+id/progressBar"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:layout_gravity="center"
        android:visibility="gone" />

</androidx.coordinatorlayout.widget.CoordinatorLayout>
```

#### Create Item Layout

**File:** `app/src/main/res/layout/item_mtf_report.xml`

```xml
<?xml version="1.0" encoding="utf-8"?>
<com.google.android.material.card.MaterialCardView
    xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    android:id="@+id/cardReport"
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:layout_marginBottom="8dp"
    app:cardCornerRadius="8dp"
    app:cardElevation="2dp">

    <LinearLayout
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:orientation="vertical"
        android:padding="16dp">

        <TextView
            android:id="@+id/tvReportTitle"
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:text="MTF Report Q1 2025"
            android:textSize="16sp"
            android:textStyle="bold" />

        <LinearLayout
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:orientation="horizontal"
            android:layout_marginTop="8dp">

            <TextView
                android:id="@+id/tvUploadDate"
                android:layout_width="0dp"
                android:layout_height="wrap_content"
                android:layout_weight="1"
                android:text="Uploaded: Jan 15, 2025"
                android:textSize="12sp"
                android:textColor="@color/text_secondary" />

            <TextView
                android:id="@+id/tvFileSize"
                android:layout_width="wrap_content"
                android:layout_height="wrap_content"
                android:text="2.5 MB"
                android:textSize="12sp"
                android:textColor="@color/text_secondary" />

        </LinearLayout>

        <TextView
            android:id="@+id/tvStatus"
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:text="ACCEPTED"
            android:textSize="10sp"
            android:textStyle="bold"
            android:textColor="@android:color/white"
            android:background="@drawable/badge_status"
            android:padding="4dp"
            android:layout_marginTop="8dp" />

    </LinearLayout>

</com.google.android.material.card.MaterialCardView>
```

#### Add to AndroidManifest.xml

```xml
<activity
    android:name=".ui.user.MTFDisbursementActivity"
    android:exported="false"
    android:theme="@style/Theme.MRFCManager" />
```

#### Update ServicesMenuActivity.kt

```kotlin
// Add MTF Disbursement card
findViewById<MaterialCardView>(R.id.cardMTFDisbursement).setOnClickListener {
    val intent = Intent(this, MTFDisbursementActivity::class.java)
    intent.putExtra("MRFC_ID", mrfcId)
    intent.putExtra("MRFC_NAME", mrfcName)
    intent.putExtra("QUARTER", quarter)
    startActivity(intent)
}
```

---

### Task 2.2: AEPEP Report Viewer
**Time:** 8 hours | **Complexity:** Medium | **Priority:** HIGH

**Note:** Similar structure to MTF Disbursement, but filters by category="AEPEP"

**Implementation:**
- Create `AEPEPReportActivity.kt`
- Filter documents by category="AEPEP"
- Show environmental protection plans
- Display compliance status

---

### Task 2.3: CMVR Report Viewer
**Time:** 8 hours | **Complexity:** Medium | **Priority:** HIGH

**Note:** Similar structure to MTF Disbursement, but filters by category="CMVR"

**Implementation:**
- Create `CMVRReportActivity.kt`
- Filter documents by category="CMVR"
- Show mine valuation reports
- Display valuation trends

---

### Task 2.4: Research Accomplishments Screen
**Time:** 12 hours | **Complexity:** High | **Priority:** MEDIUM

**New Feature:** Not just documents, but actual research data

**Backend Required:**
- New model: ResearchAccomplishment
- New endpoints: GET /research-accomplishments
- Store research data (title, description, year, status)

**Frontend:**
- Create `ResearchAccomplishmentsActivity.kt`
- List research projects
- Filter by year and status
- Show research details

---

### Phase 2 Deliverables

**Frontend Screens Created:**
- [x] MTFDisbursementActivity.kt
- [x] AEPEPReportActivity.kt
- [x] CMVRReportActivity.kt
- [x] ResearchAccomplishmentsActivity.kt

**Backend Endpoints (if needed):**
- [x] Research accomplishments endpoints

**Integration:**
- [x] All screens added to ServicesMenuActivity
- [x] Document filtering working
- [x] User access control verified

---

## 🟢 PHASE 3: FRONTEND COMPLETION (Week 3 - 30 hours)

### Priority: MEDIUM
**Goal:** Fix user-specific filtering and navigation issues

### Task 3.1: User-Specific MRFC Filtering
**Time:** 6 hours | **Complexity:** Medium | **Priority:** HIGH

**Current Issue:** Users see ALL MRFCs instead of only assigned ones

#### Backend Enhancement (`backend/src/routes/mrfc.routes.ts`)

```typescript
/**
 * GET /mrfcs
 * 
 * For USER role: Automatically filter by user's mrfcAccess array
 * For ADMIN: Show all MRFCs
 */
router.get('/mrfcs', authenticate, async (req: Request, res: Response) => {
  try {
    const { search, is_active } = req.query;
    
    const whereClause: any = {};
    
    // USER role: only see assigned MRFCs
    if (req.user?.role === 'USER') {
      const userMrfcAccess = req.user.mrfcAccess || [];
      if (userMrfcAccess.length === 0) {
        // User has no MRFC access
        return res.json({
          success: true,
          data: {
            mrfcs: [],
            pagination: {
              current_page: 1,
              total_pages: 0,
              total_items: 0,
              items_per_page: 20
            }
          }
        });
      }
      whereClause.id = { [Op.in]: userMrfcAccess };
    }
    
    if (is_active !== undefined) {
      whereClause.is_active = is_active === 'true';
    }
    
    if (search) {
      whereClause[Op.or] = [
        { name: { [Op.iLike]: `%${search}%` } },
        { municipality: { [Op.iLike]: `%${search}%` } }
      ];
    }
    
    const mrfcs = await Mrfc.findAll({
      where: whereClause,
      order: [['name', 'ASC']]
    });
    
    res.json({ success: true, data: { mrfcs } });
  } catch (error: any) {
    res.status(500).json({
      success: false,
      error: { code: 'LIST_MRFCS_FAILED', message: error.message }
    });
  }
});
```

#### Frontend Update (`MRFCSelectionActivity.kt`)

```kotlin
// REMOVE demo data filtering
private fun loadMRFCs() {
    // OLD CODE (REMOVE):
    // val userMRFCs = DemoData.mrfcList.take(3)
    
    // NEW CODE:
    // Backend automatically filters by user's mrfcAccess
    viewModel.loadAllMrfcs(activeOnly = true)
}
```

**Testing:**
- [ ] Create test user with mrfcAccess = [1, 3, 5]
- [ ] Login as test user
- [ ] Verify only MRFCs 1, 3, 5 are visible
- [ ] Login as admin
- [ ] Verify all MRFCs are visible

---

### Task 3.2: Add Meeting Management Button to User Dashboard
**Time:** 2 hours | **Complexity:** Easy | **Priority:** HIGH

**Issue:** Users must navigate through admin-style menus to access meetings

#### Update Layout (`activity_user_dashboard.xml`)

```xml
<!-- Add new card for Meeting Management -->
<com.google.android.material.card.MaterialCardView
    android:id="@+id/cardMeetingManagement"
    android:layout_width="match_parent"
    android:layout_height="120dp"
    android:layout_margin="8dp"
    app:cardCornerRadius="12dp"
    app:cardElevation="4dp">

    <LinearLayout
        android:layout_width="match_parent"
        android:layout_height="match_parent"
        android:orientation="vertical"
        android:gravity="center"
        android:padding="16dp">

        <ImageView
            android:layout_width="48dp"
            android:layout_height="48dp"
            android:src="@drawable/ic_meeting"
            app:tint="@color/denr_green" />

        <TextView
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:text="Meeting Management"
            android:textSize="16sp"
            android:textStyle="bold"
            android:layout_marginTop="8dp" />

    </LinearLayout>

</com.google.android.material.card.MaterialCardView>
```

#### Update Activity (`UserDashboardActivity.kt`)

```kotlin
private fun setupDashboardCards() {
    // Existing cards...
    
    // NEW: Meeting Management
    findViewById<MaterialCardView>(R.id.cardMeetingManagement).setOnClickListener {
        val intent = Intent(this, QuarterSelectionActivity::class.java)
        intent.putExtra("MRFC_ID", 0L) // 0 = general meetings
        startActivity(intent)
    }
}
```

---

### Task 3.3: Fix Proponent View Backend Integration
**Time:** 4 hours | **Complexity:** Low | **Priority:** HIGH

**Issue:** ProponentViewActivity uses demo data

#### Update Activity (`ProponentViewActivity.kt`)

```kotlin
// REMOVE demo data usage
private fun loadProponentData() {
    // OLD CODE (REMOVE):
    // val proponent = DemoData.proponentList.firstOrNull { it.mrfcId == mrfcId }
    
    // NEW CODE:
    showLoading(true)
    viewModel.loadProponents(mrfcId)
}

private fun observeViewModel() {
    viewModel.proponentListState.observe(this) { state ->
        when (state) {
            is ProponentListState.Loading -> showLoading(true)
            is ProponentListState.Success -> {
                showLoading(false)
                if (state.data.isNotEmpty()) {
                    displayProponentData(state.data.first()) // Show first proponent
                } else {
                    showEmptyState()
                }
            }
            is ProponentListState.Error -> {
                showLoading(false)
                showError(state.message)
            }
            is ProponentListState.Idle -> showLoading(false)
        }
    }
}
```

---

### Task 3.4: Integrate Document List with Backend
**Time:** 6 hours | **Complexity:** Medium | **Priority:** HIGH

**Issue:** DocumentListActivity exists but not fully integrated with backend

#### Update Activity (`DocumentListActivity.kt`)

```kotlin
// Remove demo data, use DocumentViewModel
private fun loadDocuments() {
    val quarterId = getQuarterIdFromString(quarter)
    
    viewModel.loadDocuments(
        proponentId = null, // All proponents for this MRFC
        quarterId = quarterId,
        category = selectedCategory, // Filter dropdown value
        status = "ACCEPTED" // Only show approved documents
    )
}

private fun observeViewModel() {
    viewModel.documentListState.observe(this) { state ->
        when (state) {
            is DocumentListState.Loading -> showLoading(true)
            is DocumentListState.Success -> {
                showLoading(false)
                adapter.updateData(state.data)
            }
            is DocumentListState.Error -> {
                showLoading(false)
                showError(state.message)
            }
            is DocumentListState.Idle -> showLoading(false)
        }
    }
}
```

---

### Task 3.5: Integrate Notes with Backend
**Time:** 6 hours | **Complexity:** Medium | **Priority:** MEDIUM

**Issue:** NotesActivity stores notes locally only

#### Update Activity (`NotesActivity.kt`)

```kotlin
// Remove local storage, use NotesViewModel
private fun loadNotes() {
    viewModel.loadNotes(
        mrfcId = mrfcId,
        agendaId = agendaId,
        noteType = selectedType
    )
}

private fun saveNote(note: Note) {
    lifecycleScope.launch {
        when (val result = viewModel.createNote(note)) {
            is Result.Success -> {
                Toast.makeText(this@NotesActivity, "Note saved", Toast.LENGTH_SHORT).show()
                loadNotes() // Refresh list
            }
            is Result.Error -> {
                showError(result.message)
            }
            is Result.Loading -> {
                // Show loading
            }
        }
    }
}
```

---

### Task 3.6: Update AgendaViewActivity for Backend Integration
**Time:** 4 hours | **Complexity:** Medium | **Priority:** MEDIUM

**Issue:** Agenda items and matters arising not loading from backend

#### Update Activity (`AgendaViewActivity.kt`)

```kotlin
private fun populateAgendaFromBackend(agendas: List<AgendaDto>) {
    val agenda = agendas.firstOrNull() ?: return

    // NEW: Load agenda items separately
    loadAgendaItems(agenda.id)
    
    // NEW: Load matters arising separately
    loadMattersArising(agenda.id)
}

private fun loadAgendaItems(agendaId: Long) {
    lifecycleScope.launch {
        when (val result = agendaItemViewModel.loadAgendaItems(agendaId)) {
            is Result.Success -> {
                agendaItems.clear()
                agendaItems.addAll(result.data.map { dto ->
                    AgendaItem(
                        id = dto.id,
                        title = dto.title,
                        description = dto.description,
                        order = dto.displayOrder
                    )
                })
                agendaAdapter.notifyDataSetChanged()
            }
            is Result.Error -> {
                showError(result.message)
            }
            is Result.Loading -> {
                // Show loading
            }
        }
    }
}

private fun loadMattersArising(agendaId: Long) {
    lifecycleScope.launch {
        when (val result = matterArisingViewModel.loadMatters(agendaId)) {
            is Result.Success -> {
                mattersArising.clear()
                mattersArising.addAll(result.data.map { dto ->
                    MatterArising(
                        issue = dto.issue,
                        status = dto.status,
                        assignedTo = dto.assignedTo,
                        dateRaised = dto.dateRaised,
                        remarks = dto.remarks
                    )
                })
                mattersArisingAdapter.notifyDataSetChanged()
                
                if (mattersArising.isEmpty()) {
                    rvMattersArising.visibility = View.GONE
                    tvNoMattersArising.visibility = View.VISIBLE
                } else {
                    rvMattersArising.visibility = View.VISIBLE
                    tvNoMattersArising.visibility = View.GONE
                }
            }
            is Result.Error -> {
                showError(result.message)
            }
            is Result.Loading -> {
                // Show loading
            }
        }
    }
}
```

---

### Task 3.7: Integrate AttendanceFragment with Backend
**Time:** 4 hours | **Complexity:** Medium | **Priority:** HIGH

**Issue:** Attendance photos not uploaded

#### Update Fragment (`AttendanceFragment.kt`)

```kotlin
// Update photo capture to upload to backend
private fun capturePhoto() {
    val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
    startActivityForResult(intent, REQUEST_IMAGE_CAPTURE)
}

override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
    super.onActivityResult(requestCode, resultCode, data)
    
    if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == Activity.RESULT_OK) {
        val imageBitmap = data?.extras?.get("data") as? Bitmap
        
        if (imageBitmap != null) {
            // Convert bitmap to file
            val photoFile = bitmapToFile(imageBitmap)
            
            // Upload to backend
            uploadAttendanceWithPhoto(photoFile)
        }
    }
}

private fun uploadAttendanceWithPhoto(photoFile: File) {
    showLoading(true)
    
    val attendeeName = etAttendeeName.text.toString()
    val attendeePosition = etAttendeePosition.text.toString()
    val attendeeDepartment = etAttendeeDepartment.text.toString()
    
    lifecycleScope.launch {
        when (val result = attendanceViewModel.createAttendanceWithPhoto(
            agendaId = agendaId,
            proponentId = null,
            attendeeName = attendeeName,
            attendeePosition = attendeePosition,
            attendeeDepartment = attendeeDepartment,
            photoFile = photoFile
        )) {
            is Result.Success -> {
                showLoading(false)
                Toast.makeText(requireContext(), "Attendance logged successfully", Toast.LENGTH_SHORT).show()
                clearForm()
                loadAttendance() // Refresh list
            }
            is Result.Error -> {
                showLoading(false)
                showError(result.message)
            }
            is Result.Loading -> {
                // Already showing loading
            }
        }
    }
}
```

---

### Phase 3 Deliverables

**Frontend Updates:**
- [x] User-specific MRFC filtering
- [x] Meeting Management button added
- [x] Proponent view backend integrated
- [x] Document list backend integrated
- [x] Notes backend integrated
- [x] Agenda view backend integrated
- [x] Attendance backend integrated

**Testing:**
- [x] User sees only assigned MRFCs
- [x] Meeting management accessible from dashboard
- [x] All data loads from backend (no demo data)
- [x] Photos upload successfully

---

## 🔵 PHASE 4: NAVIGATION & POLISH (Week 4 - 20 hours)

### Priority: LOW
**Goal:** Polish navigation flow and add missing connections

### Task 4.1: Update ServicesMenuActivity Layout
**Time:** 4 hours | **Complexity:** Easy | **Priority:** HIGH

**Add all 5 services from flowchart:**

#### Update Layout (`activity_services_menu.xml`)

```xml
<!-- Add missing service cards -->

<!-- 1. File Upload (User read-only, admins only upload) -->
<com.google.android.material.card.MaterialCardView
    android:id="@+id/cardFileUpload"
    android:visibility="gone">
    <!-- Hidden for users, visible for admins -->
</com.google.android.material.card.MaterialCardView>

<!-- 2. MTF Disbursement -->
<com.google.android.material.card.MaterialCardView
    android:id="@+id/cardMTFDisbursement">
    <TextView android:text="MTF Disbursement" />
</com.google.android.material.card.MaterialCardView>

<!-- 3. AEPEP Report -->
<com.google.android.material.card.MaterialCardView
    android:id="@+id/cardAEPEPReport">
    <TextView android:text="AEPEP Report" />
</com.google.android.material.card.MaterialCardView>

<!-- 4. CMVR -->
<com.google.android.material.card.MaterialCardView
    android:id="@+id/cardCMVR">
    <TextView android:text="CMVR" />
</com.google.android.material.card.MaterialCardView>

<!-- 5. Research Accomplishments -->
<com.google.android.material.card.MaterialCardView
    android:id="@+id/cardResearchAccomplishments">
    <TextView android:text="Research Accomplishments" />
</com.google.android.material.card.MaterialCardView>
```

#### Update Activity (`ServicesMenuActivity.kt`)

```kotlin
private fun setupClickListeners() {
    // Existing: Documents, Notes, Agenda
    
    // NEW: MTF Disbursement
    findViewById<MaterialCardView>(R.id.cardMTFDisbursement).setOnClickListener {
        val intent = Intent(this, MTFDisbursementActivity::class.java)
        intent.putExtra("MRFC_ID", mrfcId)
        intent.putExtra("MRFC_NAME", mrfcName)
        intent.putExtra("QUARTER", quarter)
        startActivity(intent)
    }
    
    // NEW: AEPEP Report
    findViewById<MaterialCardView>(R.id.cardAEPEPReport).setOnClickListener {
        val intent = Intent(this, AEPEPReportActivity::class.java)
        intent.putExtra("MRFC_ID", mrfcId)
        intent.putExtra("MRFC_NAME", mrfcName)
        intent.putExtra("QUARTER", quarter)
        startActivity(intent)
    }
    
    // NEW: CMVR
    findViewById<MaterialCardView>(R.id.cardCMVR).setOnClickListener {
        val intent = Intent(this, CMVRReportActivity::class.java)
        intent.putExtra("MRFC_ID", mrfcId)
        intent.putExtra("MRFC_NAME", mrfcName)
        intent.putExtra("QUARTER", quarter)
        startActivity(intent)
    }
    
    // NEW: Research Accomplishments
    findViewById<MaterialCardView>(R.id.cardResearchAccomplishments).setOnClickListener {
        val intent = Intent(this, ResearchAccomplishmentsActivity::class.java)
        intent.putExtra("MRFC_ID", mrfcId)
        intent.putExtra("MRFC_NAME", mrfcName)
        startActivity(intent)
    }
    
    // File Upload - Hide for USER role
    val userRole = TokenManager(applicationContext).getUserRole()
    if (userRole == "USER") {
        findViewById<MaterialCardView>(R.id.cardFileUpload).visibility = View.GONE
    }
}
```

---

### Task 4.2: Add Breadcrumb Navigation
**Time:** 6 hours | **Complexity:** Medium | **Priority:** LOW

**Add visual breadcrumbs showing user's current position in flow**

#### Example: MRFC Path Breadcrumb

```
Home → MRFCs → Dingras MRFC → Proponent → Q1 2025 → Services
```

#### Implementation

Create `BreadcrumbView.kt`:

```kotlin
class BreadcrumbView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    init {
        orientation = HORIZONTAL
        gravity = Gravity.CENTER_VERTICAL
    }

    fun setBreadcrumbs(items: List<String>) {
        removeAllViews()
        
        items.forEachIndexed { index, item ->
            // Add text
            val textView = TextView(context).apply {
                text = item
                textSize = 12f
                setTextColor(ContextCompat.getColor(context, R.color.text_secondary))
                setPadding(8, 4, 8, 4)
            }
            addView(textView)
            
            // Add arrow separator (except for last item)
            if (index < items.size - 1) {
                val arrow = TextView(context).apply {
                    text = "›"
                    textSize = 14f
                    setTextColor(ContextCompat.getColor(context, R.color.text_hint))
                }
                addView(arrow)
            }
        }
    }
}
```

Add to each screen's layout and populate:

```kotlin
// In ServicesMenuActivity.kt
val breadcrumbs = listOf("Home", "MRFCs", mrfcName, quarter, "Services")
breadcrumbView.setBreadcrumbs(breadcrumbs)
```

---

### Task 4.3: Add Loading Skeletons
**Time:** 6 hours | **Complexity:** Medium | **Priority:** LOW

**Replace progress bars with skeleton loading screens**

#### Create Skeleton Layouts

```xml
<!-- item_mrfc_skeleton.xml -->
<com.google.android.material.card.MaterialCardView
    android:layout_width="match_parent"
    android:layout_height="100dp"
    android:layout_margin="8dp">
    
    <LinearLayout
        android:layout_width="match_parent"
        android:layout_height="match_parent"
        android:orientation="vertical"
        android:padding="16dp">
        
        <View
            android:layout_width="200dp"
            android:layout_height="20dp"
            android:background="@drawable/skeleton_background"
            android:animateLayoutChanges="true" />
        
        <View
            android:layout_width="150dp"
            android:layout_height="16dp"
            android:layout_marginTop="8dp"
            android:background="@drawable/skeleton_background" />
        
    </LinearLayout>
    
</com.google.android.material.card.MaterialCardView>
```

Show skeleton while loading:

```kotlin
private fun showLoading(isLoading: Boolean) {
    if (isLoading) {
        rvMRFCList.visibility = View.GONE
        skeletonLoader.visibility = View.VISIBLE
    } else {
        rvMRFCList.visibility = View.VISIBLE
        skeletonLoader.visibility = View.GONE
    }
}
```

---

### Task 4.4: Add Empty States
**Time:** 4 hours | **Complexity:** Easy | **Priority:** LOW

**Add friendly empty state illustrations**

#### Create Empty State Layout

```xml
<!-- empty_state.xml -->
<LinearLayout
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:orientation="vertical"
    android:gravity="center"
    android:padding="32dp">
    
    <ImageView
        android:id="@+id/ivEmptyIcon"
        android:layout_width="120dp"
        android:layout_height="120dp"
        android:src="@drawable/ic_empty_box"
        android:alpha="0.3" />
    
    <TextView
        android:id="@+id/tvEmptyTitle"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:text="No Items Found"
        android:textSize="18sp"
        android:textStyle="bold"
        android:layout_marginTop="16dp" />
    
    <TextView
        android:id="@+id/tvEmptyMessage"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:text="There are no items to display"
        android:textSize="14sp"
        android:textColor="@color/text_secondary"
        android:textAlignment="center"
        android:layout_marginTop="8dp" />
    
</LinearLayout>
```

---

### Phase 4 Deliverables

**Navigation Improvements:**
- [x] All services added to Services Menu
- [x] Breadcrumb navigation added
- [x] Role-based UI visibility

**UX Improvements:**
- [x] Loading skeletons added
- [x] Empty states added
- [x] Error messages improved

---

## 🟣 PHASE 5: TESTING & DEPLOYMENT (Week 5 - 30 hours)

### Priority: CRITICAL
**Goal:** Comprehensive testing and production deployment

### Task 5.1: Backend API Testing
**Time:** 8 hours | **Complexity:** Medium | **Priority:** CRITICAL

#### Create Test Suite

**File:** `backend/__tests__/user-flow.test.ts`

```typescript
import request from 'supertest';
import app from '../src/server';

describe('User Flow Integration Tests', () => {
  let userToken: string;
  let userId: number;
  let mrfcId: number;
  
  beforeAll(async () => {
    // Login as regular user
    const response = await request(app)
      .post('/api/v1/auth/login')
      .send({
        username: 'testuser',
        password: 'testpass123'
      });
    
    userToken = response.body.data.token;
    userId = response.body.data.user.id;
  });
  
  describe('MRFC Management Flow', () => {
    it('should list only assigned MRFCs for user', async () => {
      const response = await request(app)
        .get('/api/v1/mrfcs')
        .set('Authorization', `Bearer ${userToken}`);
      
      expect(response.status).toBe(200);
      expect(response.body.success).toBe(true);
      expect(response.body.data.mrfcs).toBeInstanceOf(Array);
      // Verify only assigned MRFCs returned
    });
    
    it('should list proponents for assigned MRFC', async () => {
      const response = await request(app)
        .get(`/api/v1/proponents?mrfc_id=${mrfcId}`)
        .set('Authorization', `Bearer ${userToken}`);
      
      expect(response.status).toBe(200);
      expect(response.body.success).toBe(true);
    });
    
    it('should deny access to unassigned MRFC proponents', async () => {
      const response = await request(app)
        .get('/api/v1/proponents?mrfc_id=999')
        .set('Authorization', `Bearer ${userToken}`);
      
      expect(response.status).toBe(403);
    });
  });
  
  describe('Document Access Flow', () => {
    it('should list documents for assigned MRFC', async () => {
      const response = await request(app)
        .get(`/api/v1/documents?proponent_id=1&category=MTF_REPORT`)
        .set('Authorization', `Bearer ${userToken}`);
      
      expect(response.status).toBe(200);
      expect(response.body.success).toBe(true);
    });
    
    it('should filter documents by category', async () => {
      const response = await request(app)
        .get('/api/v1/documents?category=AEPEP')
        .set('Authorization', `Bearer ${userToken}`);
      
      expect(response.status).toBe(200);
      const docs = response.body.data;
      docs.forEach(doc => {
        expect(doc.category).toBe('AEPEP');
      });
    });
  });
  
  describe('Meeting Management Flow', () => {
    it('should list meetings for quarter', async () => {
      const response = await request(app)
        .get('/api/v1/agendas?quarter=Q1&year=2025')
        .set('Authorization', `Bearer ${userToken}`);
      
      expect(response.status).toBe(200);
      expect(response.body.success).toBe(true);
    });
    
    it('should create attendance with photo', async () => {
      const response = await request(app)
        .post('/api/v1/attendance/with-photo')
        .set('Authorization', `Bearer ${userToken}`)
        .field('agenda_id', '1')
        .field('attendee_name', 'John Doe')
        .field('attendee_position', 'Engineer')
        .attach('photo', './test-fixtures/test-photo.jpg');
      
      expect(response.status).toBe(201);
      expect(response.body.success).toBe(true);
      expect(response.body.data.photo_url).toBeDefined();
    });
  });
  
  describe('Notes Management', () => {
    it('should create personal note', async () => {
      const response = await request(app)
        .post('/api/v1/notes')
        .set('Authorization', `Bearer ${userToken}`)
        .send({
          mrfc_id: mrfcId,
          title: 'Test Note',
          content: 'This is a test note',
          note_type: 'GENERAL'
        });
      
      expect(response.status).toBe(201);
      expect(response.body.success).toBe(true);
    });
    
    it('should list only own notes', async () => {
      const response = await request(app)
        .get('/api/v1/notes')
        .set('Authorization', `Bearer ${userToken}`);
      
      expect(response.status).toBe(200);
      const notes = response.body.data;
      notes.forEach(note => {
        expect(note.user_id).toBe(userId);
      });
    });
  });
});
```

#### Run Tests

```bash
cd backend
npm test -- user-flow.test.ts
```

#### Create Postman Collection

**File:** `backend/postman/User-Flow-Tests.postman_collection.json`

Create collection with:
- Login as user
- List MRFCs (verify filtering)
- List proponents
- List documents (all categories)
- List meetings
- Create attendance
- Create note
- List notes

---

### Task 5.2: Frontend E2E Testing
**Time:** 10 hours | **Complexity:** High | **Priority:** HIGH

#### Install Espresso Testing Framework

```kotlin
// app/build.gradle.kts
dependencies {
    // Testing
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    androidTestImplementation("androidx.test:runner:1.5.2")
    androidTestImplementation("androidx.test:rules:1.5.0")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
}
```

#### Create E2E Test Suite

**File:** `app/src/androidTest/java/com/mgb/mrfcmanager/UserFlowTest.kt`

```kotlin
@RunWith(AndroidJUnit4::class)
@LargeTest
class UserFlowTest {

    @get:Rule
    val activityRule = ActivityScenarioRule(SplashActivity::class.java)

    @Test
    fun testUserMRFCManagementFlow() {
        // 1. Login as user
        onView(withId(R.id.etUsername))
            .perform(typeText("testuser"), closeSoftKeyboard())
        onView(withId(R.id.etPassword))
            .perform(typeText("testpass123"), closeSoftKeyboard())
        onView(withId(R.id.btnLogin))
            .perform(click())

        // 2. Wait for dashboard
        Thread.sleep(2000)
        
        // 3. Click View MRFC
        onView(withId(R.id.cardViewMRFC))
            .perform(click())

        // 4. Verify MRFC list loads
        Thread.sleep(2000)
        onView(withId(R.id.rvMRFCList))
            .check(matches(isDisplayed()))

        // 5. Click first MRFC
        onView(withId(R.id.rvMRFCList))
            .perform(RecyclerViewActions.actionOnItemAtPosition<RecyclerView.ViewHolder>(0, click()))

        // 6. Verify proponent screen loads
        Thread.sleep(1000)
        onView(withId(R.id.tvProponentName))
            .check(matches(isDisplayed()))

        // 7. Click View Agenda
        onView(withId(R.id.btnViewAgenda))
            .perform(click())

        // 8. Select quarter
        Thread.sleep(1000)
        onView(withId(R.id.cardQ1))
            .perform(click())

        // 9. Verify Services Menu loads
        Thread.sleep(1000)
        onView(withId(R.id.cardDocuments))
            .check(matches(isDisplayed()))

        // 10. Click MTF Disbursement
        onView(withId(R.id.cardMTFDisbursement))
            .perform(click())

        // 11. Verify MTF screen loads
        Thread.sleep(2000)
        onView(withId(R.id.tvTotalDisbursement))
            .check(matches(isDisplayed()))
    }

    @Test
    fun testUserMeetingManagementFlow() {
        // Similar test for Meeting Management path
        // Login → Dashboard → Meeting Management → Quarter → Meeting List → Services
    }
}
```

#### Run Tests

```bash
cd app
./gradlew connectedAndroidTest
```

---

### Task 5.3: User Acceptance Testing
**Time:** 6 hours | **Complexity:** Low | **Priority:** HIGH

#### Create UAT Test Plan

**File:** `USER_ACCEPTANCE_TEST_PLAN.md`

```markdown
# User Acceptance Testing - User Flow

## Test Environment
- Backend: http://your-backend-url.com
- App Version: v1.0.0
- Test Users:
  - testuser1 (assigned to MRFC 1, 3)
  - testuser2 (assigned to MRFC 2, 4, 5)
  - adminuser (admin role)

## Test Scenarios

### Scenario 1: MRFC Management Flow
**Objective:** Verify user can navigate MRFC → Proponent → Quarter → Services

**Steps:**
1. Login as testuser1
2. Click "View MRFC" on dashboard
3. Verify only MRFCs 1 and 3 are visible
4. Click MRFC 1
5. Verify proponent details load
6. Click "View Agenda"
7. Select "1st Quarter 2025"
8. Verify Services Menu displays
9. Click "MTF Disbursement"
10. Verify MTF reports load

**Expected Result:** ✅ All steps complete successfully
**Actual Result:** _________
**Status:** PASS / FAIL
**Notes:** _________

### Scenario 2: Meeting Management Flow
**Objective:** Verify user can access meetings from dashboard

**Steps:**
1. Login as testuser1
2. Click "Meeting Management" on dashboard
3. Select "2nd Quarter 2025"
4. Verify meeting list loads
5. Click first meeting
6. Verify 3-tab interface (Agenda, Attendance, Minutes)
7. Click Attendance tab
8. Verify attendance list displays
9. Click "Log Attendance"
10. Fill name, position, department
11. Capture photo
12. Submit
13. Verify attendance appears in list

**Expected Result:** ✅ Attendance logged with photo
**Actual Result:** _________
**Status:** PASS / FAIL

### Scenario 3: Document Viewing
**Objective:** Verify user can view all document types

**Steps:**
1. Navigate to Services Menu (via MRFC or Meeting path)
2. Click "MTF Disbursement"
3. Verify MTF reports display
4. Go back, click "AEPEP Report"
5. Verify AEPEP documents display
6. Go back, click "CMVR"
7. Verify CMVR reports display
8. Go back, click "Documents"
9. Verify all documents display with filters

**Expected Result:** ✅ All document types accessible
**Actual Result:** _________
**Status:** PASS / FAIL

### Scenario 4: Personal Notes
**Objective:** Verify user can create and manage notes

**Steps:**
1. Navigate to any screen with notes access
2. Click "Personal Notes"
3. Click "+" FAB
4. Enter note title and content
5. Save note
6. Verify note appears in list
7. Click note to edit
8. Modify content
9. Save changes
10. Delete note
11. Confirm deletion

**Expected Result:** ✅ Full CRUD operations work
**Actual Result:** _________
**Status:** PASS / FAIL

### Scenario 5: User Access Control
**Objective:** Verify users only see assigned MRFCs

**Steps:**
1. Login as testuser1 (assigned to MRFC 1, 3)
2. Go to MRFC list
3. Count visible MRFCs
4. Verify only 2 MRFCs visible
5. Logout
6. Login as testuser2 (assigned to MRFC 2, 4, 5)
7. Go to MRFC list
8. Count visible MRFCs
9. Verify only 3 MRFCs visible

**Expected Result:** ✅ Each user sees only assigned MRFCs
**Actual Result:** _________
**Status:** PASS / FAIL
```

#### Conduct UAT Session

1. Recruit 3-5 actual MGB users
2. Provide test credentials
3. Walk through each scenario
4. Record results
5. Collect feedback
6. Fix critical issues

---

### Task 5.4: Performance Testing
**Time:** 4 hours | **Complexity:** Medium | **Priority:** MEDIUM

#### Backend Load Testing

**Tool:** Apache JMeter

**Test Plan:**
```xml
<!-- Load test configuration -->
<ThreadGroup>
  <numThreads>100</numThreads> <!-- 100 concurrent users -->
  <rampUp>10</rampUp> <!-- Ramp up over 10 seconds -->
  <loops>10</loops> <!-- Each user performs 10 requests -->
</ThreadGroup>

<HTTPSampler>
  <domain>your-backend-url.com</domain>
  <port>443</port>
  <path>/api/v1/mrfcs</path>
  <method>GET</method>
  <header name="Authorization" value="Bearer ${token}"/>
</HTTPSampler>
```

**Performance Targets:**
- Average response time: < 500ms
- 95th percentile: < 1000ms
- Error rate: < 1%
- Throughput: > 50 requests/second

#### Frontend Performance Testing

**Tool:** Android Profiler in Android Studio

**Metrics to Monitor:**
- App startup time: < 2 seconds
- Screen navigation time: < 500ms
- Memory usage: < 150MB
- Battery consumption: Normal range

**Test Cases:**
1. List 100 MRFCs - should remain smooth
2. Load 50 documents - no lag
3. Display 100 attendance records - smooth scrolling
4. Navigate between screens 50 times - no memory leaks

---

### Task 5.5: Production Deployment
**Time:** 2 hours | **Complexity:** Low | **Priority:** CRITICAL

#### Backend Deployment Checklist

- [ ] Update environment variables
- [ ] Set production database URL
- [ ] Enable HTTPS only
- [ ] Configure CORS for production domain
- [ ] Set up error logging (Sentry/LogRocket)
- [ ] Enable rate limiting
- [ ] Set up database backups
- [ ] Configure Cloudinary production account
- [ ] Deploy to production server
- [ ] Run smoke tests on production

#### Frontend Deployment Checklist

- [ ] Update API base URL to production
- [ ] Generate signed APK
- [ ] Test signed APK on real devices
- [ ] Create release notes
- [ ] Upload to internal testing (Firebase App Distribution)
- [ ] Conduct final testing
- [ ] Upload to Google Play Store (internal track)
- [ ] Promote to beta track
- [ ] Monitor crash reports

---

### Phase 5 Deliverables

**Testing:**
- [x] Backend API tests (100% passing)
- [x] Frontend E2E tests (all scenarios passing)
- [x] User acceptance testing (completed with sign-off)
- [x] Performance testing (meets targets)

**Documentation:**
- [x] Test results documented
- [x] UAT sign-off obtained
- [x] Deployment guide created
- [x] User manual updated

**Deployment:**
- [x] Backend deployed to production
- [x] Frontend APK released to internal testing
- [x] Monitoring set up
- [x] Support process established

---

## 📊 OVERALL PROJECT SUMMARY

### Time Breakdown

| Phase | Duration | Days |
|-------|----------|------|
| Phase 1: Backend Foundation | 40 hours | 5 days |
| Phase 2: User Services | 40 hours | 5 days |
| Phase 3: Frontend Completion | 30 hours | 4 days |
| Phase 4: Navigation & Polish | 20 hours | 3 days |
| Phase 5: Testing & Deployment | 30 hours | 4 days |
| **TOTAL** | **160 hours** | **21 days** |

### Resource Requirements

**Development Team:**
- 1 Backend Developer (2 weeks full-time)
- 1 Android Developer (2 weeks full-time)
- 1 QA Engineer (1 week full-time)

**Infrastructure:**
- Production server (AWS/Heroku/Railway)
- PostgreSQL database (production)
- Cloudinary account (production)
- Domain & SSL certificate

**Budget Estimate:**
- Development: $8,000 - $12,000 (160 hours @ $50-75/hr)
- Infrastructure: $100 - $200/month
- Testing: $1,000 - $2,000

### Success Criteria

**Functional Requirements:**
- [x] All user flow paths work end-to-end
- [x] Users see only assigned MRFCs
- [x] All document types accessible
- [x] Meeting management fully functional
- [x] Attendance with photo upload works
- [x] Personal notes sync to cloud
- [x] Zero HTTP 501 errors

**Non-Functional Requirements:**
- [x] Response time < 500ms (95th percentile)
- [x] App startup < 2 seconds
- [x] Zero crashes during UAT
- [x] 100% flowchart alignment

**User Satisfaction:**
- [x] UAT sign-off from 3+ users
- [x] All critical bugs fixed
- [x] User manual completed
- [x] Training materials prepared

---

## 🚀 NEXT STEPS

### Immediate Actions (This Week)

1. **Review This Plan** with stakeholders
2. **Assign Resources** (backend dev, android dev, QA)
3. **Set Up Project Board** (Jira/Trello/GitHub Projects)
4. **Create Sprints** (5 sprints, 1 per phase)
5. **Begin Phase 1** - Backend implementation

### Week 1 Tasks

**Monday:**
- [ ] Kick-off meeting
- [ ] Repository setup
- [ ] Development environment verification

**Tuesday-Friday:**
- [ ] Implement Proponent endpoints
- [ ] Implement Attendance endpoints
- [ ] Implement Agenda Item endpoints
- [ ] Test all new endpoints

---

## 📝 APPENDIX

### A. Backend File Structure

```
backend/src/
├── controllers/
│   ├── proponent.controller.ts ✅ IMPLEMENT
│   ├── attendance.controller.ts ✅ IMPLEMENT
│   ├── agendaItem.controller.ts ✅ IMPLEMENT
│   ├── matterArising.controller.ts ✅ CREATE NEW
│   ├── note.controller.ts ✅ IMPLEMENT
│   └── quarter.controller.ts ✅ IMPLEMENT
├── routes/
│   ├── proponent.routes.ts ✅ WIRE UP
│   ├── attendance.routes.ts ✅ WIRE UP
│   ├── agendaItem.routes.ts ✅ WIRE UP
│   ├── matterArising.routes.ts ✅ CREATE NEW
│   ├── note.routes.ts ✅ WIRE UP
│   └── quarter.routes.ts ✅ WIRE UP
└── __tests__/
    └── user-flow.test.ts ✅ CREATE NEW
```

### B. Frontend File Structure

```
app/src/main/java/com/mgb/mrfcmanager/ui/user/
├── MTFDisbursementActivity.kt ✅ CREATE NEW
├── AEPEPReportActivity.kt ✅ CREATE NEW
├── CMVRReportActivity.kt ✅ CREATE NEW
├── ResearchAccomplishmentsActivity.kt ✅ CREATE NEW
├── MRFCSelectionActivity.kt ✅ UPDATE
├── ProponentViewActivity.kt ✅ UPDATE
├── DocumentListActivity.kt ✅ UPDATE
├── NotesActivity.kt ✅ UPDATE
├── AgendaViewActivity.kt ✅ UPDATE
└── UserDashboardActivity.kt ✅ UPDATE
```

### C. API Endpoint Reference

**User-Accessible Endpoints:**
```
GET    /api/v1/mrfcs                    ✅ User-filtered
GET    /api/v1/mrfcs/:id                ✅ With access check
GET    /api/v1/proponents               ⚠️ Implement
GET    /api/v1/proponents/:id           ⚠️ Implement
GET    /api/v1/quarters                 ⚠️ Implement
GET    /api/v1/agendas                  ✅ Working
GET    /api/v1/agendas/:id              ✅ Working
GET    /api/v1/agenda-items/agenda/:id  ⚠️ Implement
GET    /api/v1/attendance               ⚠️ Implement
POST   /api/v1/attendance/with-photo    ⚠️ Implement
GET    /api/v1/documents                ⚠️ Implement
GET    /api/v1/documents/:id/download   ⚠️ Implement
GET    /api/v1/minutes/meeting/:id      ✅ Working
GET    /api/v1/matters-arising/agenda/:id ⚠️ Implement
GET    /api/v1/notes                    ⚠️ Implement
POST   /api/v1/notes                    ⚠️ Implement
PUT    /api/v1/notes/:id                ⚠️ Implement
DELETE /api/v1/notes/:id                ⚠️ Implement
GET    /api/v1/notifications            ✅ Working
```

---

**Document Version:** 1.0
**Last Updated:** November 4, 2025
**Status:** APPROVED FOR IMPLEMENTATION
**Next Review:** After Phase 1 Completion

---

**END OF IMPLEMENTATION PLAN**

