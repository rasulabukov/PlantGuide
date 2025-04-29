package com.example.plantguide

import android.content.Context
import android.content.SharedPreferences
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.plantguide.db.Grains
import com.example.plantguide.databinding.GrainSelectBinding

class GrainsAdapter(
    private var grains: List<Grains>,
    private val onItemClick: (Grains) -> Unit,
    private val sharedPreferences: SharedPreferences
) : RecyclerView.Adapter<GrainsAdapter.GrainViewHolder>() {

    private val isEnglish: Boolean
        get() {
            // Получаем язык из SharedPreferences, по умолчанию "ru"
            val lang = sharedPreferences.getString("app_language", "ru") ?: "ru"
            return lang == "en"
        }

    inner class GrainViewHolder(private val binding: GrainSelectBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(grain: Grains) {
            if (isEnglish) {
                binding.grainName.text = grain.englishName
                binding.grainCategory.text = grain.englishSpecies
            } else {
                binding.grainName.text = grain.name
                binding.grainCategory.text = grain.species
            }

            binding.grainImage.setImageResource(grain.imageResId)
            binding.root.setOnClickListener { onItemClick(grain) }
        }
    }

    // Остальные методы адаптера остаются без изменений
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GrainViewHolder {
        val binding = GrainSelectBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return GrainViewHolder(binding)
    }

    override fun onBindViewHolder(holder: GrainViewHolder, position: Int) {
        holder.bind(grains[position])
    }

    override fun getItemCount() = grains.size

    fun updateData(newGrains: List<Grains>) {
        grains = newGrains
        notifyDataSetChanged()
    }
}