package com.nebulabook.cloudos.data.remote

import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody

class GitHubCodespacesApi(private val client: OkHttpClient=OkHttpClient()) {
    fun user(token: String): String = call("GET", "https://api.github.com/user", token)
    fun list(token: String): String = call("GET", "https://api.github.com/user/codespaces", token)
    fun start(token: String, name: String): String = call("POST", "https://api.github.com/user/codespaces/${name.encodeURLPath()}/start", token)
    fun stop(token: String, name: String): String = call("POST", "https://api.github.com/user/codespaces/${name.encodeURLPath()}/stop", token)
    private fun call(method: String, url: String, token: String): String {
        val request=Request.Builder().url(url).header("Authorization","Bearer $token").header("Accept","application/vnd.github+json").header("X-GitHub-Api-Version","2022-11-28").method(method, if(method=="GET") null else "{}".toRequestBody()).build()
        return client.newCall(request).execute().use { r -> if(!r.isSuccessful) error("GitHub API HTTP ${r.code}: ${r.body?.string().orEmpty()}"); r.body?.string().orEmpty() }
    }
    private fun String.encodeURLPath() = java.net.URLEncoder.encode(this, "UTF-8").replace("+", "%20")
}
