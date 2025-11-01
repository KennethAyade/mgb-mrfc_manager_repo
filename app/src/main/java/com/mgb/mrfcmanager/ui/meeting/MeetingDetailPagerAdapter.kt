package com.mgb.mrfcmanager.ui.meeting

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.mgb.mrfcmanager.ui.meeting.fragments.AgendaFragment
import com.mgb.mrfcmanager.ui.meeting.fragments.AttendanceFragment
import com.mgb.mrfcmanager.ui.meeting.fragments.MinutesFragment

/**
 * ViewPager adapter for Meeting Detail tabs
 * Manages 3 fragments: Agenda, Attendance, Minutes
 */
class MeetingDetailPagerAdapter(
    activity: FragmentActivity,
    private val agendaId: Long,
    private val mrfcId: Long
) : FragmentStateAdapter(activity) {

    override fun getItemCount(): Int = 3

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> AgendaFragment.newInstance(agendaId, mrfcId)
            1 -> AttendanceFragment.newInstance(agendaId, mrfcId)
            2 -> MinutesFragment.newInstance(agendaId, mrfcId)
            else -> AgendaFragment.newInstance(agendaId, mrfcId)
        }
    }
}
