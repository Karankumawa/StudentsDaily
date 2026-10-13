package com.example.studentsdaily.data.local

import android.content.Context
import androidx.core.content.edit
import com.example.studentsdaily.data.remote.SupabaseSession
import com.example.studentsdaily.data.remote.SupabaseUser

object AuthPreferences {
    private const val PREF_NAME = "students_daily_auth_prefs"
    private const val KEY_ACCESS_TOKEN = "access_token"
    private const val KEY_REFRESH_TOKEN = "refresh_token"
    private const val KEY_USER_ID = "user_id"
    private const val KEY_USER_EMAIL = "user_email"
    private const val KEY_USER_FULL_NAME = "user_full_name"
    private const val KEY_INTRO_COMPLETED = "has_completed_intro"
    private const val KEY_MONTHLY_BUDGET = "monthly_budget"

    fun getMonthlyBudget(context: Context, defaultBudget: Double = 5000.0): Double {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        return prefs.getFloat(KEY_MONTHLY_BUDGET, defaultBudget.toFloat()).toDouble()
    }

    fun setMonthlyBudget(context: Context, budget: Double) {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        prefs.edit { putFloat(KEY_MONTHLY_BUDGET, budget.toFloat()) }
    }

    fun isIntroCompleted(context: Context): Boolean {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        return prefs.getBoolean(KEY_INTRO_COMPLETED, false)
    }

    fun setIntroCompleted(context: Context, completed: Boolean = true) {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        prefs.edit { putBoolean(KEY_INTRO_COMPLETED, completed) }
    }

    fun saveSession(context: Context, session: SupabaseSession) {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        prefs.edit {
            putString(KEY_ACCESS_TOKEN, session.accessToken)
            putString(KEY_REFRESH_TOKEN, session.refreshToken)
            putString(KEY_USER_ID, session.user.id)
            putString(KEY_USER_EMAIL, session.user.email)
            putString(KEY_USER_FULL_NAME, session.user.fullName)
        }
    }

    fun loadSession(context: Context): SupabaseSession? {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val token = prefs.getString(KEY_ACCESS_TOKEN, null) ?: return null
        val userId = prefs.getString(KEY_USER_ID, null) ?: return null
        val email = prefs.getString(KEY_USER_EMAIL, "") ?: ""
        val fullName = prefs.getString(KEY_USER_FULL_NAME, "") ?: ""
        val refreshToken = prefs.getString(KEY_REFRESH_TOKEN, "") ?: ""

        val user = SupabaseUser(id = userId, email = email, fullName = fullName)
        return SupabaseSession(accessToken = token, refreshToken = refreshToken, user = user)
    }

    fun clearSession(context: Context) {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val introCompleted = prefs.getBoolean(KEY_INTRO_COMPLETED, false)
        prefs.edit {
            clear()
            putBoolean(KEY_INTRO_COMPLETED, introCompleted)
        }
    }
}
