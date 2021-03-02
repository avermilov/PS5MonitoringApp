package com.artermiloff.ps5monitoringapp

import android.os.Bundle
import android.text.Html
import android.text.method.LinkMovementMethod
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.CheckBox
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.Request
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import org.jsoup.Jsoup
import java.util.*

class StoreActivity : AppCompatActivity() {
    private val ps5statusLink = "https://ps5status.ru"
    private var dataFetched = false
    private var storesFetched: MutableList<StoreInfo> = mutableListOf()
    private var storeInfos: MutableList<StoreInfo> = mutableListOf()
    private var myListView: ListView? = null
    private var customAdapter: CustomAdapter? = null
    private var timer = Timer()
    private var updateInterval: Long = 5_000
    private var diskOnly = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        myListView = findViewById(R.id.listView)

        customAdapter = CustomAdapter(applicationContext, storeInfos)
        myListView!!.adapter = customAdapter

        timer.scheduleAtFixedRate(object : TimerTask() {
            override fun run() {
                updateContent(ps5statusLink)
            }
        }, 0, updateInterval)
    }

    private fun parse(content: String): MutableList<StoreInfo> {
        val doc = Jsoup.parse(content)
        val elements = doc.getElementsByClass("table-data")
        val infos = mutableListOf<StoreInfo>()
        for (el in elements) {
            val isDiskEd = "PS5" == el.child(0).text().trim().toUpperCase(Locale.ROOT)
            val status = el.child(0).className().split(" ")[1].replace("-", " ").trim()
            val lastStocked = el.child(1).child(0).text().split(" ", limit = 3)[2].trim()
            val lastChanged = el.child(1).child(1).text().split(" ", limit = 2)[1].trim()
            val link = el.attr("href").trim()
            val name = link.substring(link.lastIndexOf("/") + 1).trim()
            val imgId = resources.getIdentifier(name.replace("-", "_").replace("_digital", ""), "drawable", packageName)
            infos.add(StoreInfo(name, link, status, lastStocked, lastChanged, imgId, isDiskEd))
            Log.d("STORE", infos.last().toString())
        }
        return infos
    }

    private fun updateContent(url: String) {
        val q = Volley.newRequestQueue(this)
        dataFetched = false
        val stringRequest =
            StringRequest(
                Request.Method.GET, url, { response ->
                    storesFetched = parse(response!!)
//                    val checkbox = findViewById<CheckBox>(R.id.showDisk)
//                    Log.d("CHECKBOX", (checkbox == null).toString())
                    val newInfos =
                        storesFetched.filter { it.diskEdition == diskOnly }
                            .sortedBy { it.status != "not available" }
                    storeInfos.clear()
                    storeInfos.addAll(newInfos)
                    customAdapter!!.notifyDataSetChanged()
                },
                {
                    Toast.makeText(
                        applicationContext,
                        "Unable to fetch data. Try again later or check your internet connection.",
                        Toast.LENGTH_LONG
                    ).show()
                })
        q.add(stringRequest)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.show_info -> createAndShowDialog(
                "About the program", "PS5 Monitoring App version ${
                    packageManager.getPackageInfo(
                        packageName,
                        0
                    ).versionName
                }\r\n\n" +
                        "Author - Ermilov Artemiy Vasilevich\r\n\n" +
                        "All rights taken from us. 2021-2077.",
                "OK",
                icon = R.drawable.info
            )
            R.id.show_help -> createAndShowDialog(
                "How to use",
                "This is an app for getting up to date info on the stock availability of the disk version of PS5 in Moscow, Russia.\r\n\n" +
                        "The list shows each store status individually, including last date of status change, last update date, current availability," +
                        "and store name.\r\n\n" +
                        "If you would like to go to the product page of any store, simply open the optional menu, press \'Store Links\' and choose " +
                        "needed store.",
                "GOT IT",
                icon = R.drawable.qmark
            )
            R.id.show_links -> createAndShowDialog(
                "Store Links",
                "",
                "OK",
                storeInfos,
                R.drawable.cursor
            )
            R.id.showDisk -> {
                val newCheckStatus = !item.isChecked
                diskOnly = newCheckStatus
                item.isChecked = newCheckStatus
                val newInfos =
                    storesFetched.filter { it.diskEdition == diskOnly }
                        .sortedBy { it.status != "not available" }
                storeInfos.clear()
                storeInfos.addAll(newInfos)
                customAdapter!!.notifyDataSetChanged()

            }
        }
        return super.onOptionsItemSelected(item)
    }


    private fun createAndShowDialog(
        title: String,
        message: String,
        buttonText: String,
        stores: List<StoreInfo> = listOf(),
        icon: Int,
    ) {
        var mes = ""
        if (stores.isEmpty()) {
            mes = message
        } else {
            for (store in stores) {
                mes += "<a href=\"${store.link}\">${store.name}</a><br>"
            }
        }
        val d = AlertDialog.Builder(this)
            .setPositiveButton(buttonText, null)
            .setTitle(title)
            .setIcon(icon)
            .setMessage(if (stores.isEmpty()) mes else Html.fromHtml(mes)).create()
        d.show()

        (d.findViewById<TextView>(android.R.id.message) as TextView).movementMethod =
            LinkMovementMethod.getInstance()
    }
}