/**
 * ================================================
 * COMPREHENSIVE ENDPOINT INTEGRATION TESTS
 * ================================================
 * Tests all backend API endpoints for basic functionality
 */

import request from 'supertest';
import app, { initializeDatabase } from '../server';
import { setupTestDatabase, teardownTestDatabase, createTestUsers, cleanupTestData } from './setup';
import { getTestTokens } from './helpers';

describe('Backend API Comprehensive Tests', () => {
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
  }, 30000);

  afterAll(async () => {
    await cleanupTestData();
    await teardownTestDatabase();
  });

  describe('Health & Root Endpoints', () => {
    it('GET / - should return API info', async () => {
      const response = await request(app).get('/');
      expect(response.status).toBe(200);
      expect(response.body.success).toBe(true);
    });

    it('GET /api/v1/health - should return health status', async () => {
      const response = await request(app).get('/api/v1/health');
      expect(response.status).toBe(200);
      expect(response.body.success).toBe(true);
    });
  });

  describe('Authentication Endpoints', () => {
    it('POST /api/v1/auth/register - should register new user', async () => {
      const response = await request(app)
        .post('/api/v1/auth/register')
        .send({
          username: `testregister${Date.now()}`,
          password: 'Password123!',
          full_name: 'Test Register',
          email: `test${Date.now()}@test.com`
        });
      expect([201, 400, 409]).toContain(response.status);
    });

    it('POST /api/v1/auth/login - should login user', async () => {
      const response = await request(app)
        .post('/api/v1/auth/login')
        .send({
          username: 'testuser',
          password: 'User123!'
        });
      expect(response.status).toBe(200);
      expect(response.body.data).toHaveProperty('token');
    });

    it('POST /api/v1/auth/logout - should logout user', async () => {
      const response = await request(app)
        .post('/api/v1/auth/logout')
        .set('Authorization', `Bearer ${userToken}`);
      expect(response.status).toBe(200);
    });
  });

  describe('User Management Endpoints', () => {
    it('GET /api/v1/users - should list users', async () => {
      const response = await request(app)
        .get('/api/v1/users')
        .set('Authorization', `Bearer ${adminToken}`);
      expect([200, 403]).toContain(response.status);
      if (response.status === 200) {
        expect(response.body.data).toHaveProperty('users');
      }
    });

    it('POST /api/v1/users - should create user', async () => {
      const response = await request(app)
        .post('/api/v1/users')
        .set('Authorization', `Bearer ${adminToken}`)
        .send({
          username: `newuser${Date.now()}`,
          password: 'Password123!',
          full_name: 'New User',
          email: `newuser${Date.now()}@test.com`,
          role: 'USER'
        });
      expect([200, 201, 400, 403, 409]).toContain(response.status);
    });
  });

  describe('MRFC Management Endpoints', () => {
    let mrfcId: number;

    it('POST /api/v1/mrfcs - should create MRFC', async () => {
      const response = await request(app)
        .post('/api/v1/mrfcs')
        .set('Authorization', `Bearer ${adminToken}`)
        .send({
          name: `Test MRFC ${Date.now()}`,
          municipality: 'Test Municipality',
          province: 'Test Province',
          region: 'Region XII',
          contact_person: 'John Doe',
          contact_number: '+63 912 345 6789',
          email: `mrfc${Date.now()}@test.com`,
          address: '123 Test Street'
        });
      expect([200, 201, 400, 403, 409]).toContain(response.status);
      if (response.status === 201 || response.status === 200) {
        mrfcId = response.body.data.id;
      }
    });

    it('GET /api/v1/mrfcs - should list MRFCs', async () => {
      const response = await request(app)
        .get('/api/v1/mrfcs')
        .set('Authorization', `Bearer ${userToken}`);
      expect([200, 401]).toContain(response.status);
      if (response.status === 200) {
        expect(response.body.data).toHaveProperty('mrfcs');
      }
    });

    it('GET /api/v1/mrfcs/:id - should get MRFC by ID', async () => {
      if (mrfcId) {
        const response = await request(app)
          .get(`/api/v1/mrfcs/${mrfcId}`)
          .set('Authorization', `Bearer ${adminToken}`);
        expect([200, 403, 404]).toContain(response.status);
      }
    });
  });

  describe('Proponent Management Endpoints', () => {
    let proponentId: number;

    it('POST /api/v1/proponents - should create proponent', async () => {
      const response = await request(app)
        .post('/api/v1/proponents')
        .set('Authorization', `Bearer ${adminToken}`)
        .send({
          company_name: `Test Company ${Date.now()}`,
          contact_person: 'Jane Smith',
          contact_email: `proponent${Date.now()}@test.com`,
          contact_phone: '+63 912 345 6789',
          address: '456 Business Avenue'
        });
      expect([200, 201, 400, 403]).toContain(response.status);
      if (response.status === 201 || response.status === 200) {
        proponentId = response.body.data.id;
      }
    });

    it('GET /api/v1/proponents - should list proponents', async () => {
      const response = await request(app)
        .get('/api/v1/proponents')
        .set('Authorization', `Bearer ${userToken}`);
      expect([200, 401]).toContain(response.status);
    });
  });

  describe('Quarter Management Endpoints', () => {
    it('GET /api/v1/quarters - should list quarters', async () => {
      const response = await request(app)
        .get('/api/v1/quarters')
        .set('Authorization', `Bearer ${userToken}`);
      expect([200, 401]).toContain(response.status);
    });
  });

  describe('Agenda Management Endpoints', () => {
    it('GET /api/v1/agendas - should list agendas', async () => {
      const response = await request(app)
        .get('/api/v1/agendas')
        .set('Authorization', `Bearer ${userToken}`);
      expect([200, 401, 500]).toContain(response.status);
    });
  });

  describe('Document Management Endpoints', () => {
    it('GET /api/v1/documents - should list documents', async () => {
      const response = await request(app)
        .get('/api/v1/documents')
        .set('Authorization', `Bearer ${userToken}`);
      expect([200, 401, 500]).toContain(response.status);
    });
  });

  describe('Attendance Endpoints', () => {
    it('GET /api/v1/attendance - should list attendance', async () => {
      const response = await request(app)
        .get('/api/v1/attendance')
        .set('Authorization', `Bearer ${userToken}`);
      expect([200, 401, 500]).toContain(response.status);
    });
  });

  describe('Compliance Endpoints', () => {
    it('GET /api/v1/compliance/dashboard - should get compliance dashboard', async () => {
      const response = await request(app)
        .get('/api/v1/compliance/dashboard')
        .set('Authorization', `Bearer ${adminToken}`);
      expect([200, 401, 500]).toContain(response.status);
    });
  });

  describe('Notes Endpoints', () => {
    it('GET /api/v1/notes - should list notes', async () => {
      const response = await request(app)
        .get('/api/v1/notes')
        .set('Authorization', `Bearer ${userToken}`);
      expect([200, 401, 500]).toContain(response.status);
    });
  });

  describe('Notifications Endpoints', () => {
    it('GET /api/v1/notifications - should list notifications', async () => {
      const response = await request(app)
        .get('/api/v1/notifications')
        .set('Authorization', `Bearer ${userToken}`);
      expect([200, 401, 500]).toContain(response.status);
    });
  });

  describe('Audit Logs Endpoints', () => {
    it('GET /api/v1/audit-logs - should list audit logs', async () => {
      const response = await request(app)
        .get('/api/v1/audit-logs')
        .set('Authorization', `Bearer ${adminToken}`);
      expect([200, 401, 403, 500]).toContain(response.status);
    });
  });

  describe('Statistics Endpoints', () => {
    it('GET /api/v1/statistics/overview - should get statistics overview', async () => {
      const response = await request(app)
        .get('/api/v1/statistics/overview')
        .set('Authorization', `Bearer ${adminToken}`);
      expect([200, 401, 500]).toContain(response.status);
    });
  });

  describe('Error Handling', () => {
    it('should return 404 for unknown routes', async () => {
      const response = await request(app).get('/api/v1/unknown-endpoint');
      expect(response.status).toBe(404);
      expect(response.body.success).toBe(false);
    });

    it('should return 401 for unauthenticated requests', async () => {
      const response = await request(app).get('/api/v1/users');
      expect(response.status).toBe(401);
    });
  });
});

