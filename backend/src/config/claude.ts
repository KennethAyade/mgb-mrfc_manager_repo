/**
 * ANTHROPIC CLAUDE AI CONFIGURATION
 * ===================================
 * Configuration for Claude AI to analyze CMVR compliance
 * Uses Claude Sonnet for intelligent analysis with vision capabilities
 */

import Anthropic from '@anthropic-ai/sdk';
import * as dotenv from 'dotenv';

dotenv.config();

const ANTHROPIC_API_KEY = process.env.ANTHROPIC_API_KEY;

if (!ANTHROPIC_API_KEY) {
  console.warn('⚠️  ANTHROPIC_API_KEY not configured. AI-powered analysis will not be available.');
}

// Initialize Anthropic client
const anthropic = ANTHROPIC_API_KEY ? new Anthropic({ apiKey: ANTHROPIC_API_KEY }) : null;

/**
 * Get Anthropic client instance
 */
export const getClaudeClient = () => {
  if (!anthropic) {
    throw new Error('Claude AI not configured. Please set ANTHROPIC_API_KEY in .env');
  }
  return anthropic;
};

/**
 * Check if Claude is configured
 */
export const isClaudeConfigured = (): boolean => {
  return !!ANTHROPIC_API_KEY;
};

/**
 * Analyze CMVR compliance using Claude AI
 * @param extractedText Text extracted from PDF (via OCR or direct extraction)
 * @param documentName Name of the document
 * @returns Structured compliance analysis
 */
export const analyzeComplianceWithClaude = async (
  extractedText: string,
  documentName: string
): Promise<any> => {
  try {
    console.log('🤖 Starting Claude AI compliance analysis...');
    console.log(`   - Document: ${documentName}`);
    console.log(`   - Text length: ${extractedText.length} characters`);

    const client = getClaudeClient();

    const prompt = `You are an expert environmental compliance analyst for the Philippine Department of Environment and Natural Resources (DENR).

Analyze the following Compliance Monitoring and Validation Report (CMVR) and provide a structured compliance assessment.

DOCUMENT: ${documentName}

EXTRACTED TEXT:
${extractedText}

Please analyze the document and provide a JSON response with the following structure:

{
  "compliance_percentage": <number 0-100>,
  "compliance_rating": "<FULLY_COMPLIANT|PARTIALLY_COMPLIANT|NON_COMPLIANT>",
  "total_items": <number>,
  "compliant_items": <number>,
  "non_compliant_items": <number>,
  "na_items": <number>,
  "compliance_details": {
    "ecc_compliance": {
      "section_name": "ECC Compliance",
      "total": <number>,
      "compliant": <number>,
      "non_compliant": <number>,
      "na": <number>,
      "percentage": <number>
    },
    "epep_compliance": {
      "section_name": "EPEP Commitments",
      "total": <number>,
      "compliant": <number>,
      "non_compliant": <number>,
      "na": <number>,
      "percentage": <number>
    },
    "impact_management": {
      "section_name": "Impact Management",
      "total": <number>,
      "compliant": <number>,
      "non_compliant": <number>,
      "na": <number>,
      "percentage": <number>
    },
    "water_quality": {
      "section_name": "Water Quality",
      "total": <number>,
      "compliant": <number>,
      "non_compliant": <number>,
      "na": <number>,
      "percentage": <number>
    },
    "air_quality": {
      "section_name": "Air Quality",
      "total": <number>,
      "compliant": <number>,
      "non_compliant": <number>,
      "na": <number>,
      "percentage": <number>
    },
    "noise_quality": {
      "section_name": "Noise Quality",
      "total": <number>,
      "compliant": <number>,
      "non_compliant": <number>,
      "na": <number>,
      "percentage": <number>
    },
    "waste_management": {
      "section_name": "Waste Management",
      "total": <number>,
      "compliant": <number>,
      "non_compliant": <number>,
      "na": <number>,
      "percentage": <number>
    }
  },
  "non_compliant_list": [
    {
      "requirement": "<specific requirement>",
      "page_number": <number>,
      "severity": "<HIGH|MEDIUM|LOW>",
      "notes": "<detailed explanation>"
    }
  ]
}

INSTRUCTIONS:
1. Identify all compliance requirements mentioned in the document
2. Determine if each requirement is COMPLIED, NOT COMPLIED, or N/A
3. Calculate compliance percentage: (compliant / (total - na)) * 100
4. Categorize by section (ECC, EPEP, Water, Air, Noise, Waste, Impact Management)
5. List all non-compliant items with severity and details
6. Use FULLY_COMPLIANT if ≥90%, PARTIALLY_COMPLIANT if 70-89%, NON_COMPLIANT if <70%

Return ONLY the JSON object, no additional text.`;

    const response = await client.messages.create({
      model: 'claude-haiku-4-5-20251001',
      max_tokens: 4096,
      temperature: 0.1,
      messages: [
        {
          role: 'user',
          content: prompt
        }
      ]
    });

    const textContent = response.content.find(block => block.type === 'text');
    if (!textContent || textContent.type !== 'text') {
      throw new Error('No text response from Claude');
    }

    const text = textContent.text;

    console.log('✅ Claude AI analysis complete');
    console.log(`   - Response length: ${text.length} characters`);

    // Parse JSON response
    const jsonMatch = text.match(/\{[\s\S]*\}/);
    if (!jsonMatch) {
      throw new Error('Claude did not return valid JSON');
    }

    const analysis = JSON.parse(jsonMatch[0]);

    console.log(`   - Compliance: ${analysis.compliance_percentage}%`);
    console.log(`   - Rating: ${analysis.compliance_rating}`);
    console.log(`   - Total items: ${analysis.total_items}`);
    console.log(`   - Compliant: ${analysis.compliant_items}`);
    console.log(`   - Non-compliant: ${analysis.non_compliant_items}`);

    return analysis;

  } catch (error: any) {
    console.error('❌ Claude AI analysis failed:', error.message);
    throw new Error(`AI analysis failed: ${error.message}`);
  }
};

