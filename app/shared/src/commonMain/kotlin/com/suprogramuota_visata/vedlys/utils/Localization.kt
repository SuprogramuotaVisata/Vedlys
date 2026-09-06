package com.suprogramuota_visata.vedlys.utils

import com.suprogramuota_visata.vedlys.AppLanguage
import java.util.*

object Localization {
    private val bundles = mutableMapOf<AppLanguage, ResourceBundle>()

    fun getString(prefix: String, key: String, language: AppLanguage): String {
        val bundle = try {
            bundles.getOrPut(language) {
                val locale = when (language) {
                    AppLanguage.LT -> Locale("lt", "LT")
                    AppLanguage.EN -> Locale("en", "US")
                }
                ResourceBundle.getBundle("strings", locale, UTF8Control())
            }
        } catch (e: Exception) {
            null
        }

        if (bundle == null) return key

        val fullKey = "${prefix}_$key"
        return try {
            bundle.getString(fullKey)
        } catch (e: Exception) {
            try {
                bundle.getString(key)
            } catch (e2: Exception) {
                key
            }
        }
    }
}

private class UTF8Control : ResourceBundle.Control() {
    override fun newBundle(
        baseName: String,
        locale: Locale,
        format: String,
        loader: ClassLoader,
        reload: Boolean
    ): ResourceBundle? {
        val bundleName = toBundleName(baseName, locale)
        val resourceName = toResourceName(bundleName, "properties")
        val stream = loader.getResourceAsStream(resourceName) ?: return null
        return stream.use {
            PropertyResourceBundle(java.io.InputStreamReader(it, java.nio.charset.StandardCharsets.UTF_8))
        }
    }
}
