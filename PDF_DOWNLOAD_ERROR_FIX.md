# PDF Download Error Fix

**Date:** November 8, 2025  
**Version:** 1.3.5  
**Issue:** "Failed to open PDF" error when clicking uploaded documents

---

## 📋 Problem Description

Users reported that when trying to open an uploaded PDF file, the app:
1. Shows "Downloading PDF..." toast message
2. Then immediately shows error: "Failed to open PDF: https://res.cloudinary.com/drxibb7np/raw/uploa..."
3. The error message is truncated and doesn't show the full error details

### Screenshot Evidence
The error appears at the bottom of the screen with a partial Cloudinary URL visible.

---

## 🔍 Root Cause Analysis

The issue could be one of several problems:

1. **Cloudinary Access Restrictions**
   - Files uploaded as `resource_type: 'raw'` (PDFs, docs) might have access restrictions
   - Even with `access_mode: 'public'`, some Cloudinary accounts have additional restrictions

2. **Network/Connection Issues**
   - No timeout settings on the download connection
   - No proper error handling for network failures
   - SSL/TLS issues with Android's network security config

3. **Poor Error Reporting**
   - Error messages were truncated (showing only partial URL)
   - No detailed logging to diagnose the actual issue
   - No HTTP status code visibility

4. **Missing Request Headers**
   - No User-Agent header being sent
   - Potential rejection by Cloudinary's CDN

---

## ✅ Solution Implemented

### 1. Enhanced Error Handling & Logging

**File:** `app/src/main/java/com/mgb/mrfcmanager/ui/admin/DocumentListActivity.kt`

#### Changes Made:

**A. Improved `downloadPdfToCache()` Method:**

```kotlin
private suspend fun downloadPdfToCache(url: String, fileName: String): java.io.File = withContext(Dispatchers.IO) {
    try {
        val connection = java.net.URL(url).openConnection() as java.net.HttpURLConnection
        connection.requestMethod = "GET"
        connection.connectTimeout = 30000 // 30 seconds
        connection.readTimeout = 30000 // 30 seconds
        connection.setRequestProperty("User-Agent", "MGB MRFC Manager/1.0")
        
        val responseCode = connection.responseCode
        android.util.Log.d("DocumentList", "HTTP Response Code: $responseCode")
        
        if (responseCode == java.net.HttpURLConnection.HTTP_OK) {
            // Download file...
        } else {
            // User-friendly error messages for different HTTP codes
            val errorMsg = when (responseCode) {
                401 -> "Unauthorized access to file (401)"
                403 -> "Access forbidden (403)"
                404 -> "File not found (404)"
                500 -> "Server error (500)"
                else -> "HTTP error code: $responseCode"
            }
            throw java.io.IOException(errorMsg)
        }
    } catch (e: java.net.SocketTimeoutException) {
        throw java.io.IOException("Download timed out. Please check your internet connection.")
    } catch (e: Exception) {
        throw e
    }
}
```

**Key Improvements:**
- ✅ **30-second timeout** for connect and read operations
- ✅ **User-Agent header** to identify the app to CDN
- ✅ **HTTP status code checking** with user-friendly error messages
- ✅ **Detailed logging** for debugging (URL, response code, content length, bytes downloaded)
- ✅ **Proper exception handling** for timeouts, network errors, invalid URLs
- ✅ **File verification** to ensure download completed successfully

**B. Enhanced `onDocumentClicked()` Method:**

```kotlin
private fun onDocumentClicked(document: DocumentDto) {
    android.util.Log.d("DocumentList", "Opening document: ${document.originalName}")
    android.util.Log.d("DocumentList", "File URL: ${document.fileUrl}")
    
    lifecycleScope.launch {
        try {
            // Validate URL before downloading
            if (document.fileUrl.isBlank()) {
                throw IllegalStateException("File URL is empty")
            }
            
            // Download and open PDF...
            
        } catch (e: java.io.IOException) {
            Toast.makeText(
                this@DocumentListActivity,
                "Download failed: ${e.message}",
                Toast.LENGTH_LONG
            ).show()
        } catch (e: Exception) {
            Toast.makeText(
                this@DocumentListActivity,
                "Failed to open PDF: ${e.javaClass.simpleName} - ${e.message}",
                Toast.LENGTH_LONG
            ).show()
        }
    }
}
```

