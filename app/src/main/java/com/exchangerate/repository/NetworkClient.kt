package com.exchangerate.repository

import com.exchangerate.BuildConfig
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

class NetworkClient {

    companion object {
        @Volatile
        private var retrofit: Retrofit? = null

        @Volatile
        private var okHttpClient: OkHttpClient? = null
        private var REQUEST_TIMEOUT = 60L
        fun instance(): Retrofit {
            synchronized(this) {
                if (okHttpClient == null) {
                    initHttpClient()
                }
                if (retrofit == null) {
                    retrofit = Retrofit.Builder().baseUrl("http://api.currencylayer.com/")
                        .client(okHttpClient).addConverterFactory(GsonConverterFactory.create())
                        .build()
                }
            }
            return retrofit!!
        }

        private fun initHttpClient() {
            val builder = OkHttpClient().newBuilder().hostnameVerifier { s, sslSession -> true }
                .connectTimeout(REQUEST_TIMEOUT, TimeUnit.SECONDS)
                .readTimeout(REQUEST_TIMEOUT, TimeUnit.SECONDS)
                .writeTimeout(REQUEST_TIMEOUT, TimeUnit.SECONDS).addInterceptor { chain ->
                    val requestBuilder =
                        chain.request().newBuilder().addHeader("Accept", "application/json")
                            .addHeader("Content-Type", "application/json");
                    chain.proceed(requestBuilder.build())
                }
            if (BuildConfig.DEBUG) {
                val interceptor = HttpLoggingInterceptor()
                interceptor.setLevel(HttpLoggingInterceptor.Level.BODY)
                builder.addInterceptor(interceptor)
            }
            okHttpClient = builder.build()
        }
    }

    init {
        if (retrofit != null) {
            throw RuntimeException("Use single instance")
        }
    }
}