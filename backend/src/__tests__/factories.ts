/**
 * ================================================
 * TEST DATA FACTORIES
 * ================================================
 * Factory functions to generate consistent test data
 */

import { randomString, uniqueEmail, uniqueUsername } from './helpers';

/**
 * Factory for creating test user data
 */
export const userFactory = {
  create: (overrides: any = {}) => ({
    username: uniqueUsername(),
    password: 'TestPass123!',
    full_name: `Test User ${randomString()}`,
    email: uniqueEmail(),
    ...overrides
  }),

  admin: (overrides: any = {}) => ({
    username: uniqueUsername(),
    password: 'AdminPass123!',
    full_name: `Admin User ${randomString()}`,
    email: uniqueEmail(),
    role: 'ADMIN',
    ...overrides
  })
};

/**
 * Factory for creating test MRFC data
 */
export const mrfcFactory = {
  create: (overrides: any = {}) => ({
    name: `Test MRFC ${randomString()}`,
    municipality: 'Test Municipality',
    province: 'Test Province',
    region: 'Region XII',
    contact_person: 'John Doe',
    contact_number: '+63 912 345 6789',
    email: uniqueEmail(),
    address: '123 Test Street',
    ...overrides
  })
};

/**
 * Factory for creating test proponent data
 */
export const proponentFactory = {
  create: (overrides: any = {}) => ({
    company_name: `Test Company ${randomString()}`,
    contact_person: 'Jane Smith',
    contact_email: uniqueEmail(),
    contact_phone: '+63 912 345 6789',
    address: '456 Business Avenue',
    tin: `${Math.floor(100000000 + Math.random() * 900000000)}`,
    ...overrides
  })
};

/**
 * Factory for creating test quarter data
 */
export const quarterFactory = {
  create: (overrides: any = {}) => ({
    year: new Date().getFullYear(),
    quarter_number: 1,
    start_date: new Date(`${new Date().getFullYear()}-01-01`),
    end_date: new Date(`${new Date().getFullYear()}-03-31`),
    ...overrides
  })
};

/**
 * Factory for creating test agenda data
 */
export const agendaFactory = {
  create: (quarter_id: number, mrfc_id: number, overrides: any = {}) => ({
    quarter_id,
    mrfc_id,
    agenda_number: `Item ${Math.floor(Math.random() * 100)}`,
    title: `Test Agenda ${randomString()}`,
    description: 'Test agenda description',
    status: 'SCHEDULED',
    scheduled_date: new Date(),
    ...overrides
  })
};

/**
 * Factory for creating test note data
 */
export const noteFactory = {
  create: (mrfc_id: number, overrides: any = {}) => ({
    mrfc_id,
    title: `Test Note ${randomString()}`,
    content: 'Test note content',
    ...overrides
  })
};

/**
 * Factory for creating test document data
 */
export const documentFactory = {
  create: (mrfc_id: number, overrides: any = {}) => ({
    mrfc_id,
    filename: `test_document_${randomString()}.pdf`,
    file_url: 'https://example.com/test.pdf',
    file_size: 1024,
    file_type: 'application/pdf',
    category: 'GENERAL',
    ...overrides
  })
};

/**
 * Factory for creating test attendance data
 */
export const attendanceFactory = {
  create: (agenda_id: number, user_id: number, overrides: any = {}) => ({
    agenda_id,
    user_id,
    status: 'PRESENT',
    remarks: 'Test attendance',
    ...overrides
  })
};

