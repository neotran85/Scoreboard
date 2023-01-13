package com.bidaappscoreboard.ui.findAnOppnent

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import com.bidaappscoreboard.CustomerPlaceQuery
import com.bidaappscoreboard.R

class LimitEstablishAdapter(private val context: Context, establishmentList: List<String>?) : BaseAdapter() {

    private val itemList: List<String>? = establishmentList

    override fun getCount(): Int {
        return itemList?.size ?: 0
    }

    override fun getItem(i: Int): Any {
        return i
    }

    override fun getItemId(i: Int): Long {
        return i.toLong()
    }

    @SuppressLint("ViewHolder")
    override fun getView(i: Int, view: View?, viewGroup: ViewGroup?): View {
        val rootView: View = LayoutInflater.from(context)
            .inflate(R.layout.item_establish, viewGroup, false)
        val txtName = rootView.findViewById<TextView>(R.id.name)

        txtName.text = itemList!![i]
        return rootView
    }
}