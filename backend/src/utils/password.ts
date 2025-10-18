/**
 * PASSWORD UTILITIES
 * ==================
 * Password hashing and verification using bcrypt
 * NEVER store plaintext passwords in database
 */

import bcrypt from 'bcryptjs';
import dotenv from 'dotenv';

dotenv.config();

const BCRYPT_ROUNDS = parseInt(process.env.BCRYPT_ROUNDS || '10');

/**
 * Hash a plain text password
 * Uses bcrypt with configurable salt rounds (default: 10)
 * Higher rounds = more secure but slower
 *
 * @param plainPassword Plain text password to hash
 * @returns Hashed password string
 *
 * @example
 * const hashed = await hashPassword('MyPassword123');
 * // Returns: $2a$10$... (60 characters)
 */
export const hashPassword = async (plainPassword: string): Promise<string> => {
  try {
    const salt = await bcrypt.genSalt(BCRYPT_ROUNDS);
    return await bcrypt.hash(plainPassword, salt);
  } catch (error) {
    console.error('Password hashing error:', error);
    throw new Error('Failed to hash password');
  }
};

/**
 * Verify a plain text password against hashed password
 *
 * @param plainPassword Plain text password to verify
 * @param hashedPassword Hashed password from database
 * @returns true if passwords match, false otherwise
 *
 * @example
 * const isValid = await verifyPassword('MyPassword123', user.password_hash);
 * if (!isValid) {
 *   throw new Error('Invalid credentials');
 * }
 */
export const verifyPassword = async (
  plainPassword: string,
  hashedPassword: string
): Promise<boolean> => {
  try {
    return await bcrypt.compare(plainPassword, hashedPassword);
  } catch (error) {
    console.error('Password verification error:', error);
    return false;
  }
};

/**
 * Validate password strength
 * Requirements:
 * - Minimum 8 characters
 * - At least one uppercase letter
 * - At least one lowercase letter
 * - At least one number
 * - At least one special character
 *
 * @param password Password to validate
 * @returns Object with isValid boolean and error message if invalid
 *
 * @example
 * const validation = validatePasswordStrength('weak');
 * if (!validation.isValid) {
 *   return res.status(400).json({ error: validation.message });
 * }
 */
export const validatePasswordStrength = (password: string): {
  isValid: boolean;
  message: string;
} => {
  if (password.length < 8) {
    return {
      isValid: false,
      message: 'Password must be at least 8 characters long'
    };
  }

  if (!/[A-Z]/.test(password)) {
    return {
      isValid: false,
      message: 'Password must contain at least one uppercase letter'
    };
  }

  if (!/[a-z]/.test(password)) {
    return {
      isValid: false,
      message: 'Password must contain at least one lowercase letter'
    };
  }

  if (!/[0-9]/.test(password)) {
    return {
      isValid: false,
      message: 'Password must contain at least one number'
    };
  }

  if (!/[!@#$%^&*(),.?":{}|<>]/.test(password)) {
    return {
      isValid: false,
      message: 'Password must contain at least one special character (!@#$%^&*...)'
    };
  }

  return {
    isValid: true,
    message: 'Password is strong'
  };
};

/**
 * Generate a random password
 * Useful for temporary passwords or password reset
 *
 * @param length Password length (default: 12)
 * @returns Random password string
 *
 * @example
 * const tempPassword = generateRandomPassword(16);
 * // Send to user via email
 */
export const generateRandomPassword = (length: number = 12): string => {
  const uppercase = 'ABCDEFGHIJKLMNOPQRSTUVWXYZ';
  const lowercase = 'abcdefghijklmnopqrstuvwxyz';
  const numbers = '0123456789';
  const special = '!@#$%^&*';
  const all = uppercase + lowercase + numbers + special;

  let password = '';

  // Ensure at least one of each type
  password += uppercase[Math.floor(Math.random() * uppercase.length)];
  password += lowercase[Math.floor(Math.random() * lowercase.length)];
  password += numbers[Math.floor(Math.random() * numbers.length)];
  password += special[Math.floor(Math.random() * special.length)];

  // Fill the rest randomly
  for (let i = password.length; i < length; i++) {
    password += all[Math.floor(Math.random() * all.length)];
  }

  // Shuffle the password
  return password.split('').sort(() => Math.random() - 0.5).join('');
};
