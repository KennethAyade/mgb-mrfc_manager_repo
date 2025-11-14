# 📋 Agenda Approval Workflow - Complete Guide
**Version:** 2.0.19  
**Date:** November 14, 2025  
**Status:** ✅ COMPLETE - Ready for Testing

---

## 🎯 Overview

This guide explains the new Agenda Approval Workflow that allows regular users to propose meeting agendas for admin review and approval.

---

## ✨ What's New (v2.0.19)

### **Feature 1: File Upload Restriction** ✅
- **Change:** File Upload button now hidden for regular users
- **Who can upload:** Only ADMIN and SUPER_ADMIN roles
- **Location:** Proponent Detail screen → Services section
- **UI:** Button simply doesn't appear for regular users

### **Feature 2: Agenda Proposal System** ✨
- **Regular Users:** Can propose agendas for meetings
- **Admins:** Can approve or deny proposals with remarks
- **Status:** Automatic status based on user role
- **Workflow:** Propose → Review → Approve/Deny

### **Feature 3: Denial with Remarks** 📝
- **Required:** Admins MUST provide reason when denying
- **User Feedback:** Users see why their proposal was denied
- **UI:** Beautiful dialog with multi-line text input

---

## 👥 User Roles & Permissions

### **Regular User (USER)**
- ✅ Can create agenda proposals
- ✅ Proposals start with status = PROPOSED
- ✅ Can view their proposed agendas
- ✅ Can see denial remarks if proposal denied
- ❌ Cannot upload files
- ❌ Cannot approve/deny proposals

### **Admin (ADMIN / SUPER_ADMIN)**
- ✅ Can create agendas directly (status = DRAFT or PUBLISHED)
- ✅ Can view all pending proposals
- ✅ Can approve proposals (PROPOSED → PUBLISHED)
- ✅ Can deny proposals with remarks (PROPOSED → CANCELLED)
- ✅ Can upload files
- ✅ Full system access

---

## 🔄 Complete Workflow

### **For Regular Users (Proposing Agenda):**

```
1. Login as regular user (e.g., kennethayade)
2. Navigate to Meeting Management
3. Click "Create New Meeting" or similar
4. Fill in meeting details:
   - MRFC (select from assigned MRFCs)
   - Quarter
   - Meeting Date
   - Meeting Time
   - Location
5. Click Save
   → Status automatically set to PROPOSED
   → proposed_by = current user ID
   → proposed_at = current timestamp
6. Wait for admin approval
7. Check status:
   - If APPROVED: Agenda published ✅
   - If DENIED: See admin's remarks explaining why ❌
```

### **For Admins (Reviewing Proposals):**

```
1. Login as admin/superadmin
2. Open Admin Dashboard
3. Click "Pending Proposals" card (shows count of pending)
4. See list of all proposed agendas with:
   - MRFC Name
   - Meeting Date & Time
   - Location
   - Proposed By (user's name)
5. For each proposal, choose:
   
   OPTION A: APPROVE
   - Click "Approve" button
   - Confirm approval
   - Agenda published (status → PUBLISHED)
   - User can now see it in meeting list
   
   OPTION B: DENY
   - Click "Deny" button
   - Enter denial remarks (REQUIRED)
   - Examples:
     * "Meeting date conflicts with existing schedule"
     * "Location not available for this quarter"
     * "Duplicate proposal - already scheduled"
   - Click "Deny Proposal"
   - Agenda cancelled (status → CANCELLED)
   - User sees denial remarks
```

---

## 🗄️ Database Changes

### **New Agenda Status:**
- ✅ **DRAFT** - Being prepared by admin
- ✅ **PROPOSED** - Awaiting admin approval ← NEW!
- ✅ **PUBLISHED** - Approved and visible to all
- ✅ **COMPLETED** - Meeting finished
- ✅ **CANCELLED** - Meeting cancelled or proposal denied

### **New Fields in `agendas` Table:**

| Field | Type | Description |
|-------|------|-------------|
| `proposed_by` | BIGINT | User who proposed (FK to users.id) |
| `proposed_at` | TIMESTAMP | When proposed |
| `approved_by` | BIGINT | Admin who approved (FK to users.id) |
| `approved_at` | TIMESTAMP | When approved |
| `denied_by` | BIGINT | Admin who denied (FK to users.id) |
| `denied_at` | TIMESTAMP | When denied |
| `denial_remarks` | TEXT | Admin's reason for denial |

