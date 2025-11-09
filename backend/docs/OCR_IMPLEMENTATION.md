# OCR Implementation for CMVR Compliance Analysis

## Overview

The MGB MRFC Manager system now supports **Optical Character Recognition (OCR)** for analyzing scanned or image-based CMVR PDF documents. This enables the system to extract text from documents that don't contain selectable text, providing comprehensive compliance analysis regardless of document format.

## Architecture

```
┌─────────────────────┐
│   Android Client    │
│  ComplianceAnalysis │
│     Activity        │
└──────────┬──────────┘
           │
           │ POST /api/v1/compliance/analyze
           │ GET  /api/v1/compliance/progress/:documentId
           │
           ▼
┌─────────────────────┐
│   Backend API       │
│  Compliance         │
│  Analysis           │
│  Controller         │
└──────────┬──────────┘
           │
           │ 1. Download PDF
           │ 2. Try text extraction
           │ 3. If no text, perform OCR
           │ 4. Analyze compliance
           │ 5. Cache results
           │
           ▼
┌─────────────────────┐      ┌─────────────────────┐
│  Tesseract.js       │      │  PostgreSQL DB      │
│  OCR Engine         │      │  compliance_        │
│  (eng+fil)          │      │  analyses table     │
└─────────────────────┘      └─────────────────────┘
```

## Technology Stack

### Backend
- **Tesseract.js v4**: OCR engine for text recognition
- **pdf.js-extract**: PDF parsing and text extraction
- **Node.js + Express**: API server
- **PostgreSQL**: Database for caching OCR results
- **Languages**: English (eng) + Filipino (fil)

### Android
- **Kotlin Coroutines**: Async progress polling
- **Retrofit 2**: API calls
- **Material Design 3**: Progress dialog UI
- **LiveData**: Real-time progress updates

## Features

### 1. Intelligent PDF Analysis
- **Digital PDFs**: Fast text extraction (< 1 second)
- **Scanned PDFs**: OCR processing (30-60 seconds)
- **Automatic Detection**: System detects document type automatically

### 2. Real-Time Progress Tracking
- **Progress API**: `GET /api/v1/compliance/progress/:documentId`
- **Updates every 2 seconds**: Shows current page being processed
- **Status indicators**: pending, processing, completed, failed
- **Progress percentage**: 0-100%

### 3. OCR Result Caching
- **Database columns**:
  - `extracted_text`: Full OCR text (TEXT)
  - `ocr_confidence`: Confidence score 0-100 (DECIMAL)
  - `ocr_language`: Languages used (VARCHAR)
- **Benefits**: Subsequent analyses skip OCR, use cached text
- **Performance**: 30-60 seconds → < 1 second

### 4. Error Handling
- **Low quality PDFs**: "PDF quality too low" message
- **Low confidence**: "OCR confidence too low" warning
- **Timeouts**: "Processing timed out" with suggestions
- **User-friendly messages**: Clear guidance for users

## API Endpoints

### Analyze Document (with OCR)
```http
POST /api/v1/compliance/analyze
Content-Type: application/json
Authorization: Bearer {token}

{
  "document_id": 123
}
```

**Response:**
```json
{
  "success": true,
  "data": {
    "id": 45,
    "document_id": 123,
    "compliance_percentage": 77.42,
    "compliance_rating": "PARTIALLY_COMPLIANT",
    "extracted_text": "Full OCR text...",
    "ocr_confidence": 85.5,
    "ocr_language": "eng+fil",
    "analyzed_at": "2025-11-09T10:30:00Z"
  }
}
```

### Get Analysis Progress
```http
GET /api/v1/compliance/progress/:documentId
Authorization: Bearer {token}
```

**Response:**
```json
{
  "success": true,
  "data": {
    "status": "processing",
    "progress": 45,
    "current_step": "Processing page 15/25 (75%)",
    "error": null
  }
}
```

## Implementation Details

### Backend Processing Flow

1. **Download PDF** (Progress: 10%)
   - Fetch from Cloudinary
   - Validate file size and format

2. **Check for Selectable Text** (Progress: 20%)
   - Use pdf.js-extract
   - If > 100 characters found, skip OCR

3. **Perform OCR** (Progress: 30-80%)
   - Initialize Tesseract worker with eng+fil languages
   - Process PDF pages
   - Extract text with confidence scores
   - Update progress in real-time

4. **Analyze Compliance** (Progress: 85%)
   - Pattern matching for compliance indicators
   - Calculate percentages
   - Identify non-compliant items

5. **Cache Results** (Progress: 100%)
   - Save extracted_text to database
   - Store ocr_confidence and ocr_language
   - Complete analysis

### Android Progress Polling

```kotlin
private fun startProgressPolling() {
    lifecycleScope.launch {
        while (isPollingProgress) {
            val result = repository.getAnalysisProgress(documentId)
            
            when (result) {
                is Result.Success -> {
                    updateProgressDialog(
                        result.data.progress,
                        result.data.getDisplayMessage()
                    )
                    
                    if (result.data.isCompleted()) {
                        dismissProgressDialog()
                        viewModel.getComplianceAnalysis(documentId)
                    }
                }
            }
            
            delay(2000) // Poll every 2 seconds
        }
    }
}
```

## Database Schema

### compliance_analyses Table Extensions

