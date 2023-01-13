package com.bidaappscoreboard.ui

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import com.bidaappscoreboard.CustomerPlaceQuery
import com.bidaappscoreboard.R


class EstablishmentAdapter(private val context: Context, establishmentList: List<CustomerPlaceQuery.Customer_place>?) : BaseAdapter() {

    private val itemList: List<CustomerPlaceQuery.Customer_place>? = establishmentList

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
    override fun getView(i: Int, view: View?, viewGroup: ViewGroup): View {
        val rootView: View = LayoutInflater.from(context)
            .inflate(R.layout.item_establishment, viewGroup, false)
        val txtName = rootView.findViewById<TextView>(R.id.name)

        txtName.text = itemList!![i].name
        return rootView
    }

}