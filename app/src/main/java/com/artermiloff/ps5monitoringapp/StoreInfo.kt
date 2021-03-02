package com.artermiloff.ps5monitoringapp

data class StoreInfo(
    val name: String,
    val link: String,
    val status: String,
    val lastShip: String,
    val lastCheck: String,
    val imgCode: Int,
    val diskEdition: Boolean,
)