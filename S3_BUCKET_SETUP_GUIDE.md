# S3 Bucket Setup Guide

## Error Fixed: "The bucket does not allow ACLs"

### The Problem:
Your S3 bucket has **ACLs disabled** (modern AWS best practice). The code was trying to set `ACL: 'public-read'` which is not allowed.

### The Solution:
✅ **Removed ACL from upload code**  
✅ **Use bucket policy instead** for public access

---

## S3 Bucket Configuration

### Step 1: Create/Configure Bucket

1. Go to **AWS Console** → **S3**
2. Select bucket: `adhub-s3-demo`
3. Go to **Permissions** tab

### Step 2: Block Public Access Settings

**Uncheck these (to allow public access):**
- [ ] Block all public access
- [ ] Block public access to buckets and objects granted through new access control lists (ACLs)
- [ ] Block public access to buckets and objects granted through any access control lists (ACLs)
- [ ] Block public access to buckets and objects granted through new public bucket or access point policies
- [ ] Block public access to buckets and objects granted through any public bucket or access point policies

**Or just uncheck:**
- [ ] Block all public access

Click **Save changes**

### Step 3: Add Bucket Policy

Go to **Bucket Policy** and paste this:

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

**This allows:**
- ✅ Anyone can **read/download** files in `mgb-mrfc/` folder
- ✅ Only authenticated users can **upload/delete** (via IAM credentials)

Click **Save changes**

---

## Alternative: Private Files with Signed URLs

If you want **private files** (more secure):

### Option 1: Keep Files Private
**Don't add bucket policy** - files stay private

### Option 2: Use Pre-Signed URLs
The code already supports this:

```typescript
// Generate temporary download URL (expires in 1 hour)
const signedUrl = await getSignedDownloadUrl(s3Key, 3600);
```

Then update the Android app to request signed URLs instead of direct URLs.

---

## Current Setup (Recommended)

### Public Read Access:
- ✅ Files are publicly readable
- ✅ Direct URLs work in Android app
- ✅ No need for signed URLs
- ✅ Simpler implementation

### Private Write Access:
- ✅ Only backend can upload (via IAM credentials)
- ✅ Only backend can delete (via IAM credentials)
- ✅ Secure file management

---

## IAM User Permissions

Your IAM user needs these permissions:

```json
{
  "Version": "2012-10-17",
  "Statement": [
    {
      "Effect": "Allow",
      "Action": [
        "s3:PutObject",
        "s3:GetObject",
        "s3:DeleteObject",
        "s3:ListBucket"
      ],
      "Resource": [
        "arn:aws:s3:::adhub-s3-demo",
        "arn:aws:s3:::adhub-s3-demo/*"
      ]
    }
  ]
}
```

---

## Testing After Setup

### 1. Upload Test:
```
Android app → Upload PDF
    ↓
Backend → uploadToS3() (no ACL)
    ↓
S3 → Stores file
    ↓
Backend → Returns URL
    ↓
Android app → Shows success ✅
```

### 2. Download Test:
```
Android app → Click document
    ↓
Backend → downloadFromS3()
    ↓
S3 → Returns file
    ↓
Android app → Opens PDF ✅
```

### 3. Public Access Test:
Open the S3 URL directly in browser:
```
https://adhub-s3-demo.s3.us-east-1.amazonaws.com/mgb-mrfc/documents/file.pdf
```
Should download the file ✅

---

## What Changed in Code

### Before (With ACL):
```typescript
const command = new PutObjectCommand({
  Bucket: 'adhub-s3-demo',
  Key: 'mgb-mrfc/documents/file.pdf',
  Body: fileBuffer,
  ContentType: 'application/pdf',
  ACL: 'public-read' // ❌ Not supported if ACLs disabled
});
```

### After (Without ACL):
```typescript
const command = new PutObjectCommand({
  Bucket: 'adhub-s3-demo',
  Key: 'mgb-mrfc/documents/file.pdf',
  Body: fileBuffer,
  ContentType: 'application/pdf'
  // ✅ Uses bucket policy for public access
});
```

---

## Summary

### Error: "AccessControlListNotSupported"
**Cause:** Bucket has ACLs disabled  
**Solution:** Removed ACL from upload, use bucket policy instead

### Setup Required:
1. ✅ Unblock public access in S3 bucket settings
2. ✅ Add bucket policy for public read access
3. ✅ Verify IAM user has upload/delete permissions

### Code Changes:
- ✅ Removed `ACL: 'public-read'` from upload
- ✅ Backend will auto-restart
- ✅ Ready to test upload again

---

## Next Steps

1. ✅ **Configure S3 bucket** (unblock public access + add policy)
2. ✅ **Backend will auto-restart** (nodemon detected changes)
3. ✅ **Test upload** in Android app
4. ✅ **File should upload successfully!**

**After you configure the S3 bucket, uploads will work!** 🚀

