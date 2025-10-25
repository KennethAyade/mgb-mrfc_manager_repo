/**
 * ================================================
 * AUTHENTICATION ROUTES TESTS
 * ================================================
 * Tests for /api/v1/auth/* endpoints
 */

import request from 'supertest';
import app, { initializeDatabase } from '../../server';
import { setupTestDatabase, teardownTestDatabase, createTestUsers, cleanupTestData } from '../setup';
import { expectErrorResponse, expectSuccessResponse, uniqueEmail, uniqueUsername } from '../helpers';
import { User } from '../../models';

describe('Authentication Routes', () => {
  beforeAll(async () => {
    await setupTestDatabase();
    await initializeDatabase();
    await createTestUsers();
  });

  afterAll(async () => {
    await cleanupTestData();
    await teardownTestDatabase();
  });

  describe('POST /api/v1/auth/register', () => {
    it('should register a new user successfully', async () => {
      const userData = {
        username: `testuser${Math.floor(Math.random() * 100000)}`,
        password: 'SecurePass123!',
        full_name: 'New Test User',
        email: uniqueEmail()
      };

      const response = await request(app)
        .post('/api/v1/auth/register')
        .send(userData);

      expectSuccessResponse(response, 201);
      expect(response.body.data).toHaveProperty('id');
      expect(response.body.data.username).toBe(userData.username);
      expect(response.body.data.email).toBe(userData.email);
      expect(response.body.data.role).toBe('USER');
      expect(response.body.data).not.toHaveProperty('password_hash');
    });

    it('should reject registration with existing username', async () => {
      const response = await request(app)
        .post('/api/v1/auth/register')
        .send({
          username: 'testuser',
          password: 'SecurePass123!',
          full_name: 'Duplicate User',
          email: uniqueEmail()
        });

      expectErrorResponse(response, 409, 'USERNAME_EXISTS');
    });

    it('should reject registration with existing email', async () => {
      const response = await request(app)
        .post('/api/v1/auth/register')
        .send({
          username: `testuser${Math.floor(Math.random() * 100000)}`,
          password: 'SecurePass123!',
          full_name: 'Duplicate Email User',
          email: 'user@test.com'
        });

      expectErrorResponse(response, 409, 'EMAIL_EXISTS');
    });

    it('should reject registration with missing fields', async () => {
      const response = await request(app)
        .post('/api/v1/auth/register')
        .send({
          username: `testuser${Math.floor(Math.random() * 100000)}`
          // Missing password, full_name, email
        });

      expect(response.status).toBe(400);
      expect(response.body.success).toBe(false);
    });
  });

  describe('POST /api/v1/auth/login', () => {
    it('should login successfully with valid credentials', async () => {
      const response = await request(app)
        .post('/api/v1/auth/login')
        .send({
          username: 'testuser',
          password: 'User123!'
        });

      expectSuccessResponse(response, 200);
      expect(response.body.data).toHaveProperty('user');
      expect(response.body.data).toHaveProperty('token');
      expect(response.body.data).toHaveProperty('refreshToken');
      expect(response.body.data.user.username).toBe('testuser');
      expect(response.body.data.user).not.toHaveProperty('password_hash');
    });

    it('should reject login with invalid username', async () => {
      const response = await request(app)
        .post('/api/v1/auth/login')
        .send({
          username: 'nonexistent_user',
          password: 'SomePassword123!'
        });

      expectErrorResponse(response, 401, 'INVALID_CREDENTIALS');
    });

    it('should reject login with invalid password', async () => {
      const response = await request(app)
        .post('/api/v1/auth/login')
        .send({
          username: 'testuser',
          password: 'WrongPassword123!'
        });

      expectErrorResponse(response, 401, 'INVALID_CREDENTIALS');
    });

    it('should reject login for deactivated user', async () => {
      // Create and deactivate a user
      const inactiveUser = await User.create({
        username: uniqueUsername(),
        password_hash: 'hash',
        full_name: 'Inactive User',
        email: uniqueEmail(),
        role: 'USER',
        is_active: false,
        email_verified: false
      });

      const response = await request(app)
        .post('/api/v1/auth/login')
        .send({
          username: inactiveUser.username,
          password: 'User123!'
        });

      expectErrorResponse(response, 403, 'ACCOUNT_DEACTIVATED');

      // Cleanup
      await inactiveUser.destroy();
    });
  });

  describe('POST /api/v1/auth/refresh', () => {
    let refreshToken: string;

    beforeAll(async () => {
      const loginResponse = await request(app)
        .post('/api/v1/auth/login')
        .send({
          username: 'testuser',
          password: 'User123!'
        });

      refreshToken = loginResponse.body.data.refreshToken;
    });

    it('should refresh token successfully', async () => {
      const response = await request(app)
        .post('/api/v1/auth/refresh')
        .send({ refreshToken });

      expectSuccessResponse(response, 200);
      expect(response.body.data).toHaveProperty('token');
      expect(response.body.data).toHaveProperty('expiresIn');
    });

    it('should reject refresh with missing token', async () => {
      const response = await request(app)
        .post('/api/v1/auth/refresh')
        .send({});

      expectErrorResponse(response, 400, 'TOKEN_REQUIRED');
    });

    it('should reject refresh with invalid token', async () => {
      const response = await request(app)
        .post('/api/v1/auth/refresh')
        .send({ refreshToken: 'invalid_token_here' });

      expectErrorResponse(response, 401, 'INVALID_TOKEN');
    });
  });

  describe('POST /api/v1/auth/logout', () => {
    let userToken: string;

    beforeAll(async () => {
      const loginResponse = await request(app)
        .post('/api/v1/auth/login')
        .send({
          username: 'testuser',
          password: 'User123!'
        });

      userToken = loginResponse.body.data.token;
    });

    it('should logout successfully with valid token', async () => {
      const response = await request(app)
        .post('/api/v1/auth/logout')
        .set('Authorization', `Bearer ${userToken}`);

      expectSuccessResponse(response, 200, false);
      expect(response.body.message).toBe('Logged out successfully');
    });

    it('should reject logout without token', async () => {
      const response = await request(app)
        .post('/api/v1/auth/logout');

      expect(response.status).toBe(401);
    });
  });

  describe('POST /api/v1/auth/change-password', () => {
    let userToken: string;

    beforeEach(async () => {
      const loginResponse = await request(app)
        .post('/api/v1/auth/login')
        .send({
          username: 'testuser',
          password: 'User123!'
        });

      userToken = loginResponse.body.data.token;
    });

    it('should change password successfully', async () => {
      const response = await request(app)
        .post('/api/v1/auth/change-password')
        .set('Authorization', `Bearer ${userToken}`)
        .send({
          currentPassword: 'User123!',
          newPassword: 'NewPassword123!'
        });

      expectSuccessResponse(response, 200, false);
      expect(response.body.message).toBe('Password changed successfully');

      // Change back to original password
      const loginResponse = await request(app)
        .post('/api/v1/auth/login')
        .send({
          username: 'testuser',
          password: 'NewPassword123!'
        });

      const newToken = loginResponse.body.data.token;

      await request(app)
        .post('/api/v1/auth/change-password')
        .set('Authorization', `Bearer ${newToken}`)
        .send({
          currentPassword: 'NewPassword123!',
          newPassword: 'User123!'
        });
    });

    it('should reject change with incorrect current password', async () => {
      const response = await request(app)
        .post('/api/v1/auth/change-password')
        .set('Authorization', `Bearer ${userToken}`)
        .send({
          currentPassword: 'WrongPassword123!',
          newPassword: 'NewPassword123!'
        });

      expectErrorResponse(response, 403, 'INVALID_PASSWORD');
    });

    it('should reject change without authentication', async () => {
      const response = await request(app)
        .post('/api/v1/auth/change-password')
        .send({
          currentPassword: 'User123!',
          newPassword: 'NewPassword123!'
        });

      expect(response.status).toBe(401);
    });
  });
});

