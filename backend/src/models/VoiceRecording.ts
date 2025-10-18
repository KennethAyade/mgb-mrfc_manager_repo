/**
 * VOICE RECORDING MODEL
 * =====================
 * Represents meeting audio recordings
 * Duration is stored in seconds
 */

import { Model, DataTypes, Optional } from 'sequelize';
import sequelize from '../config/database';

// Define model attributes interface
export interface VoiceRecordingAttributes {
  id: number;
  agenda_id: number;
  recording_name: string;
  file_name: string;
  file_url: string;
  file_cloudinary_id: string | null;
  duration: number | null;
  file_size: number | null;
  recorded_by: number | null;
  recorded_at: Date;
  created_at: Date;
}

// Define attributes for creation (id and optional fields)
export interface VoiceRecordingCreationAttributes extends Optional<VoiceRecordingAttributes, 'id' | 'file_cloudinary_id' | 'duration' | 'file_size' | 'recorded_by' | 'recorded_at' | 'created_at'> {}

// Define the VoiceRecording model class
export class VoiceRecording extends Model<VoiceRecordingAttributes, VoiceRecordingCreationAttributes> implements VoiceRecordingAttributes {
  public id!: number;
  public agenda_id!: number;
  public recording_name!: string;
  public file_name!: string;
  public file_url!: string;
  public file_cloudinary_id!: string | null;
  public duration!: number | null;
  public file_size!: number | null;
  public recorded_by!: number | null;
  public recorded_at!: Date;
  public created_at!: Date;

  // Timestamps
  public readonly createdAt!: Date;
  public readonly updatedAt!: Date;
}

// Initialize the model
VoiceRecording.init(
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
    recording_name: {
      type: DataTypes.STRING(255),
      allowNull: false,
    },
    file_name: {
      type: DataTypes.STRING(255),
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
    duration: {
      type: DataTypes.INTEGER,
      allowNull: true,
      comment: 'Duration in seconds',
    },
    file_size: {
      type: DataTypes.BIGINT,
      allowNull: true,
    },
    recorded_by: {
      type: DataTypes.BIGINT,
      allowNull: true,
      references: {
        model: 'users',
        key: 'id',
      },
    },
    recorded_at: {
      type: DataTypes.DATE,
      defaultValue: DataTypes.NOW,
    },
    created_at: {
      type: DataTypes.DATE,
      defaultValue: DataTypes.NOW,
    },
  },
  {
    sequelize,
    tableName: 'voice_recordings',
    underscored: true,
    timestamps: false, // voice_recordings only has created_at, not updated_at
  }
);

export default VoiceRecording;
