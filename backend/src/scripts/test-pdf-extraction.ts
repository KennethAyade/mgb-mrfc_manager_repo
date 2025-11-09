/**
 * Test PDF Extraction
 * 
 * This script tests PDF text extraction to verify it works correctly.
 * 
 * Usage: npm run test:pdf
 */

import axios from 'axios';

const PDFExtract = require('pdf.js-extract').PDFExtract;
const pdfExtract = new PDFExtract();

async function testPdfExtraction() {
  try {
    console.log('🧪 PDF EXTRACTION TEST\n');
    console.log('========================================\n');

    // PDF URL from your logs
    const pdfUrl = 'https://res.cloudinary.com/drxjbb7np/raw/upload/v1762683193/mgb-mrfc/documents/CMVR-3Q-Dingras-Walter-E.-Galano-1762683187589-199214680_hbp2qi.pdf';

    console.log('⏬ Downloading PDF...');
    console.log(`URL: ${pdfUrl}\n`);
    
    const response = await axios.get(pdfUrl, {
      responseType: 'arraybuffer',
      timeout: 30000
    });

    const fileSizeMB = (response.data.length / (1024 * 1024)).toFixed(2);
    console.log(`✅ Downloaded: ${response.data.length} bytes (${fileSizeMB} MB)\n`);

    console.log('📖 Extracting text...\n');
    
    const pdfBuffer = Buffer.from(response.data);
    const pdfData = await pdfExtract.extractBuffer(pdfBuffer);

    console.log(`✅ Extraction complete!`);
    console.log(`   Pages: ${pdfData.pages.length}\n`);

    // Method 1: Current approach (join with space)
    console.log('--- METHOD 1: Join with space ---');
    let text1 = '';
    for (const page of pdfData.pages) {
      const pageText = page.content.map((item: any) => item.str).join(' ');
      text1 += pageText + '\n';
    }
    console.log(`Characters extracted: ${text1.length}`);
    console.log(`First 300 chars:\n"${text1.substring(0, 300)}"\n`);

    // Method 2: Join with no space
    console.log('--- METHOD 2: Join with no space ---');
    let text2 = '';
    for (const page of pdfData.pages) {
      const pageText = page.content.map((item: any) => item.str).join('');
      text2 += pageText + '\n';
    }
    console.log(`Characters extracted: ${text2.length}`);
    console.log(`First 300 chars:\n"${text2.substring(0, 300)}"\n`);

    // Method 3: Preserve positioning with newlines
    console.log('--- METHOD 3: With positioning ---');
    let text3 = '';
    for (const page of pdfData.pages) {
      // Sort by y position, then x position
      const sortedContent = page.content.sort((a: any, b: any) => {
        if (Math.abs(a.y - b.y) > 5) return a.y - b.y;
        return a.x - b.x;
      });
      
      let lastY = -1;
      for (const item of sortedContent) {
        if (lastY !== -1 && Math.abs(item.y - lastY) > 5) {
          text3 += '\n';
        }
        text3 += item.str + ' ';
        lastY = item.y;
      }
      text3 += '\n\n';
    }
    console.log(`Characters extracted: ${text3.length}`);
    console.log(`First 300 chars:\n"${text3.substring(0, 300)}"\n`);

    // Show sample page structure
    console.log('--- PAGE STRUCTURE (Page 1) ---');
    console.log(`Total content items: ${pdfData.pages[0].content.length}`);
    console.log('Sample items:');
    for (let i = 0; i < Math.min(10, pdfData.pages[0].content.length); i++) {
      const item = pdfData.pages[0].content[i];
      console.log(`  [${i}] "${item.str}" at (x:${item.x.toFixed(1)}, y:${item.y.toFixed(1)})`);
    }

    console.log('\n========================================');
    console.log('✅ Test complete!\n');

    // Search for compliance keywords
    console.log('--- KEYWORD SEARCH ---');
    const keywords = ['complied', 'compliance', 'yes', 'no', 'ECC', 'EPEP', 'monitoring', 'water', 'quality'];
    for (const keyword of keywords) {
      const count1 = (text1.match(new RegExp(keyword, 'gi')) || []).length;
      const count3 = (text3.match(new RegExp(keyword, 'gi')) || []).length;
      if (count1 > 0 || count3 > 0) {
        console.log(`  "${keyword}": Method1=${count1}, Method3=${count3}`);
      }
    }

  } catch (error: any) {
    console.error('\n❌ Test failed!');
    console.error(`Error: ${error.message}`);
    if (error.stack) {
      console.error(`\nStack: ${error.stack}`);
    }
  }
}

testPdfExtraction();

