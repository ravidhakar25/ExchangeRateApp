package com.exchangerate

import android.app.Activity
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.doAfterTextChanged
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.exchangerate.adapter.ExchangeRateAdapter
import com.exchangerate.repository.NetworkRepository
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.button.MaterialButton
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.a_exchange.*
import kotlinx.android.synthetic.main.dlg_select_currency.*

class ExchangeActivity : AppCompatActivity() {
    val mEViewModel: EViewModel by lazy { ViewModelProvider(this).get(EViewModel::class.java) }
    val mExchangeRateAdapter: ExchangeRateAdapter by lazy {
        ExchangeRateAdapter(mEViewModel.currencyMap, mEViewModel.countryMap)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.a_exchange)
        setGui()
        setListeners()
        fetchCountryList()
    }

    private fun setGui() {
        mExchangeRateView.layoutManager = LinearLayoutManager(this)
        mExchangeRateView.adapter = mExchangeRateAdapter
        mExchangeRateView.addItemDecoration(
            DividerItemDecoration(this, LinearLayoutManager.VERTICAL))
        amount.doAfterTextChanged { amount.setError(null) }
    }

    fun showExchangeRate(view: View) {
        view.isClickable = false
        view.postDelayed({ view.isClickable = true }, 250)
        val behavior = BottomSheetBehavior.from(cvCurrency)
        behavior.state =
                if (behavior.state == BottomSheetBehavior.STATE_EXPANDED) BottomSheetBehavior.STATE_COLLAPSED else BottomSheetBehavior.STATE_EXPANDED
    }

    private fun setListeners() {
        mEViewModel.mCurrencyListResult.observe(this, {
            mEViewModel.currencyMap.clear()
            mEViewModel.currencyMap.putAll(it)
            mExchangeRateAdapter.updateKeys(mEViewModel.currencyMap.keys.toList())
            mLoader.visibility = View.GONE
        })
        mEViewModel.mCountryListResult.observe(this, {
            mEViewModel.countryMap.clear()
            mEViewModel.countryMap.putAll(it)
            mEViewModel.currencyRateList()
        })
        mEViewModel.mErrorResult.observe(this, {
            mLoader.visibility = View.GONE
            showSnackbar(it)
        })
        mEViewModel.mConvertResult.observe(this, {
            result.scaleX = 0.3f
            result.scaleY = 0.3f
            result.animate().scaleX(1f).scaleY(1f).setDuration(500).start()
            result.visibility = View.VISIBLE
            result.setText("RESULT: ${it}")
        })
    }

    private fun fetchCountryList() {
        if (NetworkRepository.isNetworkAvailable(this)) {
            mEViewModel.countryList()
        } else showSnackbar(getString(R.string.no_internet))
    }

    fun showSnackbar(msg: String) {
        Snackbar.make(mRootView, msg, Snackbar.LENGTH_LONG).show()
    }

    fun selectCurrency(view: View) {
        if (mEViewModel.countryMap.isEmpty() || mEViewModel.currencyMap.isEmpty()) return
        val mBottomSheetDialog = BottomSheetDialog(this)
        mBottomSheetDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        mBottomSheetDialog.setContentView(R.layout.dlg_select_currency)
        mBottomSheetDialog.mCurrencyView.layoutManager = LinearLayoutManager(this)
        val adapter = ExchangeRateAdapter(currencyMap = mEViewModel.currencyMap,
                                          countryMap = mEViewModel.countryMap)
        mBottomSheetDialog.mCurrencyView.adapter = adapter
        mBottomSheetDialog.mCurrencyView.addItemDecoration(
            DividerItemDecoration(this, LinearLayoutManager.VERTICAL))
        adapter.onItemClick(object : ExchangeRateAdapter.OnItemClickListener {
            override fun onItemClicked(countryName: String, rate: String) {
                val mBtnView = findViewById<MaterialButton>(view.id)
                mBtnView.setText(countryName)
                mBtnView.setTag(rate)
                mBottomSheetDialog.dismiss()
            }
        })
        mBottomSheetDialog.show()
    }

    fun convert(view: View) {
        if (from.text.isNullOrEmpty()) {
            showSnackbar("Please select from currency")
            return
        }
        if (to.text.isNullOrEmpty()) {
            showSnackbar("Please select to currency")
            return
        }
        if (amount.text.isNullOrEmpty()) {
            amount.setError("Please enter amount")
            return
        }
        hideKeyboard()
        try {
            mEViewModel.convert(amount.text.toString().toFloat(), from.tag.toString(),
                                to.tag.toString())
        } catch (e: NumberFormatException) {
            amount.setError("Please enter valid amount")

        }

    }

    private fun hideKeyboard() {
        try {
            val imm = this.getSystemService(
                Context.INPUT_METHOD_SERVICE
                                               ) as InputMethodManager
            imm.hideSoftInputFromWindow(currentFocus?.windowToken, 0)
        } catch (e: Exception) {
        }
    }
}