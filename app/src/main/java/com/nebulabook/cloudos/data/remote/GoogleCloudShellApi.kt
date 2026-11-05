package com.nebulabook.cloudos.data.remote

import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody

/** Thin documented Cloud Shell API client. Cloud Shell's environment API is not itself an SSH credential API. */
class GoogleCloudShellApi(private val client: OkHttpClient = OkHttpClient()) {
    private val base="https://cloudshell.googleapis.com/v1/users/me/environments/default"
    fun start(accessToken: String, publicKey: String): String = request("POST", "$base:start", accessToken, "{\"publicKeys\":[\"${publicKey.replace("\\", "\\\\").replace("\"", "\\\"")}\"]}")
    fun getEnvironment(accessToken: String): String = request("GET", base, accessToken)
    private fun request(method: String, url: String, token: String, json: String=""): String {
        val body= if(method=="GET") null else json.toRequestBody("application/json".toMediaType())
        val req=Request.Builder().url(url).header("Authorization", "Bearer $token").method(method,body).build()
        return client.newCall(req).execute().use { response -> if(!response.isSuccessful) error("Cloud Shell API HTTP ${response.code}: ${response.body?.string().orEmpty()}"); response.body?.string().orEmpty() }
    }
}
