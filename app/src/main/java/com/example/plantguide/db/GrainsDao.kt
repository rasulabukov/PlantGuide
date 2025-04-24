package com.example.plantguide.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface GrainsDao {
    // Добавление новой культуры
    @Insert
    suspend fun insert(grain: kotlin.collections.List<com.example.plantguide.db.Grains>)

    // Обновление данных о культуре
    @Update
    fun update(grain: Grains)

    // Получение всех культур (Flow для наблюдения)
    @Query("SELECT * FROM grains")
    fun getAllGrains(): Flow<List<Grains>>

    // Получение культуры по ID
    @Query("SELECT * FROM grains WHERE id = :id")
    suspend fun getGrainById(id: Int): Grains?

    // Поиск по названию и описанию
    @Query("SELECT * FROM grains WHERE name LIKE :query OR shortDescription LIKE :query OR fullDescription LIKE :query")
    fun searchGrains(query: String): Flow<List<Grains>>

    // Фильтрация по регионам произрастания
    @Query("SELECT * FROM grains WHERE growingRegions LIKE :region")
    fun filterByRegion(region: String): Flow<List<Grains>>

    // Получение избранных культур
    @Query("SELECT * FROM grains WHERE isFavorite = 1")
    fun getFavoriteGrains(): Flow<List<Grains>>

    // Получение культур по виду (для фильтра)
    @Query("SELECT * FROM grains WHERE species = :species")
    fun filterBySpecies(species: String): Flow<List<Grains>>

    @Query("SELECT * FROM grains WHERE name LIKE '%' || :query || '%'")
    suspend fun searchGrainsByName(query: String): List<Grains>

    @Query("SELECT * FROM grains WHERE growingRegions LIKE '%' || :query || '%'")
    suspend fun searchGrainsByRegion(query: String): List<Grains>

    @Query("SELECT * FROM grains WHERE usage LIKE '%' || :query || '%'")
    suspend fun searchGrainsByUsage(query: String): List<Grains>

    @Query("SELECT * FROM grains WHERE species LIKE '%' || :query || '%'")
    suspend fun searchGrainsBySpecies(query: String): List<Grains>

    @Query("SELECT * FROM grains")
    suspend fun getAllGrainsList(): List<Grains>
}