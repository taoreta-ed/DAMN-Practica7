package com.example.damn_practica7

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.damn_practica7.databinding.ActivityCreatePostBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

/**
 * CreatePostActivity permite a los usuarios crear una nueva publicación
 * en una comunidad específica.
 */
class CreatePostActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCreatePostBinding
    private lateinit var firestore: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private lateinit var communityId: String
    private lateinit var communityName: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCreatePostBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Inicializar Firebase
        firestore = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        // Obtener el ID y el nombre de la comunidad de los extras del Intent
        communityId = intent.getStringExtra("communityId") ?: ""
        communityName = intent.getStringExtra("communityName") ?: "Comunidad Desconocida"

        // Mostrar el nombre de la comunidad en el título
        binding.tvCreatePostTitle.text = "Crear Nueva Publicación en $communityName"

        // Configurar el botón de publicar
        binding.btnPublishPost.setOnClickListener {
            publishPost()
        }
    }

    /**
     * Publica la nueva entrada en Cloud Firestore.
     * Valida los campos y muestra mensajes de éxito o error.
     */
    private fun publishPost() {
        val title = binding.etPostTitle.text.toString().trim()
        val description = binding.etPostDescription.text.toString().trim()
        val currentUser = auth.currentUser

        // Validar campos y usuario
        if (title.isEmpty() || description.isEmpty()) {
            Toast.makeText(this, "Por favor, ingresa un título y una descripción.", Toast.LENGTH_SHORT).show()
            return
        }

        if (currentUser == null) {
            Toast.makeText(this, "No hay usuario autenticado. Por favor, inicia sesión.", Toast.LENGTH_SHORT).show()
            return
        }

        // Mostrar ProgressBar y deshabilitar botón
        showLoading(true)

        // Crear un objeto Post con los datos
        val newPost = Post(
            communityId = communityId,
            title = title,
            description = description,
            authorId = currentUser.uid,
            authorEmail = currentUser.email ?: "Anónimo" // Usar "Anónimo" si el correo no está disponible
        )

        // Añadir la publicación a la subcolección 'posts' de la comunidad en Firestore
        firestore.collection("communities")
            .document(communityId)
            .collection("posts")
            .add(newPost)
            .addOnSuccessListener {
                showLoading(false) // Ocultar ProgressBar
                Toast.makeText(this, "Publicación creada exitosamente.", Toast.LENGTH_SHORT).show()
                finish() // Volver a la actividad anterior (CommunityPostsActivity)
            }
            .addOnFailureListener { e ->
                showLoading(false) // Ocultar ProgressBar
                Toast.makeText(this, "Error al crear la publicación: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }

    /**
     * Muestra u oculta el ProgressBar y habilita/deshabilita los elementos de la UI.
     */
    private fun showLoading(isLoading: Boolean) {
        binding.progressBarCreatePost.visibility = if (isLoading) View.VISIBLE else View.GONE
        binding.btnPublishPost.isEnabled = !isLoading
        binding.etPostTitle.isEnabled = !isLoading
        binding.etPostDescription.isEnabled = !isLoading
    }
}
