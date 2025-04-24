package com.example.plantguide

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import com.example.plantguide.databinding.FragmentFavoritesBinding
import com.example.plantguide.db.AppDatabase
import com.example.plantguide.db.Grains
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class FavoritesFragment : Fragment() {

    private var _binding: FragmentFavoritesBinding? = null
    private val binding get() = _binding!!
    private lateinit var adapter: GrainsAdapter
    private lateinit var database: AppDatabase

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFavoritesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        database = AppDatabase.getDatabase(requireContext())

        // Инициализация адаптера
        adapter = GrainsAdapter(emptyList()) { grain ->
            showGrainDetailsDialog(grain)
        }

        // Настройка RecyclerView
        binding.recyclerView.apply {
            layoutManager = GridLayoutManager(context, 2)
            adapter = this@FavoritesFragment.adapter
        }

        loadFavoriteGrains()
    }

    private fun loadFavoriteGrains() {
        CoroutineScope(Dispatchers.IO).launch {
            database.grainsDao().getFavoriteGrains().collect { favorites ->
                activity?.runOnUiThread {
                    if (favorites.isEmpty()) {
                        binding.emptyFavoritesText.visibility = View.VISIBLE
                        binding.recyclerView.visibility = View.GONE
                    } else {
                        binding.emptyFavoritesText.visibility = View.GONE
                        binding.recyclerView.visibility = View.VISIBLE
                        adapter.updateData(favorites)
                    }
                }
            }
        }
    }

    private fun showGrainDetailsDialog(grain: Grains) {
        val dialog = Dialog(requireContext())
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
        favButton.text = "Удалить из избранного"
        favButton.setBackgroundResource(R.drawable.bg_next_button_red)
        favButton.setTextColor(resources.getColor(android.R.color.white))

        // Обработка нажатия кнопки удаления из избранного
        favButton.setOnClickListener {
            CoroutineScope(Dispatchers.IO).launch {
                grain.isFavorite = false
                database.grainsDao().update(grain)

                // Обновляем UI
                activity?.runOnUiThread {
                    Toast.makeText(
                        context,
                        "Удалено из избранного",
                        Toast.LENGTH_SHORT
                    ).show()

                    // Закрываем диалог и обновляем список
                    dialog.dismiss()
                    loadFavoriteGrains()
                }
            }
        }


        // Настройка диалога
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        dialog.show()
    }

    override fun onResume() {
        super.onResume()
        loadFavoriteGrains()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}