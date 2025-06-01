package com.example.icm_proyecto01.model

data class ExchangePoint(
    val exchangePointId: String = "",
    val tituloLibro: String = "",
    val estadoLibro: String = "",
    val fecha: String = "",
    val hora: String = "",
    val lat: Double = 0.0,
    val lon: Double = 0.0,
    val portadaUrl: String = "",
    val direccion: String = "",
    val receiverUserId: String = ""
)