/**
 * Analyze CMVR compliance using Claude AI with PDF file directly
 * Uses Claude's vision capabilities for direct PDF analysis
 * @param pdfBuffer PDF file buffer
 * @param documentName Name of the document
 * @returns Structured compliance analysis
 */
export const analyzeComplianceWithClaudePDF = async (
  pdfBuffer: Buffer,
  documentName: string
): Promise<any> => {
  try {
    console.log('🤖 Starting Claude AI compliance analysis (direct PDF)...');
    console.log(`   - Document: ${documentName}`);
    console.log(`   - PDF size: ${(pdfBuffer.length / 1024 / 1024).toFixed(2)} MB`);

    const client = getClaudeClient();

    // Convert PDF buffer to base64 for Claude
    const base64Pdf = pdfBuffer.toString('base64');

    const prompt = `You are an expert environmental compliance analyst for the Philippine Department of Environment and Natural Resources (DENR).

Analyze the following Compliance Monitoring and Validation Report (CMVR) and provide a structured compliance assessment.

DOCUMENT: ${documentName}

Please analyze the PDF document and provide a JSON response with the following structure:

{
  "compliance_percentage": <number 0-100>,
  "compliance_rating": "<FULLY_COMPLIANT|PARTIALLY_COMPLIANT|NON_COMPLIANT>",
  "total_items": <number>,
  "compliant_items": <number>,
  "non_compliant_items": <number>,
  "na_items": <number>,
  "compliance_details": {
    "ecc_compliance": {
      "section_name": "ECC Compliance",
      "total": <number>,
      "compliant": <number>,
      "non_compliant": <number>,
      "na": <number>,
      "percentage": <number>
    },
    "epep_compliance": {
      "section_name": "EPEP Commitments",
      "total": <number>,
      "compliant": <number>,
      "non_compliant": <number>,
      "na": <number>,
      "percentage": <number>
    },
    "impact_management": {
      "section_name": "Impact Management",
      "total": <number>,
      "compliant": <number>,
      "non_compliant": <number>,
      "na": <number>,
      "percentage": <number>
    },
    "water_quality": {
      "section_name": "Water Quality",
      "total": <number>,
      "compliant": <number>,
      "non_compliant": <number>,
      "na": <number>,
      "percentage": <number>
    },
    "air_quality": {
      "section_name": "Air Quality",
      "total": <number>,
      "compliant": <number>,
      "non_compliant": <number>,
      "na": <number>,
      "percentage": <number>
    },
    "noise_quality": {
      "section_name": "Noise Quality",
      "total": <number>,
      "compliant": <number>,
      "non_compliant": <number>,
      "na": <number>,
      "percentage": <number>
    },
    "waste_management": {
      "section_name": "Waste Management",
      "total": <number>,
      "compliant": <number>,
      "non_compliant": <number>,
      "na": <number>,
      "percentage": <number>
    }
  },
  "non_compliant_list": [
    {
      "requirement": "<specific requirement>",
      "page_number": <number>,
      "severity": "<HIGH|MEDIUM|LOW>",
      "notes": "<detailed explanation>"
    }
  ]
}

INSTRUCTIONS:
1. Read and analyze the entire PDF document (including scanned pages)
2. Identify all compliance requirements mentioned in the document
3. Determine if each requirement is COMPLIED, NOT COMPLIED, or N/A
4. Calculate compliance percentage: (compliant / (total - na)) * 100
5. Categorize by section (ECC, EPEP, Water, Air, Noise, Waste, Impact Management)
6. List all non-compliant items with severity and details
7. Use FULLY_COMPLIANT if ≥90%, PARTIALLY_COMPLIANT if 70-89%, NON_COMPLIANT if <70%

Return ONLY the JSON object, no additional text.`;

    const response = await client.messages.create({
      model: 'claude-sonnet-4-20250514',
      max_tokens: 4096,
      temperature: 0.1,
      messages: [
        {
          role: 'user',
          content: [
            {
              type: 'document',
              source: {
                type: 'base64',
                media_type: 'application/pdf',
                data: base64Pdf
              }
            },
            {
              type: 'text',
              text: prompt
            }
          ]
        }
      ]
    });

    const textContent = response.content.find(block => block.type === 'text');
    if (!textContent || textContent.type !== 'text') {
      throw new Error('No text response from Claude');
    }

    const text = textContent.text;

    console.log('✅ Claude AI analysis complete');
    console.log(`   - Response length: ${text.length} characters`);

    // Parse JSON response
    const jsonMatch = text.match(/\{[\s\S]*\}/);
    if (!jsonMatch) {
      throw new Error('Claude did not return valid JSON');
    }

    const analysis = JSON.parse(jsonMatch[0]);

    console.log(`   - Compliance: ${analysis.compliance_percentage}%`);
    console.log(`   - Rating: ${analysis.compliance_rating}`);
    console.log(`   - Total items: ${analysis.total_items}`);
    console.log(`   - Compliant: ${analysis.compliant_items}`);
    console.log(`   - Non-compliant: ${analysis.non_compliant_items}`);

    return analysis;

  } catch (error: any) {
    console.error('❌ Claude AI PDF analysis failed:', error.message);
    throw new Error(`AI PDF analysis failed: ${error.message}`);
  }
};

export default {
  getClaudeClient,
  isClaudeConfigured,
  analyzeComplianceWithClaude,
  analyzeComplianceWithClaudePDF
};
