/**
 * PROPONENT MODEL
 * ===============
 * Represents mining and quarrying companies under MRFC oversight
 * Status: ACTIVE (operating), INACTIVE (not operating), SUSPENDED (permit suspended)
 */

import { Model, DataTypes, Optional } from 'sequelize';
import sequelize from '../config/database';

// Enum for proponent status matching PostgreSQL enum
export enum ProponentStatus {
  ACTIVE = 'ACTIVE',
  INACTIVE = 'INACTIVE',
  SUSPENDED = 'SUSPENDED'
}

// Define model attributes interface
export interface ProponentAttributes {
  id: number;
  mrfc_id: number;
  name: string;
  company_name: string;
  permit_number: string | null;
  permit_type: string | null;
  status: ProponentStatus;
  contact_person: string | null;
  contact_number: string | null;
  email: string | null;
  address: string | null;
  created_at: Date;
  updated_at: Date;
}

// Define attributes for creation (id, timestamps are optional)
export interface ProponentCreationAttributes extends Optional<ProponentAttributes, 'id' | 'permit_number' | 'permit_type' | 'status' | 'contact_person' | 'contact_number' | 'email' | 'address' | 'created_at' | 'updated_at'> {}

// Define the Proponent model class
export class Proponent extends Model<ProponentAttributes, ProponentCreationAttributes> implements ProponentAttributes {
  public id!: number;
  public mrfc_id!: number;
  public name!: string;
  public company_name!: string;
  public permit_number!: string | null;
  public permit_type!: string | null;
  public status!: ProponentStatus;
  public contact_person!: string | null;
  public contact_number!: string | null;
  public email!: string | null;
  public address!: string | null;
  public created_at!: Date;
  public updated_at!: Date;

  // Timestamps
  public readonly createdAt!: Date;
  public readonly updatedAt!: Date;
}

// Initialize the model
Proponent.init(
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
    name: {
      type: DataTypes.STRING(200),
      allowNull: false,
    },
    company_name: {
      type: DataTypes.STRING(200),
      allowNull: false,
    },
    permit_number: {
      type: DataTypes.STRING(50),
      allowNull: true,
    },
    permit_type: {
      type: DataTypes.STRING(50),
      allowNull: true,
    },
    status: {
      type: DataTypes.ENUM(...Object.values(ProponentStatus)),
      defaultValue: ProponentStatus.ACTIVE,
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
    tableName: 'proponents',
    underscored: true,
    timestamps: true,
    createdAt: 'created_at',
    updatedAt: 'updated_at',
  }
);

export default Proponent;
