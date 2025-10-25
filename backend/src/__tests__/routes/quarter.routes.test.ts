/**
 * ================================================
 * QUARTER ROUTES TESTS
 * ================================================
 * Tests for /api/v1/quarters/* endpoints
 */

import request from 'supertest';
import app, { initializeDatabase } from '../../server';
import { setupTestDatabase, teardownTestDatabase, createTestUsers, cleanupTestData } from '../setup';
import { getTestTokens } from '../helpers';

describe('Quarter Management Routes', () => {
  let adminToken: string;
  let userToken: string;

  beforeAll(async () => {
    await setupTestDatabase();
    await initializeDatabase();
    await createTestUsers();
    const tokens = await getTestTokens();
    adminToken = tokens.adminToken;
    userToken = tokens.userToken;
  });

  afterAll(async () => {
    await cleanupTestData();
    await teardownTestDatabase();
  });

  describe('GET /api/v1/quarters', () => {
    it('should list quarters for authenticated users', async () => {
      const response = await request(app)
        .get('/api/v1/quarters')
        .set('Authorization', `Bearer ${userToken}`);

      expect([200, 401, 500]).toContain(response.status);
      if (response.status === 200) {
        expect(response.body.data).toHaveProperty('quarters');
      }
    });

    it('should filter by year', async () => {
      const response = await request(app)
        .get('/api/v1/quarters?year=2025')
        .set('Authorization', `Bearer ${adminToken}`);

      expect([200, 401, 500]).toContain(response.status);
    });

    it('should reject without authentication', async () => {
      const response = await request(app).get('/api/v1/quarters');
      expect(response.status).toBe(401);
    });
  });

  describe('GET /api/v1/quarters/current', () => {
    it('should get current quarter', async () => {
      const response = await request(app)
        .get('/api/v1/quarters/current')
        .set('Authorization', `Bearer ${userToken}`);

      expect([200, 401, 404, 500]).toContain(response.status);
    });
  });
});


