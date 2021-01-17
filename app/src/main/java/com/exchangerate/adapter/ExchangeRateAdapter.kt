package com.exchangerate.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.exchangerate.R
import kotlinx.android.synthetic.main.item_currency_rate.view.*

class ExchangeRateAdapter(private val currencyMap: LinkedHashMap<String, String>,
                          private val countryMap: LinkedHashMap<String, String>) :
        RecyclerView.Adapter<ExchangeRateAdapter.CHolder>() {
    val keys: ArrayList<String> =
            if (currencyMap.isEmpty()) ArrayList() else currencyMap.keys.toList() as ArrayList<String>

    class CHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CHolder {
        return CHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.item_currency_rate, parent, false))
    }

    override fun onBindViewHolder(holder: CHolder, position: Int) {
        val key = keys.get(position)
        val countryKey =key.removeRange(0,3)
        holder.itemView.tvName.setText(countryMap.get(countryKey))
        holder.itemView.tvRate.setText("$" + currencyMap.get(key))
        holder.itemView.setOnClickListener {
            currencyMap.get(key)?.let { it1 ->
                countryMap.get(countryKey)?.let { it2 ->
                    mOnItemClickListener?.onItemClicked(it2, it1)
                }
            }
        }
    }

    fun updateKeys(newKeys: List<String>) {
        this.keys.clear()
        this.keys.addAll(newKeys)
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int {
        return keys.size
    }

    private var mOnItemClickListener: OnItemClickListener? = null
    fun onItemClick(onItemClickListener: OnItemClickListener) {
        this.mOnItemClickListener = onItemClickListener
    }

    interface OnItemClickListener {
        fun onItemClicked(countryName: String, rate: String)
    }
}