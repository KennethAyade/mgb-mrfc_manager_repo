/**
 * Download Tesseract Language Data Files
 * 
 * Downloads eng.traineddata and fil.traineddata for OCR
 * 
 * Usage: npm run download:lang
 */

import axios from 'axios';
import * as fs from 'fs';
import * as path from 'path';

const TESSDATA_URL = 'https://github.com/tesseract-ocr/tessdata/raw/main';
const TESSDATA_DIR = path.join(__dirname, '../../tessdata');

const languages = [
  { code: 'eng', name: 'English' },
  { code: 'fil', name: 'Filipino' }
];

async function downloadLanguageFile(langCode: string, langName: string) {
  const url = `${TESSDATA_URL}/${langCode}.traineddata`;
  const filePath = path.join(TESSDATA_DIR, `${langCode}.traineddata`);

  // Check if file already exists
  if (fs.existsSync(filePath)) {
    console.log(`✅ ${langName} (${langCode}.traineddata) already exists`);
    return;
  }

  console.log(`⏬ Downloading ${langName} (${langCode}.traineddata)...`);
  
  try {
    const response = await axios.get(url, {
      responseType: 'arraybuffer',
      timeout: 60000,
      onDownloadProgress: (progressEvent) => {
        if (progressEvent.total) {
          const percentCompleted = Math.round((progressEvent.loaded * 100) / progressEvent.total);
          process.stdout.write(`\r   Progress: ${percentCompleted}%`);
        }
      }
    });

    fs.writeFileSync(filePath, response.data);
    const sizeMB = (response.data.length / (1024 * 1024)).toFixed(2);
    console.log(`\n✅ ${langName} downloaded successfully (${sizeMB} MB)`);
  } catch (error: any) {
    console.error(`\n❌ Failed to download ${langName}: ${error.message}`);
    throw error;
  }
}

async function main() {
  console.log('📥 Downloading Tesseract Language Data Files\n');
  console.log(`Target directory: ${TESSDATA_DIR}\n`);

  // Ensure tessdata directory exists
  if (!fs.existsSync(TESSDATA_DIR)) {
    fs.mkdirSync(TESSDATA_DIR, { recursive: true });
    console.log(`✅ Created tessdata directory\n`);
  }

  // Download each language file
  for (const lang of languages) {
    await downloadLanguageFile(lang.code, lang.name);
  }

  console.log('\n========================================');
  console.log('✅ Language data download complete!');
  console.log('========================================\n');
}

main().catch((error) => {
  console.error('\n❌ Download failed:', error.message);
  process.exit(1);
});