---

## 📡 Backend API Endpoints

### **1. Create Agenda (Modified)**
```
POST /api/v1/agendas
Body: {
  "mrfc_id": 1,
  "quarter_id": 4,
  "meeting_date": "2025-12-15",
  "meeting_time": "09:00:00",
  "location": "MGB Office"
}

Behavior:
- USER role → status = PROPOSED, proposed_by = user ID
- ADMIN role → status = DRAFT (or specified status)
```

### **2. Get Pending Proposals**
```
GET /api/v1/agendas/pending-proposals
Authorization: ADMIN only

Response: [
  {
    "id": 5,
    "status": "PROPOSED",
    "mrfc": { "name": "MRFC 1" },
    "meeting_date": "2025-12-15",
    "location": "MGB Office",
    "proposed_by_user": {
      "full_name": "Kenneth Dev"
    }
  }
]
```

### **3. Approve Proposal**
```
POST /api/v1/agendas/:id/approve
Authorization: ADMIN only

Result:
- status changed to PUBLISHED
- approved_by = admin ID
- approved_at = current timestamp
```

### **4. Deny Proposal**
```
POST /api/v1/agendas/:id/deny
Authorization: ADMIN only

Body: {
  "denial_remarks": "Meeting date conflicts with existing schedule"
}

Result:
- status changed to CANCELLED
- denied_by = admin ID
- denied_at = current timestamp
- denial_remarks = admin's message
```

---

## 📱 Android UI Components

### **New Files Created:**

1. **PendingProposalsActivity.kt**
   - Screen for admins to review proposals
   - RecyclerView with proposal cards
   - Approve/Deny buttons
   - Empty state handling

2. **activity_pending_proposals.xml**
   - Material Design 3 layout
   - Toolbar with back button
   - RecyclerView for proposals
   - Loading indicator
   - Empty state message

3. **item_agenda_proposal.xml**
   - Proposal card layout
   - MRFC name (bold, large text)
   - Meeting details with icons
   - Proposed by user (italic)
   - Approve button (green)
   - Deny button (red outline)

4. **dialog_deny_proposal.xml**
   - Beautiful denial dialog
   - Title and instructions
   - TextInputLayout with multi-line input
   - Cancel/Deny buttons

### **Updated Files:**

5. **ProponentDetailActivity.kt**
   - Added role-based File Upload button visibility
   - Hidden for USER role

6. **AgendaDto.kt**
   - Added 7 new fields
   - Added SimpleUserDto
   - Added DenyProposalRequest
   - Helper properties for UI

7. **AgendaApiService.kt**
   - getPendingProposals()
   - approveProposal()
   - denyProposal()

8. **AgendaRepository.kt**
   - 3 new repository methods

9. **AgendaViewModel.kt**
   - 3 new ViewModel methods

---

## 🧪 Testing Checklist

### **Test 1: File Upload Restriction**
- [ ] Login as regular user
- [ ] Navigate to any Proponent Detail
- [ ] Verify File Upload button is HIDDEN
- [ ] Login as admin
- [ ] Verify File Upload button is VISIBLE

### **Test 2: Create Proposal (Regular User)**
- [ ] Login as regular user (kennethayade)
- [ ] Create new agenda for assigned MRFC
- [ ] Verify agenda saved with status = PROPOSED
- [ ] Agenda not visible in regular meeting list yet

### **Test 3: Review Proposals (Admin)**
- [ ] Login as admin/superadmin
- [ ] Open "Pending Proposals" from dashboard
- [ ] See list of proposed agendas
- [ ] Verify shows MRFC name, date, location, proposer

### **Test 4: Approve Proposal**
- [ ] Click "Approve" on a proposal
- [ ] Confirm approval
- [ ] Verify success message
- [ ] Proposal removed from pending list
- [ ] Check agenda now appears in published meetings

### **Test 5: Deny Proposal**
- [ ] Click "Deny" on a proposal
- [ ] Dialog appears asking for remarks
- [ ] Try to submit without remarks → Error shown
- [ ] Enter remarks (e.g., "Date conflicts")
- [ ] Click "Deny Proposal"
- [ ] Verify success message
- [ ] Proposal removed from pending list

