package com.nebulabook.cloudos.data.remote

import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject

/** REST access to the user's own Google Cloud Shell environment. */
class GoogleCloudShellApi(private val client:OkHttpClient=OkHttpClient()) {
    private val base="https://cloudshell.googleapis.com/v1/users/me/environments/default"
    fun start(accessToken:String,publicKey:String?=null):String {
        val body=JSONObject()
        if(!publicKey.isNullOrBlank()) body.put("publicKeys",JSONArray().put(twoFieldKey(publicKey)))
        return request("POST","$base:start",accessToken,body.toString())
    }
    fun addPublicKey(accessToken:String,publicKey:String):String {
        val body=JSONObject().put("key",twoFieldKey(publicKey)).toString()
        return request("POST","$base:addPublicKey",accessToken,body)
    }
    fun getEnvironment(accessToken:String):String=request("GET",base,accessToken)
    private fun twoFieldKey(key:String):String=key.trim().split(Regex("\\s+")).take(2).joinToString(" ")
    private fun request(method:String,url:String,token:String,json:String=""):String {
        val body=if(method=="GET") null else json.toRequestBody("application/json".toMediaType())
        val req=Request.Builder().url(url).header("Authorization","Bearer $token").header("Accept","application/json").method(method,body).build()
        return client.newCall(req).execute().use { response ->
            val text=response.body?.string().orEmpty()
            if(!response.isSuccessful) error("Cloud Shell API HTTP ${response.code}: $text")
            text
        }
    }
}
