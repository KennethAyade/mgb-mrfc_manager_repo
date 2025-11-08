package com.mgb.mrfcmanager.data.remote.api

import com.mgb.mrfcmanager.data.remote.dto.ApiResponse
import com.mgb.mrfcmanager.data.remote.dto.QuarterDto
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

/**
 * Quarter API Service
 * Handles quarter-related API calls
 */
interface QuarterApiService {
    
    /**
     * Get all quarters with optional filtering
     * GET /quarters
     */
    @GET("quarters")
    suspend fun getQuarters(
        @Query("year") year: Int? = null,
        @Query("is_current") isCurrent: Boolean? = null,
        @Query("sort_order") sortOrder: String = "DESC"
    ): Response<ApiResponse<List<QuarterDto>>>
}