### **Test 6: User Sees Denial**
- [ ] Login as the user who proposed
- [ ] Navigate to their proposals
- [ ] Verify denied proposal shows status = CANCELLED
- [ ] Verify denial remarks are visible to user

---

## 🎨 UI Design Details

### **Proposal Card Design:**
```
┌────────────────────────────────────────┐
│ MRFC 1                           [CARD] │
│                                          │
│ 📅 Dec 15, 2025                          │
│ 📍 MGB Region 7 Office                   │
│ 👤 Proposed by: Kenneth Dev              │
│                                          │
│ [  APPROVE  ] [    DENY    ]             │
│   (Green)      (Red outline)             │
└────────────────────────────────────────┘
```

### **Deny Dialog Design:**
```
┌────────────────────────────────────────┐
│ Deny Agenda Proposal                    │
│                                          │
│ Please provide a reason for denying     │
│ this proposal. The user will see your   │
│ remarks.                                 │
│                                          │
│ ┌────────────────────────────────────┐  │
│ │ Denial Remarks                     │  │
│ │                                    │  │
│ │ [Multi-line text input...]         │  │
│ │                                    │  │
│ └────────────────────────────────────┘  │
│                                          │
│ [  CANCEL  ]  [  DENY PROPOSAL  ]        │
│   (Outline)      (Red background)        │
└────────────────────────────────────────┘
```

---

## 🔗 Integration Points

### **Admin Dashboard Integration:**
```kotlin
// Add this card to Admin Dashboard
findViewById<MaterialCardView>(R.id.cardPendingProposals).setOnClickListener {
    startActivity(Intent(this, PendingProposalsActivity::class.java))
}
```

**Dashboard Card Should Show:**
- 📋 Icon (proposal/document icon)
- "Pending Proposals" title
- Badge with count (if > 0)
- Arrow indicator

---

## 📊 Status Flow Diagram

```
Regular User Creates Agenda
         ↓
    [PROPOSED]
         ↓
    Admin Reviews
         ↓
    ┌─────┴─────┐
    ↓           ↓
[APPROVE]   [DENY]
    ↓           ↓
[PUBLISHED] [CANCELLED]
    ↓           ↓
  User can    User sees
  access     denial remarks
```

---

## ⚠️ Important Notes

1. **Denial Remarks are REQUIRED** - Admin cannot deny without providing a reason
2. **Only PROPOSED agendas can be approved/denied** - Safety check in backend
3. **Proposals include proposer information** - Admins know who suggested the meeting
4. **File Upload is admin-only** - Regular users cannot upload documents
5. **Migration 007 must be run** - Database schema updated

---

## 🚀 Deployment Checklist

- [x] Database migration 007 applied
- [x] Backend routes updated
- [x] Agenda model updated with new fields
- [x] Android DTOs updated
- [x] Repository methods implemented
- [x] ViewModel methods implemented
- [x] UI layouts created
- [x] Activity created
- [x] Zero linter errors
- [ ] Test on device
- [ ] Deploy to production

---

## 📝 Next Steps for Admin

1. **Add Pending Proposals Card to Dashboard:**
   - Update `activity_admin_dashboard.xml`
   - Add card with icon and badge
   - Link to PendingProposalsActivity

2. **Test the Complete Flow:**
   - Have a regular user propose an agenda
   - Review as admin
   - Try both approve and deny
   - Verify user sees results

3. **Configure Notifications (Future):**
   - Notify admins when new proposal submitted
   - Notify users when proposal approved/denied

---

## 🎉 Summary

**What You Can Do Now:**

✅ **Regular users can propose meeting agendas** - No need for admin to create everything  
✅ **Admins control what gets published** - Maintains quality and prevents conflicts  
✅ **Denial feedback system** - Users understand why proposals were rejected  
✅ **File upload properly restricted** - Only admins can upload sensitive documents  
✅ **Clean, intuitive UI** - Beautiful Material Design 3 interface  
✅ **Full audit trail** - Track who proposed, who approved, who denied, and when  

---

**This feature significantly improves the workflow and empowers regular users while maintaining admin oversight!** 🎊


