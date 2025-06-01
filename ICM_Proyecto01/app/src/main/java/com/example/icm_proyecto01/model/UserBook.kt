package com.example.icm_proyecto01.model
import java.io.Serializable


data class UserBook(
    val id : String,
    val titulo: String,
    val autor: String,
    val genero: String,
    val estado: String,
    val portadaUrl: String,
    var hidden : Boolean,
    var status : String
): Serializable
