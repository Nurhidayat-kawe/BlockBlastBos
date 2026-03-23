package com.softimagines.blockblastbos.data.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.Calendar

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "game_stats")

class HighScoreDataStore(private val context: Context) {
    private val HIGH_SCORE_KEY = intPreferencesKey("high_score")
    private val COINS_KEY = intPreferencesKey("coins")
    private val LAST_REWARD_DATE = longPreferencesKey("last_reward_date")

    val highScoreFlow: Flow<Int> = context.dataStore.data.map { preferences ->
        preferences[HIGH_SCORE_KEY] ?: 0
    }

    val coinsFlow: Flow<Int> = context.dataStore.data.map { preferences ->
        preferences[COINS_KEY] ?: 0
    }

    val lastRewardDateFlow: Flow<Long> = context.dataStore.data.map { preferences ->
        preferences[LAST_REWARD_DATE] ?: 0L
    }

    suspend fun saveHighScore(score: Int) {
        context.dataStore.edit { preferences ->
            val currentHighScore = preferences[HIGH_SCORE_KEY] ?: 0
            if (score > currentHighScore) {
                preferences[HIGH_SCORE_KEY] = score
            }
        }
    }

    suspend fun addCoins(amount: Int) {
        context.dataStore.edit { preferences ->
            val currentCoins = preferences[COINS_KEY] ?: 0
            preferences[COINS_KEY] = currentCoins + amount
        }
    }

    suspend fun spendCoins(amount: Int): Boolean {
        var success = false
        context.dataStore.edit { preferences ->
            val currentCoins = preferences[COINS_KEY] ?: 0
            if (currentCoins >= amount) {
                preferences[COINS_KEY] = currentCoins - amount
                success = true
            }
        }
        return success
    }

    suspend fun updateLastRewardDate() {
        context.dataStore.edit { preferences ->
            preferences[LAST_REWARD_DATE] = System.currentTimeMillis()
        }
    }

    fun isDailyRewardAvailable(lastDate: Long): Boolean {
        if (lastDate == 0L) return true
        val last = Calendar.getInstance().apply { timeInMillis = lastDate }
        val now = Calendar.getInstance()
        return now.get(Calendar.DAY_OF_YEAR) != last.get(Calendar.DAY_OF_YEAR) ||
               now.get(Calendar.YEAR) != last.get(Calendar.YEAR)
    }
}
