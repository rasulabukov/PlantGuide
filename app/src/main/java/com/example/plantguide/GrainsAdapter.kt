package com.example.plantguide

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.plantguide.db.Grains
import com.example.plantguide.databinding.GrainSelectBinding

class GrainsAdapter(
    private var grains: List<Grains>,
    private val onItemClick: (Grains) -> Unit
) : RecyclerView.Adapter<GrainsAdapter.GrainViewHolder>() {

    inner class GrainViewHolder(private val binding: GrainSelectBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(grain: Grains) {
            binding.grainName.text = grain.name
            binding.grainCategory.text = grain.species
            binding.grainImage.setImageResource(grain.imageResId)

            binding.root.setOnClickListener {
                onItemClick(grain)
            }
        }
    }

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