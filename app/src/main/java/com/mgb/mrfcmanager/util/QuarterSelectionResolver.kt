package com.mgb.mrfcmanager.util

import com.mgb.mrfcmanager.data.remote.dto.QuarterDto

sealed class QuarterLoadState {
    object Loading : QuarterLoadState()
    data class Available(
        val quarters: List<QuarterDto>,
        val years: List<Int>,
        val selectedYear: Int,
        val selectedQuarter: QuarterDto?
    ) : QuarterLoadState()
    data class Empty(val requestedYear: Int) : QuarterLoadState()
    data class Failed(val message: String) : QuarterLoadState()
}

object QuarterSelectionResolver {
    fun resolve(
        quarters: List<QuarterDto>,
        currentYear: Int,
        currentQuarterNumber: Int,
        preferredQuarterId: Long? = null
    ): QuarterLoadState {
        if (quarters.isEmpty()) {
            return QuarterLoadState.Empty(currentYear)
        }

        val sorted = quarters.sortedWith(
            compareByDescending<QuarterDto> { it.year }
                .thenBy { it.quarterNumber }
        )
        val years = sorted.map { it.year }.distinct()
        val preferred = preferredQuarterId?.let { id -> sorted.find { it.id == id } }
        val selectedYear = preferred?.year
            ?: currentYear.takeIf { it in years }
            ?: years.first()
        val yearQuarters = sorted.filter { it.year == selectedYear }
        val selectedQuarter = preferred
            ?: yearQuarters.find { it.isCurrent }
            ?: currentQuarterNumber
                .takeIf {
                    selectedYear == currentYear &&
                        yearQuarters.map { quarter -> quarter.quarterNumber }.toSet() == setOf(1, 2, 3, 4)
                }
                ?.let { number -> yearQuarters.find { it.quarterNumber == number } }
            ?: yearQuarters.firstOrNull()

        return QuarterLoadState.Available(
            quarters = sorted,
            years = years,
            selectedYear = selectedYear,
            selectedQuarter = selectedQuarter
        )
    }

    fun quartersForYear(state: QuarterLoadState.Available, year: Int): List<QuarterDto> =
        state.quarters.filter { it.year == year }.sortedBy { it.quarterNumber }
}

