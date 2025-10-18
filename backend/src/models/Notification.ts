/**
 * NOTIFICATION MODEL
 * ==================
 * Represents user notifications (in-app)
 * Types: MEETING (meeting alerts), COMPLIANCE (compliance reminders),
 *        ALERT (urgent notices), GENERAL (general info)
 * NULL user_id means broadcast to all users
 */

import { Model, DataTypes, Optional } from 'sequelize';
import sequelize from '../config/database';

// Enum for notification type matching PostgreSQL enum
export enum NotificationType {
  MEETING = 'MEETING',
  COMPLIANCE = 'COMPLIANCE',
  ALERT = 'ALERT',
  GENERAL = 'GENERAL'
}

// Define model attributes interface
export interface NotificationAttributes {
  id: number;
  user_id: number | null;
  type: NotificationType;
  title: string;
  message: string | null;
  is_read: boolean;
  read_at: Date | null;
  created_at: Date;
  sent_at: Date | null;
}

// Define attributes for creation (id and optional fields)
export interface NotificationCreationAttributes extends Optional<NotificationAttributes, 'id' | 'user_id' | 'message' | 'is_read' | 'read_at' | 'created_at' | 'sent_at'> {}

// Define the Notification model class
export class Notification extends Model<NotificationAttributes, NotificationCreationAttributes> implements NotificationAttributes {
  public id!: number;
  public user_id!: number | null;
  public type!: NotificationType;
  public title!: string;
  public message!: string | null;
  public is_read!: boolean;
  public read_at!: Date | null;
  public created_at!: Date;
  public sent_at!: Date | null;

  // Timestamps
  public readonly createdAt!: Date;
  public readonly updatedAt!: Date;
}

// Initialize the model
Notification.init(
  {
    id: {
      type: DataTypes.BIGINT,
      primaryKey: true,
      autoIncrement: true,
    },
    user_id: {
      type: DataTypes.BIGINT,
      allowNull: true,
      references: {
        model: 'users',
        key: 'id',
      },
      onDelete: 'CASCADE',
    },
    type: {
      type: DataTypes.ENUM(...Object.values(NotificationType)),
      allowNull: false,
    },
    title: {
      type: DataTypes.STRING(200),
      allowNull: false,
    },
    message: {
      type: DataTypes.TEXT,
      allowNull: true,
    },
    is_read: {
      type: DataTypes.BOOLEAN,
      defaultValue: false,
    },
    read_at: {
      type: DataTypes.DATE,
      allowNull: true,
    },
    created_at: {
      type: DataTypes.DATE,
      defaultValue: DataTypes.NOW,
    },
    sent_at: {
      type: DataTypes.DATE,
      allowNull: true,
    },
  },
  {
    sequelize,
    tableName: 'notifications',
    underscored: true,
    timestamps: false, // notifications has custom timestamp handling
  }
);

export default Notification;
