/**
 * AGENDA ITEM MODEL
 * ==================
 * Represents discussion topics added to meetings
 * All users can add agenda items
 * Auto-tagged with contributor name and username
 */

import { Model, DataTypes, Optional } from 'sequelize';
import sequelize from '../config/database';

// Define model attributes interface
export interface AgendaItemAttributes {
  id: number;
  agenda_id: number;
  title: string;
  description: string | null;
  added_by: number;
  added_by_name: string;
  added_by_username: string;
  order_index: number;
  created_at: Date;
  updated_at: Date;
}

// Define attributes for creation (id, timestamps are optional)
export interface AgendaItemCreationAttributes extends Optional<AgendaItemAttributes, 'id' | 'description' | 'order_index' | 'created_at' | 'updated_at'> {}

// Define the AgendaItem model class
export class AgendaItem extends Model<AgendaItemAttributes, AgendaItemCreationAttributes> implements AgendaItemAttributes {
  public id!: number;
  public agenda_id!: number;
  public title!: string;
  public description!: string | null;
  public added_by!: number;
  public added_by_name!: string;
  public added_by_username!: string;
  public order_index!: number;
  public created_at!: Date;
  public updated_at!: Date;

  // Timestamps
  public readonly createdAt!: Date;
  public readonly updatedAt!: Date;
}

// Initialize the model
AgendaItem.init(
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
    title: {
      type: DataTypes.STRING(255),
      allowNull: false,
    },
    description: {
      type: DataTypes.TEXT,
      allowNull: true,
    },
    added_by: {
      type: DataTypes.BIGINT,
      allowNull: false,
      references: {
        model: 'users',
        key: 'id',
      },
    },
    added_by_name: {
      type: DataTypes.STRING(100),
      allowNull: false,
    },
    added_by_username: {
      type: DataTypes.STRING(50),
      allowNull: false,
    },
    order_index: {
      type: DataTypes.INTEGER,
      defaultValue: 0,
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
    tableName: 'agenda_items',
    underscored: true,
    timestamps: true,
    createdAt: 'created_at',
    updatedAt: 'updated_at',
    indexes: [
      {
        fields: ['agenda_id'],
      },
      {
        fields: ['added_by'],
      },
      {
        fields: ['created_at'],
      },
    ],
  }
);

export default AgendaItem;
