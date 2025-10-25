/**
 * ================================================
 * USER MANAGEMENT ROUTES TESTS
 * ================================================
 * Tests for /api/v1/users/* endpoints
 */

import request from 'supertest';
import app, { initializeDatabase } from '../../server';
import { setupTestDatabase, teardownTestDatabase, createTestUsers, cleanupTestData } from '../setup';
import { expectErrorResponse, expectSuccessResponse, getTestTokens, uniqueEmail } from '../helpers';
import { User } from '../../models';

describe('User Management Routes', () => {
  let adminToken: string;
  let userToken: string;
  let superAdminToken: string;

  beforeAll(async () => {
    await setupTestDatabase();
    await initializeDatabase();
    await createTestUsers();
    const tokens = await getTestTokens();
    adminToken = tokens.adminToken;
    userToken = tokens.userToken;
    superAdminToken = tokens.superAdminToken;
  });

  afterAll(async () => {
    await cleanupTestData();
    await teardownTestDatabase();
  });

  describe('GET /api/v1/users', () => {
    it('should list users for admin', async () => {
      const response = await request(app)
        .get('/api/v1/users')
        .set('Authorization', `Bearer ${adminToken}`);

      expectSuccessResponse(response, 200);
      expect(response.body.data).toHaveProperty('users');
      expect(response.body.data).toHaveProperty('pagination');
      expect(Array.isArray(response.body.data.users)).toBe(true);
    });

    it('should reject unauthorized access', async () => {
      const response = await request(app)
        .get('/api/v1/users')
        .set('Authorization', `Bearer ${userToken}`);

      expect(response.status).toBe(403);
    });

    it('should support pagination', async () => {
      const response = await request(app)
        .get('/api/v1/users?page=1&limit=10')
        .set('Authorization', `Bearer ${adminToken}`);

      expectSuccessResponse(response, 200);
      expect(response.body.data.pagination).toHaveProperty('page');
      expect(response.body.data.pagination).toHaveProperty('totalPages');
      expect(response.body.data.pagination).toHaveProperty('total');
    });

    it('should support search', async () => {
      const response = await request(app)
        .get('/api/v1/users?search=test')
        .set('Authorization', `Bearer ${adminToken}`);

      expectSuccessResponse(response, 200);
      expect(Array.isArray(response.body.data.users)).toBe(true);
    });
  });

  describe('POST /api/v1/users', () => {
    it('should create new user as admin', async () => {
      const userData = {
        username: `newuser${Math.floor(Math.random() * 100000)}`,
        password: 'Password123!',
        full_name: 'New User',
        email: uniqueEmail(),
        role: 'USER'
      };

      const response = await request(app)
        .post('/api/v1/users')
        .set('Authorization', `Bearer ${adminToken}`)
        .send(userData);

      expectSuccessResponse(response, 201);
      expect(response.body.data.username).toBe(userData.username);
      expect(response.body.data).not.toHaveProperty('password_hash');

      // Cleanup
      await User.destroy({ where: { username: userData.username } });
    });

    it('should reject non-admin user creation', async () => {
      const userData = {
        username: `newuser${Math.floor(Math.random() * 100000)}`,
        password: 'Password123!',
        full_name: 'New User',
        email: uniqueEmail(),
        role: 'USER'
      };

      const response = await request(app)
        .post('/api/v1/users')
        .set('Authorization', `Bearer ${userToken}`)
        .send(userData);

      expect(response.status).toBe(403);
    });

    it('should reject duplicate username', async () => {
      const userData = {
        username: 'testuser',
        password: 'Password123!',
        full_name: 'Duplicate User',
        email: uniqueEmail(),
        role: 'USER'
      };

      const response = await request(app)
        .post('/api/v1/users')
        .set('Authorization', `Bearer ${adminToken}`)
        .send(userData);

      expect(response.status).toBe(409);
    });
  });

  describe('GET /api/v1/users/:id', () => {
    it('should get user by id as admin', async () => {
      const user = await User.findOne({ where: { username: 'testuser' } });

      const response = await request(app)
        .get(`/api/v1/users/${user?.id}`)
        .set('Authorization', `Bearer ${adminToken}`);

      expectSuccessResponse(response, 200);
      expect(response.body.data.username).toBe('testuser');
      expect(response.body.data).not.toHaveProperty('password_hash');
    });

    it('should return 404 for non-existent user', async () => {
      const response = await request(app)
        .get('/api/v1/users/999999')
        .set('Authorization', `Bearer ${adminToken}`);

      expectErrorResponse(response, 404, 'USER_NOT_FOUND');
    });
  });

  describe('PUT /api/v1/users/:id', () => {
    it('should update user as admin', async () => {
      const user = await User.findOne({ where: { username: 'testuser' } });

      const response = await request(app)
        .put(`/api/v1/users/${user?.id}`)
        .set('Authorization', `Bearer ${adminToken}`)
        .send({
          full_name: 'Updated Test User'
        });

      expectSuccessResponse(response, 200);
      expect(response.body.data.full_name).toBe('Updated Test User');

      // Restore original name
      await user?.update({ full_name: 'Test User' });
    });

    it('should reject non-admin update', async () => {
      const user = await User.findOne({ where: { username: 'testadmin' } });

      const response = await request(app)
        .put(`/api/v1/users/${user?.id}`)
        .set('Authorization', `Bearer ${userToken}`)
        .send({
          full_name: 'Hacked Admin'
        });

      expect(response.status).toBe(403);
    });
  });

  describe('DELETE /api/v1/users/:id', () => {
    it('should soft delete user as admin', async () => {
      // Create user to delete
      const userToDelete = await User.create({
        username: `deleteuser${Math.floor(Math.random() * 100000)}`,
        password_hash: 'hash',
        full_name: 'Delete Me',
        email: uniqueEmail(),
        role: 'USER',
        is_active: true,
        email_verified: false
      });

      const response = await request(app)
        .delete(`/api/v1/users/${userToDelete.id}`)
        .set('Authorization', `Bearer ${adminToken}`);

      expectSuccessResponse(response, 200, false);

      // Verify soft delete
      await userToDelete.reload();
      expect(userToDelete.is_active).toBe(false);

      // Cleanup
      await userToDelete.destroy();
    });

    it('should reject non-admin deletion', async () => {
      const user = await User.findOne({ where: { username: 'testadmin' } });

      const response = await request(app)
        .delete(`/api/v1/users/${user?.id}`)
        .set('Authorization', `Bearer ${userToken}`);

      expect(response.status).toBe(403);
    });
  });

  describe('PUT /api/v1/users/:id/toggle-status', () => {
    it('should toggle user status as admin', async () => {
      const user = await User.findOne({ where: { username: 'testuser' } });
      const originalStatus = user?.is_active;

      const response = await request(app)
        .put(`/api/v1/users/${user?.id}/toggle-status`)
        .set('Authorization', `Bearer ${adminToken}`);

      expectSuccessResponse(response, 200);

      // Restore original status
      await user?.update({ is_active: originalStatus });
    });

    it('should reject non-admin status toggle', async () => {
      const user = await User.findOne({ where: { username: 'testadmin' } });

      const response = await request(app)
        .put(`/api/v1/users/${user?.id}/toggle-status`)
        .set('Authorization', `Bearer ${userToken}`);

      expect(response.status).toBe(403);
    });
  });
});

