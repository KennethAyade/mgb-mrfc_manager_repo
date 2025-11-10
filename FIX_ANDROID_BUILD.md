# Fix Android Build Error

## Error: "Could not load module <Error module>"

This is a kapt/Kotlin compiler cache issue that happens after deleting files.

## Solution: Invalidate Caches in Android Studio

### Step 1: In Android Studio
1. Click **File** → **Invalidate Caches**
2. Check **all boxes**:
   - [x] Clear file system cache and Local History
   - [x] Clear VCS Log caches and indexes
   - [x] Clear downloaded shared indexes
3. Click **Invalidate and Restart**
4. Wait for Android Studio to restart and re-index

### Step 2: Clean Build
After Android Studio restarts:
```bash
cd C:\Users\solis\source\repos\mgb-mrfc_manager_repo
.\gradlew.bat clean
.\gradlew.bat :app:assembleDebug
```

### Step 3: If Still Fails
Try building directly in Android Studio:
1. Click **Build** → **Clean Project**
2. Click **Build** → **Rebuild Project**
3. Click **Run** (▶️)

---

## What I Did

### Restored Files:
1. ✅ `app/src/main/java/com/mgb/mrfcmanager/utils/DemoData.kt`
   - Kept for backward compatibility
   - Not actively used (app uses backend data)

2. ✅ `app/src/main/java/com/mgb/mrfcmanager/data/model/Document.kt`
   - Minimal local model
   - App uses DocumentDto from backend

### Downgraded Kotlin:
- Changed from Kotlin 2.0.21 to 1.9.24
- Better kapt support

---

## Why This Happens

Kapt (Kotlin Annotation Processing Tool) caches generated code. When you delete source files, the cache can get corrupted and reference non-existent modules.

**Solution**: Invalidate caches to rebuild everything from scratch.

---

## Alternative: Use Android Studio UI

If command line doesn't work:
1. Open project in Android Studio
2. File → Invalidate Caches → Invalidate and Restart
3. After restart: Build → Rebuild Project
4. Run the app

This always works! ✅

---

## Status

**Files**: ✅ Restored  
**Kotlin**: ✅ Downgraded to 1.9.24  
**Next Step**: Invalidate caches in Android Studio  

The kapt error should be resolved after invalidating caches!

