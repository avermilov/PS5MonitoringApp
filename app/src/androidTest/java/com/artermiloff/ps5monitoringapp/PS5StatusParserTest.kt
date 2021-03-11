package com.artermiloff.ps5monitoringapp

import androidx.test.platform.app.InstrumentationRegistry.getInstrumentation
import org.junit.Test
import org.junit.jupiter.api.Assertions

internal class PS5StatusParserTest {


    @Test
    fun parse_InvalidResponse_ReturnsEmptyStoreList() {
        getInstrumentation().runOnMainSync {
            val context = getInstrumentation().targetContext
            val parser = PS5StatusParser(context)

            val response = "this is an invalid response"

            val result = parser.parse(response)
            Assertions.assertEquals(0, result.size)
        }
    }

    @Test
    fun parse_ValidResponse_ReturnsSingleElementList() {
        getInstrumentation().runOnMainSync {
            val context = getInstrumentation().targetContext
            val parser = PS5StatusParser(context)

            val response =
                "<a class=\"table-data\" href=\"https://ps5status.ru/u/mvideo\" target=\"_blank\">\n" +
                        "                <div class=\"table-data-title not-available \">\n" +
                        "                    <span>PS5</span></div>\n" +
                        "                <div class=\"cell-content\">\n" +
                        "                    <p>\n" +
                        "                        <span>последнее изменение</span><br>\n" +
                        "                        <time>23:35:45 27.11.2020</time>\n" +
                        "                    </p>\n" +
                        "                    <p>\n" +
                        "                        <span>обновлено</span><br>\n" +
                        "                        <time>21:36:19 11.03.2021</time>\n" +
                        "                    </p>\n" +
                        "                </div>\n" +
                        "            </a>"

            val result = parser.parse(response)
            Assertions.assertEquals(1, result.size)

            val store = result[0]
            Assertions.assertTrue(store.diskEdition)
            Assertions.assertEquals("mvideo", store.name)
            Assertions.assertEquals("https://ps5status.ru/u/mvideo", store.link)
            Assertions.assertEquals("not available", store.status)
            Assertions.assertEquals("23:35:45 27.11.2020", store.lastShip)
            Assertions.assertEquals("21:36:19 11.03.2021", store.lastCheck)
        }
    }

    @Test
    fun parse_ValidResponse_ReturnsTwoElementList() {
        getInstrumentation().runOnMainSync {
            val context = getInstrumentation().targetContext
            val parser = PS5StatusParser(context)

            val response =
                "<a class=\"table-data\" href=\"https://ps5status.ru/u/mvideo\" target=\"_blank\">\n" +
                        "                <div class=\"table-data-title not-available \">\n" +
                        "                    <span>PS5</span></div>\n" +
                        "                <div class=\"cell-content\">\n" +
                        "                    <p>\n" +
                        "                        <span>последнее изменение</span><br>\n" +
                        "                        <time>23:35:45 27.11.2020</time>\n" +
                        "                    </p>\n" +
                        "                    <p>\n" +
                        "                        <span>обновлено</span><br>\n" +
                        "                        <time>21:36:19 11.03.2021</time>\n" +
                        "                    </p>\n" +
                        "                </div>\n" +
                        "            </a>\n" +
                        "<a class=\"table-data\" href=\"https://ps5status.ru/u/onec_digital\" target=\"_blank\">\n" +
                        "                <div class=\"table-data-title not-available \">\n" +
                        "                    <span>PS5 DE</span></div>\n" +
                        "                <div class=\"cell-content\">\n" +
                        "                    <p>\n" +
                        "                        <span>последнее изменение</span><br>\n" +
                        "                        <time>13:01:15 23.12.2020</time>\n" +
                        "                    </p>\n" +
                        "                    <p>\n" +
                        "                        <span>обновлено</span><br>\n" +
                        "                        <time>21:49:12 11.03.2021</time>\n" +
                        "                    </p>\n" +
                        "                </div>\n" +
                        "            </a>"

            val result = parser.parse(response)
            Assertions.assertEquals(2, result.size)

            val store = result[0]
            Assertions.assertTrue(store.diskEdition)
            Assertions.assertEquals("mvideo", store.name)
            Assertions.assertEquals("https://ps5status.ru/u/mvideo", store.link)
            Assertions.assertEquals("not available", store.status)
            Assertions.assertEquals("23:35:45 27.11.2020", store.lastShip)
            Assertions.assertEquals("21:36:19 11.03.2021", store.lastCheck)

            val store2 = result[1]
            Assertions.assertFalse(store2.diskEdition)
            Assertions.assertEquals("onec digital", store2.name)
            Assertions.assertEquals("https://ps5status.ru/u/onec_digital", store2.link)
            Assertions.assertEquals("not available", store2.status)
            Assertions.assertEquals("13:01:15 23.12.2020", store2.lastShip)
            Assertions.assertEquals("21:49:12 11.03.2021", store2.lastCheck)
        }
    }
}