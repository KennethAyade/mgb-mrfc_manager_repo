# 🚀 Next Steps - Backend Implementation Plan

**Current Status:** 16/52 endpoints complete (31%)
**Last Completed:** User Management (6 endpoints) + MRFC Management (5 endpoints)
**Time Elapsed:** ~1 hour
**Estimated Remaining:** 3-4 days for all 36 endpoints

---

## 📊 Progress Overview

```
✅ COMPLETED (16/52)
├─ Authentication (5)
├─ MRFC Management (5)
└─ User Management (6)

🚧 TO IMPLEMENT (36/52)
├─ Proponent Management (5)
├─ Quarter Management (2)
├─ Agenda Management (5)
├─ Attendance Tracking (3)
├─ Document Management (6)
├─ Compliance Tracking (4)
└─ Supporting Features (11)
```

---

## 🎯 Immediate Next: Proponent Management (5 Endpoints)

### Why This First?
- ✅ Proponents are core business entities
- ✅ Required before Agenda Management
- ✅ Relatively simple CRUD operations
- ✅ Can be completed in ~30 minutes

### Endpoints to Implement

#### 1. **GET /proponents** - List Proponents
```
Endpoint:    GET /api/v1/proponents
Query Params:
  - mrfc_id (required): Filter by MRFC
  - page: Pagination (default 1)
  - limit: Results per page (default 20)
  - search: Search by name/company
  - is_active: Filter by status (default true)
  - sort_by: Sort field (created_at)
  - sort_order: ASC or DESC

Response:
{
  "success": true,
  "data": {
    "proponents": [...],
    "pagination": { page, limit, total, totalPages, hasNext, hasPrev }
  }
}
```

#### 2. **GET /proponents/:id** - Get Single Proponent
```
Endpoint:    GET /api/v1/proponents/:id
Auth:        Required

Response:
{
  "success": true,
  "data": {
    "id": "...",
    "mrfc_id": "...",
    "name": "...",
    "company": "...",
    "email": "...",
    "phone": "...",
    "is_active": true,
    "created_at": "...",
    "updated_at": "..."
  }
}
```

#### 3. **POST /proponents** - Create Proponent
```
Endpoint:    POST /api/v1/proponents
Auth:        Required (Admin+)

Body:
{
  "mrfc_id": "1",
  "name": "John Doe",
  "company": "Mining Corp",
  "email": "john@example.com",
  "phone": "09123456789"
}

Validation:
- All fields required
- Valid email format
- MRFC must exist and be active
- Unique: name + company per MRFC

Response: Created proponent with id
```

#### 4. **PUT /proponents/:id** - Update Proponent
```
Endpoint:    PUT /api/v1/proponents/:id
Auth:        Required (Admin+)

Body: (Can update any field)
{
  "name": "...",
  "company": "...",
  "email": "...",
  "phone": "...",
  "is_active": true/false
}

Logging: Audit log with old/new values
```

#### 5. **DELETE /proponents/:id** - Delete Proponent
```
Endpoint:    DELETE /api/v1/proponents/:id
Auth:        Required (Admin+)

Action: Soft delete (is_active = false)
Logging: Audit log with deletion details
```

### Implementation Steps

1. **Check Model**
   ```bash
   # File: backend/src/models/Proponent.ts
   # Should already exist from schema
   ```

2. **Create Controller**
   ```bash
   # File: backend/src/controllers/proponent.controller.ts
   # Implement 5 functions (same pattern as MRFC)
   ```

3. **Create Routes**
   ```bash
   # File: backend/src/routes/proponent.routes.ts
   # Mount 5 endpoints
   ```

4. **Test**
   ```bash
   # Update test-working.ps1 with proponent tests
   # Verify all 5 endpoints
   ```

5. **Update Status**
   ```bash
   # Update IMPLEMENTATION_STATUS.md
   # Mark as 5/5 complete
   ```

### Time Estimate: 30-45 minutes

---

## 📅 Phase 2: Quarter Management (2 Endpoints)

### Endpoints to Implement

#### 1. **GET /quarters** - List All Quarters
```
Endpoint:    GET /api/v1/quarters
No Auth:     Public (but can be filtered)
Query Params:
  - year: Filter by year (default current)
  - is_active: Filter by status

Response:
{
  "success": true,
  "data": {
    "quarters": [
      { "id": "1", "name": "Q1 2025", "start_date": "...", "end_date": "...", "year": 2025 },
      ...
    ]
  }
}
```

#### 2. **GET /quarters/current** - Get Current Quarter
```
Endpoint:    GET /api/v1/quarters/current
No Auth:     Public

Response:
{
  "success": true,
  "data": {
    "id": "...",
    "name": "Q4 2025",
    "start_date": "...",
    "end_date": "...",
    "year": 2025
  }
}
```

