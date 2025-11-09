/**
 * MODELS INDEX
 * ============
 * Central hub for all Sequelize models
 * - Imports all model definitions
 * - Sets up all associations (relationships)
 * - Exports all models as a single object
 */

import User from './User';
import Mrfc from './Mrfc';
import Proponent from './Proponent';
import Quarter from './Quarter';
import Agenda from './Agenda';
import AgendaItem from './AgendaItem';
import MeetingMinutes from './MeetingMinutes';
import MatterArising from './MatterArising';
import Attendance from './Attendance';
import Document, { DocumentCategory, DocumentStatus } from './Document';
import VoiceRecording from './VoiceRecording';
import Note from './Note';
import Notification from './Notification';
import UserMrfcAccess from './UserMrfcAccess';
import ComplianceLog from './ComplianceLog';
import ComplianceAnalysis, { AnalysisStatus, ComplianceRating } from './ComplianceAnalysis';
import AuditLog, { AuditAction } from './AuditLog';

/**
 * ASSOCIATIONS / RELATIONSHIPS
 * ============================
 * Define all foreign key relationships between models
 */

// ==========================================
// USER ASSOCIATIONS
// ==========================================

// User -> Mrfc (creator)
User.hasMany(Mrfc, {
  foreignKey: 'created_by',
  as: 'created_mrfcs'
});
Mrfc.belongsTo(User, {
  foreignKey: 'created_by',
  as: 'creator'
});

// User -> Note (owner)
User.hasMany(Note, {
  foreignKey: 'user_id',
  as: 'notes'
});
Note.belongsTo(User, {
  foreignKey: 'user_id',
  as: 'user'
});

// User -> Notification (recipient)
User.hasMany(Notification, {
  foreignKey: 'user_id',
  as: 'notifications'
});
Notification.belongsTo(User, {
  foreignKey: 'user_id',
  as: 'user'
});

// User -> UserMrfcAccess (many-to-many with Mrfc)
User.hasMany(UserMrfcAccess, {
  foreignKey: 'user_id',
  as: 'mrfc_access'
});
UserMrfcAccess.belongsTo(User, {
  foreignKey: 'user_id',
  as: 'user'
});

// User -> UserMrfcAccess (granter)
User.hasMany(UserMrfcAccess, {
  foreignKey: 'granted_by',
  as: 'granted_access'
});
UserMrfcAccess.belongsTo(User, {
  foreignKey: 'granted_by',
  as: 'granter'
});

// User -> Document (uploader)
User.hasMany(Document, {
  foreignKey: 'uploaded_by',
  as: 'uploaded_documents'
});
Document.belongsTo(User, {
  foreignKey: 'uploaded_by',
  as: 'uploader'
});

// User -> Document (reviewer)
User.hasMany(Document, {
  foreignKey: 'reviewed_by',
  as: 'reviewed_documents'
});
Document.belongsTo(User, {
  foreignKey: 'reviewed_by',
  as: 'reviewer'
});

// User -> Attendance (marker)
User.hasMany(Attendance, {
  foreignKey: 'marked_by',
  as: 'marked_attendance'
});
Attendance.belongsTo(User, {
  foreignKey: 'marked_by',
  as: 'marker'
});

// User -> VoiceRecording (recorder)
User.hasMany(VoiceRecording, {
  foreignKey: 'recorded_by',
  as: 'voice_recordings'
});
VoiceRecording.belongsTo(User, {
  foreignKey: 'recorded_by',
  as: 'recorder'
});

// User -> ComplianceLog (calculator)
User.hasMany(ComplianceLog, {
  foreignKey: 'calculated_by',
  as: 'calculated_logs'
});
ComplianceLog.belongsTo(User, {
  foreignKey: 'calculated_by',
  as: 'calculator'
});

// User -> ComplianceLog (overrider)
User.hasMany(ComplianceLog, {
  foreignKey: 'override_by',
  as: 'overridden_logs'
});
ComplianceLog.belongsTo(User, {
  foreignKey: 'override_by',
  as: 'overrider'
});

