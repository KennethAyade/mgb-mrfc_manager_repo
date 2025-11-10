/**
 * GOOGLE GEMINI AI CONFIGURATION
 * ===============================
 * Configuration for Gemini AI to analyze CMVR compliance
 * Uses Gemini 1.5 Flash (free tier: 15 requests/minute, 1 million tokens/minute)
 */

import { GoogleGenerativeAI } from '@google/generative-ai';
import * as dotenv from 'dotenv';

dotenv.config();

const GEMINI_API_KEY = process.env.GEMINI_API_KEY;

if (!GEMINI_API_KEY) {
  console.warn('⚠️  GEMINI_API_KEY not configured. AI-powered analysis will not be available.');
}

// Initialize Gemini AI
const genAI = GEMINI_API_KEY ? new GoogleGenerativeAI(GEMINI_API_KEY) : null;

/**
 * Get Gemini model instance
 * Using gemini-1.5-flash-latest for fast, cost-effective analysis
 */
export const getGeminiModel = () => {
  if (!genAI) {
    throw new Error('Gemini AI not configured. Please set GEMINI_API_KEY in .env');
  }
  return genAI.getGenerativeModel({ model: 'gemini-2.5-flash' });
};

/**
 * Check if Gemini is configured
 */
export const isGeminiConfigured = (): boolean => {
  return !!GEMINI_API_KEY;
};

/**
 * Analyze CMVR compliance using Gemini AI
 * @param extractedText Text extracted from PDF (via OCR or direct extraction)
 * @param documentName Name of the document
 * @returns Structured compliance analysis
 */
export const analyzeComplianceWithGemini = async (
  extractedText: string,
  documentName: string
): Promise<any> => {
  try {
    console.log('🤖 Starting Gemini AI compliance analysis...');
    console.log(`   - Document: ${documentName}`);
    console.log(`   - Text length: ${extractedText.length} characters`);

    const model = getGeminiModel();

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

    const result = await model.generateContent(prompt);
    const response = await result.response;
    const text = response.text();

    console.log('✅ Gemini AI analysis complete');
    console.log(`   - Response length: ${text.length} characters`);

    // Parse JSON response
    const jsonMatch = text.match(/\{[\s\S]*\}/);
    if (!jsonMatch) {
      throw new Error('Gemini did not return valid JSON');
    }

    const analysis = JSON.parse(jsonMatch[0]);

    console.log(`   - Compliance: ${analysis.compliance_percentage}%`);
    console.log(`   - Rating: ${analysis.compliance_rating}`);
    console.log(`   - Total items: ${analysis.total_items}`);
    console.log(`   - Compliant: ${analysis.compliant_items}`);
    console.log(`   - Non-compliant: ${analysis.non_compliant_items}`);

    return analysis;

  } catch (error: any) {
    console.error('❌ Gemini AI analysis failed:', error.message);
    throw new Error(`AI analysis failed: ${error.message}`);
  }
};

export default {
  getGeminiModel,
  isGeminiConfigured,
  analyzeComplianceWithGemini
};

