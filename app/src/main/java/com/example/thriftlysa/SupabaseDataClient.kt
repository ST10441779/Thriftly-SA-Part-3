
package com.example.thriftlysa

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object SupabaseDataClient {

    val api: SupabaseListingsApi by lazy {

        Retrofit.Builder()
            .baseUrl(
                BuildConfig.SUPABASE_URL.trimEnd('/') + "/"
            )
            .addConverterFactory(
                GsonConverterFactory.create()
            )
            .build()
            .create(SupabaseListingsApi::class.java)
    }
}
