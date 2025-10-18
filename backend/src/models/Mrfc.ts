/**
 * MRFC MODEL
 * ==========
 * Represents Municipal Resource and Finance Committees
 */

import { Model, DataTypes, Optional } from 'sequelize';
import sequelize from '../config/database';

// Define model attributes interface
export interface MrfcAttributes {
  id: number;
  name: string;
  municipality: string;
  province: string | null;
  region: string | null;
  contact_person: string | null;
  contact_number: string | null;
  email: string | null;
  address: string | null;
  is_active: boolean;
  created_by: number | null;
  created_at: Date;
  updated_at: Date;
}

// Define attributes for creation (id, timestamps are optional)
export interface MrfcCreationAttributes extends Optional<MrfcAttributes, 'id' | 'province' | 'region' | 'contact_person' | 'contact_number' | 'email' | 'address' | 'is_active' | 'created_by' | 'created_at' | 'updated_at'> {}

// Define the Mrfc model class
export class Mrfc extends Model<MrfcAttributes, MrfcCreationAttributes> implements MrfcAttributes {
  public id!: number;
  public name!: string;
  public municipality!: string;
  public province!: string | null;
  public region!: string | null;
  public contact_person!: string | null;
  public contact_number!: string | null;
  public email!: string | null;
  public address!: string | null;
  public is_active!: boolean;
  public created_by!: number | null;
  public created_at!: Date;
  public updated_at!: Date;

  // Timestamps
  public readonly createdAt!: Date;
  public readonly updatedAt!: Date;
}

// Initialize the model
Mrfc.init(
  {
    id: {
      type: DataTypes.BIGINT,
      primaryKey: true,
      autoIncrement: true,
    },
    name: {
      type: DataTypes.STRING(200),
      allowNull: false,
    },
    municipality: {
      type: DataTypes.STRING(100),
      allowNull: false,
    },
    province: {
      type: DataTypes.STRING(100),
      allowNull: true,
    },
    region: {
      type: DataTypes.STRING(50),
      allowNull: true,
    },
    contact_person: {
      type: DataTypes.STRING(100),
      allowNull: true,
    },
    contact_number: {
      type: DataTypes.STRING(20),
      allowNull: true,
    },
    email: {
      type: DataTypes.STRING(100),
      allowNull: true,
    },
    address: {
      type: DataTypes.TEXT,
      allowNull: true,
    },
    is_active: {
      type: DataTypes.BOOLEAN,
      defaultValue: true,
    },
    created_by: {
      type: DataTypes.BIGINT,
      allowNull: true,
      references: {
        model: 'users',
        key: 'id',
      },
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
    tableName: 'mrfcs',
    underscored: true,
    timestamps: true,
    createdAt: 'created_at',
    updatedAt: 'updated_at',
  }
);

export default Mrfc;
