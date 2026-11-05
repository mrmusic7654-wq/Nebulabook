package com.nebulabook.cloudos.data.remote

import android.util.Base64
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody

/** Kaggle API access. GPU availability and quotas are controlled by Kaggle and can vary by account. */
class KaggleRenderApi(private val client: OkHttpClient=OkHttpClient()) {
    private fun auth(user: String, key: String) = "Basic " + Base64.encodeToString("$user:$key".toByteArray(), Base64.NO_WRAP)
    fun status(user: String, key: String, slug: String): String = get("https://www.kaggle.com/api/v1/kernels/status?userName=${enc(user)}&kernelSlug=${enc(slug)}", user,key)
    fun output(user: String, key: String, slug: String): String = get("https://www.kaggle.com/api/v1/kernels/output?userName=${enc(user)}&kernelSlug=${enc(slug)}", user,key)
    fun push(user: String, key: String, payload: String): String {
        val request=Request.Builder().url("https://www.kaggle.com/api/v1/kernels/push").header("Authorization",auth(user,key)).post(payload.toRequestBody("application/json".toMediaType())).build()
        return client.newCall(request).execute().use { r -> if(!r.isSuccessful) error("Kaggle API HTTP ${r.code}: ${r.body?.string().orEmpty()}"); r.body?.string().orEmpty() }
    }
    private fun get(url: String,user:String,key:String):String { val req=Request.Builder().url(url).header("Authorization",auth(user,key)).get().build(); return client.newCall(req).execute().use { r -> if(!r.isSuccessful) error("Kaggle API HTTP ${r.code}: ${r.body?.string().orEmpty()}"); r.body?.string().orEmpty() } }
    private fun enc(v:String)=java.net.URLEncoder.encode(v,"UTF-8")
}
