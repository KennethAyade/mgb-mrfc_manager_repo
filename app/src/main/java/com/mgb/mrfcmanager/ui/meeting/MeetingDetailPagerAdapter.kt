package com.mgb.mrfcmanager.ui.meeting

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.mgb.mrfcmanager.ui.meeting.fragments.AgendaFragment
import com.mgb.mrfcmanager.ui.meeting.fragments.AttendanceFragment
import com.mgb.mrfcmanager.ui.meeting.fragments.MinutesFragment
import com.mgb.mrfcmanager.ui.meeting.fragments.ProposalsFragment
import com.mgb.mrfcmanager.ui.meeting.fragments.VoiceRecordingFragment

/**
 * ViewPager adapter for Meeting Detail tabs
 * Manages 5 fragments: Attendance, Agenda, Proposals, Minutes, Recordings
 */
class MeetingDetailPagerAdapter(
    activity: FragmentActivity,
    private val agendaId: Long,
    private val mrfcId: Long
) : FragmentStateAdapter(activity) {

    override fun getItemCount(): Int = 5

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> AttendanceFragment.newInstance(agendaId, mrfcId)
            1 -> AgendaFragment.newInstance(agendaId, mrfcId)
            2 -> ProposalsFragment.newInstance(agendaId)
            3 -> MinutesFragment.newInstance(agendaId, mrfcId)
            4 -> VoiceRecordingFragment.newInstance(agendaId, mrfcId)
            else -> AttendanceFragment.newInstance(agendaId, mrfcId)
        }
    }
}
