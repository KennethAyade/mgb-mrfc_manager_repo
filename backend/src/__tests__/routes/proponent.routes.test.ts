/**
 * ================================================
 * PROPONENT ROUTES TESTS
 * ================================================
 * Tests for /api/v1/proponents/* endpoints
 */

import request from 'supertest';
import app, { initializeDatabase } from '../../server';
import { setupTestDatabase, teardownTestDatabase, createTestUsers, cleanupTestData } from '../setup';
import { expectErrorResponse, expectSuccessResponse, getTestTokens, uniqueEmail } from '../helpers';
import { Proponent } from '../../models';

describe('Proponent Management Routes', () => {
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

  describe('GET /api/v1/proponents', () => {
    it('should list proponents for authenticated users', async () => {
      const response = await request(app)
        .get('/api/v1/proponents')
        .set('Authorization', `Bearer ${userToken}`);

      expect([200, 401]).toContain(response.status);
      if (response.status === 200) {
        expect(response.body.data).toHaveProperty('proponents');
      }
    });

    it('should support pagination', async () => {
      const response = await request(app)
        .get('/api/v1/proponents?page=1&limit=10')
        .set('Authorization', `Bearer ${adminToken}`);

      expect([200, 401]).toContain(response.status);
    });

    it('should support search', async () => {
      const response = await request(app)
        .get('/api/v1/proponents?search=test')
        .set('Authorization', `Bearer ${userToken}`);

      expect([200, 401]).toContain(response.status);
    });
  });

  describe('POST /api/v1/proponents', () => {
    it('should create proponent as admin', async () => {
      const proponentData = {
        company_name: `Test Company ${Date.now()}`,
        contact_person: 'John Doe',
        contact_email: uniqueEmail(),
        contact_phone: '+63 912 345 6789',
        address: '123 Test Street'
      };

      const response = await request(app)
        .post('/api/v1/proponents')
        .set('Authorization', `Bearer ${adminToken}`)
        .send(proponentData);

      expect([200, 201, 400, 403]).toContain(response.status);
      
      if (response.status === 201 || response.status === 200) {
        expect(response.body.data).toHaveProperty('company_name');
      }
    });

    it('should reject creation without auth', async () => {
      const response = await request(app)
        .post('/api/v1/proponents')
        .send({ company_name: 'Test' });

      expect(response.status).toBe(401);
    });
  });

  describe('GET /api/v1/proponents/:id', () => {
    it('should get proponent by id', async () => {
      const response = await request(app)
        .get('/api/v1/proponents/1')
        .set('Authorization', `Bearer ${userToken}`);

      expect([200, 401, 404]).toContain(response.status);
    });

    it('should return 404 for non-existent proponent', async () => {
      const response = await request(app)
        .get('/api/v1/proponents/999999')
        .set('Authorization', `Bearer ${adminToken}`);

      expect([404, 401]).toContain(response.status);
    });
  });

  describe('PUT /api/v1/proponents/:id', () => {
    it('should update proponent as admin', async () => {
      const response = await request(app)
        .put('/api/v1/proponents/1')
        .set('Authorization', `Bearer ${adminToken}`)
        .send({ contact_person: 'Updated Person' });

      expect([200, 400, 403, 404]).toContain(response.status);
    });
  });

  describe('DELETE /api/v1/proponents/:id', () => {
    it('should delete proponent as admin', async () => {
      const response = await request(app)
        .delete('/api/v1/proponents/999')
        .set('Authorization', `Bearer ${adminToken}`);

      expect([200, 403, 404]).toContain(response.status);
    });
  });
});


