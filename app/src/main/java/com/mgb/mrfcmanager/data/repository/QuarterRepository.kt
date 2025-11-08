package com.mgb.mrfcmanager.data.repository

import com.mgb.mrfcmanager.data.remote.api.QuarterApiService
import com.mgb.mrfcmanager.data.remote.dto.ApiResponse
import com.mgb.mrfcmanager.data.remote.dto.QuarterDto
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Calendar

/**
 * Quarter Repository
 * Handles quarter data operations
 */
class QuarterRepository(private val apiService: QuarterApiService) {
    
    /**
     * Get all quarters with optional year filter
     */
    suspend fun getQuarters(year: Int? = null): Result<List<QuarterDto>> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.getQuarters(year = year)
                
                if (response.isSuccessful) {
                    val body = response.body()
                    if (body?.success == true) {
                        Result.Success(body.data ?: emptyList())
                    } else {
                        Result.Error(body?.error?.message ?: "Failed to get quarters")
                    }
                } else {
                    Result.Error("HTTP ${response.code()}: ${response.message()}")
                }
            } catch (e: Exception) {
                Result.Error(e.localizedMessage ?: "Failed to get quarters")
            }
        }
    }
    
    /**
     * Get quarters for current year
     */
    suspend fun getCurrentYearQuarters(): Result<List<QuarterDto>> {
        val currentYear = Calendar.getInstance().get(Calendar.YEAR)
        return getQuarters(year = currentYear)
    }
    
    /**
     * Find quarter by quarter number and year
     */
    suspend fun findQuarterByNumberAndYear(quarterNumber: Int, year: Int): Result<QuarterDto?> {
        return when (val result = getQuarters(year = year)) {
            is Result.Success -> {
                val quarter = result.data.find { it.quarterNumber == quarterNumber }
                Result.Success(quarter)
            }
            is Result.Error -> Result.Error(result.message)
            is Result.Loading -> Result.Loading
        }
    }
}

