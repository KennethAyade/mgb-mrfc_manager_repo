/**
 * ATTENDANCE MODEL
 * ================
 * Represents meeting attendance records with photo documentation
 * One attendance record per proponent per meeting
 */

import { Model, DataTypes, Optional } from 'sequelize';
import sequelize from '../config/database';

// Define model attributes interface
export interface AttendanceAttributes {
  id: number;
  agenda_id: number;
  proponent_id: number;
  is_present: boolean;
  photo_url: string | null;
  photo_cloudinary_id: string | null;
  marked_at: Date;
  marked_by: number | null;
  remarks: string | null;
}

// Define attributes for creation (id and some optional fields)
export interface AttendanceCreationAttributes extends Optional<AttendanceAttributes, 'id' | 'is_present' | 'photo_url' | 'photo_cloudinary_id' | 'marked_at' | 'marked_by' | 'remarks'> {}

// Define the Attendance model class
export class Attendance extends Model<AttendanceAttributes, AttendanceCreationAttributes> implements AttendanceAttributes {
  public id!: number;
  public agenda_id!: number;
  public proponent_id!: number;
  public is_present!: boolean;
  public photo_url!: string | null;
  public photo_cloudinary_id!: string | null;
  public marked_at!: Date;
  public marked_by!: number | null;
  public remarks!: string | null;

  // Timestamps
  public readonly createdAt!: Date;
  public readonly updatedAt!: Date;
}

// Initialize the model
Attendance.init(
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
    proponent_id: {
      type: DataTypes.BIGINT,
      allowNull: false,
      references: {
        model: 'proponents',
        key: 'id',
      },
      onDelete: 'CASCADE',
    },
    is_present: {
      type: DataTypes.BOOLEAN,
      defaultValue: false,
    },
    photo_url: {
      type: DataTypes.TEXT,
      allowNull: true,
    },
    photo_cloudinary_id: {
      type: DataTypes.STRING(255),
      allowNull: true,
    },
    marked_at: {
      type: DataTypes.DATE,
      defaultValue: DataTypes.NOW,
    },
    marked_by: {
      type: DataTypes.BIGINT,
      allowNull: true,
      references: {
        model: 'users',
        key: 'id',
      },
    },
    remarks: {
      type: DataTypes.TEXT,
      allowNull: true,
    },
  },
  {
    sequelize,
    tableName: 'attendance',
    underscored: true,
    timestamps: false, // attendance table uses marked_at instead of timestamps
    indexes: [
      {
        unique: true,
        fields: ['agenda_id', 'proponent_id'],
      },
    ],
  }
);

export default Attendance;
