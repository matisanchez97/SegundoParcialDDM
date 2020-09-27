package com.utn.primerparcial

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.EditTextPreference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceManager
import com.google.android.material.appbar.MaterialToolbar
import com.utn.primerparcial.database.appDatabase
import com.utn.primerparcial.database.userDao
import com.utn.primerparcial.entities.User

class SettingsActivity : AppCompatActivity() {

    lateinit var passwordValue: EditTextPreference
    lateinit var firstnameValue: EditTextPreference

    private var db: appDatabase? = null
    private var userDao: userDao? = null
    private val PREF_NAME = "myPreferences"
    private var currentUser: User? = null



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
        val toolbar = findViewById<MaterialToolbar>(R.id.topAppBar2)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = getString(R.string.settings)
        supportFragmentManager
            .beginTransaction()
            .replace(R.id.settings, SettingsFragment())
            .commit()
    }
    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return super.onSupportNavigateUp()
    }
    override fun onStart() {
        super.onStart()
        db = appDatabase.getAppDataBase(this)
        userDao = db?.userDao()
        val prefs = PreferenceManager.getDefaultSharedPreferences(this)
        val sharedPref: SharedPreferences = getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val editor = prefs.edit()
        val currentUserId = sharedPref.getInt("CURRENT_USER_ID",-1)
        currentUser = userDao?.loadPersonById(currentUserId)
        editor.putString("first_name",currentUser!!.name)
        editor.putString("password",currentUser!!.password)
        editor.apply()
    }


    class SettingsFragment : PreferenceFragmentCompat() {
        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            setPreferencesFromResource(R.xml.root_preferences, rootKey)
        }
    }
}