// User -> AuditLog
User.hasMany(AuditLog, {
  foreignKey: 'user_id',
  as: 'audit_logs'
});
AuditLog.belongsTo(User, {
  foreignKey: 'user_id',
  as: 'user'
});

// ==========================================
// MRFC ASSOCIATIONS
// ==========================================

// Mrfc -> Proponent (one-to-many)
Mrfc.hasMany(Proponent, {
  foreignKey: 'mrfc_id',
  as: 'proponents',
  onDelete: 'CASCADE'
});
Proponent.belongsTo(Mrfc, {
  foreignKey: 'mrfc_id',
  as: 'mrfc'
});

// Mrfc -> Agenda (one-to-many)
Mrfc.hasMany(Agenda, {
  foreignKey: 'mrfc_id',
  as: 'agendas',
  onDelete: 'CASCADE'
});
Agenda.belongsTo(Mrfc, {
  foreignKey: 'mrfc_id',
  as: 'mrfc'
});

// Mrfc -> Note (optional link)
Mrfc.hasMany(Note, {
  foreignKey: 'mrfc_id',
  as: 'notes',
  onDelete: 'CASCADE'
});
Note.belongsTo(Mrfc, {
  foreignKey: 'mrfc_id',
  as: 'mrfc'
});

// Mrfc -> UserMrfcAccess (many-to-many with User)
Mrfc.hasMany(UserMrfcAccess, {
  foreignKey: 'mrfc_id',
  as: 'user_access',
  onDelete: 'CASCADE'
});
UserMrfcAccess.belongsTo(Mrfc, {
  foreignKey: 'mrfc_id',
  as: 'mrfc'
});

// Mrfc -> User (many-to-many through UserMrfcAccess)
Mrfc.belongsToMany(User, {
  through: UserMrfcAccess,
  foreignKey: 'mrfc_id',
  otherKey: 'user_id',
  as: 'users'
});
User.belongsToMany(Mrfc, {
  through: UserMrfcAccess,
  foreignKey: 'user_id',
  otherKey: 'mrfc_id',
  as: 'mrfcs'
});

// ==========================================
// QUARTER ASSOCIATIONS
// ==========================================

// Quarter -> Agenda (one-to-many)
Quarter.hasMany(Agenda, {
  foreignKey: 'quarter_id',
  as: 'agendas'
});
Agenda.belongsTo(Quarter, {
  foreignKey: 'quarter_id',
  as: 'quarter'
});

// Quarter -> Document (one-to-many)
Quarter.hasMany(Document, {
  foreignKey: 'quarter_id',
  as: 'documents'
});
Document.belongsTo(Quarter, {
  foreignKey: 'quarter_id',
  as: 'quarter'
});

// Quarter -> Note (optional link)
Quarter.hasMany(Note, {
  foreignKey: 'quarter_id',
  as: 'notes'
});
Note.belongsTo(Quarter, {
  foreignKey: 'quarter_id',
  as: 'quarter'
});

// Quarter -> ComplianceLog (one-to-many)
Quarter.hasMany(ComplianceLog, {
  foreignKey: 'quarter_id',
  as: 'compliance_logs'
});
ComplianceLog.belongsTo(Quarter, {
  foreignKey: 'quarter_id',
  as: 'quarter'
});

// ==========================================
// PROPONENT ASSOCIATIONS
// ==========================================

// Proponent -> Attendance (one-to-many)
Proponent.hasMany(Attendance, {
  foreignKey: 'proponent_id',
  as: 'attendance',
  onDelete: 'CASCADE'
});
Attendance.belongsTo(Proponent, {
  foreignKey: 'proponent_id',
  as: 'proponent'
});

// Proponent -> Document (one-to-many)
Proponent.hasMany(Document, {
  foreignKey: 'proponent_id',
  as: 'documents',
  onDelete: 'CASCADE'
});
Document.belongsTo(Proponent, {
  foreignKey: 'proponent_id',
  as: 'proponent'
});

// Proponent -> ComplianceLog (one-to-many)
Proponent.hasMany(ComplianceLog, {
  foreignKey: 'proponent_id',
  as: 'compliance_logs',
  onDelete: 'CASCADE'
});
ComplianceLog.belongsTo(Proponent, {
  foreignKey: 'proponent_id',
  as: 'proponent'
});

