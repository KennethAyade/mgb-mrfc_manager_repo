'use strict';

/** @type {import('sequelize-cli').Migration} */
module.exports = {
  async up(queryInterface, Sequelize) {
    await queryInterface.createTable('compliance_analyses', {
      id: {
        type: Sequelize.BIGINT,
        primaryKey: true,
        autoIncrement: true,
      },
      document_id: {
        type: Sequelize.BIGINT,
        allowNull: false,
        unique: true,
        references: {
          model: 'documents',
          key: 'id',
        },
        onDelete: 'CASCADE',
      },
      document_name: {
        type: Sequelize.STRING(255),
        allowNull: false,
      },
      analysis_status: {
        type: Sequelize.ENUM('PENDING', 'COMPLETED', 'FAILED'),
        allowNull: false,
        defaultValue: 'PENDING',
      },
      compliance_percentage: {
        type: Sequelize.DECIMAL(5, 2),
        allowNull: true,
      },
      compliance_rating: {
        type: Sequelize.ENUM('FULLY_COMPLIANT', 'PARTIALLY_COMPLIANT', 'NON_COMPLIANT'),
        allowNull: true,
      },
      total_items: {
        type: Sequelize.INTEGER,
        allowNull: true,
      },
      compliant_items: {
        type: Sequelize.INTEGER,
        allowNull: true,
      },
      non_compliant_items: {
        type: Sequelize.INTEGER,
        allowNull: true,
      },
      na_items: {
        type: Sequelize.INTEGER,
        allowNull: true,
      },
      applicable_items: {
        type: Sequelize.INTEGER,
        allowNull: true,
      },
      compliance_details: {
        type: Sequelize.JSONB,
        allowNull: true,
      },
      non_compliant_list: {
        type: Sequelize.JSONB,
        allowNull: true,
      },
      admin_adjusted: {
        type: Sequelize.BOOLEAN,
        defaultValue: false,
      },
      admin_notes: {
        type: Sequelize.TEXT,
        allowNull: true,
      },
      analyzed_at: {
        type: Sequelize.DATE,
        allowNull: true,
      },
      created_at: {
        type: Sequelize.DATE,
        defaultValue: Sequelize.literal('CURRENT_TIMESTAMP'),
      },
      updated_at: {
        type: Sequelize.DATE,
        defaultValue: Sequelize.literal('CURRENT_TIMESTAMP'),
      },
    });

    // Add index for faster lookups
    await queryInterface.addIndex('compliance_analyses', ['document_id']);
    await queryInterface.addIndex('compliance_analyses', ['analysis_status']);
    await queryInterface.addIndex('compliance_analyses', ['compliance_rating']);
  },

  async down(queryInterface, Sequelize) {
    await queryInterface.dropTable('compliance_analyses');
  },
};

