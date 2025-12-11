/**
 * USER MRFC ACCESS MODEL
 * ======================
 * Represents many-to-many relationship: users can access multiple MRFCs
 * One access record per user per MRFC
 * Tracks which admin granted access and when
 */

import { Model, DataTypes, Optional } from 'sequelize';
import sequelize from '../config/database';

// Define model attributes interface
export interface UserMrfcAccessAttributes {
  id: number;
  user_id: number;
  mrfc_id: number;
  granted_by: number | null;
  granted_at: Date;
  is_active: boolean;
}

// Define attributes for creation (id and optional fields)
export interface UserMrfcAccessCreationAttributes extends Optional<UserMrfcAccessAttributes, 'id' | 'granted_by' | 'granted_at' | 'is_active'> {}

// Define the UserMrfcAccess model class
export class UserMrfcAccess extends Model<UserMrfcAccessAttributes, UserMrfcAccessCreationAttributes> implements UserMrfcAccessAttributes {
  public id!: number;
  public user_id!: number;
  public mrfc_id!: number;
  public granted_by!: number | null;
  public granted_at!: Date;
  public is_active!: boolean;

  // Timestamps
  public readonly createdAt!: Date;
  public readonly updatedAt!: Date;
}

// Initialize the model
UserMrfcAccess.init(
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
      allowNull: false,
      references: {
        model: 'mrfcs',
        key: 'id',
      },
      onDelete: 'CASCADE',
    },
    granted_by: {
      type: DataTypes.BIGINT,
      allowNull: true,
      references: {
        model: 'users',
        key: 'id',
      },
      onDelete: 'CASCADE',
    },
    granted_at: {
      type: DataTypes.DATE,
      defaultValue: DataTypes.NOW,
    },
    is_active: {
      type: DataTypes.BOOLEAN,
      defaultValue: true,
    },
  },
  {
    sequelize,
    tableName: 'user_mrfc_access',
    underscored: true,
    timestamps: false, // user_mrfc_access uses granted_at instead of timestamps
    indexes: [
      {
        unique: true,
        fields: ['user_id', 'mrfc_id'],
      },
    ],
  }
);

export default UserMrfcAccess;