### Implementation Notes
- Quarters already created in database (2025 Q1-Q4)
- Just need to retrieve and format
- Simple endpoints, mostly query logic
- Time estimate: 15-20 minutes

---

## 📋 Phase 3: Agenda Management (5 Endpoints)

### Most Complex Module
- Meeting scheduling
- Standard agenda items
- Matters arising table
- Document associations
- Attendance tracking

### Time Estimate: 1.5-2 hours

### Endpoints
1. GET /agendas - List with filtering (mrfc_id, quarter_id)
2. GET /agendas/:id - Full agenda with items
3. POST /agendas - Create agenda
4. PUT /agendas/:id - Update agenda
5. DELETE /agendas/:id - Soft delete

---

## 📸 Phase 4: Attendance Tracking (3 Endpoints)

### Features
- Photo upload to Cloudinary
- Present/absent marking
- Timestamp recording
- Attendee linking

### Endpoints
1. GET /attendance - List by agenda
2. POST /attendance - Create with photo
3. PUT /attendance/:id - Update attendance

### Time Estimate: 1-1.5 hours

---

## 📁 Phase 5: Document Management (6 Endpoints)

### Features
- File upload to Cloudinary
- File download/streaming
- Category management
- Proponent-quarter linking

### Endpoints
1. GET /documents - List with filters
2. GET /documents/:id - Get details
3. GET /documents/:id/download - Stream file
4. POST /documents/upload - Upload file
5. PUT /documents/:id - Update metadata
6. DELETE /documents/:id - Delete file

### Time Estimate: 1.5-2 hours

---

## ⚙️ Phase 6: Compliance Tracking (4 Endpoints)

### Features
- Auto-calculate compliance percentage
- History tracking
- Override capability
- Dashboard analytics

### Endpoints
1. GET /compliance/proponent/:id - Single proponent compliance
2. GET /compliance/mrfc/:id - All proponents in MRFC
3. POST /compliance/calculate - Recalculate all
4. POST /compliance/override - Manual override

### Time Estimate: 1-1.5 hours

---

## 🎁 Phase 7: Supporting Features (11 Endpoints)

### Notes (4 endpoints)
- GET /notes - List
- POST /notes - Create
- PUT /notes/:id - Update
- DELETE /notes/:id - Delete

### Notifications (4 endpoints)
- GET /notifications - List
- POST /notifications - Create
- PATCH /notifications/:id/read - Mark read
- DELETE /notifications/:id - Delete

### Audit Logs (1 endpoint)
- GET /audit-logs - List with filters

### Statistics (2 endpoints)
- GET /statistics/dashboard - Dashboard stats
- GET /statistics/compliance - Compliance stats

### Time Estimate: 1.5-2 hours

---

## 📌 Detailed Timeline

### Today (if continuing)
- **Proponent Management** (45 min) - START HERE
- **Quarter Management** (20 min)
- **Quick Tests** (15 min)
- **Total:** ~80 minutes

### Tomorrow
- **Agenda Management** (2 hours)
- **Attendance Tracking** (1.5 hours)
- **Total:** ~3.5 hours

### Day 3
- **Document Management** (2 hours)
- **Compliance Tracking** (1.5 hours)
- **Total:** ~3.5 hours

### Day 4
- **Supporting Features** (2 hours)
- **Full Testing** (1 hour)
- **Documentation** (1 hour)
- **Total:** ~4 hours

### Overall: 3-4 days for all 52 endpoints

---

## 🛠️ Implementation Pattern (Copy-Paste)

### 1. Create Controller File
```typescript
// File: backend/src/controllers/proponent.controller.ts

import { Request, Response } from 'express';
import { Op } from 'sequelize';
import { Proponent, Mrfc, AuditLog } from '../models';
import { AuditAction } from '../models/AuditLog';
import sequelize from '../config/database';

/**
 * List all proponents
 * GET /api/v1/proponents
 */
export const listProponents = async (req: Request, res: Response): Promise<void> => {
  try {
    const { mrfc_id, page = '1', limit = '20', search = '', is_active = 'true', sort_by = 'created_at', sort_order = 'DESC' } = req.query;

    if (!mrfc_id) {
      res.status(400).json({
        success: false,
        error: { code: 'MISSING_FIELD', message: 'mrfc_id is required' }
      });
      return;
    }

    // Verify MRFC exists
    const mrfc = await Mrfc.findByPk(mrfc_id);
    if (!mrfc) {
      res.status(404).json({
        success: false,
        error: { code: 'MRFC_NOT_FOUND', message: 'MRFC not found' }
      });
      return;
    }

    const pageNum = parseInt(page as string);
    const limitNum = Math.min(parseInt(limit as string), 100);
    const offset = (pageNum - 1) * limitNum;

    const where: any = { mrfc_id };
    if (is_active !== 'all') {
      where.is_active = is_active === 'true';
    }
    if (search) {
      where[Op.or] = [
        { name: { [Op.iLike]: `%${search}%` } },
        { company: { [Op.iLike]: `%${search}%` } }
      ];
    }

    const { count, rows } = await Proponent.findAndCountAll({
      where,
      limit: limitNum,
      offset,
      order: [[sort_by as string, sort_order as string]]
    });

    res.json({
      success: true,
      data: {
        proponents: rows,
        pagination: {
          page: pageNum,
          limit: limitNum,
          total: count,
          totalPages: Math.ceil(count / limitNum),
          hasNext: pageNum < Math.ceil(count / limitNum),
          hasPrev: pageNum > 1
        }
      }
    });
  } catch (error) {
    console.error('Error listing proponents:', error);
    res.status(500).json({
      success: false,
      error: { code: 'SERVER_ERROR', message: 'Failed to list proponents' }
    });
  }
};

// ... implement other 4 functions similarly
```

