package com.example.icm_proyecto01.model
import java.io.Serializable


data class UserBook(
    val titulo: String,
    val autor: String,
    val genero: String,
    val estado: String,
    val portadaUrl: String
): Serializable
