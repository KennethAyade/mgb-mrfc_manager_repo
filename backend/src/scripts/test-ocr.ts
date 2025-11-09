/**
 * OCR FUNCTIONALITY TEST
 * ======================
 * Tests Tesseract.js OCR on a sample PDF to validate functionality
 * 
 * Usage: npm run test:ocr
 */

import axios from 'axios';
import * as path from 'path';
import * as fs from 'fs';

const Tesseract = require('tesseract.js');
const PDFExtract = require('pdf.js-extract').PDFExtract;
const pdfExtract = new PDFExtract();

// Test PDF URL (use existing uploaded PDF or replace with your own)
const TEST_PDF_URL = 'https://res.cloudinary.com/drxjbb7np/raw/upload/v1762683193/mgb-mrfc/documents/CMVR-3Q-Dingras-Walter-E.-Galano-1762683187589-199214680_hbp2qi.pdf';

async function testOcrFunctionality() {
  console.log('\n========================================');
  console.log('🧪 TESSERACT.JS OCR FUNCTIONALITY TEST');
  console.log('========================================\n');

  const startTime = Date.now();
  const tempDir = path.join(__dirname, '../../temp');
  const tessdata = path.join(__dirname, '../../tessdata');

  try {
    // Step 1: Download PDF
    console.log('⏬ Step 1: Downloading test PDF...');
    const response = await axios.get(TEST_PDF_URL, {
      responseType: 'arraybuffer',
      timeout: 30000
    });
    const fileSizeMB = (response.data.length / (1024 * 1024)).toFixed(2);
    console.log(`✅ Downloaded: ${fileSizeMB} MB\n`);

    // Step 2: Check for selectable text
    console.log('📖 Step 2: Checking for selectable text...');
    const pdfBuffer = Buffer.from(response.data);
    const pdfData = await pdfExtract.extractBuffer(pdfBuffer);
    
    let quickText = '';
    for (const page of pdfData.pages) {
      const pageText = page.content.map((item: any) => item.str).join(' ');
      quickText += pageText + ' ';
    }
    
    console.log(`   Pages: ${pdfData.pages.length}`);
    console.log(`   Text found: ${quickText.trim().length} characters`);
    
    if (quickText.trim().length > 100) {
      console.log(`✅ PDF has selectable text (digital PDF)\n`);
      console.log('📝 Text sample (first 200 chars):');
      console.log(`   "${quickText.substring(0, 200).replace(/\n/g, ' ')}..."\n`);
      
      console.log('========================================');
      console.log('✅ TEST PASSED: PDF text extraction works!');
      console.log(`⏱️  Total time: ${Date.now() - startTime}ms`);
      console.log('========================================\n');
      return;
    }

    // Step 3: Test OCR
    console.log(`⚠️  No selectable text found, testing OCR...\n`);
    console.log('🔍 Step 3: Performing OCR test...');
    console.log(`   Languages: English + Filipino`);
    console.log(`   Using ${pdfData.pages.length} pages from PDF\n`);

    // Create temp directory
    if (!fs.existsSync(tempDir)) {
      fs.mkdirSync(tempDir, { recursive: true });
    }

    // Save PDF to temp file
    const pdfPath = path.join(tempDir, 'test-document.pdf');
    fs.writeFileSync(pdfPath, pdfBuffer);
    console.log(`   Saved PDF to: ${pdfPath}`);

    // Initialize Tesseract worker
    console.log('   Initializing Tesseract worker...');
    const worker = await Tesseract.createWorker('eng+fil', 1, {
      langPath: tessdata,
      logger: (m: any) => {
        if (m.status === 'recognizing text') {
          const progress = Math.round(m.progress * 100);
          if (progress % 20 === 0 || progress === 100) {
            console.log(`   OCR Progress: ${progress}%`);
          }
        }
      }
    });

    try {
      // Recognize text
      console.log('   Starting text recognition...\n');
      const result = await worker.recognize(pdfPath);
      const ocrText = result.data.text;
      const confidence = result.data.confidence;

      console.log(`✅ OCR Complete!`);
      console.log(`   Characters extracted: ${ocrText.length}`);
      console.log(`   Confidence: ${confidence.toFixed(2)}%\n`);

      if (ocrText.length > 0) {
        console.log('📝 OCR text sample (first 300 chars):');
        console.log(`   "${ocrText.substring(0, 300).replace(/\n/g, ' ')}..."\n`);
      }

      // Test compliance keyword detection
      console.log('🔍 Testing compliance keyword detection:');
      const keywords = ['compliance', 'complied', 'recommendation', 'requirements', 'environmental'];
      keywords.forEach(keyword => {
        const regex = new RegExp(keyword, 'gi');
        const matches = (ocrText.match(regex) || []).length;
        console.log(`   - "${keyword}": ${matches} occurrences`);
      });
      console.log('');

      // Validate results
      if (ocrText.length < 50) {
        console.warn('⚠️  WARNING: Very low text extraction (< 50 chars)');
        console.warn('   Possible causes:');
        console.warn('   - PDF may be image-based with poor quality');
        console.warn('   - Scan resolution too low');
        console.warn('   - Text is too blurry or distorted');
        console.log('');
      }

      if (confidence < 30) {
        console.warn('⚠️  WARNING: Low OCR confidence (< 30%)');
        console.warn('   Consider improving scan quality');
        console.log('');
      }

      console.log('========================================');
      console.log('✅ TEST COMPLETED');
      console.log(`⏱️  Total time: ${Date.now() - startTime}ms`);
      console.log('========================================\n');

    } finally {
      await worker.terminate();
      
      // Cleanup
      try {
        if (fs.existsSync(pdfPath)) fs.unlinkSync(pdfPath);
      } catch (e) {
        console.warn(`   Warning: Failed to cleanup temp file`);
      }
    }

  } catch (error: any) {
    console.log('\n========================================');
    console.error('❌ TEST FAILED');
    console.log('========================================');
    console.error(`Error: ${error.message}`);
    if (error.stack) {
      console.error(`\nStack trace:\n${error.stack}`);
    }
    console.log('');
    throw error;
  }
}

// Run test
testOcrFunctionality().catch((error) => {
  process.exit(1);
});

