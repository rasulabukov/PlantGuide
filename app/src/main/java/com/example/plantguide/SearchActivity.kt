package com.example.plantguide

import android.app.Dialog
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

class SearchActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySearchBinding
    private lateinit var database: AppDatabase
    private lateinit var adapter: GrainsAdapter
    private var currentFilterType = "Название" // По умолчанию фильтр по названию

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
        adapter = GrainsAdapter(emptyList()) { grain ->
            showGrainDetailsDialog(grain)
        }

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
                // Если запрос пустой, показываем все культуры
                loadAllGrains()
            }
        }
    }

    private fun setupFilterSpinner() {
        val filterOptions = arrayOf("Название", "Регион", "Использование", "Вид")
        val spinnerAdapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,
            filterOptions
        )
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.filterSpinner.adapter = spinnerAdapter

        binding.filterSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                currentFilterType = filterOptions[position]
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    private fun performSearch(query: String) {
        CoroutineScope(Dispatchers.IO).launch {
            val results = when (currentFilterType) {
                "Название" -> database.grainsDao().searchGrainsByName(query)
                "Регион" -> database.grainsDao().searchGrainsByRegion(query)
                "Использование" -> database.grainsDao().searchGrainsByUsage(query)
                "Вид" -> database.grainsDao().searchGrainsBySpecies(query)
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

        // Находим элементы в диалоге
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

        // Заполняем данные
        image.setImageResource(grain.imageResId)
        name.text = grain.name
        species.text = "${grain.species} (${grain.subspecies})"
        description.text = grain.fullDescription
        climate.text = "Климатические условия: ${grain.climateConditions}"
        yield.text = "Урожайность: ${grain.yield}"
        diseases.text = "Болезни и вредители: ${grain.diseases}"
        usage.text = "Использование: ${grain.usage}"
        regions.text = "Регионы произрастания: ${grain.growingRegions}"

        // Настройка кнопки избранного
        favButton.text = if (grain.isFavorite) "Удалить из избранного" else "Добавить в избранное"
        favButton.setBackgroundResource(
            if (grain.isFavorite) R.drawable.bg_next_button_red else R.drawable.bg_next_button
        )
        favButton.setTextColor(resources.getColor(android.R.color.white))

        favButton.setOnClickListener {
            CoroutineScope(Dispatchers.IO).launch {
                grain.isFavorite = !grain.isFavorite
                database.grainsDao().update(grain)

                runOnUiThread {
                    val message = if (grain.isFavorite) "Добавлено в избранное" else "Удалено из избранного"
                    Toast.makeText(this@SearchActivity, message, Toast.LENGTH_SHORT).show()

                    favButton.text = if (grain.isFavorite) "Удалить из избранного" else "Добавить в избранное"
                    favButton.setBackgroundResource(
                        if (grain.isFavorite) R.drawable.bg_next_button_red else R.drawable.bg_next_button
                    )
                }
            }
        }



        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        dialog.show()
    }
}