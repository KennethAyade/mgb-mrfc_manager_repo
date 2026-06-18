package com.mgb.mrfcmanager

import com.mgb.mrfcmanager.data.remote.dto.QuarterDto
import com.mgb.mrfcmanager.util.QuarterLoadState
import com.mgb.mrfcmanager.util.QuarterSelectionResolver
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class QuarterSelectionResolverTest {
    private fun quarter(
        id: Long,
        year: Int,
        number: Int,
        current: Boolean = false
    ) = QuarterDto(
        id = id,
        name = "Q$number $year",
        year = year,
        quarterNumber = number,
        startDate = "$year-01-01",
        endDate = "$year-12-31",
        isCurrent = current
    )

    @Test
    fun selectsCurrentYearAndServerCurrentQuarter() {
        val state = QuarterSelectionResolver.resolve(
            quarters = (1..4).map { quarter(it.toLong(), 2026, it, it == 2) },
            currentYear = 2026,
            currentQuarterNumber = 2
        ) as QuarterLoadState.Available

        assertEquals(2026, state.selectedYear)
        assertEquals(2, state.selectedQuarter?.quarterNumber)
    }

    @Test
    fun fallsBackToNewestHistoricalYear() {
        val state = QuarterSelectionResolver.resolve(
            quarters = (1..4).map { quarter(it.toLong(), 2025, it) },
            currentYear = 2026,
            currentQuarterNumber = 2
        ) as QuarterLoadState.Available

        assertEquals(2025, state.selectedYear)
    }

    @Test
    fun preservesPreferredQuarterAcrossYears() {
        val quarters = listOf(
            quarter(1, 2025, 1),
            quarter(2, 2025, 2),
            quarter(5, 2026, 1),
            quarter(6, 2026, 2, true)
        )
        val state = QuarterSelectionResolver.resolve(
            quarters,
            currentYear = 2026,
            currentQuarterNumber = 2,
            preferredQuarterId = 2
        ) as QuarterLoadState.Available

        assertEquals(2025, state.selectedYear)
        assertEquals(2L, state.selectedQuarter?.id)
    }

    @Test
    fun returnsEmptyStateForNoQuarters() {
        val state = QuarterSelectionResolver.resolve(
            emptyList(),
            currentYear = 2026,
            currentQuarterNumber = 2
        )

        assertTrue(state is QuarterLoadState.Empty)
    }
}
