package com.malikenterprises.app

import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

data class SupabaseResponse(val code: Int, val body: String)

class SupabaseClient(
    private val baseUrl: String = SupabaseConfig.URL,
    private val apiKey: String = SupabaseConfig.PUBLISHABLE_KEY
) {
    private var accessToken: String? = null

    fun setAccessToken(token: String?) { accessToken = token }

    fun request(
        method: String,
        path: String,
        body: String? = null,
        extraHeaders: Map<String, String> = emptyMap()
    ): SupabaseResponse {
        val connection = (URL(baseUrl + path).openConnection() as HttpURLConnection)
        connection.requestMethod = method
        connection.setRequestProperty("apikey", apiKey)
        connection.setRequestProperty("Authorization", "Bearer ${accessToken ?: apiKey}")
        connection.setRequestProperty("Content-Type", "application/json")
        connection.setRequestProperty("Accept", "application/json")
        extraHeaders.forEach { (k, v) -> connection.setRequestProperty(k, v) }

        if (body != null) {
            connection.doOutput = true
            connection.outputStream.use { it.write(body.toByteArray(Charsets.UTF_8)) }
        }

        val code = connection.responseCode
        val stream = if (code in 200..399) connection.inputStream else connection.errorStream
        val text = stream?.bufferedReader()?.use { it.readText() } ?: ""
        connection.disconnect()
        return SupabaseResponse(code, text)
    }

    fun signIn(email: String, password: String): SupabaseResponse {
        val payload = JSONObject()
            .put("email", email)
            .put("password", password)
            .toString()

        val response = request("POST", "/auth/v1/token?grant_type=password", payload)
        if (response.code in 200..299 && response.body.isNotBlank()) {
            accessToken = JSONObject(response.body).optString("access_token", null)
        }
        return response
    }

    fun signUp(email: String, password: String): SupabaseResponse {
        val payload = JSONObject()
            .put("email", email)
            .put("password", password)
            .toString()
        return request("POST", "/auth/v1/signup", payload)
    }

    fun signOut(): SupabaseResponse =
        request("POST", "/auth/v1/logout", "{}")

    fun getProducts(): SupabaseResponse =
        request(
            "GET",
            "/rest/v1/products?select=*&order=name.asc",
            null,
            mapOf("Prefer" to "return=representation")
        )

    fun getVeterinaryDoctors(): SupabaseResponse =
        request(
            "GET",
            "/rest/v1/veterinary_doctors?select=*&order=name.asc"
        )

    fun getEducationContent(): SupabaseResponse =
        request(
            "GET",
            "/rest/v1/education_content?select=*&order=created_at.desc"
        )

    fun getMyProfile(): SupabaseResponse =
        request(
            "GET",
            "/rest/v1/profiles?select=*&id=eq.auth.uid()"
        )
}
