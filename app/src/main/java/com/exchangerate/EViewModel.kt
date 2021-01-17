package com.exchangerate

import android.app.Application
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.text.TextUtils
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.exchangerate.constants.AppConstants
import com.exchangerate.repository.NetworkRepository
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*


class EViewModel(application: Application) : AndroidViewModel(application) {
    val mCurrencyListResult = MutableLiveData<LinkedHashMap<String, String>>()
    val mCountryListResult = MutableLiveData<LinkedHashMap<String, String>>()
    val mConvertResult = MutableLiveData<Float>()
    val mErrorResult = MutableLiveData<String>()
    val currencyMap = LinkedHashMap<String, String>()
    val countryMap = LinkedHashMap<String, String>()
    private val mapType = object : TypeToken<LinkedHashMap<String, String>>() {}.type
    private val mSharedPreferences =
            application.getSharedPreferences(AppConstants.app_pref, Context.MODE_PRIVATE)
    private val INTERVAL = 1800000L

    fun currencyRateList() {
        val timeDifference = System.currentTimeMillis() - mSharedPreferences.getLong(
            AppConstants.last_refresh_time, System.currentTimeMillis())
        if (timeDifference < INTERVAL && !TextUtils.isEmpty(
                mSharedPreferences.getString(AppConstants.quotes, ""))) {
            mCurrencyListResult.postValue(
                Gson().fromJson(mSharedPreferences.getString(AppConstants.quotes, ""), mapType))
            refreshCurrencyRateList(timeDifference)
            return
        }
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val response = NetworkRepository.instance()
                    .rateList(getApplication<Application>().getString(R.string.api_key))

                if (response.get(AppConstants.success).asBoolean) {
                    val quotes = response.getAsJsonObject(AppConstants.quotes)
                    mCurrencyListResult.postValue(Gson().fromJson(quotes, mapType))
                    mSharedPreferences.edit()
                        .putLong(AppConstants.last_refresh_time, System.currentTimeMillis()).apply()
                    mSharedPreferences.edit().putString(AppConstants.quotes, quotes.toString())
                        .apply()
                } else {
                    val error = response.getAsJsonObject(AppConstants.error)
                    mErrorResult.postValue(error.get(AppConstants.info).asString)
                }

            } catch (e: Exception) {
                e.printStackTrace()
                mErrorResult.postValue(e.message)
            }
            refreshCurrencyRateList(INTERVAL)
        }
    }

    fun countryList() {
        if (!TextUtils.isEmpty(mSharedPreferences.getString(AppConstants.currencies, ""))) {
            mCountryListResult.postValue(
                Gson().fromJson(mSharedPreferences.getString(AppConstants.currencies, ""), mapType))
            return
        }
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val response = NetworkRepository.instance()
                    .countryList(getApplication<Application>().getString(R.string.api_key))
                if (response.get(AppConstants.success).asBoolean) {
                    val currencies = response.getAsJsonObject(AppConstants.currencies)
                    mCountryListResult.postValue(Gson().fromJson(currencies, mapType))
                    mSharedPreferences.edit()
                        .putString(AppConstants.currencies, currencies.toString()).apply()
                } else {
                    val error = response.getAsJsonObject(AppConstants.error)
                    mErrorResult.postValue(error.get(AppConstants.info).asString)
                }
            } catch (e: Exception) {
                e.printStackTrace()
                mErrorResult.postValue(e.message)
            }
        }
    }

    private fun refreshCurrencyRateList(interval: Long) {
        val mHandler = Handler(Looper.getMainLooper())
        mHandler.postDelayed({ currencyRateList() }, interval)
    }

    fun convert(amount: Float, from: String, to: String) {
        val fromAmt = from.toFloat()
        val toAmt = to.toFloat()
        mConvertResult.postValue(toAmt * amount / fromAmt)

    }
}