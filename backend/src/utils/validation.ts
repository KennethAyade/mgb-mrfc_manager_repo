/**
 * INPUT VALIDATION UTILITIES
 * ==========================
 * Joi schemas for validating request bodies, params, and queries
 * CRITICAL: Always validate user input to prevent injection attacks
 */

import Joi from 'joi';

/**
 * ===========================================
 * AUTHENTICATION VALIDATIONS
 * ===========================================
 */

export const registerSchema = Joi.object({
  username: Joi.string()
    .alphanum()
    .min(3)
    .max(50)
    .required()
    .messages({
      'string.alphanum': 'Username must only contain alphanumeric characters',
      'string.min': 'Username must be at least 3 characters',
      'string.max': 'Username cannot exceed 50 characters',
      'any.required': 'Username is required'
    }),
  password: Joi.string()
    .min(8)
    .required()
    .messages({
      'string.min': 'Password must be at least 8 characters',
      'any.required': 'Password is required'
    }),
  full_name: Joi.string()
    .min(2)
    .max(100)
    .required()
    .messages({
      'string.min': 'Full name must be at least 2 characters',
      'any.required': 'Full name is required'
    }),
  email: Joi.string()
    .email()
    .required()
    .messages({
      'string.email': 'Must be a valid email address',
      'any.required': 'Email is required'
    }),
  role: Joi.string()
    .valid('USER')
    .default('USER')
    .messages({
      'any.only': 'Self-registration only allows USER role'
    })
});

export const loginSchema = Joi.object({
  username: Joi.string()
    .required()
    .messages({
      'any.required': 'Username is required'
    }),
  password: Joi.string()
    .required()
    .messages({
      'any.required': 'Password is required'
    })
});

export const changePasswordSchema = Joi.object({
  currentPassword: Joi.string()
    .required()
    .messages({
      'any.required': 'Current password is required'
    }),
  newPassword: Joi.string()
    .min(8)
    .required()
    .messages({
      'string.min': 'New password must be at least 8 characters',
      'any.required': 'New password is required'
    })
});

/**
 * ===========================================
 * USER MANAGEMENT VALIDATIONS
 * ===========================================
 */

export const createUserSchema = Joi.object({
  username: Joi.string()
    .alphanum()
    .min(3)
    .max(50)
    .required(),
  password: Joi.string()
    .min(8)
    .required(),
  full_name: Joi.string()
    .min(2)
    .max(100)
    .required(),
  email: Joi.string()
    .email()
    .required(),
  role: Joi.string()
    .valid('SUPER_ADMIN', 'ADMIN', 'USER')
    .default('USER')
});

export const updateUserSchema = Joi.object({
  full_name: Joi.string()
    .min(2)
    .max(100),
  email: Joi.string()
    .email(),
  role: Joi.string()
    .valid('SUPER_ADMIN', 'ADMIN', 'USER'),
  is_active: Joi.boolean()
});

/**
 * ===========================================
 * MRFC VALIDATIONS
 * ===========================================
 */

export const createMrfcSchema = Joi.object({
  name: Joi.string()
    .min(3)
    .max(200)
    .required()
    .messages({
      'any.required': 'MRFC name is required'
    }),
  municipality: Joi.string()
    .min(2)
    .max(100)
    .required()
    .messages({
      'any.required': 'Municipality is required'
    }),
  province: Joi.string()
    .max(100)
    .optional(),
  region: Joi.string()
    .max(50)
    .optional(),
  contact_person: Joi.string()
    .max(100)
    .optional(),
  contact_number: Joi.string()
    .pattern(/^[0-9+\-() ]+$/)
    .max(20)
    .optional()
    .messages({
      'string.pattern.base': 'Invalid phone number format'
    }),
  email: Joi.string()
    .email()
    .optional(),
  address: Joi.string()
    .max(500)
    .optional()
});

export const updateMrfcSchema = Joi.object({
  name: Joi.string()
    .min(3)
    .max(200),
  municipality: Joi.string()
    .min(2)
    .max(100),
  province: Joi.string()
    .max(100),
  region: Joi.string()
    .max(50),
  contact_person: Joi.string()
    .max(100),
  contact_number: Joi.string()
    .pattern(/^[0-9+\-() ]+$/)
    .max(20),
  email: Joi.string()
    .email(),
  address: Joi.string()
    .max(500),
  is_active: Joi.boolean()
});

/**
 * ===========================================
 * PROPONENT VALIDATIONS
 * ===========================================
 */

export const createProponentSchema = Joi.object({
  name: Joi.string()
    .min(2)
    .max(200)
    .required()
    .messages({
      'any.required': 'Proponent name is required'
    }),
  company_name: Joi.string()
    .min(2)
    .max(200)
    .required()
    .messages({
      'any.required': 'Company name is required'
    }),
  permit_number: Joi.string()
    .max(50)
    .optional(),
  permit_type: Joi.string()
    .max(50)
    .optional(),
  status: Joi.string()
    .valid('ACTIVE', 'INACTIVE', 'SUSPENDED')
    .default('ACTIVE'),
  contact_person: Joi.string()
    .max(100)
    .optional(),
  contact_number: Joi.string()
    .pattern(/^[0-9+\-() ]+$/)
    .max(20)
    .optional(),
  email: Joi.string()
    .email()
    .optional(),
  address: Joi.string()
    .max(500)
    .optional()
});

export const updateProponentSchema = Joi.object({
  name: Joi.string()
    .min(2)
    .max(200),
  company_name: Joi.string()
    .min(2)
    .max(200),
  permit_number: Joi.string()
    .max(50),
  permit_type: Joi.string()
    .max(50),
  status: Joi.string()
    .valid('ACTIVE', 'INACTIVE', 'SUSPENDED'),
  contact_person: Joi.string()
    .max(100),
  contact_number: Joi.string()
    .pattern(/^[0-9+\-() ]+$/)
    .max(20),
  email: Joi.string()
    .email(),
  address: Joi.string()
    .max(500)
});

