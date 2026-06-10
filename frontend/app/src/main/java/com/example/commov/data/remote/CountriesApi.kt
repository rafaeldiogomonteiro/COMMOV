package com.example.commov.data.remote

import android.util.Log
import com.example.commov.data.local.LocaleHelper
import org.json.JSONArray
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL

class CountriesApi {
    fun fetchLanguageFlagUrls(): Result<Map<String, String>> {
        val connection = (URL(FLAGS_URL).openConnection() as HttpURLConnection).apply {
            requestMethod = "GET"
            connectTimeout = 10_000
            readTimeout = 10_000
            setRequestProperty("Accept", "application/json")
        }

        return try {
            val code = connection.responseCode
            val responseBody = if (code in 200..299) {
                connection.inputStream.bufferedReader().use { it.readText() }
            } else {
                connection.errorStream?.bufferedReader()?.use { it.readText() }.orEmpty()
            }

            if (code !in 200..299) {
                Log.d("CountriesApi", "fetchLanguageFlagUrls <- HTTP $code")
                return Result.failure(IOException("HTTP $code"))
            }

            Result.success(parseLanguageFlags(responseBody))
        } catch (e: IOException) {
            Log.d("CountriesApi", "fetchLanguageFlagUrls network error: ${e.message}")
            Result.failure(e)
        } catch (e: Exception) {
            Log.d("CountriesApi", "fetchLanguageFlagUrls error: ${e.message}")
            Result.failure(e)
        } finally {
            connection.disconnect()
        }
    }

    private fun parseLanguageFlags(responseBody: String): Map<String, String> {
        val countries = JSONArray(responseBody)
        val flagsByCountryName = mutableMapOf<String, String>()
        for (index in 0 until countries.length()) {
            val country = countries.getJSONObject(index)
            val name = country.getJSONObject("name").getString("common")
            val pngUrl = country.getJSONObject("flags").getString("png")
            flagsByCountryName[name] = pngUrl
        }

        return LANGUAGE_COUNTRY_NAMES.mapNotNull { (language, countryName) ->
            flagsByCountryName[countryName]?.let { language to it }
        }.toMap()
    }

    companion object {
        private const val FLAGS_URL = "https://restcountries.com/v3.1/all?fields=name,flags"

        private val LANGUAGE_COUNTRY_NAMES = mapOf(
            LocaleHelper.LANGUAGE_ENGLISH to "United Kingdom",
            LocaleHelper.LANGUAGE_PORTUGUESE to "Portugal"
        )

        val fallbackFlagUrls = mapOf(
            LocaleHelper.LANGUAGE_ENGLISH to "https://flagcdn.com/w320/gb.png",
            LocaleHelper.LANGUAGE_PORTUGUESE to "https://flagcdn.com/w320/pt.png"
        )
    }
}
