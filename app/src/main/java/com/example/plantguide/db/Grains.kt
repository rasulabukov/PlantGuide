package com.example.plantguide.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "grains")
data class Grains(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val englishName: String,
    val species: String,
    val englishSpecies: String,
    val subspecies: String,
    val shortDescription: String,
    val englishShortDescription: String,
    val fullDescription: String,
    val englishFullDescription: String,
    val climateConditions: String,
    val englishClimateConditions: String,
    val yield: String,
    val englishYield: String,
    val diseases: String,
    val englishDiseases: String,
    val usage: String,
    val englishUsage: String,
    val growingRegions: String,
    val englishGrowingRegions: String,
    val imageResId: Int,
    var isFavorite: Boolean = false
)