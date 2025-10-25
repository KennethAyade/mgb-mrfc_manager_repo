/**
 * JWT UTILITIES
 * ==============
 * JSON Web Token generation and verification utilities
 * Used for user authentication and authorization
 */

import jwt from 'jsonwebtoken';
import dotenv from 'dotenv';

dotenv.config();

const JWT_SECRET = process.env.JWT_SECRET || 'your-super-secret-key-change-in-production';
const JWT_EXPIRES_IN = process.env.JWT_EXPIRES_IN || '24h';
const JWT_REFRESH_SECRET = process.env.JWT_REFRESH_SECRET || 'your-refresh-secret-key';
const JWT_REFRESH_EXPIRES_IN = process.env.JWT_REFRESH_EXPIRES_IN || '7d';

/**
 * JWT Payload structure
 */
export interface JwtPayload {
  userId: number;
  username: string;
  role: 'SUPER_ADMIN' | 'ADMIN' | 'USER';
  email: string;
  mrfcAccess?: number[]; // Array of MRFC IDs user can access (for USER role)
}

/**
 * Generate JWT access token
 * @param payload User data to encode in token
 * @returns JWT token string
 */
export const generateToken = (payload: JwtPayload): string => {
  return jwt.sign(payload, JWT_SECRET, {
    expiresIn: JWT_EXPIRES_IN as string
  } as jwt.SignOptions);
};

/**
 * Generate JWT refresh token
 * Refresh tokens are longer-lived and used to get new access tokens
 * @param payload User data to encode in token
 * @returns JWT refresh token string
 */
export const generateRefreshToken = (payload: JwtPayload): string => {
  return jwt.sign(payload, JWT_REFRESH_SECRET, {
    expiresIn: JWT_REFRESH_EXPIRES_IN as string
  } as jwt.SignOptions);
};

/**
 * Verify and decode JWT token
 * @param token JWT token string
 * @returns Decoded payload if valid
 * @throws Error if token is invalid or expired
 */
export const verifyToken = (token: string): JwtPayload => {
  try {
    return jwt.verify(token, JWT_SECRET) as JwtPayload;
  } catch (error) {
    if (error instanceof jwt.TokenExpiredError) {
      throw new Error('Token expired');
    } else if (error instanceof jwt.JsonWebTokenError) {
      throw new Error('Invalid token');
    }
    throw error;
  }
};

/**
 * Verify refresh token
 * @param token JWT refresh token string
 * @returns Decoded payload if valid
 * @throws Error if token is invalid or expired
 */
export const verifyRefreshToken = (token: string): JwtPayload => {
  try {
    return jwt.verify(token, JWT_REFRESH_SECRET) as JwtPayload;
  } catch (error) {
    if (error instanceof jwt.TokenExpiredError) {
      throw new Error('Refresh token expired');
    } else if (error instanceof jwt.JsonWebTokenError) {
      throw new Error('Invalid refresh token');
    }
    throw error;
  }
};

/**
 * Extract token from Authorization header
 * Expected format: "Bearer <token>"
 * @param authHeader Authorization header value
 * @returns Token string or null if not found
 */
export const extractTokenFromHeader = (authHeader: string | undefined): string | null => {
  if (!authHeader) {
    return null;
  }

  const parts = authHeader.split(' ');
  if (parts.length !== 2 || parts[0] !== 'Bearer') {
    return null;
  }

  return parts[1];
};