// ==========================================
// DOCUMENT ASSOCIATIONS
// ==========================================

// Document -> ComplianceAnalysis (one-to-one)
Document.hasOne(ComplianceAnalysis, {
  foreignKey: 'document_id',
  as: 'compliance_analysis',
  onDelete: 'CASCADE'
});
ComplianceAnalysis.belongsTo(Document, {
  foreignKey: 'document_id',
  as: 'document'
});

// ==========================================
// AGENDA ASSOCIATIONS
// ==========================================

// Agenda -> MatterArising (one-to-many)
Agenda.hasMany(MatterArising, {
  foreignKey: 'agenda_id',
  as: 'matters_arising',
  onDelete: 'CASCADE'
});
MatterArising.belongsTo(Agenda, {
  foreignKey: 'agenda_id',
  as: 'agenda'
});

// Agenda -> Attendance (one-to-many)
Agenda.hasMany(Attendance, {
  foreignKey: 'agenda_id',
  as: 'attendance',
  onDelete: 'CASCADE'
});
Attendance.belongsTo(Agenda, {
  foreignKey: 'agenda_id',
  as: 'agenda'
});

// Agenda -> VoiceRecording (one-to-many)
Agenda.hasMany(VoiceRecording, {
  foreignKey: 'agenda_id',
  as: 'voice_recordings',
  onDelete: 'CASCADE'
});
VoiceRecording.belongsTo(Agenda, {
  foreignKey: 'agenda_id',
  as: 'agenda'
});

// Agenda -> AgendaItem (one-to-many)
Agenda.hasMany(AgendaItem, {
  foreignKey: 'agenda_id',
  as: 'agenda_items',
  onDelete: 'CASCADE'
});
AgendaItem.belongsTo(Agenda, {
  foreignKey: 'agenda_id',
  as: 'agenda'
});

// User -> AgendaItem (one-to-many)
User.hasMany(AgendaItem, {
  foreignKey: 'added_by',
  as: 'agenda_items'
});
AgendaItem.belongsTo(User, {
  foreignKey: 'added_by',
  as: 'contributor'
});

// Agenda -> MeetingMinutes (one-to-one)
Agenda.hasOne(MeetingMinutes, {
  foreignKey: 'agenda_id',
  as: 'meeting_minutes',
  onDelete: 'CASCADE'
});
MeetingMinutes.belongsTo(Agenda, {
  foreignKey: 'agenda_id',
  as: 'agenda'
});

// User -> MeetingMinutes (creator/organizer)
User.hasMany(MeetingMinutes, {
  foreignKey: 'created_by',
  as: 'created_minutes'
});
MeetingMinutes.belongsTo(User, {
  foreignKey: 'created_by',
  as: 'organizer'
});

// User -> MeetingMinutes (approver)
User.hasMany(MeetingMinutes, {
  foreignKey: 'approved_by',
  as: 'approved_minutes'
});
MeetingMinutes.belongsTo(User, {
  foreignKey: 'approved_by',
  as: 'approver'
});

/**
 * Export all models and enums
 */
export {
  User,
  Mrfc,
  Proponent,
  Quarter,
  Agenda,
  AgendaItem,
  MeetingMinutes,
  MatterArising,
  Attendance,
  Document,
  DocumentCategory,
  DocumentStatus,
  VoiceRecording,
  Note,
  Notification,
  UserMrfcAccess,
  ComplianceLog,
  ComplianceAnalysis,
  AnalysisStatus,
  ComplianceRating,
  AuditLog,
  AuditAction
};

/**
 * Export default object containing all models
 */
export default {
  User,
  Mrfc,
  Proponent,
  Quarter,
  Agenda,
  AgendaItem,
  MeetingMinutes,
  MatterArising,
  Attendance,
  Document,
  VoiceRecording,
  Note,
  Notification,
  UserMrfcAccess,
  ComplianceLog,
  ComplianceAnalysis,
  AuditLog
};
