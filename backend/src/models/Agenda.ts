/**
 * AGENDA MODEL
 * ============
 * Represents MRFC meeting agendas (one per MRFC per quarter)
 * Status: DRAFT (being prepared), PUBLISHED (visible to users),
 *         COMPLETED (meeting done), CANCELLED (meeting cancelled)
 */

import { Model, DataTypes, Optional, Op } from 'sequelize';
import sequelize from '../config/database';

// Enum for agenda status matching PostgreSQL enum
export enum AgendaStatus {
  DRAFT = 'DRAFT',
  PROPOSED = 'PROPOSED',  // New: User-proposed agenda awaiting admin approval
  PUBLISHED = 'PUBLISHED',
  COMPLETED = 'COMPLETED',
  CANCELLED = 'CANCELLED'
}

// Define model attributes interface
export interface AgendaAttributes {
  id: number;
  mrfc_id: number | null; // null for general meetings not tied to specific MRFC
  quarter_id: number;
  meeting_title: string | null;
  meeting_date: Date;
  meeting_time: string | null;
  meeting_end_time: string | null;
  location: string | null;
  status: AgendaStatus;
  proposed_by: number | null; // User who proposed (for PROPOSED status)
  proposed_at: Date | null;
  approved_by: number | null; // Admin who approved
  approved_at: Date | null;
  denied_by: number | null; // Admin who denied
  denied_at: Date | null;
  denial_remarks: string | null; // Admin's reason for denial
  created_at: Date;
  updated_at: Date;
}

// Define attributes for creation (id, timestamps, and mrfc_id are optional)
export interface AgendaCreationAttributes extends Optional<AgendaAttributes, 'id' | 'mrfc_id' | 'meeting_title' | 'meeting_time' | 'meeting_end_time' | 'location' | 'status' | 'created_at' | 'updated_at'> {}

// Define the Agenda model class
export class Agenda extends Model<AgendaAttributes, AgendaCreationAttributes> implements AgendaAttributes {
  public id!: number;
  public mrfc_id!: number | null; // null for general meetings
  public quarter_id!: number;
  public meeting_title!: string | null;
  public meeting_date!: Date;
  public meeting_time!: string | null;
  public meeting_end_time!: string | null;
  public location!: string | null;
  public status!: AgendaStatus;
  public proposed_by!: number | null;
  public proposed_at!: Date | null;
  public approved_by!: number | null;
  public approved_at!: Date | null;
  public denied_by!: number | null;
  public denied_at!: Date | null;
  public denial_remarks!: string | null;
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
      allowNull: true, // Allow null for general meetings
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
    meeting_title: {
      type: DataTypes.STRING(255),
      allowNull: true,
    },
    meeting_date: {
      type: DataTypes.DATEONLY,
      allowNull: false,
    },
    meeting_time: {
      type: DataTypes.STRING(10),
      allowNull: true,
    },
    meeting_end_time: {
      type: DataTypes.STRING(10),
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
    proposed_by: {
      type: DataTypes.BIGINT,
      allowNull: true,
      references: {
        model: 'users',
        key: 'id',
      },
    },
    proposed_at: {
      type: DataTypes.DATE,
      allowNull: true,
    },
    approved_by: {
      type: DataTypes.BIGINT,
      allowNull: true,
      references: {
        model: 'users',
        key: 'id',
      },
    },
    approved_at: {
      type: DataTypes.DATE,
      allowNull: true,
    },
    denied_by: {
      type: DataTypes.BIGINT,
      allowNull: true,
      references: {
        model: 'users',
        key: 'id',
      },
    },
    denied_at: {
      type: DataTypes.DATE,
      allowNull: true,
    },
    denial_remarks: {
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
    tableName: 'agendas',
    underscored: true,
    timestamps: true,
    createdAt: 'created_at',
    updatedAt: 'updated_at',
    indexes: [
      {
        // Unique constraint: one meeting per MRFC per quarter
        // Note: This allows multiple NULL mrfc_id entries (general meetings)
        unique: true,
        fields: ['mrfc_id', 'quarter_id'],
        where: {
          mrfc_id: { [Op.ne]: null } // Only enforce uniqueness when mrfc_id is not null
        },
      },
    ],
  }
);

export default Agenda;
