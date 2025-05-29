package com.example.icm_proyecto01.model

data class ExchangePoint(
    var tituloLibro: String = "",
    var estadoLibro: String = "",
    var fecha: String = "",
    var hora: String = "",
    var lat: Double = 0.0,
    var lon: Double = 0.0,
    var portadaUrl: String = "",
    var direccion: String = ""
)
