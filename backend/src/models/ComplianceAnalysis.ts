/**
 * COMPLIANCE ANALYSIS MODEL
 * =========================
 * Represents automatic PDF analysis results for CMVR documents
 * Stores compliance percentage, section-wise breakdown, and non-compliant items
 */

import { Model, DataTypes, Optional } from 'sequelize';
import sequelize from '../config/database';

// Enum for analysis status
export enum AnalysisStatus {
  PENDING = 'PENDING',
  COMPLETED = 'COMPLETED',
  FAILED = 'FAILED'
}

// Enum for compliance rating
export enum ComplianceRating {
  FULLY_COMPLIANT = 'FULLY_COMPLIANT',
  PARTIALLY_COMPLIANT = 'PARTIALLY_COMPLIANT',
  NON_COMPLIANT = 'NON_COMPLIANT'
}

// Interface for compliance section
export interface ComplianceSection {
  section_name: string;
  total: number;
  compliant: number;
  non_compliant: number;
  na: number;
  percentage: number;
}

// Interface for non-compliant item
export interface NonCompliantItem {
  requirement: string;
  page_number?: number;
  severity?: string;
  notes?: string;
}

// Interface for compliance details
export interface ComplianceDetails {
  ecc_compliance?: ComplianceSection;
  epep_compliance?: ComplianceSection;
  impact_management?: ComplianceSection;
  water_quality?: ComplianceSection;
  air_quality?: ComplianceSection;
  noise_quality?: ComplianceSection;
  waste_management?: ComplianceSection;
}

// Define model attributes interface
export interface ComplianceAnalysisAttributes {
  id: number;
  document_id: number;
  document_name: string;
  analysis_status: AnalysisStatus;
  compliance_percentage: number | null;
  compliance_rating: ComplianceRating | null;
  total_items: number | null;
  compliant_items: number | null;
  non_compliant_items: number | null;
  na_items: number | null;
  applicable_items: number | null;
  compliance_details: ComplianceDetails | null;
  non_compliant_list: NonCompliantItem[] | null;
  admin_adjusted: boolean;
  admin_notes: string | null;
  analyzed_at: Date | null;
  extracted_text: string | null;
  ocr_confidence: number | null;
  ocr_language: string | null;
  created_at: Date;
  updated_at: Date;
}

// Define attributes for creation
export interface ComplianceAnalysisCreationAttributes
  extends Optional<
    ComplianceAnalysisAttributes,
    | 'id'
    | 'compliance_percentage'
    | 'compliance_rating'
    | 'total_items'
    | 'compliant_items'
    | 'non_compliant_items'
    | 'na_items'
    | 'applicable_items'
    | 'compliance_details'
    | 'non_compliant_list'
    | 'admin_adjusted'
    | 'admin_notes'
    | 'analyzed_at'
    | 'extracted_text'
    | 'ocr_confidence'
    | 'ocr_language'
    | 'created_at'
    | 'updated_at'
  > {}

// Define the ComplianceAnalysis model class
export class ComplianceAnalysis
  extends Model<ComplianceAnalysisAttributes, ComplianceAnalysisCreationAttributes>
  implements ComplianceAnalysisAttributes
{
  public id!: number;
  public document_id!: number;
  public document_name!: string;
  public analysis_status!: AnalysisStatus;
  public compliance_percentage!: number | null;
  public compliance_rating!: ComplianceRating | null;
  public total_items!: number | null;
  public compliant_items!: number | null;
  public non_compliant_items!: number | null;
  public na_items!: number | null;
  public applicable_items!: number | null;
  public compliance_details!: ComplianceDetails | null;
  public non_compliant_list!: NonCompliantItem[] | null;
  public admin_adjusted!: boolean;
  public admin_notes!: string | null;
  public analyzed_at!: Date | null;
  public extracted_text!: string | null;
  public ocr_confidence!: number | null;
  public ocr_language!: string | null;

  // Timestamps
  public readonly created_at!: Date;
  public readonly updated_at!: Date;
}

// Initialize the model
ComplianceAnalysis.init(
  {
    id: {
      type: DataTypes.BIGINT,
      primaryKey: true,
      autoIncrement: true,
    },
    document_id: {
      type: DataTypes.BIGINT,
      allowNull: false,
      references: {
        model: 'documents',
        key: 'id',
      },
      onDelete: 'CASCADE',
      unique: true, // One analysis per document
    },
    document_name: {
      type: DataTypes.STRING(255),
      allowNull: false,
    },
    analysis_status: {
      type: DataTypes.ENUM(...Object.values(AnalysisStatus)),
      allowNull: false,
      defaultValue: AnalysisStatus.PENDING,
    },
    compliance_percentage: {
      type: DataTypes.DECIMAL(5, 2),
      allowNull: true,
    },
    compliance_rating: {
      type: DataTypes.ENUM(...Object.values(ComplianceRating)),
      allowNull: true,
    },
    total_items: {
      type: DataTypes.INTEGER,
      allowNull: true,
    },
    compliant_items: {
      type: DataTypes.INTEGER,
      allowNull: true,
    },
    non_compliant_items: {
      type: DataTypes.INTEGER,
      allowNull: true,
    },
    na_items: {
      type: DataTypes.INTEGER,
      allowNull: true,
    },
    applicable_items: {
      type: DataTypes.INTEGER,
      allowNull: true,
    },
    compliance_details: {
      type: DataTypes.JSONB,
      allowNull: true,
    },
    non_compliant_list: {
      type: DataTypes.JSONB,
      allowNull: true,
    },
    admin_adjusted: {
      type: DataTypes.BOOLEAN,
      defaultValue: false,
    },
    admin_notes: {
      type: DataTypes.TEXT,
      allowNull: true,
    },
    analyzed_at: {
      type: DataTypes.DATE,
      allowNull: true,
    },
    extracted_text: {
      type: DataTypes.TEXT,
      allowNull: true,
    },
    ocr_confidence: {
      type: DataTypes.DECIMAL(5, 2),
      allowNull: true,
    },
    ocr_language: {
      type: DataTypes.STRING(20),
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
    tableName: 'compliance_analyses',
    underscored: true,
    timestamps: true,
    createdAt: 'created_at',
    updatedAt: 'updated_at',
  }
);

export default ComplianceAnalysis;

