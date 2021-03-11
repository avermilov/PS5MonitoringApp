package com.artermiloff.ps5monitoringapp

import android.content.Context
import org.jsoup.Jsoup
import java.util.*

class PS5StatusParser(private val context: Context) : Parser {

    override fun parse(content: String): MutableList<StoreInfo> {
        val doc = Jsoup.parse(content)
        val elements = doc.getElementsByClass("table-data")
        val infos = mutableListOf<StoreInfo>()
        for (el in elements) {
            val isDiskEd = "PS5" == el.child(0).text().trim().toUpperCase(Locale.ROOT)
            val status = el.child(0).className().split(" ")[1].replace("-", " ").trim()
            val lastStocked = el.child(1).child(0).text().split(" ", limit = 3)[2].trim()
            val lastChanged = el.child(1).child(1).text().split(" ", limit = 2)[1].trim()
            val link = el.attr("href").trim()
            val name = link.substring(link.lastIndexOf("/") + 1).trim().replace("_", " ")
            val imgId = context.resources.getIdentifier(
                name.replace("-", "_").replace(" digital", ""),
                "drawable",
                context.packageName
            )
            infos.add(StoreInfo(name, link, status, lastStocked, lastChanged, imgId, isDiskEd))
        }
        return infos
    }
}