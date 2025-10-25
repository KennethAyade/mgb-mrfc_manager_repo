/**
 * ================================================
 * TEST HELPER FUNCTIONS
 * ================================================
 * Common utilities for API testing
 */

import request from 'supertest';
import app from '../server';

/**
 * Login helper to get authentication token
 */
export const loginUser = async (username: string, password: string): Promise<string> => {
  const response = await request(app)
    .post('/api/v1/auth/login')
    .send({ username, password });

  if (response.status !== 200) {
    throw new Error(`Login failed: ${response.body.error?.message || 'Unknown error'}`);
  }

  return response.body.data.token;
};

/**
 * Get test tokens for different user roles
 */
export const getTestTokens = async () => {
  const superAdminToken = await loginUser('testsuperadmin', 'SuperAdmin123!');
  const adminToken = await loginUser('testadmin', 'Admin123!');
  const userToken = await loginUser('testuser', 'User123!');

  return {
    superAdminToken,
    adminToken,
    userToken
  };
};

/**
 * Make authenticated request
 */
export const authenticatedRequest = (token: string) => {
  return {
    get: (url: string) => request(app).get(url).set('Authorization', `Bearer ${token}`),
    post: (url: string) => request(app).post(url).set('Authorization', `Bearer ${token}`),
    put: (url: string) => request(app).put(url).set('Authorization', `Bearer ${token}`),
    delete: (url: string) => request(app).delete(url).set('Authorization', `Bearer ${token}`)
  };
};

/**
 * Verify error response structure
 */
export const expectErrorResponse = (response: any, statusCode: number, errorCode: string) => {
  expect(response.status).toBe(statusCode);
  expect(response.body).toHaveProperty('success', false);
  expect(response.body).toHaveProperty('error');
  expect(response.body.error).toHaveProperty('code', errorCode);
  expect(response.body.error).toHaveProperty('message');
};

/**
 * Verify success response structure
 */
export const expectSuccessResponse = (response: any, statusCode: number = 200, requireData: boolean = true) => {
  expect(response.status).toBe(statusCode);
  expect(response.body).toHaveProperty('success', true);
  if (requireData) {
    expect(response.body).toHaveProperty('data');
  }
};

/**
 * Generate random string for unique test data
 */
export const randomString = (length: number = 8): string => {
  return Math.random().toString(36).substring(2, length + 2);
};

/**
 * Generate unique email
 */
export const uniqueEmail = (): string => {
  return `test_${randomString()}@example.com`;
};

/**
 * Generate unique username
 */
export const uniqueUsername = (): string => {
  return `testuser_${randomString()}`;
};

