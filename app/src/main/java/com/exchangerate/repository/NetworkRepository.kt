package com.exchangerate.repository

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.util.Base64
import com.google.gson.JsonObject
import java.nio.charset.Charset


class NetworkRepository {
    companion object {
        private var networkRepository: NetworkRepository? = null
        fun instance(): NetworkRepository {
            synchronized(this) {
                if (networkRepository == null) networkRepository = NetworkRepository()
                return networkRepository!!
            }
        }

        fun isNetworkAvailable(mContext: Context): Boolean {
            val connectivityManager =
                    mContext.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                try {
                    val networkInfo = connectivityManager.activeNetworkInfo
                    if (networkInfo != null) {
                        return networkInfo.isConnected
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                connectivityManager.activeNetwork?.let {
                    return hasCapability(connectivityManager.getNetworkCapabilities(it))
                }
            } else {
                val networkInfo = connectivityManager.activeNetworkInfo
                return networkInfo != null && networkInfo.isConnected
            }
            return false
        }

        private fun hasCapability(networkCapabilities: NetworkCapabilities?): Boolean {
            networkCapabilities?.let {
                return it.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            }
            return false
        }
    }

    init {
        if (networkRepository != null) {
            throw Exception("Use Single instance")
        }
    }

    suspend fun countryList(accessKey: String): JsonObject {
        return NetworkClient.instance().create(ApiServices::class.java)
            .list(String(Base64.decode(accessKey, Base64.DEFAULT), Charset.forName("UTF-8")))
    }

    suspend fun rateList(accessKey: String): JsonObject {
        return NetworkClient.instance().create(ApiServices::class.java)
            .live(String(Base64.decode(accessKey, Base64.DEFAULT), Charset.forName("UTF-8")))
    }
}