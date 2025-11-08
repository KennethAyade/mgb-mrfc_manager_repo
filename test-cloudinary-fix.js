/**
 * TEST CLOUDINARY 401 FIX
 * Simulates document download through the backend API
 */

const https = require('https');
const http = require('http');

const BASE_URL = 'http://localhost:3000';
const TEST_USER = {
  username: 'superadmin',
  password: 'Change@Me'
};

console.log('\n=====================================================');
console.log('  Testing Cloudinary 401 Fix');
console.log('=====================================================\n');

let authToken = '';

// Step 1: Login to get auth token
async function login() {
  console.log('Step 1: Logging in...');
  
  return new Promise((resolve, reject) => {
    const data = JSON.stringify(TEST_USER);
    
    const options = {
      hostname: 'localhost',
      port: 3000,
      path: '/api/v1/auth/login',
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'Content-Length': data.length
      }
    };

    const req = http.request(options, (res) => {
      let body = '';
      res.on('data', (chunk) => body += chunk);
      res.on('end', () => {
        if (res.statusCode === 200) {
          const response = JSON.parse(body);
          authToken = response.data.token;
          console.log('✅ Login successful');
          console.log(`   Token: ${authToken.substring(0, 20)}...`);
          resolve();
        } else {
          console.log('❌ Login failed:', body);
          reject(new Error('Login failed'));
        }
      });
    });

    req.on('error', (error) => {
      console.log('❌ Connection error:', error.message);
      console.log('\n⚠️  Make sure backend is running: cd backend && npm run dev');
      reject(error);
    });

    req.write(data);
    req.end();
  });
}

// Step 2: Get list of documents
async function getDocuments() {
  console.log('\nStep 2: Getting documents...');
  
  return new Promise((resolve, reject) => {
    const options = {
      hostname: 'localhost',
      port: 3000,
      path: '/api/v1/documents?limit=10', // Get all documents with limit
      method: 'GET',
      headers: {
        'Authorization': `Bearer ${authToken}`
      }
    };

    const req = http.request(options, (res) => {
      let body = '';
      res.on('data', (chunk) => body += chunk);
      res.on('end', () => {
        if (res.statusCode === 200) {
          const response = JSON.parse(body);
          console.log(`   API Response structure:`, Object.keys(response));
          const documents = response.data || response.documents || response || [];
          
          // Handle pagination
          const docArray = Array.isArray(documents) ? documents : 
                          (documents.rows ? documents.rows : []);
          
          console.log(`✅ Found ${docArray.length} document(s)`);
          
          if (docArray.length > 0) {
            console.log('\nDocuments:');
            docArray.forEach((doc, index) => {
              console.log(`   ${index + 1}. ${doc.original_name}`);
              console.log(`      ID: ${doc.id}`);
              console.log(`      Category: ${doc.category}`);
              console.log(`      Size: ${(doc.file_size / 1024).toFixed(2)} KB`);
            });
            resolve(docArray[0]); // Return first document for testing
          } else {
            console.log('\n⚠️  No documents found in database');
            console.log('   Upload a document via the Android app first');
            resolve(null);
          }
        } else {
          console.log('❌ Failed to get documents:', body);
          reject(new Error('Failed to get documents'));
        }
      });
    });

    req.on('error', reject);
    req.end();
  });
}

// Step 3: Stream a document (this tests the 401 fix)
async function streamDocument(documentId, filename) {
  console.log(`\nStep 3: Testing document stream (ID: ${documentId})...`);
  console.log('   This is where the 401 error happened before!');
  
  return new Promise((resolve, reject) => {
    const options = {
      hostname: 'localhost',
      port: 3000,
      path: `/api/v1/documents/${documentId}/stream`,
      method: 'GET',
      headers: {
        'Authorization': `Bearer ${authToken}`
      }
    };

    console.log(`\n📥 Requesting: GET /api/v1/documents/${documentId}/stream`);

    const req = http.request(options, (res) => {
      console.log(`📍 Response status: ${res.statusCode}`);
      console.log(`📍 Content-Type: ${res.headers['content-type']}`);
      console.log(`📍 Content-Length: ${res.headers['content-length']} bytes`);
      
      if (res.statusCode === 200) {
        let dataSize = 0;
        
        res.on('data', (chunk) => {
          dataSize += chunk.length;
          process.stdout.write(`\r📥 Downloading: ${(dataSize / 1024).toFixed(2)} KB`);
        });
        
        res.on('end', () => {
          console.log(`\n\n✅ SUCCESS! Document streamed successfully!`);
          console.log(`   Downloaded ${(dataSize / 1024).toFixed(2)} KB`);
          console.log(`\n🎉 The 401 error is FIXED!`);
          resolve();
        });
      } else if (res.statusCode === 401) {
        console.log('\n❌ 401 UNAUTHORIZED - The fix did not work');
        let body = '';
        res.on('data', (chunk) => body += chunk);
        res.on('end', () => {
          console.log('   Error details:', body);
          reject(new Error('401 error still occurring'));
        });
      } else {
        console.log(`\n❌ Unexpected status: ${res.statusCode}`);
        let body = '';
        res.on('data', (chunk) => body += chunk);
        res.on('end', () => {
          console.log('   Response:', body);
          reject(new Error(`HTTP ${res.statusCode}`));
        });
      }
    });

    req.on('error', (error) => {
      console.log('\n❌ Request error:', error.message);
      reject(error);
    });

    req.end();
  });
}

// Run the test
async function runTest() {
  try {
    await login();
    const document = await getDocuments();
    
    if (document) {
      await streamDocument(document.id, document.original_name);
      
      console.log('\n=====================================================');
      console.log('  Test Summary');
      console.log('=====================================================');
      console.log('✅ Login: SUCCESS');
      console.log('✅ Get Documents: SUCCESS');
      console.log('✅ Stream Document: SUCCESS (No 401 error!)');
      console.log('\n🎉 The fix is working! You can now test in Android app.');
    } else {
      console.log('\n=====================================================');
      console.log('  Test Summary');
      console.log('=====================================================');
      console.log('⚠️  No documents to test');
      console.log('   Upload a document via Android app first');
    }
    
    console.log('\n=====================================================\n');
    
  } catch (error) {
    console.log('\n=====================================================');
    console.log('  Test Failed');
    console.log('=====================================================');
    console.log('❌ Error:', error.message);
    console.log('\n💡 Make sure:');
    console.log('   1. Backend is running (cd backend && npm run dev)');
    console.log('   2. Database is accessible');
    console.log('   3. At least one document is uploaded');
    console.log('\n=====================================================\n');
    process.exit(1);
  }
}

// Start the test
runTest();

