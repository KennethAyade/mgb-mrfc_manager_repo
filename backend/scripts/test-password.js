/**
 * Test password hashing and verification
 */

const bcrypt = require('bcryptjs');

async function testPassword() {
  const password = 'Change@Me#2025';
  const storedHash = '$2a$10$RArpd.FdfPVNiCh6XBHvme6K0WxbQvBYppBCNiAGV7T15SpWolxUm';

  console.log('Testing password:', password);
  console.log('Stored hash:', storedHash);

  const isValid = await bcrypt.compare(password, storedHash);
  console.log('Password valid:', isValid);

  // Also test creating a new hash
  const newHash = await bcrypt.hash(password, 10);
  console.log('\nNew hash:', newHash);
  const isNewValid = await bcrypt.compare(password, newHash);
  console.log('New hash valid:', isNewValid);
}

testPassword();