/**
 * ===========================================
 * AGENDA VALIDATIONS
 * ===========================================
 */

export const createAgendaSchema = Joi.object({
  quarter_id: Joi.number()
    .integer()
    .positive()
    .required()
    .messages({
      'any.required': 'Quarter ID is required'
    }),
  meeting_date: Joi.date()
    .required()
    .messages({
      'any.required': 'Meeting date is required'
    }),
  meeting_time: Joi.string()
    .pattern(/^([0-1]?[0-9]|2[0-3]):[0-5][0-9]$/)
    .optional()
    .messages({
      'string.pattern.base': 'Time must be in HH:MM format (e.g., 09:00)'
    }),
  location: Joi.string()
    .min(5)
    .max(500)
    .required()
    .messages({
      'any.required': 'Meeting location is required'
    }),
  matters_arising: Joi.array()
    .items(
      Joi.object({
        issue: Joi.string().required(),
        status: Joi.string().valid('PENDING', 'IN_PROGRESS', 'RESOLVED').default('PENDING'),
        assigned_to: Joi.string().optional(),
        date_raised: Joi.date().required(),
        remarks: Joi.string().optional()
      })
    )
    .optional()
});

export const updateAgendaSchema = Joi.object({
  meeting_date: Joi.date(),
  meeting_time: Joi.string()
    .pattern(/^([0-1]?[0-9]|2[0-3]):[0-5][0-9]$/),
  location: Joi.string()
    .min(5)
    .max(500),
  status: Joi.string()
    .valid('DRAFT', 'PUBLISHED', 'COMPLETED', 'CANCELLED')
});

/**
 * ===========================================
 * DOCUMENT VALIDATIONS
 * ===========================================
 */

export const uploadDocumentSchema = Joi.object({
  proponent_id: Joi.number()
    .integer()
    .positive()
    .required()
    .messages({
      'any.required': 'Proponent ID is required'
    }),
  quarter_id: Joi.number()
    .integer()
    .positive()
    .required()
    .messages({
      'any.required': 'Quarter ID is required'
    }),
  category: Joi.string()
    .valid('MTF_REPORT', 'AEPEP', 'CMVR', 'SDMP', 'PRODUCTION', 'SAFETY', 'OTHER')
    .required()
    .messages({
      'any.required': 'Document category is required'
    }),
  remarks: Joi.string()
    .max(500)
    .optional()
});

export const updateDocumentSchema = Joi.object({
  status: Joi.string()
    .valid('PENDING', 'ACCEPTED', 'REJECTED'),
  remarks: Joi.string()
    .max(500)
});

/**
 * ===========================================
 * COMPLIANCE VALIDATIONS
 * ===========================================
 */

export const complianceOverrideSchema = Joi.object({
  proponent_id: Joi.number()
    .integer()
    .positive()
    .required(),
  quarter_id: Joi.number()
    .integer()
    .positive()
    .required(),
  override_pct: Joi.number()
    .min(0)
    .max(100)
    .required()
    .messages({
      'any.required': 'Override percentage is required',
      'number.min': 'Percentage cannot be negative',
      'number.max': 'Percentage cannot exceed 100'
    }),
  override_justification: Joi.string()
    .min(20)
    .max(1000)
    .required()
    .messages({
      'any.required': 'Justification is REQUIRED for compliance overrides',
      'string.min': 'Justification must be at least 20 characters (provide detailed reason)'
    })
});

/**
 * ===========================================
 * NOTES VALIDATIONS
 * ===========================================
 */

export const createNoteSchema = Joi.object({
  mrfc_id: Joi.number()
    .integer()
    .positive()
    .optional(),
  quarter_id: Joi.number()
    .integer()
    .positive()
    .optional(),
  title: Joi.string()
    .min(1)
    .max(200)
    .required()
    .messages({
      'any.required': 'Note title is required'
    }),
  content: Joi.string()
    .max(5000)
    .optional()
});

export const updateNoteSchema = Joi.object({
  title: Joi.string()
    .min(1)
    .max(200),
  content: Joi.string()
    .max(5000)
});

/**
 * ===========================================
 * QUERY PARAMETER VALIDATIONS
 * ===========================================
 */

export const paginationSchema = Joi.object({
  page: Joi.number()
    .integer()
    .min(1)
    .default(1),
  limit: Joi.number()
    .integer()
    .min(1)
    .max(100)
    .default(20),
  search: Joi.string()
    .max(100)
    .optional(),
  sort_by: Joi.string()
    .optional(),
  sort_order: Joi.string()
    .valid('ASC', 'DESC')
    .default('DESC')
});

/**
 * ===========================================
 * VALIDATION MIDDLEWARE
 * ===========================================
 */

/**
 * Middleware to validate request body, params, or query
 * @param schema Joi schema to validate against
 * @param property Request property to validate ('body', 'params', 'query')
 */
export const validate = (
  schema: Joi.ObjectSchema,
  property: 'body' | 'params' | 'query' = 'body'
) => {
  return (req: any, res: any, next: any) => {
    const { error, value } = schema.validate(req[property], {
      abortEarly: false, // Return all errors, not just first
      stripUnknown: true // Remove unknown fields
    });

    if (error) {
      const errors = error.details.map((detail) => ({
        field: detail.path.join('.'),
        message: detail.message
      }));

      return res.status(400).json({
        success: false,
        error: {
          code: 'VALIDATION_ERROR',
          message: 'Invalid input data',
          details: errors
        }
      });
    }

    // Replace request property with validated and sanitized value
    req[property] = value;
    next();
  };
};
