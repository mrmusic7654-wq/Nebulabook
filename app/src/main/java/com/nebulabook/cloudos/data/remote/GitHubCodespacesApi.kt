package com.nebulabook.cloudos.data.remote

import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject

class GitHubCodespacesApi(private val client: OkHttpClient=OkHttpClient()) {
    fun user(token:String):String=call("GET","https://api.github.com/user",token)
    fun list(token:String):String=call("GET","https://api.github.com/user/codespaces?per_page=100",token)
    fun get(token:String,name:String):String=call("GET", "https://api.github.com/user/codespaces/${name.encodeURLPath()}",token)
    fun machines(token:String,name:String):String=call("GET", "https://api.github.com/user/codespaces/${name.encodeURLPath()}/machines",token)
    fun setMachine(token:String,name:String,machine:String):String=call("PATCH", "https://api.github.com/user/codespaces/${name.encodeURLPath()}",token,JSONObject().put("machine",machine).toString())
    fun start(token:String,name:String):String=call("POST","https://api.github.com/user/codespaces/${name.encodeURLPath()}/start",token,"{}")
    fun stop(token:String,name:String):String=call("POST","https://api.github.com/user/codespaces/${name.encodeURLPath()}/stop",token,"{}")

    private fun call(method:String,url:String,token:String,json:String?=null):String {
        val body=if(method=="GET") null else (json ?: "{}").toRequestBody("application/json".toMediaType())
        val request=Request.Builder().url(url)
            .header("Authorization","Bearer $token")
            .header("Accept","application/vnd.github+json")
            .header("X-GitHub-Api-Version","2022-11-28")
            .method(method,body).build()
        return client.newCall(request).execute().use { response ->
            val text=response.body?.string().orEmpty()
            if(!response.isSuccessful) error("GitHub API HTTP ${response.code}: $text")
            text
        }
    }
    private fun String.encodeURLPath()=java.net.URLEncoder.encode(this,"UTF-8").replace("+","%20")
}
