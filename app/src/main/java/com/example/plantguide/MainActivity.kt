package com.example.plantguide

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.res.Configuration
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.Fragment
import com.example.plantguide.databinding.ActivityMainBinding
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import java.util.Locale

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var sharedPreferences: SharedPreferences

    override fun attachBaseContext(newBase: Context) {
        // Получаем SharedPreferences перед созданием контекста
        sharedPreferences = newBase.getSharedPreferences("AppPreferences", Context.MODE_PRIVATE)
        val langCode = sharedPreferences.getString("app_language", "ru") ?: "ru"

        // Устанавливаем язык для базового контекста
        val locale = Locale(langCode)
        val config = Configuration()
        config.setLocale(locale)
        val context = newBase.createConfigurationContext(config)
        super.attachBaseContext(context)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        // Инициализируем SharedPreferences
        sharedPreferences = getSharedPreferences("AppPreferences", Context.MODE_PRIVATE)

        // Устанавливаем тему
        val isDarkTheme = sharedPreferences.getBoolean("dark_theme", false)
        if (isDarkTheme) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        }

        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Настройка BottomNavigationView
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottom_navigation)
        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.action_home -> {
                    replaceFragment(HomeFragment())
                    true
                }
                R.id.action_favorites -> {
                    replaceFragment(FavoritesFragment())
                    true
                }
                R.id.action_settings -> {
                    replaceFragment(SettingsFragment())
                    true
                }
                R.id.action_about -> {
                    replaceFragment(AboutFragment())
                    true
                }
                else -> false
            }
        }

        // Установка начального фрагмента
        if (savedInstanceState == null) {
            bottomNav.selectedItemId = R.id.action_home
        }

        // Настройка FAB кнопок
        val fabSearch = findViewById<FloatingActionButton>(R.id.fab_search)
        fabSearch.setOnClickListener {
            val intent = Intent(this, SearchActivity::class.java)
            startActivity(intent)
        }

        val fabTest = findViewById<FloatingActionButton>(R.id.fab_test)
        fabTest.setOnClickListener {
            val intent = Intent(this, TestActivity::class.java)
            startActivity(intent)
        }
    }

    private fun replaceFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()
    }

    override fun onResume() {
        super.onResume()
        // Обновляем язык при возвращении в активность
        val langCode = sharedPreferences.getString("app_language", "ru") ?: "ru"
        setAppLanguage(langCode)
    }

    private fun setAppLanguage(languageCode: String) {
        val locale = Locale(languageCode)
        Locale.setDefault(locale)
        val config = resources.configuration
        config.setLocale(locale)
        resources.updateConfiguration(config, resources.displayMetrics)
    }
}