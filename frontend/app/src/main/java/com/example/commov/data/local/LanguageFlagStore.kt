package com.example.commov.data.local

import com.example.commov.data.remote.CountriesApi

object LanguageFlagStore {
    @Volatile
    private var cachedFlags: Map<String, String>? = null

    fun getFlagUrl(language: String): String? {
        return cachedFlags?.get(language) ?: CountriesApi.fallbackFlagUrls[language]
    }

    @Synchronized
    fun refreshFlags(): Map<String, String> {
        cachedFlags?.let { return it }

        val flags = CountriesApi().fetchLanguageFlagUrls().getOrElse { CountriesApi.fallbackFlagUrls }
        cachedFlags = flags
        return flags
    }
}
