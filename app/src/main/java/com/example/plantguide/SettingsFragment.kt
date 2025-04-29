package com.example.plantguide

import android.content.Context
import android.content.SharedPreferences
import android.content.res.Configuration
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.Fragment
import com.example.plantguide.databinding.FragmentSettingsBinding
import java.util.*

class SettingsFragment : Fragment() {

    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!
    private lateinit var sharedPreferences: SharedPreferences
    private val languages = arrayOf("Русский", "English")
    private val languageCodes = mapOf(
        "Русский" to "ru",
        "English" to "en"
    )

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        sharedPreferences = requireActivity().getSharedPreferences("AppPreferences", Context.MODE_PRIVATE)

        setupThemeSwitch()
        setupLanguageSpinner()
    }

    private fun setupThemeSwitch() {
        val isDarkTheme = sharedPreferences.getBoolean("dark_theme", false)
        binding.themeSwitch.isChecked = isDarkTheme
        binding.themeSwitch.setOnCheckedChangeListener { _, isChecked ->
            setDarkTheme(isChecked)
            saveThemePreference(isChecked)
        }
    }

    private fun setupLanguageSpinner() {
        val adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            languages
        ).apply {
            setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        }

        binding.languageSpinner.adapter = adapter

        // Устанавливаем русский язык по умолчанию
        val defaultLanguageCode = "ru"
        val savedLanguageCode = sharedPreferences.getString("app_language", defaultLanguageCode)
        val savedLanguageIndex = languages.indexOfFirst {
            languageCodes[it] == savedLanguageCode
        }

        // Если язык не сохранен или сохранен русский - выбираем первый элемент
        binding.languageSpinner.setSelection(if (savedLanguageIndex != -1) savedLanguageIndex else 0)

        binding.languageSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val selectedLanguage = languages[position]
                val languageCode = languageCodes[selectedLanguage] ?: defaultLanguageCode
                if (languageCode != getCurrentLanguage()) {
                    setAppLanguage(languageCode)
                    saveLanguagePreference(languageCode)
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    private fun getCurrentLanguage(): String {
        return sharedPreferences.getString("app_language", "ru") ?: "ru"
    }

    private fun setDarkTheme(isDark: Boolean) {
        if (isDark) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        }
        requireActivity().recreate()
    }

    private fun setAppLanguage(languageCode: String) {
        val locale = Locale(languageCode)
        Locale.setDefault(locale)

        val config = Configuration()
        config.setLocale(locale)

        resources.updateConfiguration(config, resources.displayMetrics)
        requireActivity().recreate()
    }

    private fun saveThemePreference(isDark: Boolean) {
        sharedPreferences.edit().putBoolean("dark_theme", isDark).apply()
    }

    private fun saveLanguagePreference(languageCode: String) {
        sharedPreferences.edit().putString("app_language", languageCode).apply()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}