**Key Improvements:**
- ✅ **URL validation** before attempting download
- ✅ **Detailed logging** of document name and URL
- ✅ **Specific error handling** for different exception types
- ✅ **Full error messages** (not truncated) including exception class name
- ✅ **Check for PDF viewer apps** before attempting to open

---

## 🧪 Testing Instructions

### Step 1: View Android Logs

Open **Logcat** in Android Studio and filter by tag: `DocumentList`

You'll see detailed logs like:
```
D/DocumentList: Opening document: CMVR-3Q-Dingras-Walter-E.-Galano.pdf
D/DocumentList: File URL: https://res.cloudinary.com/drxibb7np/raw/upload/...
D/DocumentList: Downloading PDF from: https://res.cloudinary.com/drxibb7np/raw/upload/...
D/DocumentList: HTTP Response Code: 200
D/DocumentList: Content Length: 6291456 bytes
D/DocumentList: Downloaded 6291456 bytes
D/DocumentList: PDF ready: /data/user/0/com.mgb.mrfcmanager/cache/pdfs/CMVR-3Q-Dingras-Walter-E.-Galano.pdf (6291456 bytes)
```

### Step 2: Test PDF Opening

1. **Start Backend**
   ```bash
   cd backend
   npm run dev
   ```

2. **Run Android App**
   - Open in Android Studio
   - Run on emulator

3. **Navigate to Documents**
   - Login as Admin
   - Open any MRFC
   - Click on a Proponent
   - Select a Quarter
   - Click on any service button (e.g., "View CMVR")

4. **Click on a PDF File**
   - You should see "Downloading PDF..." toast
   - Check Logcat for detailed logs
   - PDF should open in "Open with" dialog

### Step 3: Identify the Error

If the PDF fails to open, check **Logcat** for the exact error:

**Scenario A: HTTP 401 or 403 Error**
```
D/DocumentList: HTTP Response Code: 401
E/DocumentList: IO Error opening PDF: java.io.IOException: Unauthorized access to file (401)
```
**Solution:** Old files with restricted access. Re-upload the file or run clear-documents script.

**Scenario B: Network Timeout**
```
E/DocumentList: Download timeout: java.net.SocketTimeoutException
```
**Solution:** Check internet connection. The 30-second timeout should be sufficient for most files.

**Scenario C: Invalid URL**
```
E/DocumentList: Invalid URL: https://...
```
**Solution:** Check backend Cloudinary configuration and database records.

**Scenario D: File Not Found (404)**
```
D/DocumentList: HTTP Response Code: 404
```
**Solution:** File was deleted from Cloudinary but record exists in database.

---

## 🔧 Possible Solutions

### Solution 1: Re-upload Files (Recommended)

If you're getting 401/403 errors on old files:

1. **Clear Old Documents** (if needed)
   ```bash
   cd backend
   psql -U your_username -d mgb_mrfc -f clear-documents.sql
   ```

2. **Upload New Files**
   - Use the File Upload feature in the app
   - New files will have `access_mode: 'public'` set correctly

### Solution 2: Verify Cloudinary Settings

Check `backend/src/config/cloudinary.ts`:

```typescript
export const uploadToCloudinary = async (
  filePath: string,
  folder: string,
  resourceType: 'image' | 'video' | 'raw' | 'auto' = 'auto'
) => {
  const result = await cloudinary.uploader.upload(filePath, {
    folder: folder,
    resource_type: resourceType,
    use_filename: true,
    unique_filename: true,
    access_mode: 'public',  // ✅ Must be present
    type: 'upload'           // ✅ Must be 'upload' not 'authenticated'
  });
  // ...
};
```

### Solution 3: Check Cloudinary Account Settings

