# AWS S3 Migration - COMPLETE ✅

## What Was Done

### 1. ✅ Installed AWS SDK
```bash
npm install @aws-sdk/client-s3 @aws-sdk/s3-request-presigner
```

### 2. ✅ Created S3 Configuration
**File**: `backend/src/config/s3.ts`

**Features:**
- Upload files to S3
- Delete files from S3
- Download files from S3
- Generate pre-signed URLs
- Configuration validation

### 3. ✅ Replaced Cloudinary with S3
**Files Modified:**
- `backend/src/controllers/document.controller.ts` - Document uploads/downloads
- `backend/src/routes/attendance.routes.ts` - Photo uploads

**Changes:**
- `uploadToCloudinary()` → `uploadToS3()`
- `deleteFromCloudinary()` → `deleteFromS3()`
- `downloadFromCloudinaryUrl()` → `downloadFromS3()`
- `CLOUDINARY_FOLDERS` → `S3_FOLDERS`

---

## Environment Variables Required

### Add to `backend/.env`:
```env
# AWS S3 Configuration
S3_BUCKET_NAME=adhub-s3-demo
AWS_ACCESS_KEY_ID=your_access_key_here
AWS_SECRET_ACCESS_KEY=your_secret_key_here
AWS_REGION=us-east-1
```

**Note:** You mentioned you already added these! ✅

---

## S3 Bucket Structure

```
adhub-s3-demo/
├── mgb-mrfc/
│   ├── documents/          ← PDF documents (CMVR, MTF, AEPEP, etc.)
│   ├── photos/             ← Attendance photos
│   └── temp/               ← Temporary files
```

---

## How It Works Now

### Document Upload:
```
1. User uploads PDF through Android app
   ↓
2. Backend receives file via Multer
   ↓
3. Backend calls uploadToS3()
   ↓
4. File uploaded to S3: adhub-s3-demo/mgb-mrfc/documents/
   ↓
5. Returns S3 URL: https://adhub-s3-demo.s3.us-east-1.amazonaws.com/mgb-mrfc/documents/file.pdf
   ↓
6. Saves URL and S3 key to database
   ↓
7. Deletes temporary local file
```

### Document Download:
```
1. User clicks download in Android app
   ↓
2. Backend receives download request
   ↓
3. Backend calls downloadFromS3()
   ↓
4. Downloads file from S3
   ↓
5. Streams to Android app
```

### Document Deletion:
```
1. Admin deletes document
   ↓
2. Backend calls deleteFromS3()
   ↓
3. Deletes from S3 bucket
   ↓
4. Deletes from database
```

---

## Database Fields

### No Schema Changes Needed!
The existing `documents` table works with S3:

```sql
CREATE TABLE documents (
  id BIGSERIAL PRIMARY KEY,
  file_url VARCHAR(500),           -- Now contains S3 URL
  file_cloudinary_id VARCHAR(255)  -- Now contains S3 key
  ...
);
```

**Note:** `file_cloudinary_id` field now stores the **S3 key** instead of Cloudinary public_id. No migration needed!

---

## S3 vs Cloudinary Comparison

### Cloudinary (Old):
```
URL: https://res.cloudinary.com/drxjbb7np/raw/upload/v1762683193/mgb-mrfc/documents/test-1762684196076-73358346_yih6ht.pdf
Public ID: mgb-mrfc/documents/test-1762684196076-73358346_yih6ht
```

### S3 (New):
```
URL: https://adhub-s3-demo.s3.us-east-1.amazonaws.com/mgb-mrfc/documents/test-1762684196076-73358346.pdf
S3 Key: mgb-mrfc/documents/test-1762684196076-73358346.pdf
```

---

## S3 Bucket Permissions

### Required S3 Bucket Policy:
```json
{
  "Version": "2012-10-17",
  "Statement": [
    {
      "Sid": "PublicReadGetObject",
      "Effect": "Allow",
      "Principal": "*",
      "Action": "s3:GetObject",
      "Resource": "arn:aws:s3:::adhub-s3-demo/mgb-mrfc/*"
    }
  ]
}
```

### IAM User Permissions:
Your AWS user needs these permissions:
- `s3:PutObject` - Upload files
- `s3:GetObject` - Download files
- `s3:DeleteObject` - Delete files
- `s3:ListBucket` - List files

---

## Testing

### 1. Check S3 Configuration:
```bash
cd backend
npx ts-node -e "
const s3 = require('./dist/config/s3').default;
console.log('S3 Configuration:', s3.getS3Info());
"
```

### 2. Test File Upload:
Upload a document through the Android app and check:
- ✅ File appears in S3 bucket
- ✅ URL is saved to database
- ✅ File can be downloaded

### 3. Test File Download:
Click on a document in Android app:
- ✅ File downloads from S3
- ✅ Opens in PDF viewer

---

## Migration for Existing Files

### Option 1: Keep Old Files in Cloudinary
- Old documents still use Cloudinary URLs
- New documents use S3 URLs
- Both work simultaneously

### Option 2: Migrate Old Files to S3
Create a migration script:
```bash
cd backend
npx ts-node src/scripts/migrate-cloudinary-to-s3.ts
```

---

## Advantages of S3

### Cost:
- ✅ **S3**: $0.023 per GB/month
- ✅ **Free tier**: 5GB storage, 20,000 GET requests/month

### Performance:
- ✅ **Faster downloads** in most regions
- ✅ **Better integration** with AWS services
- ✅ **More reliable** for large files

### Features:
- ✅ **Versioning** support
- ✅ **Lifecycle policies** for automatic archiving
- ✅ **Server-side encryption**
- ✅ **Fine-grained access control**

---

## Files Modified

### Backend (3 files):
1. ✅ `backend/src/config/s3.ts` - NEW: S3 configuration and helpers
2. ✅ `backend/src/controllers/document.controller.ts` - Replaced Cloudinary with S3
3. ✅ `backend/src/routes/attendance.routes.ts` - Replaced Cloudinary with S3

### Package.json:
4. ✅ Added `@aws-sdk/client-s3` and `@aws-sdk/s3-request-presigner`

---

## Next Steps

### 1. Verify Environment Variables
Check `backend/.env` has:
```env
S3_BUCKET_NAME=adhub-s3-demo
AWS_ACCESS_KEY_ID=AKIA...
AWS_SECRET_ACCESS_KEY=...
AWS_REGION=us-east-1
```

### 2. Restart Backend
```bash
cd backend
npm run dev
```
Backend will auto-restart with nodemon

### 3. Test Upload
1. Open Android app
2. Upload a new document
3. Check S3 bucket - file should appear
4. Check database - URL should be S3 URL

### 4. Test Download
1. Click on the uploaded document
2. File should download from S3
3. Should open in PDF viewer

---

## Troubleshooting

### If Upload Fails:
```
Error: Failed to upload file to S3: Access Denied
```
**Solution:** Check AWS credentials and S3 bucket permissions

### If Download Fails:
```
Error: Failed to download file from S3: NoSuchKey
```
**Solution:** Check if file exists in S3 bucket

### Check S3 Configuration:
```bash
cd backend
npx ts-node -e "
const { getS3Info } = require('./dist/config/s3');
console.log(getS3Info());
"
```

---

## Summary

✅ **AWS SDK installed**  
✅ **S3 configuration created**  
✅ **Document controller updated**  
✅ **Attendance routes updated**  
✅ **No database migration needed**  
✅ **Backend will auto-restart**  

**Status**: 🚀 **S3 MIGRATION COMPLETE**

All new file uploads will now go to AWS S3 instead of Cloudinary!

