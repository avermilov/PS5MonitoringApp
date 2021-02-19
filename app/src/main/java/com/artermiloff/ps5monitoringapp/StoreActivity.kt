package com.artermiloff.ps5monitoringapp

import android.os.Bundle
import android.text.SpannableString
import android.text.method.LinkMovementMethod
import android.text.util.Linkify
import android.view.Gravity
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Button
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
    private var webContent: String? = null
    private var refreshButton: Button? = null
    private var getStatusButton: Button? = null
    private var dataRefreshed = false
    private var storeInfos: MutableList<StoreInfo> = mutableListOf()
    private var myListView: ListView? = null
    private var customAdapter: CustomAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        refreshButton = findViewById(R.id.refreshButton)
        getStatusButton = findViewById(R.id.getStatusButton)
        myListView = findViewById(R.id.listView)

        customAdapter = CustomAdapter(applicationContext, storeInfos)
        myListView!!.adapter = customAdapter


        requestContent(ps5statusLink)
        refreshButton!!.setOnClickListener {
            refreshContent()
        }

        getStatusButton!!.setOnClickListener {
            fetchContent()
        }

    }

    private fun parse(content: String): List<StoreInfo> {
        val doc = Jsoup.parse(content)
        val elements = doc.getElementsByClass("table-data")
        val infos = mutableListOf<StoreInfo>()
        for (el in elements) {
            val isDiskEd = el.child(0).text().trim().toUpperCase(Locale.ROOT) == "PS5"
            if (!isDiskEd) {
                continue
            }
            val status = el.child(0).className().split(" ")[1].replace("-", " ")
            val lastStocked = el.child(1).child(0).text().split(" ", limit = 3)[2]
            val lastChanged = el.child(1).child(1).text().split(" ", limit = 2)[1]
            val link = el.attr("href")
            val name = link.substring(link.lastIndexOf("/") + 1)
            var imgId = resources.getIdentifier(name, "drawable", packageName)
            infos.add(StoreInfo(name, link, status, lastStocked, lastChanged, imgId))
            println(infos.last())
        }
        return infos
    }

    private fun requestContent(url: String) {
        val q = Volley.newRequestQueue(this)
        val stringRequest =
            StringRequest(
                Request.Method.GET, url, { response -> webContent = response },
                {
                    Toast.makeText(
                        applicationContext,
                        "Unable to fetch data. Try again later or check your internet connection.",
                        Toast.LENGTH_LONG
                    ).show()
                    println(it.message)
                })
        q.add(stringRequest)
    }

    private fun refreshContent() {
        if (webContent != null) {
            storeInfos.clear()
            storeInfos.addAll(parse(webContent!!))
            customAdapter!!.notifyDataSetChanged()
            dataRefreshed = true
            refreshButton!!.visibility = View.INVISIBLE
            getStatusButton!!.visibility = View.VISIBLE
        }
    }

    private fun fetchContent() {
        val toast =
            Toast.makeText(
                applicationContext,
                "Press the 'Refresh' button to update the store list.",
                Toast.LENGTH_SHORT
            )
        toast.setGravity(Gravity.CENTER, 0, 0)
        toast.show()
        requestContent(ps5statusLink)
        refreshButton!!.visibility = View.VISIBLE
        storeInfos.clear()
        dataRefreshed = false
        customAdapter!!.notifyDataSetChanged()
        getStatusButton!!.visibility = View.INVISIBLE
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
                        "All rights taken from us. 2021-2077.", "OK"
            )
            R.id.show_help -> createAndShowDialog(
                "How to use",
                "This is an app for getting up to date info on the stock availability of the disk version of PS5.\r\n\n" +
                        "To refresh the current update request, press the \'Refresh\' button.\r\n\n" +
                        "To send another update request, press the \'Get Status\' button.",
                "GOT IT"
            )
            R.id.show_links -> createAndShowDialog(
                "Store Links",
                "List of stores currently being tracked and their links",
                "OK", storeInfos
            )
        }
        return super.onOptionsItemSelected(item)
    }

    private fun createAndShowDialog(
        title: String,
        message: String,
        buttonText: String,
        stores: List<StoreInfo> = listOf(),
    ) {
        val dialog = AlertDialog.Builder(this)
        var mes = message
        if (stores.isNotEmpty()) {
            for (store in stores) {
                mes += "\n${store.name}:\n${store.link}"
            }
        }
        var textView = TextView(this)

        val spannableString = SpannableString(mes)
        Linkify.addLinks(spannableString, Linkify.WEB_URLS)
        textView.text = spannableString
        textView.movementMethod = LinkMovementMethod.getInstance()
//        dialog.setMessage(mes)
        dialog.setTitle(title)
        dialog.setIcon(R.mipmap.ic_launcher_round)
        dialog.setNeutralButton(buttonText) { dia, _ -> dia.dismiss() }
        dialog.setView(textView)
        val alertDialog = dialog.create()
        alertDialog.show()
    }
}