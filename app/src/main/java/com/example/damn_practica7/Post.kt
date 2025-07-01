package com.example.damn_practica7

import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

/**
 * Clase de datos que representa una publicación en una comunidad.
 * @param id Identificador único de la publicación.
 * @param communityId ID de la comunidad a la que pertenece la publicación.
 * @param title Título de la publicación.
 * @param description Descripción o contenido de la publicación.
 * @param authorId ID del usuario que realizó la publicación.
 * @param authorEmail Correo electrónico del usuario que realizó la publicación.
 * @param timestamp Marca de tiempo de cuándo se creó la publicación (generada por el servidor).
 */
data class Post(
    val id: String = "",
    val communityId: String = "",
    val title: String = "",
    val description: String = "",
    val authorId: String = "",
    val authorEmail: String = "",
    @ServerTimestamp // Anotación para que Firestore genere automáticamente la marca de tiempo
    val timestamp: Date? = null
)
