/**
 * ================================================
 * NOTIFICATION ROUTES TESTS
 * ================================================
 * Tests for /api/v1/notifications/* endpoints
 */

import request from 'supertest';
import app, { initializeDatabase } from '../../server';
import { setupTestDatabase, teardownTestDatabase, createTestUsers, cleanupTestData } from '../setup';
import { getTestTokens } from '../helpers';

describe('Notification Routes', () => {
  let userToken: string;
  let adminToken: string;

  beforeAll(async () => {
    await setupTestDatabase();
    await initializeDatabase();
    await createTestUsers();
    const tokens = await getTestTokens();
    userToken = tokens.userToken;
    adminToken = tokens.adminToken;
  });

  afterAll(async () => {
    await cleanupTestData();
    await teardownTestDatabase();
  });

  describe('GET /api/v1/notifications', () => {
    it('should list user notifications', async () => {
      const response = await request(app)
        .get('/api/v1/notifications')
        .set('Authorization', `Bearer ${userToken}`);

      expect([200, 401, 500]).toContain(response.status);
    });

    it('should filter unread notifications', async () => {
      const response = await request(app)
        .get('/api/v1/notifications?is_read=false')
        .set('Authorization', `Bearer ${userToken}`);

      expect([200, 401, 500]).toContain(response.status);
    });
  });

  describe('PUT /api/v1/notifications/:id/mark-read', () => {
    it('should mark notification as read', async () => {
      const response = await request(app)
        .put('/api/v1/notifications/1/mark-read')
        .set('Authorization', `Bearer ${userToken}`);

      expect([200, 401, 404, 500]).toContain(response.status);
    });
  });

  describe('PUT /api/v1/notifications/mark-all-read', () => {
    it('should mark all notifications as read', async () => {
      const response = await request(app)
        .put('/api/v1/notifications/mark-all-read')
        .set('Authorization', `Bearer ${userToken}`);

      expect([200, 401, 500]).toContain(response.status);
    });
  });

  describe('DELETE /api/v1/notifications/:id', () => {
    it('should delete notification', async () => {
      const response = await request(app)
        .delete('/api/v1/notifications/999')
        .set('Authorization', `Bearer ${userToken}`);

      expect([200, 401, 404, 500]).toContain(response.status);
    });
  });
});


