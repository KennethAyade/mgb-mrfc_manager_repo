# ChatGPT AI Setup Guide

## Quick Setup (5 minutes)

### Step 1: Get OpenAI API Key

1. Go to https://platform.openai.com/api-keys
2. Click "Create new secret key"
3. Give it a name (e.g., "MGB MRFC Backend")
4. Copy the API key (starts with `sk-...`)
5. **Important**: Save it immediately - you can't see it again!

### Step 2: Add to Environment

Add to `backend/.env`:
```env
OPENAI_API_KEY=sk-...your-key-here
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
🤖 Using ChatGPT AI for intelligent analysis...
✅ ChatGPT AI analysis successful
   - Compliance: 78.5%
```

---

## API Pricing

### GPT-4o (Default Model):
- **Input**: $2.50 per 1M tokens
- **Output**: $10.00 per 1M tokens
- **Per document**: ~$0.01-0.03 (depending on size)
- **Very affordable for production use!**

### Typical Usage:
- **Per document**: 1 request, ~5,000-10,000 tokens
- **Monthly cost (100 docs)**: ~$2-3
- **More than enough for normal use!**

### Free Trial:
- New accounts get $5 free credits
- Valid for 3 months
- Perfect for testing!

---

## How It Works

### With ChatGPT AI:
```
1. Extract text from PDF (OCR or direct)
2. Send to ChatGPT with structured prompt
3. ChatGPT analyzes compliance intelligently
4. Returns detailed JSON analysis
5. Save to database
6. Display in Android app
```

### With Vision API (Scanned PDFs):
```
1. Send PDF directly to ChatGPT Vision
2. ChatGPT reads and analyzes the document
3. No OCR needed!
4. Returns detailed JSON analysis
5. Save to database
```

### Without ChatGPT (Fallback):
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

### ChatGPT AI Provides:
- ✅ Context-aware analysis
- ✅ Better accuracy
- ✅ Detailed non-compliant items
- ✅ Intelligent severity assignment
- ✅ Proper section categorization
- ✅ Specific page numbers
- ✅ Meaningful notes
- ✅ Vision API for scanned PDFs

### vs Keyword Matching:
- ❌ Simple pattern matching
- ❌ No context understanding
- ❌ Generic non-compliant items
- ❌ Basic severity (HIGH/MEDIUM/LOW)
- ❌ Approximate categorization

---

## Testing

### Check if ChatGPT is Configured:
```bash
cd backend
npx ts-node -e "
const { isOpenAIConfigured } = require('./dist/config/openai');
console.log('ChatGPT configured:', isOpenAIConfigured());
"
```

### Test Analysis:
1. Click on any CMVR document in Android app
2. Check backend logs for:
   - `🤖 Using ChatGPT AI...` (if configured)
   - `📊 Using keyword-based analysis...` (if not configured)

---

## Troubleshooting

### Error: "OpenAI not configured"
**Solution**: Add OPENAI_API_KEY to backend/.env

### Error: "Incorrect API key provided"
**Solution**:
1. Check API key is correct (starts with sk-)
2. Verify API key is active in OpenAI dashboard
3. Check you have credits available

### Error: "Quota exceeded" or "Rate limit"
**Solution**:
- Check your usage limits in OpenAI dashboard
- Add payment method if free credits expired
- Upgrade to higher tier plan if needed

### Fallback Working:
If you see "Falling back to keyword-based analysis", ChatGPT failed but system still works!

---

## Cost Management

### Free Trial Credits ($5):
- **Perfect for testing**
- ~150-500 documents depending on size
- Valid for 3 months

### Production Use:
- **Cost per document**: $0.01-0.03
- **100 documents/month**: ~$2-3
- **1,000 documents/month**: ~$20-30
- **Very affordable!**

### Cost Optimization:
- Digital PDFs are cheaper (no vision API)
- Cached analyses are free (no API call)
- System automatically uses cache when available

---

## Customization

### Improve Analysis Accuracy:

Edit the prompt in `backend/src/config/openai.ts`:

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

### Change Model:

In `backend/src/config/openai.ts`, change the model:

```typescript
// Default (GPT-4o - Best for vision)
model: 'gpt-4o'

// Alternatives:
model: 'gpt-4o-mini'  // Cheaper, faster, still good
model: 'gpt-4-turbo'  // More expensive, slightly better
```

---

## Summary

**Status**: ✅ READY TO USE

**Setup**:
1. Get API key from OpenAI
2. Add to .env
3. Restart backend
4. Test with any CMVR document

**Features**:
- Intelligent AI analysis
- Vision API for scanned PDFs
- Automatic fallback
- Affordable pricing
- Easy to configure

**Your compliance analysis is now powered by ChatGPT!** 🤖✨
