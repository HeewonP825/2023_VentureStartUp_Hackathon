package com.hackathon.gameselect

import retrofit2.Call
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Query

interface YouTubeApiService {
    @GET("youtube/v3/subscriptions")
    fun getSubscriptions(
        @Query("part") part: String,
        @Query("mine") mine: Boolean = true,
        @Query("key") apiKey: String,
        @Header("Authorization") accessToken: String
    ): Call<SubscriptionResponse>
}