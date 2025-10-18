/**
 * USER MODEL
 * ==========
 * Represents user accounts with role-based access control
 * Roles: SUPER_ADMIN (can create admins), ADMIN (full access), USER (read-only)
 */

import { Model, DataTypes, Optional } from 'sequelize';
import sequelize from '../config/database';

// Enum for user roles matching PostgreSQL enum
export enum UserRole {
  SUPER_ADMIN = 'SUPER_ADMIN',
  ADMIN = 'ADMIN',
  USER = 'USER'
}

// Define model attributes interface
export interface UserAttributes {
  id: number;
  username: string;
  password_hash: string;
  full_name: string;
  email: string;
  role: UserRole;
  is_active: boolean;
  email_verified: boolean;
  last_login: Date | null;
  created_at: Date;
  updated_at: Date;
}

// Define attributes for creation (id, timestamps are optional)
export interface UserCreationAttributes extends Optional<UserAttributes, 'id' | 'role' | 'is_active' | 'email_verified' | 'last_login' | 'created_at' | 'updated_at'> {}

// Define the User model class
export class User extends Model<UserAttributes, UserCreationAttributes> implements UserAttributes {
  public id!: number;
  public username!: string;
  public password_hash!: string;
  public full_name!: string;
  public email!: string;
  public role!: UserRole;
  public is_active!: boolean;
  public email_verified!: boolean;
  public last_login!: Date | null;
  public created_at!: Date;
  public updated_at!: Date;

  // Timestamps
  public readonly createdAt!: Date;
  public readonly updatedAt!: Date;
}

// Initialize the model
User.init(
  {
    id: {
      type: DataTypes.BIGINT,
      primaryKey: true,
      autoIncrement: true,
    },
    username: {
      type: DataTypes.STRING(50),
      allowNull: false,
      unique: true,
    },
    password_hash: {
      type: DataTypes.STRING(255),
      allowNull: false,
    },
    full_name: {
      type: DataTypes.STRING(100),
      allowNull: false,
    },
    email: {
      type: DataTypes.STRING(100),
      allowNull: false,
      unique: true,
    },
    role: {
      type: DataTypes.ENUM(...Object.values(UserRole)),
      allowNull: false,
      defaultValue: UserRole.USER,
    },
    is_active: {
      type: DataTypes.BOOLEAN,
      defaultValue: true,
    },
    email_verified: {
      type: DataTypes.BOOLEAN,
      defaultValue: false,
    },
    last_login: {
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
    tableName: 'users',
    underscored: true,
    timestamps: true,
    createdAt: 'created_at',
    updatedAt: 'updated_at',
  }
);

export default User;
