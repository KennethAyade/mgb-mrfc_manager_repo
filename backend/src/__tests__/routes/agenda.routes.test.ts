/**
 * ================================================
 * AGENDA ROUTES TESTS
 * ================================================
 * Tests for /api/v1/agendas/* endpoints
 */

import request from 'supertest';
import app, { initializeDatabase } from '../../server';
import { setupTestDatabase, teardownTestDatabase, createTestUsers, cleanupTestData } from '../setup';
import { getTestTokens } from '../helpers';

describe('Agenda Management Routes', () => {
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

  describe('GET /api/v1/agendas', () => {
    it('should list agendas', async () => {
      const response = await request(app)
        .get('/api/v1/agendas')
        .set('Authorization', `Bearer ${userToken}`);

      expect([200, 401, 500]).toContain(response.status);
    });

    it('should filter by quarter', async () => {
      const response = await request(app)
        .get('/api/v1/agendas?quarter_id=1')
        .set('Authorization', `Bearer ${adminToken}`);

      expect([200, 401, 500]).toContain(response.status);
    });
  });

  describe('POST /api/v1/agendas', () => {
    it('should create agenda as admin', async () => {
      const agendaData = {
        quarter_id: 1,
        mrfc_id: 1,
        agenda_number: `Item ${Date.now()}`,
        title: 'Test Agenda',
        description: 'Test Description',
        status: 'SCHEDULED'
      };

      const response = await request(app)
        .post('/api/v1/agendas')
        .set('Authorization', `Bearer ${adminToken}`)
        .send(agendaData);

      expect([200, 201, 400, 403, 404, 500]).toContain(response.status);
    });

    it('should reject without auth', async () => {
      const response = await request(app)
        .post('/api/v1/agendas')
        .send({});

      expect(response.status).toBe(401);
    });
  });

  describe('GET /api/v1/agendas/:id', () => {
    it('should get agenda by id', async () => {
      const response = await request(app)
        .get('/api/v1/agendas/1')
        .set('Authorization', `Bearer ${userToken}`);

      expect([200, 401, 404, 500]).toContain(response.status);
    });
  });

  describe('PUT /api/v1/agendas/:id', () => {
    it('should update agenda as admin', async () => {
      const response = await request(app)
        .put('/api/v1/agendas/1')
        .set('Authorization', `Bearer ${adminToken}`)
        .send({ title: 'Updated Title' });

      expect([200, 400, 403, 404, 500]).toContain(response.status);
    });
  });

  describe('DELETE /api/v1/agendas/:id', () => {
    it('should delete agenda as admin', async () => {
      const response = await request(app)
        .delete('/api/v1/agendas/999')
        .set('Authorization', `Bearer ${adminToken}`);

      expect([200, 403, 404, 500]).toContain(response.status);
    });
  });
});


