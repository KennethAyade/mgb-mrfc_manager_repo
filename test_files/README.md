# 📁 Test Files for File Upload Feature

This folder is for storing test files that you want to transfer to the Android emulator for testing the file upload feature.

---

## 🎯 **Quick Guide: Testing File Uploads in Emulator**

### **Method 1: Drag & Drop (RECOMMENDED - Easiest!)**

1. **Start your Android Emulator** from Android Studio
2. **Drag ANY file** from your PC directly onto the emulator screen
3. The file is automatically saved to the emulator's **Download folder**
4. **Open the MGB MRFC Manager app**
5. Navigate to: **MRFC List → Click any MRFC → Click a Proponent → Select Quarter → File Upload**
6. Use the Android file picker - your files will be in **Downloads**!

✅ **This is the EASIEST method - no commands needed!**

---

### **Method 2: Using ADB Push Command**

If drag & drop doesn't work, use the batch script:

1. **Place your test PDF files** in this folder (`D:\FREELANCE\MGB\test_files\`)
2. **Run the batch script:**
   ```
   D:\FREELANCE\MGB\transfer_test_files.bat
   ```
3. **Follow the on-screen instructions**
4. Files will be transferred to the emulator's Download folder

---

## 📄 **Recommended Test Files**

For testing the MGB MRFC Manager file upload, you should test with:

### **NTE Disbursement Reports:**
- PDF files containing disbursement information
- Sample file names: `NTE_Disbursement_Q1_2024.pdf`

### **AEPEP Reports:**
- PDF files containing AEPEP (Annual Environmental Protection and Enhancement Program) reports
- Sample file names: `AEPEP_Report_Q2_2024.pdf`

### **OMVR (Operation & Maintenance Verification Reports):**
- PDF files containing verification reports
- Sample file names: `OMVR_Q3_2024.pdf`

### **Research Accomplishments:**
- PDF files containing research documents
- Sample file names: `Research_Accomplishments_Q4_2024.pdf`

---

## 🧪 **Testing Workflow**

1. **Login to the app:**
   - Username: `superadmin`
   - Password: `Change@Me`

2. **Navigate to File Upload:**
   - Dashboard → MRFC Management
   - Click any MRFC card
   - Click a Proponent
   - Select a Quarter (Q1, Q2, Q3, or Q4)
   - Click "File Upload" button

3. **Test Upload:**
   - Select a category (NTE, AEPEP, OMVR, or Research)
   - Click "Choose File"
   - Select your test file from Downloads
   - Add a title and description
   - Click Upload

4. **Verify Upload:**
   - Click "View NTE" / "View AEPEP" / "View OMVR" / "View Research"
   - Your uploaded file should appear in the list
   - Click the file to view/download

---

## 💡 **Tips**

- **Drag & drop works with any file type** (not just PDFs)
- **The emulator's Download folder** is the default location for all transferred files
- **Use real-looking filenames** to make testing more realistic
- **Test with different file sizes** to ensure the upload works for large files
- **The backend stores files on Cloudinary**, so you need an internet connection

---

## 🔧 **Troubleshooting**

### **Can't find files in the app?**
- Make sure you're looking in the **Downloads** folder in the file picker
- Try restarting the app after transferring files

### **Drag & drop not working?**
- Make sure the emulator window is fully loaded
- Try the ADB push method instead
- Ensure the emulator has enough storage space

### **Upload fails?**
- Check backend is running (`http://localhost:3000`)
- Check internet connection (needed for Cloudinary)
- Check backend console for errors

---

## 📝 **Sample Files**

If you don't have test PDFs, you can:
1. Create blank PDFs using Microsoft Word (Save As → PDF)
2. Use any existing PDF on your computer
3. Download sample PDFs from the internet

The app accepts **any PDF file**, so use whatever you have available!

---

**Happy Testing! 🚀**

