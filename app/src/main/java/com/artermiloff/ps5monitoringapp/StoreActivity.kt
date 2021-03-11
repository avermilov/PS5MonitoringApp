package com.artermiloff.ps5monitoringapp

import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import android.os.Bundle
import android.text.Html
import android.text.method.LinkMovementMethod
import android.view.Menu
import android.view.MenuItem
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.android.volley.Request
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import java.util.*


class StoreActivity : AppCompatActivity() {
    private val ps5statusLink = "https://ps5status.ru"
    private var prevStoresFetched: MutableList<StoreInfo> = mutableListOf()
    private var storesFetched: MutableList<StoreInfo> = mutableListOf()
    private var storeInfos: MutableList<StoreInfo> = mutableListOf()
    private lateinit var myListView: ListView
    private lateinit var customAdapter: CustomAdapter
    private var timer = Timer()
    private var updateInterval: Long = 5_000
    private var diskOnly = true
    private var parser: Parser = PS5StatusParser(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        myListView = findViewById(R.id.listView)

        customAdapter = CustomAdapter(applicationContext, storeInfos)
        myListView.adapter = customAdapter

        timer.scheduleAtFixedRate(object : TimerTask() {
            override fun run() {
                updateContent()
            }
        }, 0, updateInterval)

        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.O) {
            val nc = NotificationChannel(
                "My Notification",
                "My Notification",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            val manager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(nc)
        }
    }

    private fun saveNewAndCheckForChanges(newStores: MutableList<StoreInfo>) {
        var notificationText = ""
        for (store in newStores) {
            val prevStore = prevStoresFetched.find { it.name == store.name }
            if (prevStore != null && prevStore.status != store.status) {
                notificationText += "New ${store.name} status: ${store.status}\n"
            }
        }

        if (notificationText.isNotEmpty()) {
            val builder = NotificationCompat.Builder(this, "My Notification")
            builder.setContentTitle("Changes in availability status!")
                .setContentText(notificationText)
                .setSmallIcon(R.drawable.cursor)
                .setAutoCancel(true)

            val managerCompat = NotificationManagerCompat.from(this)
            managerCompat.notify(1, builder.build())
        }

        prevStoresFetched = newStores
    }

    private fun updateContent() {
        val q = Volley.newRequestQueue(this)
        val stringRequest =
            StringRequest(
                Request.Method.GET, ps5statusLink, { response ->
                    storesFetched = parser.parse(response!!)
                    saveNewAndCheckForChanges(storesFetched)
                    val newInfos =
                        storesFetched.filter { it.diskEdition == diskOnly }
                            .sortedBy { it.status != "not available" }
                    storeInfos.clear()
                    storeInfos.addAll(newInfos)
                    customAdapter.notifyDataSetChanged()
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
                "This is an app for getting up to date info on the stock availability of the PS5 console in Moscow, Russia.\r\n\n" +
                        "The list shows each store status individually, including last date of status change, last refresh date, current availability," +
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
                customAdapter.notifyDataSetChanged()

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