### 2. Create Routes File
```typescript
// File: backend/src/routes/proponent.routes.ts

import { Router } from 'express';
import * as proponentController from '../controllers/proponent.controller';
import { authenticate, adminOnly } from '../middleware/auth';

const router = Router();

router.get('/', authenticate, proponentController.listProponents);
router.get('/:id', authenticate, proponentController.getProponentById);
router.post('/', authenticate, adminOnly, proponentController.createProponent);
router.put('/:id', authenticate, adminOnly, proponentController.updateProponent);
router.delete('/:id', authenticate, adminOnly, proponentController.deleteProponent);

export default router;
```

### 3. Add to Index Routes
```typescript
// In backend/src/routes/index.ts
import proponentRoutes from './proponent.routes';
router.use('/proponents', proponentRoutes);
```

### 4. Test
```bash
# Run test script
powershell -ExecutionPolicy Bypass -File test-working.ps1
```

---

## 📚 Reference Files

### Database Schema
- **File:** `backend/database/schema.sql`
- **Tables:** All 14 tables already created
- **Proponent Table:** Has columns for name, company, email, phone, mrfc_id

### Model Definitions
- **File:** `backend/src/models/`
- **Proponent.ts:** Already defined
- **Relationships:** Linked to MRFC

### Existing Controllers (Reference)
- **File:** `backend/src/controllers/mrfc.controller.ts`
- **File:** `backend/src/controllers/user.controller.ts`
- **Pattern:** Follow same structure

---

## ✅ Quality Checklist for Each Module

Before moving to next module, verify:

- [ ] Controller file created with all endpoints
- [ ] Route file created with all routes
- [ ] Routes mounted in index.ts
- [ ] TypeScript compilation successful
- [ ] Server starts without errors
- [ ] All tests pass
- [ ] Audit logging works
- [ ] Database transactions functional
- [ ] Error handling for all cases
- [ ] README/docs updated

---

## 🚀 Start Now!

### Quick Start for Proponent Management

1. **Create Controller**
   ```bash
   # Copy from MRFC controller pattern
   # Modify for Proponent model
   ```

2. **Create Routes**
   ```bash
   # Copy from MRFC routes pattern
   ```

3. **Add to Index**
   ```bash
   # Import and use in routes/index.ts
   ```

4. **Test**
   ```bash
   # Update test script or test manually
   ```

5. **Verify**
   ```bash
   # Run npm run dev and test-working.ps1
   ```

**Time for Proponent Management: ~45 minutes**

---

## 📞 Commands You'll Need

```bash
# Start server
cd backend
npm run dev

# Test (new terminal)
powershell -ExecutionPolicy Bypass -File test-working.ps1

# Build/compile
npm run build

# Check types
npm run type-check
```

---

## 📈 Success Criteria

Each module should have:
- ✅ All endpoints working
- ✅ Tests passing
- ✅ Database operations verified
- ✅ Audit logging confirmed
- ✅ Error handling tested
- ✅ Documentation updated

---

## 💡 Pro Tips

1. **Follow the Pattern** - Each module follows same CRUD pattern
2. **Copy-Paste Liberally** - Models already exist, just implement controllers
3. **Test as You Go** - Don't wait until end to test
4. **Keep it Simple** - Just CRUD operations, nothing fancy
5. **Update Status** - Keep IMPLEMENTATION_STATUS.md current

---

**Ready to start? Begin with Proponent Management!**

**Estimated Time:** ~45 minutes
**Difficulty:** Easy (just follow the pattern)
**Dependencies:** None (all models ready)

---

*Plan Created: October 25, 2025*
*Status: Ready to Implement*
*Next Module: Proponent Management (5 endpoints)*
