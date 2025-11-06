/**
 * DOCUMENT MODEL
 * ==============
 * Represents compliance documents uploaded by proponents
 * Categories: MTF_REPORT, AEPEP, NTE_DISBURSEMENT, OMVR, CMVR, RESEARCH_ACCOMPLISHMENTS, SDMP, PRODUCTION, SAFETY, OTHER
 * Status: PENDING (awaiting review), ACCEPTED (approved), REJECTED (not accepted)
 */

import { Model, DataTypes, Optional } from 'sequelize';
import sequelize from '../config/database';

// Enum for document category matching PostgreSQL enum
export enum DocumentCategory {
  MTF_REPORT = 'MTF_REPORT',
  AEPEP = 'AEPEP',
  NTE_DISBURSEMENT = 'NTE_DISBURSEMENT',
  OMVR = 'OMVR',
  CMVR = 'CMVR',
  RESEARCH_ACCOMPLISHMENTS = 'RESEARCH_ACCOMPLISHMENTS',
  SDMP = 'SDMP',
  PRODUCTION = 'PRODUCTION',
  SAFETY = 'SAFETY',
  OTHER = 'OTHER'
}

// Enum for document status matching PostgreSQL enum
export enum DocumentStatus {
  PENDING = 'PENDING',
  ACCEPTED = 'ACCEPTED',
  REJECTED = 'REJECTED'
}

// Define model attributes interface
export interface DocumentAttributes {
  id: number;
  proponent_id: number;
  quarter_id: number;
  uploaded_by: number | null;
  file_name: string;
  original_name: string;
  file_type: string | null;
  file_size: number | null;
  category: DocumentCategory;
  file_url: string;
  file_cloudinary_id: string | null;
  upload_date: Date;
  status: DocumentStatus;
  reviewed_by: number | null;
  reviewed_at: Date | null;
  remarks: string | null;
  created_at: Date;
  updated_at: Date;
}

// Define attributes for creation (id, timestamps, and optional fields)
export interface DocumentCreationAttributes extends Optional<DocumentAttributes, 'id' | 'uploaded_by' | 'file_type' | 'file_size' | 'file_cloudinary_id' | 'upload_date' | 'status' | 'reviewed_by' | 'reviewed_at' | 'remarks' | 'created_at' | 'updated_at'> {}

// Define the Document model class
export class Document extends Model<DocumentAttributes, DocumentCreationAttributes> implements DocumentAttributes {
  public id!: number;
  public proponent_id!: number;
  public quarter_id!: number;
  public uploaded_by!: number | null;
  public file_name!: string;
  public original_name!: string;
  public file_type!: string | null;
  public file_size!: number | null;
  public category!: DocumentCategory;
  public file_url!: string;
  public file_cloudinary_id!: string | null;
  public upload_date!: Date;
  public status!: DocumentStatus;
  public reviewed_by!: number | null;
  public reviewed_at!: Date | null;
  public remarks!: string | null;
  public created_at!: Date;
  public updated_at!: Date;

  // Timestamps
  public readonly createdAt!: Date;
  public readonly updatedAt!: Date;
}

// Initialize the model
Document.init(
  {
    id: {
      type: DataTypes.BIGINT,
      primaryKey: true,
      autoIncrement: true,
    },
    proponent_id: {
      type: DataTypes.BIGINT,
      allowNull: false,
      references: {
        model: 'proponents',
        key: 'id',
      },
      onDelete: 'CASCADE',
    },
    quarter_id: {
      type: DataTypes.BIGINT,
      allowNull: false,
      references: {
        model: 'quarters',
        key: 'id',
      },
    },
    uploaded_by: {
      type: DataTypes.BIGINT,
      allowNull: true,
      references: {
        model: 'users',
        key: 'id',
      },
    },
    file_name: {
      type: DataTypes.STRING(255),
      allowNull: false,
    },
    original_name: {
      type: DataTypes.STRING(255),
      allowNull: false,
    },
    file_type: {
      type: DataTypes.STRING(50),
      allowNull: true,
    },
    file_size: {
      type: DataTypes.BIGINT,
      allowNull: true,
    },
    category: {
      type: DataTypes.ENUM(...Object.values(DocumentCategory)),
      allowNull: false,
    },
    file_url: {
      type: DataTypes.TEXT,
      allowNull: false,
    },
    file_cloudinary_id: {
      type: DataTypes.STRING(255),
      allowNull: true,
    },
    upload_date: {
      type: DataTypes.DATE,
      defaultValue: DataTypes.NOW,
    },
    status: {
      type: DataTypes.ENUM(...Object.values(DocumentStatus)),
      defaultValue: DocumentStatus.PENDING,
    },
    reviewed_by: {
      type: DataTypes.BIGINT,
      allowNull: true,
      references: {
        model: 'users',
        key: 'id',
      },
    },
    reviewed_at: {
      type: DataTypes.DATE,
      allowNull: true,
    },
    remarks: {
      type: DataTypes.TEXT,
      allowNull: true,
    },
    created_at: {
      type: DataTypes.DATE,
      defaultValue: DataTypes.NOW,
    },
    updated_at: {
      type: DataTypes.DATE,
      defaultValue: DataTypes.NOW,
    },
  },
  {
    sequelize,
    tableName: 'documents',
    underscored: true,
    timestamps: true,
    createdAt: 'created_at',
    updatedAt: 'updated_at',
  }
);

export default Document;
