# Cloudinary 401 Error Fix Guide

## 🐛 Problem

When clicking on uploaded documents in the app, you get **HTTP ERROR 401** from Cloudinary. This happens because documents were uploaded with **restricted access** instead of **public access**.

---

## ✅ Solution

The issue has been fixed in the code by adding `access_mode: 'public'` to the Cloudinary upload configuration. However, **existing uploaded documents** still have restricted access.

---

## 🔧 Steps to Fix

### Option 1: Clear Database Only (Recommended)

If you don't have many documents or don't mind leaving orphaned files in Cloudinary:

1. **Stop the backend** (Ctrl+C in the terminal where backend is running)

2. **Run the SQL script** using PostgreSQL:
   ```bash
   cd backend
   psql -U postgres -d mrfc_manager -f clear-documents.sql
   ```
   
   Or if you have a different database name/user:
   ```bash
   psql -U your_username -d your_database_name -f clear-documents.sql
   ```

3. **Restart the backend**:
   ```bash
   npm run dev
   ```

4. **Re-upload test documents** - New uploads will now be publicly accessible!

### Option 2: Clear Both Database and Cloudinary (Clean)

If you want to completely clean up:

1. **Login to Cloudinary Dashboard**:
   - Go to: https://cloudinary.com/console
   - Navigate to **Media Library**
   - Go to the `mgb-mrfc/documents` folder
   - Select all files and **Delete**

2. **Clear database** (follow Option 1 steps 1-4)

3. **Re-upload test documents**

---

## 🧪 Testing the Fix

1. **Re-upload a document**:
   - Open the app
   - Navigate to a Proponent
   - Select a Quarter
   - Click "File Upload"
   - Upload a new PDF

2. **Click on the uploaded document**:
   - It should now open successfully in a PDF viewer
   - No more 401 errors!

---

## 📝 What Changed in Code

**File:** `backend/src/config/cloudinary.ts`

Added public access configuration:
```typescript
const result = await cloudinary.uploader.upload(filePath, {
  folder: folder,
  resource_type: resourceType,
  use_filename: true,
  unique_filename: true,
  access_mode: 'public',  // ✅ NEW: Ensures public access
  type: 'upload'           // ✅ NEW: Upload type
});
```

---

## ❓ Why This Happened

Cloudinary accounts can have different default access modes:
- Some accounts default to **public** access (URLs work without authentication)
- Some accounts default to **authenticated** access (URLs require authentication)

By explicitly setting `access_mode: 'public'`, we ensure all uploads are publicly accessible.

---

## 🚨 Important Notes

- **New uploads** after the code fix will work correctly
- **Old uploads** will continue to show 401 errors until you clear and re-upload them
- This only affects **document files** (PDFs), not other app functionality
- The fix is already applied in the code, just need to clear old data

---

## 💡 Quick Test Commands

Check if documents exist in database:
```bash
psql -U postgres -d mrfc_manager -c "SELECT COUNT(*) FROM documents;"
```

View all documents:
```bash
psql -U postgres -d mrfc_manager -c "SELECT id, file_name, category FROM documents;"
```

---

## Need Help?

If you encounter issues:
1. Make sure the backend is running (`npm run dev`)
2. Check backend logs for errors
3. Verify PostgreSQL is running
4. Ensure Cloudinary credentials are set in `.env`

