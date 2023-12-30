package com.adrianbl.traductorml

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit

class SharedPreferencesHelper(context: Context) {
    private val sharedPreferences: SharedPreferences = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)

    fun saveButtonColors(primaryColor: Int, primaryDarkColor: Int, accentColor: Int) {
        sharedPreferences.edit {
            putInt("primaryColor", primaryColor)
            putInt("primaryDarkColor", primaryDarkColor)
            putInt("accentColor", accentColor)
            commit()
        }
    }

    fun getPrimaryColor(): Int = sharedPreferences.getInt("primaryColor", R.color.colorPrimary)
    fun getPrimaryDarkColor(): Int = sharedPreferences.getInt("primaryDarkColor", R.color.colorPrimaryDark)
    fun getAccentColor(): Int = sharedPreferences.getInt("accentColor", R.color.colorAccent)
}