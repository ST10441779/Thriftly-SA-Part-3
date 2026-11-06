package com.example.thriftlysa

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

/**
 * Retrofit client. Configure THRIFTLY_API_BASE_URL in gradle.properties
 * for the host machine/server used during testing.
 */
object ApiClient {
    private val baseUrl: String = BuildConfig.API_BASE_URL

    val api: ApiService by lazy {
        require(baseUrl.startsWith("https://") || baseUrl.startsWith("http://")) {
            "THRIFTLY_API_BASE_URL must be an HTTP(S) URL."
        }
        require(baseUrl.endsWith("/")) {
            "THRIFTLY_API_BASE_URL must end with /."
        }
        Retrofit.Builder()
            .baseUrl(baseUrl)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }
}
