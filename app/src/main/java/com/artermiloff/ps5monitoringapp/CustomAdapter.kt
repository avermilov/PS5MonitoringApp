package com.artermiloff.ps5monitoringapp

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView
import java.util.*

class CustomAdapter(private val context: Context, private val storeInfos: List<StoreInfo>) :
    BaseAdapter() {
    private class ViewHolder(row: View?) {
        var storeNameAndStatus: TextView = row?.findViewById(R.id.tvName) as TextView
        var storeLastShip: TextView = row?.findViewById(R.id.tvLastShip) as TextView
        var storeLastCheck: TextView = row?.findViewById(R.id.tvLastCheck) as TextView
        var ivImage: ImageView = row?.findViewById(R.id.ivStore) as ImageView
    }

    override fun getCount(): Int = storeInfos.size

    override fun getItem(position: Int): Any = storeInfos[position]

    override fun getItemId(position: Int): Long = position.toLong()

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val view: View?
        val viewHolder: ViewHolder
        if (convertView == null) {
            val layout = LayoutInflater.from(context)
            view = layout.inflate(R.layout.store_item_list, convertView, false)
            viewHolder = ViewHolder(view)
            view.tag = viewHolder
        } else {
            view = convertView
            viewHolder = view.tag as ViewHolder
        }

        println(viewHolder.storeNameAndStatus)

        val store: StoreInfo = getItem(position) as StoreInfo
        if(store.status.toLowerCase().endsWith("not available")){
            viewHolder.storeNameAndStatus.setTextColor(Color.RED)
        }else{
            viewHolder.storeNameAndStatus.setTextColor(Color.GREEN)
        }
        viewHolder.storeNameAndStatus.text = "${store.name}: ${store.status}"
        viewHolder.ivImage.setImageResource(store.imgCode)
        viewHolder.storeLastShip.text = "Last shipped: ${store.lastShip}"
        viewHolder.storeLastCheck.text = "Last checked: ${store.lastCheck}"

        return view as View
    }
}