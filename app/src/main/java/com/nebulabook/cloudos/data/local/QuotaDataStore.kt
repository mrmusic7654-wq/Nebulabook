package com.nebulabook.cloudos.data.local

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first

private val Context.quotaDataStore by preferencesDataStore("quota_ledger")
class QuotaDataStore(private val context: Context) {
    suspend fun googleHours(): Double = context.quotaDataStore.data.first()[doublePreferencesKey("google_hours")] ?: 50.0
    suspend fun githubHours(): Double = context.quotaDataStore.data.first()[doublePreferencesKey("github_hours")] ?: 60.0
    suspend fun recordGoogle(seconds: Long) { adjust("google_hours", seconds / 3600.0, 50.0) }
    suspend fun recordGithub(seconds: Long) { adjust("github_hours", seconds / 3600.0, 60.0) }
    private suspend fun adjust(name: String, used: Double, max: Double) { context.quotaDataStore.edit { p -> val k=doublePreferencesKey(name); p[k]=((p[k] ?: max)-used).coerceAtLeast(0.0) } }
}
