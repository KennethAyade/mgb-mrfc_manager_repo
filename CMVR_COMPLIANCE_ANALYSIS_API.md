# CMVR Compliance Analysis API Documentation

## Overview
This document describes the backend API endpoints required for the automatic CMVR compliance percentage calculator feature. The backend should analyze uploaded CMVR PDF documents and calculate compliance percentages.

## Architecture

### PDF Analysis Process
1. **PDF Upload**: When a CMVR PDF is uploaded via existing document upload API
2. **Text Extraction**: Backend extracts text from the PDF using libraries like PyPDF2, pdfplumber, or Apache Tika
3. **Pattern Recognition**: Analyze text for compliance indicators:
   - Yes/No/N/A checkboxes in tables
   - "Complied" / "Not Complied" / "Ongoing" status words
   - Existing rating indicators (PC/C/NC)
4. **Calculation**: Calculate compliance percentage
5. **Storage**: Store analysis results in database
6. **API Response**: Return results to mobile app for admin review

---

## API Endpoints

### 1. Trigger Compliance Analysis

**Endpoint**: `POST /api/compliance/analyze`

**Description**: Triggers PDF analysis for a document. This can be called automatically after upload or manually by admin.

**Request Body**:
```json
{
  "document_id": 123
}
```

**Response** (200 OK):
```json
{
  "document_id": 123,
  "document_name": "CMVR-3Q-Dingras-Walter-E.pdf",
  "analysis_status": "COMPLETED",
  "compliance_percentage": 78.5,
  "compliance_rating": "PARTIALLY_COMPLIANT",
  "total_items": 33,
  "compliant_items": 24,
  "non_compliant_items": 7,
  "na_items": 2,
  "applicable_items": 31,
  "compliance_details": {
    "ecc_compliance": {
      "section_name": "ECC Compliance",
      "total": 7,
      "compliant": 6,
      "non_compliant": 1,
      "na": 0,
      "percentage": 85.7
    },
    "epep_compliance": {
      "section_name": "EPEP Commitments",
      "total": 5,
      "compliant": 4,
      "non_compliant": 1,
      "na": 0,
      "percentage": 80.0
    },
    "impact_management": {
      "section_name": "Impact Management",
      "total": 6,
      "compliant": 5,
      "non_compliant": 1,
      "na": 0,
      "percentage": 83.3
    },
    "water_quality": {
      "section_name": "Water Quality",
      "total": 4,
      "compliant": 3,
      "non_compliant": 0,
      "na": 1,
      "percentage": 100.0
    },
    "air_quality": {
      "section_name": "Air Quality",
      "total": 4,
      "compliant": 2,
      "non_compliant": 2,
      "na": 0,
      "percentage": 50.0
    },
    "noise_quality": {
      "section_name": "Noise Quality",
      "total": 3,
      "compliant": 2,
      "non_compliant": 1,
      "na": 0,
      "percentage": 66.7
    },
    "waste_management": {
      "section_name": "Waste Management",
      "total": 4,
      "compliant": 2,
      "non_compliant": 1,
      "na": 1,
      "percentage": 66.7
    },
    "recommendations": {
      "total": 10,
      "complied": 7,
      "not_complied": 2,
      "ongoing": 1,
      "percentage": 70.0
    }
  },
  "non_compliant_list": [
    {
      "section": "ECC Compliance",
      "item_description": "Water quality monitoring not performed as required",
      "page_number": 12
    },
    {
      "section": "Air Quality",
      "item_description": "Air quality monitoring equipment not calibrated",
      "page_number": 18
    },
    {
      "section": "Air Quality",
      "item_description": "Emission limits exceeded in Q2",
      "page_number": 19
    }
  ],
  "admin_notes": null,
  "admin_adjusted": false,
  "analyzed_at": "2025-11-08T10:30:00Z",
  "reviewed_at": null,
  "reviewed_by": null
}
```

**Response** (202 Accepted) - If analysis takes time:
```json
{
  "document_id": 123,
  "analysis_status": "PENDING",
  "message": "Analysis in progress. Poll GET endpoint for results."
}
```

**Response** (400 Bad Request):
```json
{
  "error": "Document not found or not a CMVR document"
}
```

---

### 2. Get Compliance Analysis

**Endpoint**: `GET /api/compliance/document/{documentId}`

**Description**: Retrieve existing compliance analysis results for a document.

**Response** (200 OK): Same as POST response above

**Response** (404 Not Found):
```json
{
  "error": "Compliance analysis not found for this document"
}
```

---

### 3. Update Compliance Analysis

**Endpoint**: `PUT /api/compliance/document/{documentId}`

**Description**: Update compliance analysis with admin adjustments.

**Request Body**:
```json
{
  "document_id": 123,
  "compliance_percentage": 85.0,
  "compliance_rating": "PARTIALLY_COMPLIANT",
  "admin_notes": "Adjusted percentage based on manual review. Some items were incorrectly flagged.",
  "admin_adjusted": true
}
```

**Response** (200 OK): Updated analysis object (same structure as GET)

---

### 4. Get Proponent Compliance Analyses

**Endpoint**: `GET /api/compliance/proponent/{proponentId}`

**Description**: Get all compliance analyses for a proponent (multiple CMVR documents).

**Response** (200 OK):
```json
[
  {
    "document_id": 123,
    "document_name": "CMVR-3Q-2024.pdf",
    "compliance_percentage": 78.5,
    "compliance_rating": "PARTIALLY_COMPLIANT",
    "analyzed_at": "2025-11-08T10:30:00Z"
  },
  {
    "document_id": 124,
    "document_name": "CMVR-4Q-2024.pdf",
    "compliance_percentage": 92.0,
    "compliance_rating": "FULLY_COMPLIANT",
    "analyzed_at": "2025-12-05T14:20:00Z"
  }
]
```

