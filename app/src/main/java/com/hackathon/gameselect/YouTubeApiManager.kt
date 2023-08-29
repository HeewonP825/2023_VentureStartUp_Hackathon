package com.hackathon.gameselect

import retrofit2.Callback
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class YouTubeApiManager(private val apiKey: String) {
    private val apiService: YouTubeApiService

    init {
        val retrofit = Retrofit.Builder()
            .baseUrl("https://www.googleapis.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        apiService = retrofit.create(YouTubeApiService::class.java)
    }

    fun getSubscriptions(callback: Callback<SubscriptionResponse>, tokenId: String) {
        val accessToken = tokenId  // 사용자의 Google ID Token (액세스 토큰)
        apiService.getSubscriptions("snippet", apiKey = apiKey, accessToken = "Bearer $accessToken")
            .enqueue(callback)
    }

}
