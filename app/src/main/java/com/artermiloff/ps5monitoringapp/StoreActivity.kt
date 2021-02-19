package com.artermiloff.ps5monitoringapp

import android.os.Bundle
import android.text.SpannableString
import android.text.method.LinkMovementMethod
import android.text.util.Linkify
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
    private var dataFetched = false
    private var storeInfos: MutableList<StoreInfo> = mutableListOf()
    private var myListView: ListView? = null
    private var customAdapter: CustomAdapter? = null
    private var timer = Timer()
    private var updateInterval: Long = 5_000;

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

    private fun parse(content: String): List<StoreInfo> {
        val doc = Jsoup.parse(content)
        val elements = doc.getElementsByClass("table-data")
        val infos = mutableListOf<StoreInfo>()
        for (el in elements) {
            val isDiskEd = el.child(0).text().trim().toUpperCase(Locale.ROOT) == "PS5"
            if (!isDiskEd) {
                continue
            }
            val status = el.child(0).className().split(" ")[1].replace("-", " ").trim()
            val lastStocked = el.child(1).child(0).text().split(" ", limit = 3)[2].trim()
            val lastChanged = el.child(1).child(1).text().split(" ", limit = 2)[1].trim()
            val link = el.attr("href").trim()
            val name = link.substring(link.lastIndexOf("/") + 1).trim()
            var imgId = resources.getIdentifier(name.replace("-", "_"), "drawable", packageName)
            infos.add(StoreInfo(name, link, status, lastStocked, lastChanged, imgId))
            println(infos.last())
        }
        return infos
    }

    private fun updateContent(url: String) {
        val q = Volley.newRequestQueue(this)
        dataFetched = false;
        val stringRequest =
            StringRequest(
                Request.Method.GET, url, { response ->
                    val newInfos = parse(response!!)
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
                    println(it.message)
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