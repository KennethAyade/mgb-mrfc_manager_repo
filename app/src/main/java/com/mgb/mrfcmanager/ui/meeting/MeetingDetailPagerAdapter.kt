package com.mgb.mrfcmanager.ui.meeting

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.mgb.mrfcmanager.ui.meeting.fragments.AgendaFragment
import com.mgb.mrfcmanager.ui.meeting.fragments.AttendanceFragment
import com.mgb.mrfcmanager.ui.meeting.fragments.MinutesFragment
import com.mgb.mrfcmanager.ui.meeting.fragments.OtherMattersFragment
import com.mgb.mrfcmanager.ui.meeting.fragments.ProposalsFragment
import com.mgb.mrfcmanager.ui.meeting.fragments.VoiceRecordingFragment

/**
 * ViewPager adapter for Meeting Detail tabs
 * Manages fragments based on user role:
 * - Regular users: Attendance, Agenda, Other Matters, Minutes, Proposals (5 tabs)
 * - Admin users: Same + Recordings (6 tabs)
 *
 * Tab order:
 * 0: Attendance - Log attendance with photo
 * 1: Agenda - View approved agenda items
 * 2: Other Matters - Items added after main agenda finalized
 * 3: Minutes - Meeting minutes/notes
 * 4: Proposals - Propose/approve agenda items
 * 5: Recordings - Voice recordings (Admin only)
 */
class MeetingDetailPagerAdapter(
    activity: FragmentActivity,
    private val agendaId: Long,
    private val mrfcId: Long,
    private val tabCount: Int = 6  // Default 6 for admin (5 for regular users)
) : FragmentStateAdapter(activity) {

    override fun getItemCount(): Int = tabCount

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> AttendanceFragment.newInstance(agendaId, mrfcId)
            1 -> AgendaFragment.newInstance(agendaId, mrfcId)
            2 -> OtherMattersFragment.newInstance(agendaId, mrfcId)
            3 -> MinutesFragment.newInstance(agendaId, mrfcId)
            4 -> ProposalsFragment.newInstance(agendaId)
            5 -> VoiceRecordingFragment.newInstance(agendaId, mrfcId)
            else -> AttendanceFragment.newInstance(agendaId, mrfcId)
        }
    }
}
