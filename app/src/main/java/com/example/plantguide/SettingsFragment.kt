package com.example.plantguide

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import com.example.plantguide.databinding.FragmentSettingsBinding

class SettingsFragment : Fragment() {

    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!
    private lateinit var sharedPreferences: SharedPreferences

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

        sharedPreferences = requireActivity().getPreferences(Context.MODE_PRIVATE)

        // Загружаем текущее состояние темы
        val isDarkTheme = sharedPreferences.getBoolean("dark_theme", false)
        binding.themeSwitch.isChecked = isDarkTheme

        // Устанавливаем слушатель для переключателя
        binding.themeSwitch.setOnCheckedChangeListener { _, isChecked ->
            setDarkTheme(isChecked)
            saveThemePreference(isChecked)
        }
    }

    private fun setDarkTheme(isDark: Boolean) {
        if (isDark) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        }

        // Пересоздаем активность для применения темы
        requireActivity().recreate()
    }

    private fun saveThemePreference(isDark: Boolean) {
        with(sharedPreferences.edit()) {
            putBoolean("dark_theme", isDark)
            apply()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}