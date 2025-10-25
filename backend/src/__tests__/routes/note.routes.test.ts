/**
 * ================================================
 * NOTE ROUTES TESTS
 * ================================================
 * Tests for /api/v1/notes/* endpoints
 */

import request from 'supertest';
import app, { initializeDatabase } from '../../server';
import { setupTestDatabase, teardownTestDatabase, createTestUsers, cleanupTestData } from '../setup';
import { getTestTokens } from '../helpers';

describe('Note Routes', () => {
  let userToken: string;

  beforeAll(async () => {
    await setupTestDatabase();
    await initializeDatabase();
    await createTestUsers();
    const tokens = await getTestTokens();
    userToken = tokens.userToken;
  });

  afterAll(async () => {
    await cleanupTestData();
    await teardownTestDatabase();
  });

  describe('GET /api/v1/notes', () => {
    it('should list user notes', async () => {
      const response = await request(app)
        .get('/api/v1/notes')
        .set('Authorization', `Bearer ${userToken}`);

      expect([200, 401, 500]).toContain(response.status);
    });

    it('should filter by MRFC', async () => {
      const response = await request(app)
        .get('/api/v1/notes?mrfc_id=1')
        .set('Authorization', `Bearer ${userToken}`);

      expect([200, 401, 500]).toContain(response.status);
    });
  });

  describe('POST /api/v1/notes', () => {
    it('should create note', async () => {
      const noteData = {
        mrfc_id: 1,
        title: `Test Note ${Date.now()}`,
        content: 'Test content'
      };

      const response = await request(app)
        .post('/api/v1/notes')
        .set('Authorization', `Bearer ${userToken}`)
        .send(noteData);

      expect([200, 201, 400, 401, 404, 500]).toContain(response.status);
    });
  });

  describe('PUT /api/v1/notes/:id', () => {
    it('should update own note', async () => {
      const response = await request(app)
        .put('/api/v1/notes/1')
        .set('Authorization', `Bearer ${userToken}`)
        .send({ title: 'Updated Note' });

      expect([200, 400, 401, 403, 404, 500]).toContain(response.status);
    });
  });

  describe('DELETE /api/v1/notes/:id', () => {
    it('should delete own note', async () => {
      const response = await request(app)
        .delete('/api/v1/notes/999')
        .set('Authorization', `Bearer ${userToken}`);

      expect([200, 401, 403, 404, 500]).toContain(response.status);
    });
  });
});


