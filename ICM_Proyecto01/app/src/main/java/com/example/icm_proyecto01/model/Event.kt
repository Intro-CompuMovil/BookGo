package com.example.icm_proyecto01.model

data class Event(
    val userId: String = "",
    val name: String = "",
    val location: String = "",
    val date: String = "",
    val description: String = "",
    val lat: Double = 0.0,
    val lon: Double = 0.0
)
