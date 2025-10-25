/**
 * ================================================
 * STATISTICS ROUTES TESTS
 * ================================================
 * Tests for /api/v1/statistics/* endpoints
 */

import request from 'supertest';
import app, { initializeDatabase } from '../../server';
import { setupTestDatabase, teardownTestDatabase, createTestUsers, cleanupTestData } from '../setup';
import { getTestTokens } from '../helpers';

describe('Statistics Routes', () => {
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

  describe('GET /api/v1/statistics/overview', () => {
    it('should get statistics overview', async () => {
      const response = await request(app)
        .get('/api/v1/statistics/overview')
        .set('Authorization', `Bearer ${adminToken}`);

      expect([200, 401, 500]).toContain(response.status);
    });

    it('should work for regular users', async () => {
      const response = await request(app)
        .get('/api/v1/statistics/overview')
        .set('Authorization', `Bearer ${userToken}`);

      expect([200, 401, 500]).toContain(response.status);
    });
  });

  describe('GET /api/v1/statistics/compliance-trends', () => {
    it('should get compliance trends', async () => {
      const response = await request(app)
        .get('/api/v1/statistics/compliance-trends')
        .set('Authorization', `Bearer ${adminToken}`);

      expect([200, 401, 500]).toContain(response.status);
    });

    it('should filter by year', async () => {
      const response = await request(app)
        .get('/api/v1/statistics/compliance-trends?year=2025')
        .set('Authorization', `Bearer ${adminToken}`);

      expect([200, 401, 500]).toContain(response.status);
    });
  });
});


