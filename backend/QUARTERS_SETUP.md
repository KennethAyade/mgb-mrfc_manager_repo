# Quarters Setup Guide

## ⚠️ IMPORTANT: Quarters are Required!

The **quarters table MUST be seeded** for the following features to work:

### Features that depend on Quarters:
1. **📤 File Upload** - Cannot upload documents without quarters (shows "Please wait, loading quarters")
2. **📋 Document Management** - Documents are organized by quarter
3. **📊 Compliance Analysis** - Compliance is tracked per quarter
4. **📅 Meeting Agendas** - Meetings are organized by quarter
5. **🔍 Document Filtering** - Users filter documents by quarter (Q1/Q2/Q3/Q4)

---

## 🚀 Quick Setup

### If you just reset the database:
```bash
cd backend
npm run db:seed
```

### If quarters already exist:
```bash
# The seed script will detect existing quarters and skip
npm run db:seed
```

---

## 📊 What Gets Seeded

The seed script creates **4 quarters for 2025**:

| Quarter | Name | Dates | Status |
|---------|------|-------|--------|
| Q1 | Q1-2025 | Jan 1 - Mar 31 | |
| Q2 | Q2-2025 | Apr 1 - Jun 30 | |
| Q3 | Q3-2025 | Jul 1 - Sep 30 | |
| Q4 | Q4-2025 | Oct 1 - Dec 31 | ✅ CURRENT |

---

## 🔧 Available Commands

```bash
# Seed quarters (safe, won't create duplicates)
npm run db:seed
npm run db:seed:quarters

# Reset entire database (auto-seeds quarters)
npm run db:reset

# Check if quarters exist
psql $DATABASE_URL -c "SELECT * FROM quarters WHERE year = 2025;"
```

---

## ❓ Troubleshooting

### Problem: "Please wait, loading quarters" never finishes

**Cause:** Quarters table is empty

**Solution:**
```bash
cd backend
npm run db:seed
```

### Problem: Error "Quarters already exist"

**Cause:** Quarters are already seeded

**Solution:** Nothing needed! This is normal behavior to prevent duplicates.

### Problem: Need quarters for different years

**Solution:** Edit `backend/scripts/seed-quarters.js` and add new quarters:

```javascript
const quarters2026 = [
  {
    name: 'Q1-2026',
    year: 2026,
    quarter_number: 1,
    start_date: '2026-01-01',
    end_date: '2026-03-31',
    is_current: false
  },
  // ... Q2, Q3, Q4
];
```

---

## 🔄 Database Reset Workflow

**After running `npm run db:reset`:**

✅ Quarters are **automatically seeded**! No manual action needed.

The reset script now:
1. Drops all tables
2. Recreates all tables
3. Creates superadmin user
4. **Seeds Q1-Q4 2025** ← NEW!

---

## 📝 Files Involved

| File | Purpose |
|------|---------|
| `backend/scripts/seed-quarters.js` | Seed script for quarters |
| `backend/scripts/reset-database.ts` | Database reset (includes quarter seeding) |
| `backend/package.json` | npm scripts configuration |
| `backend/src/models/Quarter.ts` | Quarter model definition |
| `backend/src/routes/quarter.routes.ts` | Quarter API endpoints |

---

## 🎯 API Endpoints

```
GET  /api/v1/quarters        - List all quarters
GET  /api/v1/quarters/:id    - Get quarter by ID
GET  /api/v1/quarters/year/:year - Get quarters by year
POST /api/v1/quarters        - Create quarter (admin only)
```

---

## 💡 Developer Notes

- **Q4 2025 is marked as `is_current: true`** (we're in November 2025)
- File upload defaults to **Q4** if no selection made
- Document filtering supports **"All Quarters"** option
- Quarters are **required**, not optional
- Future years must be seeded manually or script updated

---

**Last Updated:** November 11, 2025  
**Version:** 2.0.2

