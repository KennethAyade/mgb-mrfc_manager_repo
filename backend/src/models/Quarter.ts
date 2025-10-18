/**
 * QUARTER MODEL
 * =============
 * Represents quarter periods for compliance reporting (Q1-Q4 per year)
 * Only one quarter should be current at a time
 */

import { Model, DataTypes, Optional } from 'sequelize';
import sequelize from '../config/database';

// Define model attributes interface
export interface QuarterAttributes {
  id: number;
  name: string;
  year: number;
  quarter_number: number;
  start_date: Date;
  end_date: Date;
  is_current: boolean;
}

// Define attributes for creation (id is optional)
export interface QuarterCreationAttributes extends Optional<QuarterAttributes, 'id' | 'is_current'> {}

// Define the Quarter model class
export class Quarter extends Model<QuarterAttributes, QuarterCreationAttributes> implements QuarterAttributes {
  public id!: number;
  public name!: string;
  public year!: number;
  public quarter_number!: number;
  public start_date!: Date;
  public end_date!: Date;
  public is_current!: boolean;

  // Timestamps
  public readonly createdAt!: Date;
  public readonly updatedAt!: Date;
}

// Initialize the model
Quarter.init(
  {
    id: {
      type: DataTypes.BIGINT,
      primaryKey: true,
      autoIncrement: true,
    },
    name: {
      type: DataTypes.STRING(20),
      allowNull: false,
      unique: true,
    },
    year: {
      type: DataTypes.INTEGER,
      allowNull: false,
    },
    quarter_number: {
      type: DataTypes.INTEGER,
      allowNull: false,
      validate: {
        min: 1,
        max: 4,
      },
    },
    start_date: {
      type: DataTypes.DATEONLY,
      allowNull: false,
    },
    end_date: {
      type: DataTypes.DATEONLY,
      allowNull: false,
    },
    is_current: {
      type: DataTypes.BOOLEAN,
      defaultValue: false,
    },
  },
  {
    sequelize,
    tableName: 'quarters',
    underscored: true,
    timestamps: false, // quarters table doesn't have timestamps
    indexes: [
      {
        unique: true,
        fields: ['year', 'quarter_number'],
      },
    ],
  }
);

export default Quarter;
