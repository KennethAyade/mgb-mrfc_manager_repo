# ==============================================
# CLOUDINARY 401 ERROR - COMPLETE FIX GUIDE
# ==============================================

## PROBLEM DIAGNOSIS
Your error shows: "Cloudinary returned status: 401"

This happens because documents were uploaded with AUTHENTICATED access mode
before the code was fixed to use PUBLIC access mode.

## THE FIX

### Step 1: Clear Old Documents from Database
cd backend
psql -U postgres -d mrfc_manager -f clear-documents.sql

### Step 2: Verify Documents are Cleared
psql -U postgres -d mrfc_manager -c "SELECT COUNT(*) FROM documents;"
# Should show 0

### Step 3: Restart Backend (if running)
# Press Ctrl+C in the terminal running the backend
# Then run: npm run dev

### Step 4: Re-upload Documents
Open your Android app and upload test documents.
The new uploads will have PUBLIC access and work correctly!

## ALTERNATIVE: Make Existing Cloudinary Files Public

If you want to keep existing files, you need to update their access mode in Cloudinary:

1. Go to Cloudinary Dashboard: https://cloudinary.com/console
2. Navigate to Media Library
3. Go to folder: mgb-mrfc/documents  
4. For each file:
   - Click on the file
   - Click "Edit" or "Settings"
   - Change Access Mode to "Public"
   - Save

Then your existing document URLs will work without re-uploading.

## WHY THIS HAPPENED

Cloudinary's default access mode depends on account settings:
- Some accounts default to PUBLIC access
- Some accounts default to AUTHENTICATED access

The code now explicitly sets access_mode: 'public' to prevent this issue.

## VERIFY THE FIX WORKS

After clearing and re-uploading:

1. Upload a new document via the app
2. Check backend logs - should show:
   ✅ Uploaded to Cloudinary: [public_id]
   📍 Access mode: public
   📍 URL: https://res.cloudinary.com/...

3. Click on the document in the app
4. It should open successfully without 401 errors!

## CLOUDINARY SECURITY SETTINGS

Your current security settings are CORRECT:
✅ "Uploaded" is NOT restricted (good!)
✅ "PDF and ZIP files delivery" is enabled (good!)

The issue is NOT with security settings, but with the ACCESS MODE
of existing uploaded files.

