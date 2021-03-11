package com.artermiloff.ps5monitoringapp

interface Parser {
    fun parse(content: String): MutableList<StoreInfo>
}