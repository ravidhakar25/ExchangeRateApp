package com.exchangerate.repository

import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.net.ssl.HostnameVerifier

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
                    retrofit = Retrofit.Builder()
                        .baseUrl("https://api.currencylayer.com")
                        .client(okHttpClient)
                        .addConverterFactory(GsonConverterFactory.create())
                        .build()
                }
            }
            return retrofit!!
        }

        private fun initHttpClient() {
            val builder = OkHttpClient().newBuilder()
                .hostnameVerifier(HostnameVerifier { s, sslSession -> true })
                .connectTimeout(REQUEST_TIMEOUT, TimeUnit.SECONDS)
                .readTimeout(REQUEST_TIMEOUT, TimeUnit.SECONDS)
                .writeTimeout(REQUEST_TIMEOUT, TimeUnit.SECONDS)
                .retryOnConnectionFailure(true)
                .followRedirects(true)
                .followSslRedirects(true)
            okHttpClient = builder.build()
        }
    }

    init {
        if (retrofit != null) {
            throw RuntimeException("Use single instance")
        }
    }
}