1. Login to [Cloudinary Dashboard](https://cloudinary.com/console)
2. Go to **Settings** → **Upload**
3. Ensure **"Enable unsigned uploading"** is OFF (we use signed uploads)
4. Check **"Delivery and storage settings"** for any restrictions

### Solution 4: Network Security Config (if needed)

If you're getting SSL/TLS errors, add to `app/src/main/AndroidManifest.xml`:

```xml
<application
    android:networkSecurityConfig="@xml/network_security_config"
    ...>
```

Create `app/src/main/res/xml/network_security_config.xml`:

```xml
<?xml version="1.0" encoding="utf-8"?>
<network-security-config>
    <domain-config cleartextTrafficPermitted="false">
        <domain includeSubdomains="true">res.cloudinary.com</domain>
        <trust-anchors>
            <certificates src="system" />
        </trust-anchors>
    </domain-config>
</network-security-config>
```

---

## 📊 Expected Behavior After Fix

### ✅ Success Case:

1. User clicks on PDF document
2. Toast shows: "Downloading PDF..."
3. Logcat shows:
   ```
   D/DocumentList: Opening document: filename.pdf
   D/DocumentList: File URL: https://res.cloudinary.com/...
   D/DocumentList: HTTP Response Code: 200
   D/DocumentList: Content Length: XXXXX bytes
   D/DocumentList: Downloaded XXXXX bytes
   D/DocumentList: PDF ready: /path/to/cache/filename.pdf
   ```
4. "Open PDF with" dialog appears
5. User selects PDF viewer app
6. PDF opens successfully

### ❌ Error Cases (with clear messages):

**Network Timeout:**
- Toast: "Download timed out. Please check your internet connection."

**Access Denied:**
- Toast: "Download failed: Unauthorized access to file (401)"

**File Not Found:**
- Toast: "Download failed: File not found (404)"

**No PDF Viewer:**
- Toast: "No PDF viewer app found. Please install one."

---

## 📁 Files Modified

### Android (Kotlin)

1. **`app/src/main/java/com/mgb/mrfcmanager/ui/admin/DocumentListActivity.kt`**
   - Enhanced `downloadPdfToCache()` with proper HTTP connection handling
   - Added timeouts (30 seconds connect/read)
   - Added User-Agent header
   - Added HTTP status code checking
   - Added detailed logging for debugging
   - Enhanced `onDocumentClicked()` with URL validation and better error handling
   - Improved error messages (full details, not truncated)

### Documentation

2. **`PDF_DOWNLOAD_ERROR_FIX.md`** (this file)
   - Comprehensive fix documentation
   - Troubleshooting guide
   - Testing instructions

---

## 🎯 Next Steps

1. **Test with Current Setup:**
   - Deploy the updated APK
   - Try opening the PDF again
   - Check Logcat for detailed error message
   - Share the error logs for further diagnosis

2. **If 401/403 Error:**
   - Delete and re-upload the document
   - New uploads will have correct public access settings

3. **If Network Timeout:**
   - Check internet connection speed
   - Try with a smaller file first
   - Consider increasing timeout if needed

4. **If Still Failing:**
   - Share the full Logcat output (filter by `DocumentList`)
   - Include the exact error message
   - Include the HTTP response code

---

## 📝 Additional Notes

### Why HttpURLConnection Instead of Generic URLConnection?

- ✅ Access to HTTP-specific features (response codes, headers)
- ✅ Better control over timeouts
- ✅ Proper connection management
- ✅ More reliable for large file downloads

### Why 30-Second Timeout?

- **Connect Timeout:** 30 seconds is sufficient for establishing connection
- **Read Timeout:** 30 seconds for a 6MB file at ~200KB/s = adequate
- For slower connections, this may need adjustment

### Why User-Agent Header?

- Some CDNs (including Cloudinary) may reject requests without User-Agent
- Helps identify legitimate app traffic vs. scrapers
- Best practice for HTTP clients

---

## 🔗 Related Documentation

- **Main Status:** [PROJECT_STATUS.md](./PROJECT_STATUS.md)
- **Dynamic Progress Bar:** [DYNAMIC_PROGRESS_BAR_FIX.md](./DYNAMIC_PROGRESS_BAR_FIX.md)
- **Cloudinary Fix:** [CLOUDINARY_FIX_GUIDE.md](./CLOUDINARY_FIX_GUIDE.md)

---

**Next Action:** Test the updated app and check Logcat for the specific error details. The improved error handling will show exactly what's failing! 🔍