---

### 5. Get MRFC Compliance Analyses

**Endpoint**: `GET /api/compliance/mrfc/{mrfcId}`

**Description**: Get all compliance analyses for an MRFC (all proponents).

**Response**: Array of compliance analysis objects

---

## Data Models

### ComplianceRating Enum
- `FULLY_COMPLIANT`: 90-100%
- `PARTIALLY_COMPLIANT`: 70-89%
- `NON_COMPLIANT`: Below 70%

### AnalysisStatus Enum
- `PENDING`: Analysis in progress
- `COMPLETED`: Analysis finished successfully
- `FAILED`: Analysis failed (PDF unreadable, wrong format, etc.)

---

## PDF Analysis Implementation Guide

### 1. Text Extraction
Use Python libraries like:
- **pdfplumber**: Best for tables and structured data
- **PyPDF2**: General text extraction
- **Apache Tika**: Robust for various PDF formats

### 2. Pattern Recognition

#### Identify Compliance Tables
Look for keywords in sections:
- "ECC Conditions"
- "EPEP Commitments"
- "Impact Management"
- "Water Quality"
- "Air Quality"
- "Noise Quality"
- "Waste Management"

#### Extract Yes/No/N/A Values
```python
# Example patterns to search for
patterns = [
    r'\[X\]\s*Yes\s*\[ \]\s*No',  # Checked Yes
    r'\[ \]\s*Yes\s*\[X\]\s*No',  # Checked No
    r'\[X\]\s*Complied',
    r'\[X\]\s*Not Complied',
    r'Status:\s*(Complied|Not Complied|Ongoing)',
]
```

#### Handle Tables
```python
import pdfplumber

with pdfplumber.open("cmvr.pdf") as pdf:
    for page in pdf.pages:
        tables = page.extract_tables()
        for table in tables:
            # Analyze rows for compliance indicators
            for row in table:
                # Check for Yes/No/Complied/Not Complied
                pass
```

### 3. Calculation Logic

```python
def calculate_compliance(items):
    total_items = len(items)
    na_items = sum(1 for item in items if item.status == "N/A")
    applicable_items = total_items - na_items
    compliant_items = sum(1 for item in items if item.status == "Yes" or item.status == "Complied")
    
    if applicable_items == 0:
        return 0.0
    
    percentage = (compliant_items / applicable_items) * 100
    
    # Determine rating
    if percentage >= 90:
        rating = "FULLY_COMPLIANT"
    elif percentage >= 70:
        rating = "PARTIALLY_COMPLIANT"
    else:
        rating = "NON_COMPLIANT"
    
    return {
        "percentage": percentage,
        "rating": rating,
        "total_items": total_items,
        "compliant_items": compliant_items,
        "applicable_items": applicable_items,
        "na_items": na_items
    }
```

### 4. Error Handling

- If PDF is corrupted: Return `FAILED` status
- If no compliance tables found: Return estimated percentage based on keywords
- If structure is ambiguous: Err on side of lower compliance (admin can adjust)

### 5. Performance Considerations

- For large PDFs, consider async processing
- Cache analysis results
- Provide progress updates for long-running analyses

---

## Database Schema Suggestion

```sql
CREATE TABLE compliance_analyses (
    id BIGSERIAL PRIMARY KEY,
    document_id BIGINT NOT NULL REFERENCES documents(id),
    analysis_status VARCHAR(20) NOT NULL,
    compliance_percentage DECIMAL(5,2),
    compliance_rating VARCHAR(30),
    total_items INTEGER,
    compliant_items INTEGER,
    non_compliant_items INTEGER,
    na_items INTEGER,
    applicable_items INTEGER,
    compliance_details JSONB,
    non_compliant_list JSONB,
    admin_notes TEXT,
    admin_adjusted BOOLEAN DEFAULT FALSE,
    analyzed_at TIMESTAMP,
    reviewed_at TIMESTAMP,
    reviewed_by BIGINT REFERENCES users(id),
    created_at TIMESTAMP DEFAULT NOW(),
    updated_at TIMESTAMP DEFAULT NOW()
);

CREATE INDEX idx_compliance_document ON compliance_analyses(document_id);
CREATE INDEX idx_compliance_status ON compliance_analyses(analysis_status);
```

---

## Testing Recommendations

1. **Unit Tests**: Test pattern recognition with sample text snippets
2. **Integration Tests**: Test full PDF analysis with sample CMVR documents
3. **Edge Cases**:
   - PDFs with scanned images (OCR needed)
   - PDFs with complex table layouts
   - PDFs with minimal text
   - Non-standard CMVR formats

---

## Notes for Mobile App Team

- The mobile app will automatically trigger analysis when a CMVR PDF is uploaded
- Admin can review and adjust the calculated percentage
- Results are stored permanently for compliance tracking over time
- The "Analyze" button is only shown for CMVR category documents

---

## Example CMVR Document Patterns

### Pattern 1: Checkbox Format
```
ECC CONDITIONS COMPLIANCE
[ ] Yes [X] No   Water quality monitoring performed monthly
[X] Yes [ ] No   Air emissions within limits
```

### Pattern 2: Table Format
```
| Item | Requirement | Status |
|------|-------------|--------|
| 1    | Monthly monitoring | Complied |
| 2    | Equipment calibration | Not Complied |
| 3    | Report submission | Complied |
```

### Pattern 3: Status Indicators
```
Recommendation 1: Complied
Recommendation 2: Ongoing
Recommendation 3: Not Complied
```

---

## Support

For questions or issues with the compliance analysis API, contact the backend development team or file an issue in the project repository.

