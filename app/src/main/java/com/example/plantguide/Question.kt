package com.example.plantguide

data class Question(
    val text: String,
    val answers: List<String>,
    val correctAnswer: Int
)