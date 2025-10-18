/**
 * COMPLIANCE LOG MODEL
 * ====================
 * Represents historical compliance tracking per proponent per quarter
 * Status: COMPLIANT (>=80%), PARTIALLY_COMPLIANT (50-79%), NON_COMPLIANT (<50%)
 * Supports admin override with justification
 */

import { Model, DataTypes, Optional } from 'sequelize';
import sequelize from '../config/database';

// Enum for compliance status matching PostgreSQL enum
export enum ComplianceStatus {
  COMPLIANT = 'COMPLIANT',
  PARTIALLY_COMPLIANT = 'PARTIALLY_COMPLIANT',
  NON_COMPLIANT = 'NON_COMPLIANT'
}

// Define model attributes interface
export interface ComplianceLogAttributes {
  id: number;
  proponent_id: number;
  quarter_id: number;
  compliance_pct: number;
  documents_submitted: number;
  documents_required: number;
  status: ComplianceStatus;
  override_pct: number | null;
  override_justification: string | null;
  override_by: number | null;
  override_at: Date | null;
  calculated_at: Date;
  calculated_by: number | null;
}

// Define attributes for creation (id and optional fields)
export interface ComplianceLogCreationAttributes extends Optional<ComplianceLogAttributes, 'id' | 'override_pct' | 'override_justification' | 'override_by' | 'override_at' | 'calculated_at' | 'calculated_by'> {}

// Define the ComplianceLog model class
export class ComplianceLog extends Model<ComplianceLogAttributes, ComplianceLogCreationAttributes> implements ComplianceLogAttributes {
  public id!: number;
  public proponent_id!: number;
  public quarter_id!: number;
  public compliance_pct!: number;
  public documents_submitted!: number;
  public documents_required!: number;
  public status!: ComplianceStatus;
  public override_pct!: number | null;
  public override_justification!: string | null;
  public override_by!: number | null;
  public override_at!: Date | null;
  public calculated_at!: Date;
  public calculated_by!: number | null;

  // Timestamps
  public readonly createdAt!: Date;
  public readonly updatedAt!: Date;
}

// Initialize the model
ComplianceLog.init(
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
    compliance_pct: {
      type: DataTypes.DECIMAL(5, 2),
      allowNull: false,
    },
    documents_submitted: {
      type: DataTypes.INTEGER,
      allowNull: false,
    },
    documents_required: {
      type: DataTypes.INTEGER,
      allowNull: false,
    },
    status: {
      type: DataTypes.ENUM(...Object.values(ComplianceStatus)),
      allowNull: false,
    },
    override_pct: {
      type: DataTypes.DECIMAL(5, 2),
      allowNull: true,
    },
    override_justification: {
      type: DataTypes.TEXT,
      allowNull: true,
    },
    override_by: {
      type: DataTypes.BIGINT,
      allowNull: true,
      references: {
        model: 'users',
        key: 'id',
      },
    },
    override_at: {
      type: DataTypes.DATE,
      allowNull: true,
    },
    calculated_at: {
      type: DataTypes.DATE,
      defaultValue: DataTypes.NOW,
    },
    calculated_by: {
      type: DataTypes.BIGINT,
      allowNull: true,
      references: {
        model: 'users',
        key: 'id',
      },
    },
  },
  {
    sequelize,
    tableName: 'compliance_logs',
    underscored: true,
    timestamps: false, // compliance_logs uses calculated_at instead of timestamps
  }
);

export default ComplianceLog;
