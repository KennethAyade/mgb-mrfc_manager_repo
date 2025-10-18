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
  title: string;
  content: string | null;
  created_at: Date;
  updated_at: Date;
}

// Define attributes for creation (id, timestamps, and optional fields)
export interface NoteCreationAttributes extends Optional<NoteAttributes, 'id' | 'mrfc_id' | 'quarter_id' | 'content' | 'created_at' | 'updated_at'> {}

// Define the Note model class
export class Note extends Model<NoteAttributes, NoteCreationAttributes> implements NoteAttributes {
  public id!: number;
  public user_id!: number;
  public mrfc_id!: number | null;
  public quarter_id!: number | null;
  public title!: string;
  public content!: string | null;
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
    title: {
      type: DataTypes.STRING(200),
      allowNull: false,
    },
    content: {
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
    tableName: 'notes',
    underscored: true,
    timestamps: true,
    createdAt: 'created_at',
    updatedAt: 'updated_at',
  }
);

export default Note;
