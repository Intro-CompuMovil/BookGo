package com.example.icm_proyecto01.model

import java.io.Serializable

data class ExchangePoint(
    val tituloLibro: String,
    val direccion: String,
    val fecha: String,
    val hora: String,
    val lat: Double,
    val lon: Double,
    val estadoLibro: String = "",
    val portadaUrl: String = ""
) : Serializable