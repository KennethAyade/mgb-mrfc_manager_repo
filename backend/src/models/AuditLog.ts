/**
 * AUDIT LOG MODEL
 * ===============
 * Represents complete audit trail for all CREATE/UPDATE/DELETE actions
 * Cannot be deleted by users - immutable audit trail
 * Stores JSON snapshots of records before and after changes
 */

import { Model, DataTypes, Optional } from 'sequelize';
import sequelize from '../config/database';

// Enum for audit action matching PostgreSQL enum
export enum AuditAction {
  CREATE = 'CREATE',
  UPDATE = 'UPDATE',
  DELETE = 'DELETE'
}

// Define model attributes interface
export interface AuditLogAttributes {
  id: number;
  user_id: number | null;
  entity_type: string;
  entity_id: number;
  action: AuditAction;
  old_values: object | null;
  new_values: object | null;
  ip_address: string | null;
  user_agent: string | null;
  created_at: Date;
}

// Define attributes for creation (id and optional fields)
export interface AuditLogCreationAttributes extends Optional<AuditLogAttributes, 'id' | 'user_id' | 'old_values' | 'new_values' | 'ip_address' | 'user_agent' | 'created_at'> {}

// Define the AuditLog model class
export class AuditLog extends Model<AuditLogAttributes, AuditLogCreationAttributes> implements AuditLogAttributes {
  public id!: number;
  public user_id!: number | null;
  public entity_type!: string;
  public entity_id!: number;
  public action!: AuditAction;
  public old_values!: object | null;
  public new_values!: object | null;
  public ip_address!: string | null;
  public user_agent!: string | null;
  public created_at!: Date;

  // Timestamps
  public readonly createdAt!: Date;
  public readonly updatedAt!: Date;
}

// Initialize the model
AuditLog.init(
  {
    id: {
      type: DataTypes.BIGINT,
      primaryKey: true,
      autoIncrement: true,
    },
    user_id: {
      type: DataTypes.BIGINT,
      allowNull: true,
      references: {
        model: 'users',
        key: 'id',
      },
    },
    entity_type: {
      type: DataTypes.STRING(50),
      allowNull: false,
      comment: 'Table name (e.g., "proponents", "documents")',
    },
    entity_id: {
      type: DataTypes.BIGINT,
      allowNull: false,
    },
    action: {
      type: DataTypes.ENUM(...Object.values(AuditAction)),
      allowNull: false,
    },
    old_values: {
      type: DataTypes.JSONB,
      allowNull: true,
      comment: 'JSON snapshot of record before change',
    },
    new_values: {
      type: DataTypes.JSONB,
      allowNull: true,
      comment: 'JSON snapshot of record after change',
    },
    ip_address: {
      type: DataTypes.STRING(45),
      allowNull: true,
    },
    user_agent: {
      type: DataTypes.TEXT,
      allowNull: true,
    },
    created_at: {
      type: DataTypes.DATE,
      defaultValue: DataTypes.NOW,
    },
  },
  {
    sequelize,
    tableName: 'audit_logs',
    underscored: true,
    timestamps: false, // audit_logs only has created_at, immutable
  }
);

export default AuditLog;
