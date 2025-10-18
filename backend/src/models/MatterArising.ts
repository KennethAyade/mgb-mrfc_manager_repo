/**
 * MATTER ARISING MODEL
 * ====================
 * Represents unresolved issues from previous meetings requiring follow-up
 * Status: PENDING (not started), IN_PROGRESS (being worked on), RESOLVED (completed)
 */

import { Model, DataTypes, Optional } from 'sequelize';
import sequelize from '../config/database';

// Enum for matter status matching PostgreSQL enum
export enum MatterStatus {
  PENDING = 'PENDING',
  IN_PROGRESS = 'IN_PROGRESS',
  RESOLVED = 'RESOLVED'
}

// Define model attributes interface
export interface MatterArisingAttributes {
  id: number;
  agenda_id: number;
  issue: string;
  status: MatterStatus;
  assigned_to: string | null;
  date_raised: Date;
  date_resolved: Date | null;
  remarks: string | null;
  created_at: Date;
  updated_at: Date;
}

// Define attributes for creation (id, timestamps are optional)
export interface MatterArisingCreationAttributes extends Optional<MatterArisingAttributes, 'id' | 'status' | 'assigned_to' | 'date_resolved' | 'remarks' | 'created_at' | 'updated_at'> {}

// Define the MatterArising model class
export class MatterArising extends Model<MatterArisingAttributes, MatterArisingCreationAttributes> implements MatterArisingAttributes {
  public id!: number;
  public agenda_id!: number;
  public issue!: string;
  public status!: MatterStatus;
  public assigned_to!: string | null;
  public date_raised!: Date;
  public date_resolved!: Date | null;
  public remarks!: string | null;
  public created_at!: Date;
  public updated_at!: Date;

  // Timestamps
  public readonly createdAt!: Date;
  public readonly updatedAt!: Date;
}

// Initialize the model
MatterArising.init(
  {
    id: {
      type: DataTypes.BIGINT,
      primaryKey: true,
      autoIncrement: true,
    },
    agenda_id: {
      type: DataTypes.BIGINT,
      allowNull: false,
      references: {
        model: 'agendas',
        key: 'id',
      },
      onDelete: 'CASCADE',
    },
    issue: {
      type: DataTypes.TEXT,
      allowNull: false,
    },
    status: {
      type: DataTypes.ENUM(...Object.values(MatterStatus)),
      defaultValue: MatterStatus.PENDING,
    },
    assigned_to: {
      type: DataTypes.STRING(100),
      allowNull: true,
    },
    date_raised: {
      type: DataTypes.DATEONLY,
      allowNull: false,
    },
    date_resolved: {
      type: DataTypes.DATEONLY,
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
    tableName: 'matters_arising',
    underscored: true,
    timestamps: true,
    createdAt: 'created_at',
    updatedAt: 'updated_at',
  }
);

export default MatterArising;
