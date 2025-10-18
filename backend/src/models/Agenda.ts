/**
 * AGENDA MODEL
 * ============
 * Represents MRFC meeting agendas (one per MRFC per quarter)
 * Status: DRAFT (being prepared), PUBLISHED (visible to users),
 *         COMPLETED (meeting done), CANCELLED (meeting cancelled)
 */

import { Model, DataTypes, Optional } from 'sequelize';
import sequelize from '../config/database';

// Enum for agenda status matching PostgreSQL enum
export enum AgendaStatus {
  DRAFT = 'DRAFT',
  PUBLISHED = 'PUBLISHED',
  COMPLETED = 'COMPLETED',
  CANCELLED = 'CANCELLED'
}

// Define model attributes interface
export interface AgendaAttributes {
  id: number;
  mrfc_id: number;
  quarter_id: number;
  meeting_date: Date;
  meeting_time: string | null;
  location: string | null;
  status: AgendaStatus;
  created_at: Date;
  updated_at: Date;
}

// Define attributes for creation (id, timestamps are optional)
export interface AgendaCreationAttributes extends Optional<AgendaAttributes, 'id' | 'meeting_time' | 'location' | 'status' | 'created_at' | 'updated_at'> {}

// Define the Agenda model class
export class Agenda extends Model<AgendaAttributes, AgendaCreationAttributes> implements AgendaAttributes {
  public id!: number;
  public mrfc_id!: number;
  public quarter_id!: number;
  public meeting_date!: Date;
  public meeting_time!: string | null;
  public location!: string | null;
  public status!: AgendaStatus;
  public created_at!: Date;
  public updated_at!: Date;

  // Timestamps
  public readonly createdAt!: Date;
  public readonly updatedAt!: Date;
}

// Initialize the model
Agenda.init(
  {
    id: {
      type: DataTypes.BIGINT,
      primaryKey: true,
      autoIncrement: true,
    },
    mrfc_id: {
      type: DataTypes.BIGINT,
      allowNull: false,
      references: {
        model: 'mrfcs',
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
    meeting_date: {
      type: DataTypes.DATEONLY,
      allowNull: false,
    },
    meeting_time: {
      type: DataTypes.TIME,
      allowNull: true,
    },
    location: {
      type: DataTypes.TEXT,
      allowNull: true,
    },
    status: {
      type: DataTypes.ENUM(...Object.values(AgendaStatus)),
      defaultValue: AgendaStatus.DRAFT,
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
    tableName: 'agendas',
    underscored: true,
    timestamps: true,
    createdAt: 'created_at',
    updatedAt: 'updated_at',
    indexes: [
      {
        unique: true,
        fields: ['mrfc_id', 'quarter_id'],
      },
    ],
  }
);

export default Agenda;
