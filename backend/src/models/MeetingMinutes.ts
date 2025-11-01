/**
 * MEETING MINUTES MODEL
 * =====================
 * Represents meeting summaries, decisions, and action items
 * Only meeting organizer can add/edit
 * All users can view
 */

import { Model, DataTypes, Optional } from 'sequelize';
import sequelize from '../config/database';

// Define model attributes interface
export interface MeetingMinutesAttributes {
  id: number;
  agenda_id: number;
  summary: string;
  decisions: Array<{ decision: string; status?: string }>;
  action_items: Array<{ item: string; assigned_to?: string; deadline?: string }>;
  attendees_summary: string | null;
  next_meeting_notes: string | null;
  is_final: boolean;
  created_by: number;
  created_by_name: string;
  approved_by: number | null;
  approved_at: Date | null;
  created_at: Date;
  updated_at: Date;
}

// Define attributes for creation (id, timestamps are optional)
export interface MeetingMinutesCreationAttributes extends Optional<MeetingMinutesAttributes, 'id' | 'decisions' | 'action_items' | 'attendees_summary' | 'next_meeting_notes' | 'is_final' | 'approved_by' | 'approved_at' | 'created_at' | 'updated_at'> {}

// Define the MeetingMinutes model class
export class MeetingMinutes extends Model<MeetingMinutesAttributes, MeetingMinutesCreationAttributes> implements MeetingMinutesAttributes {
  public id!: number;
  public agenda_id!: number;
  public summary!: string;
  public decisions!: Array<{ decision: string; status?: string }>;
  public action_items!: Array<{ item: string; assigned_to?: string; deadline?: string }>;
  public attendees_summary!: string | null;
  public next_meeting_notes!: string | null;
  public is_final!: boolean;
  public created_by!: number;
  public created_by_name!: string;
  public approved_by!: number | null;
  public approved_at!: Date | null;
  public created_at!: Date;
  public updated_at!: Date;

  // Timestamps
  public readonly createdAt!: Date;
  public readonly updatedAt!: Date;
}

// Initialize the model
MeetingMinutes.init(
  {
    id: {
      type: DataTypes.BIGINT,
      primaryKey: true,
      autoIncrement: true,
    },
    agenda_id: {
      type: DataTypes.BIGINT,
      allowNull: false,
      unique: true,
      references: {
        model: 'agendas',
        key: 'id',
      },
      onDelete: 'CASCADE',
    },
    summary: {
      type: DataTypes.TEXT,
      allowNull: false,
    },
    decisions: {
      type: DataTypes.JSONB,
      defaultValue: [],
    },
    action_items: {
      type: DataTypes.JSONB,
      defaultValue: [],
    },
    attendees_summary: {
      type: DataTypes.TEXT,
      allowNull: true,
    },
    next_meeting_notes: {
      type: DataTypes.TEXT,
      allowNull: true,
    },
    is_final: {
      type: DataTypes.BOOLEAN,
      defaultValue: false,
    },
    created_by: {
      type: DataTypes.BIGINT,
      allowNull: false,
      references: {
        model: 'users',
        key: 'id',
      },
    },
    created_by_name: {
      type: DataTypes.STRING(100),
      allowNull: false,
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
    tableName: 'meeting_minutes',
    underscored: true,
    timestamps: true,
    createdAt: 'created_at',
    updatedAt: 'updated_at',
    indexes: [
      {
        unique: true,
        fields: ['agenda_id'],
      },
      {
        fields: ['created_by'],
      },
      {
        fields: ['is_final'],
      },
    ],
  }
);

export default MeetingMinutes;
