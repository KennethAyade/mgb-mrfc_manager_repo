import { Document, ComplianceAnalysis } from '../models';
import sequelize from '../config/database';

async function checkDocuments() {
  try {
    await sequelize.authenticate();
    console.log('✅ Database connected\n');

    const docs = await Document.findAll({
      attributes: ['id', 'original_name', 'category'],
      order: [['id', 'ASC']]
    });

    console.log('📄 Documents in database:');
    console.log('========================================');
    docs.forEach(d => {
      console.log(`  ID ${d.id}: ${d.original_name} (${d.category})`);
    });

    const analyses = await ComplianceAnalysis.findAll({
      attributes: ['id', 'document_id', 'compliance_percentage', 'compliance_rating'],
      order: [['document_id', 'ASC']]
    });

    console.log('\n📊 Compliance Analyses:');
    console.log('========================================');
    if (analyses.length === 0) {
      console.log('  (none)');
    } else {
      analyses.forEach(a => {
        console.log(`  Analysis ID ${a.id}: Document ${a.document_id} = ${a.compliance_percentage}% (${a.compliance_rating})`);
      });
    }

    console.log('\n');
    process.exit(0);
  } catch (error: any) {
    console.error('Error:', error.message);
    process.exit(1);
  }
}

checkDocuments();

