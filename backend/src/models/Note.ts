/**
 * NOTE MODEL
 * ==========
 * Represents personal notes created by users
 * Notes are private to each user and can optionally be linked to specific MRFC and/or quarter
 */

import { Model, DataTypes, Optional } from 'sequelize';
import sequelize from '../config/database';

// Define model attributes interface
export interface NoteAttributes {
  id: number;
  user_id: number;
  mrfc_id: number | null;
  quarter_id: number | null;
  agenda_id: number | null;
  title: string;
  content: string | null;
  is_pinned: boolean;
  created_at: Date;
  updated_at: Date;
}

// Define attributes for creation (id, timestamps, and optional fields)
export interface NoteCreationAttributes extends Optional<NoteAttributes, 'id' | 'mrfc_id' | 'quarter_id' | 'agenda_id' | 'content' | 'is_pinned' | 'created_at' | 'updated_at'> {}

// Define the Note model class
export class Note extends Model<NoteAttributes, NoteCreationAttributes> implements NoteAttributes {
  public id!: number;
  public user_id!: number;
  public mrfc_id!: number | null;
  public quarter_id!: number | null;
  public agenda_id!: number | null;
  public title!: string;
  public content!: string | null;
  public is_pinned!: boolean;
  public created_at!: Date;
  public updated_at!: Date;

  // Timestamps
  public readonly createdAt!: Date;
  public readonly updatedAt!: Date;
}

// Initialize the model
Note.init(
  {
    id: {
      type: DataTypes.BIGINT,
      primaryKey: true,
      autoIncrement: true,
    },
    user_id: {
      type: DataTypes.BIGINT,
      allowNull: false,
      references: {
        model: 'users',
        key: 'id',
      },
      onDelete: 'CASCADE',
    },
    mrfc_id: {
      type: DataTypes.BIGINT,
      allowNull: true,
      references: {
        model: 'mrfcs',
        key: 'id',
      },
      onDelete: 'CASCADE',
    },
    quarter_id: {
      type: DataTypes.BIGINT,
      allowNull: true,
      references: {
        model: 'quarters',
        key: 'id',
      },
    },
    agenda_id: {
      type: DataTypes.BIGINT,
      allowNull: true,
      references: {
        model: 'agendas',
        key: 'id',
      },
      onDelete: 'CASCADE',
    },
    title: {
      type: DataTypes.STRING(200),
      allowNull: false,
    },
    content: {
      type: DataTypes.TEXT,
      allowNull: true,
    },
    is_pinned: {
      type: DataTypes.BOOLEAN,
      allowNull: false,
      defaultValue: false,
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
    tableName: 'notes',
    underscored: true,
    timestamps: true,
    createdAt: 'created_at',
    updatedAt: 'updated_at',
  }
);

export default Note;
