/**
 * MRFC MODEL
 * ==========
 * Represents Municipal Resource and Finance Committees
 */

import { Model, DataTypes, Optional } from 'sequelize';
import sequelize from '../config/database';

// Enum for compliance status
export enum ComplianceStatus {
  COMPLIANT = 'COMPLIANT',
  NON_COMPLIANT = 'NON_COMPLIANT',
  PARTIAL = 'PARTIAL',
  NOT_ASSESSED = 'NOT_ASSESSED'
}

// Define model attributes interface
export interface MrfcAttributes {
  id: number;
  name: string;
  municipality: string;
  province: string | null;
  region: string | null;
  contact_person: string | null;
  contact_number: string | null;
  email: string | null;
  address: string | null;
  is_active: boolean;
  created_by: number | null;
  compliance_percentage: number | null;
  compliance_status: ComplianceStatus;
  compliance_updated_at: Date | null;
  compliance_updated_by: number | null;
  assigned_admin_id: number | null;
  mrfc_code: string | null;
  created_at: Date;
  updated_at: Date;
}

// Define attributes for creation (id, timestamps, and optional fields)
export interface MrfcCreationAttributes extends Optional<MrfcAttributes, 'id' | 'province' | 'region' | 'contact_person' | 'contact_number' | 'email' | 'address' | 'is_active' | 'created_by' | 'compliance_percentage' | 'compliance_status' | 'compliance_updated_at' | 'compliance_updated_by' | 'assigned_admin_id' | 'mrfc_code' | 'created_at' | 'updated_at'> {}

// Define the Mrfc model class
export class Mrfc extends Model<MrfcAttributes, MrfcCreationAttributes> implements MrfcAttributes {
  public id!: number;
  public name!: string;
  public municipality!: string;
  public province!: string | null;
  public region!: string | null;
  public contact_person!: string | null;
  public contact_number!: string | null;
  public email!: string | null;
  public address!: string | null;
  public is_active!: boolean;
  public created_by!: number | null;
  public compliance_percentage!: number | null;
  public compliance_status!: ComplianceStatus;
  public compliance_updated_at!: Date | null;
  public compliance_updated_by!: number | null;
  public assigned_admin_id!: number | null;
  public mrfc_code!: string | null;
  public created_at!: Date;
  public updated_at!: Date;

  // Timestamps
  public readonly createdAt!: Date;
  public readonly updatedAt!: Date;
}

// Initialize the model
Mrfc.init(
  {
    id: {
      type: DataTypes.BIGINT,
      primaryKey: true,
      autoIncrement: true,
    },
    name: {
      type: DataTypes.STRING(200),
      allowNull: false,
    },
    municipality: {
      type: DataTypes.STRING(100),
      allowNull: false,
    },
    province: {
      type: DataTypes.STRING(100),
      allowNull: true,
    },
    region: {
      type: DataTypes.STRING(50),
      allowNull: true,
    },
    contact_person: {
      type: DataTypes.STRING(100),
      allowNull: true,
    },
    contact_number: {
      type: DataTypes.STRING(20),
      allowNull: true,
    },
    email: {
      type: DataTypes.STRING(100),
      allowNull: true,
    },
    address: {
      type: DataTypes.TEXT,
      allowNull: true,
    },
    is_active: {
      type: DataTypes.BOOLEAN,
      defaultValue: true,
    },
    created_by: {
      type: DataTypes.BIGINT,
      allowNull: true,
      references: {
        model: 'users',
        key: 'id',
      },
    },
    compliance_percentage: {
      type: DataTypes.DECIMAL(5, 2),
      allowNull: true,
      validate: {
        min: 0,
        max: 100
      }
    },
    compliance_status: {
      type: DataTypes.ENUM(...Object.values(ComplianceStatus)),
      defaultValue: ComplianceStatus.NOT_ASSESSED,
    },
    compliance_updated_at: {
      type: DataTypes.DATE,
      allowNull: true,
    },
    compliance_updated_by: {
      type: DataTypes.BIGINT,
      allowNull: true,
      references: {
        model: 'users',
        key: 'id',
      },
    },
    assigned_admin_id: {
      type: DataTypes.BIGINT,
      allowNull: true,
      references: {
        model: 'users',
        key: 'id',
      },
    },
    mrfc_code: {
      type: DataTypes.STRING(50),
      allowNull: true,
      unique: true,
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
    tableName: 'mrfcs',
    underscored: true,
    timestamps: true,
    createdAt: 'created_at',
    updatedAt: 'updated_at',
  }
);

export default Mrfc;
