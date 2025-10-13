package com.example.wirelesslocationstud.data.remote.api

import com.example.wirelesslocationstud.data.remote.model.MapCellResponse
import com.example.wirelesslocationstud.data.remote.model.MapSizeResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface WirelessMapApi {
    @GET("size")
    suspend fun getMapSize(): MapSizeResponse

    @GET("wilibox-column")
    suspend fun getMapColumn(@Query("x") x: Int): List<MapCellResponse>
}

