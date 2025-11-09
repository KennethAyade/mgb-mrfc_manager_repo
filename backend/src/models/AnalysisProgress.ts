/**
 * ANALYSIS PROGRESS TRACKING
 * ===========================
 * In-memory storage for tracking OCR analysis progress
 * Used for real-time progress updates to frontend
 */

export interface AnalysisProgressData {
  document_id: number;
  status: 'pending' | 'processing' | 'completed' | 'failed';
  progress: number; // 0-100
  current_step: string | null;
  error: string | null;
  updated_at: Date;
}

// In-memory storage for progress tracking
const progressStore = new Map<number, AnalysisProgressData>();

// Auto-cleanup old progress entries after 1 hour
setInterval(() => {
  const oneHourAgo = new Date(Date.now() - 60 * 60 * 1000);
  for (const [documentId, progress] of progressStore.entries()) {
    if (progress.updated_at < oneHourAgo) {
      progressStore.delete(documentId);
    }
  }
}, 60 * 60 * 1000); // Run every hour

export class AnalysisProgress {
  /**
   * Initialize progress tracking for a document
   */
  static create(documentId: number): void {
    progressStore.set(documentId, {
      document_id: documentId,
      status: 'pending',
      progress: 0,
      current_step: 'Initializing...',
      error: null,
      updated_at: new Date()
    });
  }

  /**
   * Update progress for a document
   */
  static update(
    documentId: number,
    progress: number,
    currentStep: string,
    status: 'pending' | 'processing' | 'completed' | 'failed' = 'processing'
  ): void {
    const existing = progressStore.get(documentId);
    if (existing) {
      progressStore.set(documentId, {
        ...existing,
        status,
        progress: Math.min(100, Math.max(0, progress)),
        current_step: currentStep,
        updated_at: new Date()
      });
    }
  }

  /**
   * Mark analysis as completed
   */
  static complete(documentId: number): void {
    const existing = progressStore.get(documentId);
    if (existing) {
      progressStore.set(documentId, {
        ...existing,
        status: 'completed',
        progress: 100,
        current_step: 'Analysis complete',
        updated_at: new Date()
      });
    }
  }

  /**
   * Mark analysis as failed
   */
  static fail(documentId: number, error: string): void {
    const existing = progressStore.get(documentId);
    if (existing) {
      progressStore.set(documentId, {
        ...existing,
        status: 'failed',
        error,
        updated_at: new Date()
      });
    }
  }

  /**
   * Get progress for a document
   */
  static get(documentId: number): AnalysisProgressData | null {
    return progressStore.get(documentId) || null;
  }

  /**
   * Delete progress entry
   */
  static delete(documentId: number): void {
    progressStore.delete(documentId);
  }

  /**
   * Check if progress exists
   */
  static exists(documentId: number): boolean {
    return progressStore.has(documentId);
  }
}

export default AnalysisProgress;

