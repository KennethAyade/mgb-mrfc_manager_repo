/**
 * QUICK TEST - Direct document stream test
 * Tests document ID 9 directly (from error logs)
 */

const http = require('http');

const DOC_ID = 11; // Correct document ID from database
const AUTH_TOKEN = ''; // Will get from login

console.log('\n=====================================================');
console.log('  Quick Cloudinary 401 Fix Test');
console.log('=====================================================\n');

// Login first
function login() {
  return new Promise((resolve, reject) => {
    const data = JSON.stringify({
      username: 'superadmin',
      password: 'Change@Me'
    });
    
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
          console.log('✅ Logged in successfully\n');
          resolve(response.data.token);
        } else {
          console.log('❌ Login failed:', body);
          reject(new Error('Login failed'));
        }
      });
    });

    req.on('error', reject);
    req.write(data);
    req.end();
  });
}

// Test streaming document
async function testStream(token) {
  console.log(`Testing document stream (ID: ${DOC_ID})...`);
  console.log('This is where the 401 error happened!\n');
  
  return new Promise((resolve, reject) => {
    const options = {
      hostname: 'localhost',
      port: 3000,
      path: `/api/v1/documents/${DOC_ID}/stream`,
      method: 'GET',
      headers: {
        'Authorization': `Bearer ${token}`
      }
    };

    const req = http.request(options, (res) => {
      console.log(`📍 Response Status: ${res.statusCode}`);
      console.log(`📍 Content-Type: ${res.headers['content-type']}`);
      
      if (res.statusCode === 200) {
        let dataSize = 0;
        res.on('data', (chunk) => {
          dataSize += chunk.length;
          process.stdout.write(`\r📥 Downloading: ${(dataSize / 1024).toFixed(2)} KB`);
        });
        res.on('end', () => {
          console.log(`\n\n🎉🎉🎉 SUCCESS! 🎉🎉🎉`);
          console.log(`✅ Downloaded ${(dataSize / 1024).toFixed(2)} KB`);
          console.log(`✅ No 401 error!`);
          console.log(`\n=====================================================`);
          console.log('  THE FIX WORKS!');
          console.log('=====================================================');
          console.log('\nThe signed URL bypassed the authentication!');
          console.log('You can now use this in your Android app.');
          console.log('=====================================================\n');
          resolve();
        });
      } else if (res.statusCode === 401) {
        console.log(`\n❌ 401 UNAUTHORIZED - Fix did NOT work\n`);
        let body = '';
        res.on('data', (chunk) => body += chunk);
        res.on('end', () => {
          console.log('Error:', body);
          reject(new Error('401 error'));
        });
      } else {
        console.log(`\n❌ Unexpected status: ${res.statusCode}\n`);
        let body = '';
        res.on('data', (chunk) => body += chunk);
        res.on('end', () => {
          console.log('Response:', body);
          reject(new Error(`HTTP ${res.statusCode}`));
        });
      }
    });

    req.on('error', (error) => {
      console.log(`\n❌ Connection error:`, error.message);
      console.log('\n⚠️  Make sure backend is running!');
      reject(error);
    });

    req.end();
  });
}

// Run test
async function run() {
  try {
    const token = await login();
    await testStream(token);
  } catch (error) {
    console.log('\n❌ Test failed:', error.message);
    console.log('\n💡 Make sure:');
    console.log('   1. Backend is running: cd backend && npm run dev');
    console.log('   2. Document ID 9 exists in database\n');
    process.exit(1);
  }
}

run();

