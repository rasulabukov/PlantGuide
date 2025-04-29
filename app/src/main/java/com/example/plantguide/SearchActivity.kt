package com.example.plantguide

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.res.Configuration
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.GridLayoutManager
import com.example.plantguide.databinding.ActivitySearchBinding
import com.example.plantguide.db.AppDatabase
import com.example.plantguide.db.Grains
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*

class SearchActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySearchBinding
    private lateinit var database: AppDatabase
    private lateinit var adapter: GrainsAdapter
    private lateinit var sharedPreferences: SharedPreferences
    private var currentFilterType = "name" // По умолчанию фильтр по названию

    override fun attachBaseContext(newBase: Context) {
        sharedPreferences = newBase.getSharedPreferences("AppPreferences", Context.MODE_PRIVATE)
        val langCode = sharedPreferences.getString("app_language", "ru") ?: "ru"
        val locale = Locale(langCode)
        val config = Configuration()
        config.setLocale(locale)
        val context = newBase.createConfigurationContext(config)
        super.attachBaseContext(context)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySearchBinding.inflate(layoutInflater)
        enableEdgeToEdge()
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        database = AppDatabase.getDatabase(this)
        setupRecyclerView()
        setupSearch()
        setupFilterSpinner()

        binding.backbtn.setOnClickListener {
            finish()
        }
    }

    private fun setupRecyclerView() {
        val sharedPreferences = this.getSharedPreferences("AppPreferences", Context.MODE_PRIVATE)

        adapter = GrainsAdapter(emptyList(), { grain ->
            showGrainDetailsDialog(grain)
        }, sharedPreferences)

        binding.recyclerView.apply {
            layoutManager = GridLayoutManager(this@SearchActivity, 2)
            adapter = this@SearchActivity.adapter
        }
    }

    private fun setupSearch() {
        binding.searchButton.setOnClickListener {
            val query = binding.searchEditText.text.toString().trim()
            if (query.isNotEmpty()) {
                performSearch(query)
            } else {
                loadAllGrains()
            }
        }
    }

    private fun setupFilterSpinner() {
        val filterOptions = arrayOf(
            getString(R.string.filter_name),
            getString(R.string.filter_region),
            getString(R.string.filter_usage),
            getString(R.string.filter_species)
        )

        val filterKeys = arrayOf("name", "region", "usage", "species")

        val spinnerAdapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,
            filterOptions
        )
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.filterSpinner.adapter = spinnerAdapter

        binding.filterSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                currentFilterType = filterKeys[position]
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    private fun performSearch(query: String) {
        CoroutineScope(Dispatchers.IO).launch {
            val results = when (currentFilterType) {
                "name" -> database.grainsDao().searchGrainsByName(query)
                "region" -> database.grainsDao().searchGrainsByRegion(query)
                "usage" -> database.grainsDao().searchGrainsByUsage(query)
                "species" -> database.grainsDao().searchGrainsBySpecies(query)
                else -> emptyList()
            }

            runOnUiThread {
                if (results.isEmpty()) {
                    binding.emptyResultsText.visibility = View.VISIBLE
                    binding.recyclerView.visibility = View.GONE
                } else {
                    binding.emptyResultsText.visibility = View.GONE
                    binding.recyclerView.visibility = View.VISIBLE
                    adapter.updateData(results)
                }
            }
        }
    }

    private fun loadAllGrains() {
        CoroutineScope(Dispatchers.IO).launch {
            val allGrains = database.grainsDao().getAllGrainsList()
            runOnUiThread {
                adapter.updateData(allGrains)
                binding.emptyResultsText.visibility = View.GONE
                binding.recyclerView.visibility = View.VISIBLE
            }
        }
    }

    private fun showGrainDetailsDialog(grain: Grains) {
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.grain_detail_dialog)
        val isEnglish = isEnglish()

        // Инициализация элементов диалога
        val image = dialog.findViewById<ImageView>(R.id.dialog_grain_image)
        val name = dialog.findViewById<TextView>(R.id.dialog_grain_name)
        val species = dialog.findViewById<TextView>(R.id.dialog_grain_species)
        val description = dialog.findViewById<TextView>(R.id.dialog_grain_description)
        val climate = dialog.findViewById<TextView>(R.id.dialog_grain_climate)
        val yield = dialog.findViewById<TextView>(R.id.dialog_grain_yield)
        val diseases = dialog.findViewById<TextView>(R.id.dialog_grain_diseases)
        val usage = dialog.findViewById<TextView>(R.id.dialog_grain_usage)
        val regions = dialog.findViewById<TextView>(R.id.dialog_grain_regions)
        val favButton = dialog.findViewById<Button>(R.id.dialog_fav_button)
        val moreButton = dialog.findViewById<Button>(R.id.dialog_more_button)

        // Заполнение данных с учетом языка
        image.setImageResource(grain.imageResId)
        name.text = if (isEnglish) grain.englishName else grain.name
        species.text = if (isEnglish) grain.englishSpecies else grain.species
        description.text = if (isEnglish) grain.englishFullDescription else grain.fullDescription

        climate.text = if (isEnglish)
            "Climate conditions: ${grain.englishClimateConditions}"
        else
            "Климатические условия: ${grain.climateConditions}"

        yield.text = if (isEnglish)
            "Yield: ${grain.englishYield}"
        else
            "Урожайность: ${grain.yield}"

        diseases.text = if (isEnglish)
            "Diseases: ${grain.englishDiseases}"
        else
            "Болезни и вредители: ${grain.diseases}"

        usage.text = if (isEnglish)
            "Usage: ${grain.englishUsage}"
        else
            "Использование: ${grain.usage}"

        regions.text = if (isEnglish)
            "Growing regions: ${grain.englishGrowingRegions}"
        else
            "Регионы произрастания: ${grain.growingRegions}"
        // Настройка кнопки избранного
        updateFavoriteButton(favButton, grain.isFavorite)

        favButton.setOnClickListener {
            CoroutineScope(Dispatchers.IO).launch {
                grain.isFavorite = !grain.isFavorite
                database.grainsDao().update(grain)

                runOnUiThread {
                    val message = if (grain.isFavorite)
                        getString(R.string.added_to_favorites)
                    else
                        getString(R.string.removed_from_favorites)

                    Toast.makeText(this@SearchActivity, message, Toast.LENGTH_SHORT).show()
                    updateFavoriteButton(favButton, grain.isFavorite)
                }
            }
        }

        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        dialog.show()

        moreButton.setOnClickListener {
            val query = grain.name.replace(" ", "_") // Заменяем пробелы на подчеркивания для URL
            val url = "https://ru.wikipedia.org/wiki/$query" // Формируем URL для Wikipedia
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)) // Создаем Intent
            startActivity(intent) // Открываем браузер с URL
        }
    }
    private fun isEnglish(): Boolean {
        val lang = sharedPreferences.getString("app_language", "ru") ?: "ru"
        return lang == "en"
    }

    private fun updateFavoriteButton(button: Button, isFavorite: Boolean) {
        button.text = if (isFavorite)
            getString(R.string.remove_from_favorites)
        else
            getString(R.string.add_to_favorites)

        button.setBackgroundResource(
            if (isFavorite) R.drawable.bg_next_button_red else R.drawable.bg_next_button
        )
        button.setTextColor(resources.getColor(android.R.color.white))
    }
}