```sql
-- OCR Caching Columns
ALTER TABLE compliance_analyses 
ADD COLUMN extracted_text TEXT,
ADD COLUMN ocr_confidence DECIMAL(5,2),
ADD COLUMN ocr_language VARCHAR(20);
```

**Migration Script**: `backend/migrations/20251109-add-extracted-text.js`
**Run with**: `npm run db:migrate:ocr`

## Configuration

### Environment Variables (.env)

```env
# OCR Configuration
OCR_TIMEOUT=120000          # 2 minutes
OCR_MAX_PAGES=50            # Maximum pages to process
OCR_LANGUAGES=eng+fil       # English + Filipino
TESSDATA_PREFIX=./tessdata  # Language data path
```

### Language Data Files

Location: `backend/tessdata/`
- `eng.traineddata` (22.38 MB) - English
- `fil.traineddata` (2.39 MB) - Filipino

Download with: `npm run download:lang`

## Testing

### Test OCR Functionality
```bash
cd backend
npm run test:ocr
```

**Output:**
```
========================================
🧪 TESSERACT.JS OCR FUNCTIONALITY TEST
========================================

⏬ Step 1: Downloading test PDF...
✅ Downloaded: 6.01 MB

📖 Step 2: Checking for selectable text...
   Pages: 25
   Text found: 45 characters
⚠️  No selectable text found, testing OCR...

🔍 Step 3: Performing OCR test...
   Languages: English + Filipino
   Initializing Tesseract worker...
   Starting text recognition...
   
   OCR Progress: 20%
   OCR Progress: 40%
   OCR Progress: 60%
   OCR Progress: 80%
   OCR Progress: 100%
   
✅ OCR Complete!
   Characters extracted: 12,450
   Confidence: 85.32%

📝 OCR text sample (first 300 chars):
   "COMPLIANCE MONITORING AND VALIDATION REPORT Environmental Compliance Certificate Requirements..."

🔍 Testing compliance keyword detection:
   - "compliance": 45 occurrences
   - "complied": 32 occurrences
   - "recommendation": 18 occurrences

========================================
✅ TEST COMPLETED
⏱️  Total time: 45,230ms
========================================
```

### Clear OCR Cache
```bash
cd backend
npm run db:clear-compliance
```

## Performance

### Digital PDFs (with selectable text)
- **Processing time**: < 1 second
- **Network**: Download only
- **Storage**: Minimal (text cached)

### Scanned PDFs (OCR required)
- **First analysis**: 30-60 seconds
- **Subsequent analyses**: < 1 second (cached)
- **Network**: Download + OCR processing
- **Storage**: ~10-50KB per document (cached text)

### Optimization Strategies
1. **Caching**: Extracted text stored for reuse
2. **Smart detection**: Skip OCR for digital PDFs
3. **Progress updates**: Keep user informed
4. **Async processing**: Non-blocking API calls

## Error Messages & User Guidance

### PDF Quality Too Low
```
Error: PDF quality too low. Please upload a clearer scan.
Suggestion: Try scanning at higher resolution (300 DPI or higher)
```

### OCR Confidence Too Low
```
Error: OCR confidence too low. Document may be poor quality or corrupted.
Suggestion: Check document quality and re-upload
```

### Processing Timeout
```
Error: Processing timed out. Document may be too large.
Suggestion: Consider splitting into smaller files
```

## Best Practices

### For Administrators
1. **Prefer digital PDFs** over scanned documents
2. **Scan at 300 DPI or higher** for best OCR results
3. **Ensure clear text** - avoid handwritten notes
4. **Monitor cache** - clear periodically if needed

### For Developers
1. **Always check cached text first** before OCR
2. **Implement progress tracking** for long operations
3. **Handle all error cases** with user-friendly messages
4. **Test with real documents** before deployment

## Troubleshooting

### Issue: OCR extracts very little text

**Possible Causes:**
- PDF is image-based with poor scan quality
- Resolution too low (< 150 DPI)
- Document contains mainly graphics/tables

**Solutions:**
- Request higher quality scan
- Use digital PDF if possible
- Manually review and adjust if necessary

### Issue: OCR takes too long

**Possible Causes:**
- Document has many pages (> 50)
- Large file size (> 10 MB)
- Server resource constraints

**Solutions:**
- Split large documents
- Increase `OCR_TIMEOUT` in config
- Optimize server resources

### Issue: Language not detected correctly

**Possible Causes:**
- Wrong language data files
- Mixed language document
- Poor OCR quality

**Solutions:**
- Verify `eng.traineddata` and `fil.traineddata` exist
- Use `eng+fil` for mixed documents
- Improve scan quality

## Future Enhancements

1. **Multi-page image PDFs**: Better handling of large scanned documents
2. **Custom training data**: Improve recognition of domain-specific terms
3. **Batch processing**: Analyze multiple documents concurrently
4. **OCR quality metrics**: Detailed confidence scores per section
5. **Image preprocessing**: Auto-enhance low-quality scans

## References

- [Tesseract.js Documentation](https://tesseract.projectnaptha.com/)
- [pdf.js-extract](https://www.npmjs.com/package/pdf.js-extract)
- [Tesseract Language Data](https://github.com/tesseract-ocr/tessdata)

## Support

For issues or questions:
- Check logs: `backend/logs/`
- Run test script: `npm run test:ocr`
- Review backend console during analysis
- Contact system administrator

---

**Last Updated**: November 9, 2025
**Version**: 1.0.0
**Implementation Status**: ✅ Complete

