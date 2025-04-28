package com.example.icm_proyecto01.model

import java.io.Serializable

data class User(
    val uid: String,
    val nombre: String,
    val correo: String,
    val contraseña: String,
    val fotoPerfilUrl: String,
    val readerLvl: Int = 0,
    val libros: List<UserBook> = emptyList()
) : Serializable