package com.suprogramuota_visata.vedlys.utils

import com.suprogramuota_visata.api.ApiSvClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URL
import java.security.cert.X509Certificate
import javax.net.ssl.HttpsURLConnection
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager

object ImageHelper {

    fun extractImageUuid(value: String?): String? {
        if (value.isNullOrBlank()) return null
        val trimmed = value.trim()
        val regexMatch = Regex("\"(?:id|uuid)\"\\s*:\\s*\"([^\"]+)\"", RegexOption.IGNORE_CASE).find(trimmed)
        if (regexMatch != null) {
            return regexMatch.groupValues[1]
        }
        if (!trimmed.startsWith("{") && !trimmed.startsWith("[")) {
            return trimmed
        }
        return null
    }

    fun buildImageJson(uuid: String, sizeBytes: Long, format: String): String {
        return """{"id":"$uuid","size":$sizeBytes,"format":"$format"}"""
    }

    private fun configureSsl(conn: HttpURLConnection) {
        if (conn is HttpsURLConnection) {
            val trustAllCerts = arrayOf<TrustManager>(object : X509TrustManager {
                override fun checkClientTrusted(chain: Array<out X509Certificate>?, authType: String?) {}
                override fun checkServerTrusted(chain: Array<out X509Certificate>?, authType: String?) {}
                override fun getAcceptedIssuers(): Array<X509Certificate> = emptyArray()
            })
            val sc = SSLContext.getInstance("TLS")
            sc.init(null, trustAllCerts, java.security.SecureRandom())
            conn.sslSocketFactory = sc.socketFactory
            conn.hostnameVerifier = javax.net.ssl.HostnameVerifier { _, _ -> true }
        }
    }

    suspend fun uploadImageBytes(
        apiClient: ApiSvClient,
        uuid: String,
        bytes: ByteArray,
        format: String = "jpg"
    ): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val cleanBase = apiClient.baseUrl.trimEnd('/')
            val url = "$cleanBase/images/upload/$uuid"
            val conn = (URL(url).openConnection() as HttpURLConnection).apply {
                configureSsl(this)
                requestMethod = "POST"
                doOutput = true
                connectTimeout = 10000
                readTimeout = 15000
                val mime = if (format.equals("png", ignoreCase = true)) "image/png" else "image/jpeg"
                setRequestProperty("Content-Type", mime)
                val token = apiClient.authRepository.getToken()
                if (!token.isNullOrBlank()) {
                    setRequestProperty("Authorization", "Bearer $token")
                }
            }
            conn.outputStream.use { it.write(bytes) }
            val code = conn.responseCode
            if (code in 200..299) {
                Result.success(Unit)
            } else {
                val err = conn.errorStream?.bufferedReader()?.use { it.readText() } ?: ""
                Result.failure(Exception("Įkėlimo klaida (HTTP $code): $err"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun fetchImageBytes(
        apiClient: ApiSvClient,
        uuid: String
    ): Result<ByteArray> = withContext(Dispatchers.IO) {
        try {
            val cleanBase = apiClient.baseUrl.trimEnd('/')
            val url = "$cleanBase/images/$uuid"
            val conn = (URL(url).openConnection() as HttpURLConnection).apply {
                configureSsl(this)
                requestMethod = "GET"
                connectTimeout = 8000
                readTimeout = 10000
                val token = apiClient.authRepository.getToken()
                if (!token.isNullOrBlank()) {
                    setRequestProperty("Authorization", "Bearer $token")
                }
            }
            val code = conn.responseCode
            if (code == 200) {
                val bytes = conn.inputStream.use { it.readBytes() }
                Result.success(bytes)
            } else {
                Result.failure(Exception("Nuotrauka nerasta (HTTP $code)"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteImage(
        apiClient: ApiSvClient,
        uuid: String
    ): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val cleanBase = apiClient.baseUrl.trimEnd('/')
            val url = "$cleanBase/images/$uuid"
            val conn = (URL(url).openConnection() as HttpURLConnection).apply {
                configureSsl(this)
                requestMethod = "DELETE"
                connectTimeout = 8000
                readTimeout = 10000
                val token = apiClient.authRepository.getToken()
                if (!token.isNullOrBlank()) {
                    setRequestProperty("Authorization", "Bearer $token")
                }
            }
            val code = conn.responseCode
            if (code in 200..299 || code == 404) {
                Result.success(Unit)
            } else {
                Result.failure(Exception("Trynimo klaida (HTTP $code)"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
