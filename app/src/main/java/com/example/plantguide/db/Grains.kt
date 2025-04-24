package com.example.plantguide.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "grains")
data class Grains(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,                   // Название культуры
    val species: String,                // Вид
    val subspecies: String,             // Подвид
    val shortDescription: String,       // Краткое описание для карточки
    val fullDescription: String,        // Полное описание
    val climateConditions: String,      // Климатические условия
    val yield: String,                  // Урожайность
    val diseases: String,               // Болезни и вредители
    val usage: String,                  // Использование
    val growingRegions: String,         // Регионы произрастания
    val imageResId: Int,                // ID изображения
    var isFavorite: Boolean = false     // В избранном
)