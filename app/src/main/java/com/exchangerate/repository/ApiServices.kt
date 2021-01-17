package com.exchangerate.repository

import com.google.gson.JsonObject
import retrofit2.http.GET
import retrofit2.http.Query

interface ApiServices {
    @GET("live?format=1")
    suspend fun live(@Query("access_key") access_key: String): JsonObject

    @GET("list?format=1")
    suspend fun list(@Query("access_key") access_key: String): JsonObject
}