# Gemini AI Setup Guide

## Quick Setup (5 minutes)

### Step 1: Get Gemini API Key

1. Go to https://makersuite.google.com/app/apikey
2. Click "Create API Key"
3. Select your Google Cloud project (or create new)
4. Copy the API key (starts with `AIzaSy...`)

### Step 2: Add to Environment

Add to `backend/.env`:
```env
GEMINI_API_KEY=AIzaSy...your-key-here
```

### Step 3: Restart Backend

```bash
cd backend
# Backend will auto-restart if using nodemon
# Or manually restart:
npm run dev
```

### Step 4: Test

Open Android app and click on any CMVR document. Backend logs will show:
```
🤖 Using Gemini AI for intelligent analysis...
✅ Gemini AI analysis successful
   - Compliance: 78.5%
```

---

## Free Tier Limits

### Gemini 1.5 Flash (Free):
- **15 requests per minute**
- **1 million tokens per minute**  
- **1,500 requests per day**

### Typical Usage:
- **Per document**: 1 request, ~2,000-5,000 tokens
- **Daily capacity**: ~1,500 documents
- **More than enough for normal use!**

---

## How It Works

### With Gemini AI:
```
1. Extract text from PDF (OCR or direct)
2. Send to Gemini with structured prompt
3. Gemini analyzes compliance intelligently
4. Returns detailed JSON analysis
5. Save to database
6. Display in Android app
```

### Without Gemini (Fallback):
```
1. Extract text from PDF
2. Use keyword-based analysis
3. Count "complied" and "not complied"
4. Calculate percentages
5. Save to database
6. Display in Android app
```

---

## Benefits

### Gemini AI Provides:
- ✅ Context-aware analysis
- ✅ Better accuracy
- ✅ Detailed non-compliant items
- ✅ Intelligent severity assignment
- ✅ Proper section categorization
- ✅ Specific page numbers
- ✅ Meaningful notes

### vs Keyword Matching:
- ❌ Simple pattern matching
- ❌ No context understanding
- ❌ Generic non-compliant items
- ❌ Basic severity (HIGH/MEDIUM/LOW)
- ❌ Approximate categorization

---

## Testing

### Check if Gemini is Configured:
```bash
cd backend
npx ts-node -e "
const { isGeminiConfigured } = require('./dist/config/gemini');
console.log('Gemini configured:', isGeminiConfigured());
"
```

### Test Analysis:
1. Click on document 17 in Android app
2. Check backend logs for:
   - `🤖 Using Gemini AI...` (if configured)
   - `📊 Using keyword-based analysis...` (if not configured)

---

## Troubleshooting

### Error: "Gemini AI not configured"
**Solution**: Add GEMINI_API_KEY to backend/.env

### Error: "API key not valid"
**Solution**: 
1. Check API key is correct
2. Verify API key is enabled in Google Cloud Console
3. Check Generative AI API is enabled

### Error: "Quota exceeded"
**Solution**:
- Free tier: 1,500 requests/day
- Wait for quota reset (midnight Pacific Time)
- Or upgrade to paid tier

### Fallback Working:
If you see "Falling back to keyword-based analysis", Gemini failed but system still works!

---

## Cost

### Free Tier (Recommended):
- **Cost**: $0
- **Limit**: 1,500 requests/day
- **Perfect for**: Development and moderate production use

### Paid Tier (If Needed):
- **Cost**: ~$0.00015 per 1K tokens (input)
- **Example**: 100 documents/day = ~$0.75/month
- **Very affordable!**

---

## Customization

### Improve Analysis Accuracy:

Edit the prompt in `backend/src/config/gemini.ts`:

```typescript
const prompt = `
You are an expert environmental compliance analyst...

ADDITIONAL CONTEXT:
- Focus on Philippine DENR regulations
- ECC requirements are mandatory
- EPEP commitments are contractual obligations
- Water/Air/Noise quality must meet DAO standards

[Rest of prompt...]
`;
```

Add examples, specific regulations, or custom instructions!

---

## Summary

**Status**: ✅ READY TO USE

**Setup**:
1. Get API key from Google
2. Add to .env
3. Restart backend
4. Test with any CMVR document

**Features**:
- Intelligent AI analysis
- Automatic fallback
- Free tier available
- Easy to configure

**Your compliance analysis is now powered by AI!** 🤖✨

