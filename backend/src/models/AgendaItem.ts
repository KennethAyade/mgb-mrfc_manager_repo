/**
 * AGENDA ITEM MODEL
 * ==================
 * Represents discussion topics added to meetings
 * All users can add agenda items
 * Auto-tagged with contributor name and username
 */

import { Model, DataTypes, Optional } from 'sequelize';
import sequelize from '../config/database';

// Enum for agenda item status
export enum AgendaItemStatus {
  PROPOSED = 'PROPOSED',  // User-proposed, awaiting approval
  APPROVED = 'APPROVED',  // Approved by admin
  DENIED = 'DENIED'       // Denied by admin
}

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
  status: AgendaItemStatus;
  approved_by: number | null;
  approved_at: Date | null;
  denied_by: number | null;
  denied_at: Date | null;
  denial_remarks: string | null;
  mrfc_id: number | null;
  proponent_id: number | null;
  file_category: string | null;
  // Feature 2: Other Matters tab
  is_other_matter: boolean;
  // Feature 7: Highlight discussed items
  is_highlighted: boolean;
  highlighted_by: number | null;
  highlighted_at: Date | null;
  created_at: Date;
  updated_at: Date;
}

// Define attributes for creation (id, timestamps are optional)
export interface AgendaItemCreationAttributes extends Optional<AgendaItemAttributes, 'id' | 'description' | 'order_index' | 'is_other_matter' | 'is_highlighted' | 'highlighted_by' | 'highlighted_at' | 'created_at' | 'updated_at'> {}

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
  public status!: AgendaItemStatus;
  public approved_by!: number | null;
  public approved_at!: Date | null;
  public denied_by!: number | null;
  public denied_at!: Date | null;
  public denial_remarks!: string | null;
  public mrfc_id!: number | null;
  public proponent_id!: number | null;
  public file_category!: string | null;
  // Feature 2: Other Matters tab
  public is_other_matter!: boolean;
  // Feature 7: Highlight discussed items
  public is_highlighted!: boolean;
  public highlighted_by!: number | null;
  public highlighted_at!: Date | null;
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
    status: {
      type: DataTypes.ENUM('PROPOSED', 'APPROVED', 'DENIED'),
      defaultValue: 'APPROVED',
    },
    approved_by: {
      type: DataTypes.BIGINT,
      allowNull: true,
      references: {
        model: 'users',
        key: 'id',
      },
    },
    approved_at: {
      type: DataTypes.DATE,
      allowNull: true,
    },
    denied_by: {
      type: DataTypes.BIGINT,
      allowNull: true,
      references: {
        model: 'users',
        key: 'id',
      },
    },
    denied_at: {
      type: DataTypes.DATE,
      allowNull: true,
    },
    denial_remarks: {
      type: DataTypes.TEXT,
      allowNull: true,
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
    proponent_id: {
      type: DataTypes.BIGINT,
      allowNull: true,
      references: {
        model: 'proponents',
        key: 'id',
      },
      onDelete: 'CASCADE',
    },
    file_category: {
      type: DataTypes.STRING(50),
      allowNull: true,
    },
    // Feature 2: Other Matters tab - items added after agenda is finalized
    is_other_matter: {
      type: DataTypes.BOOLEAN,
      defaultValue: false,
      allowNull: false,
    },
    // Feature 7: Highlight discussed items (green background)
    is_highlighted: {
      type: DataTypes.BOOLEAN,
      defaultValue: false,
      allowNull: false,
    },
    highlighted_by: {
      type: DataTypes.BIGINT,
      allowNull: true,
      references: {
        model: 'users',
        key: 'id',
      },
    },
    highlighted_at: {
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
