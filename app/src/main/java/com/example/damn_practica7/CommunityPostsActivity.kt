package com.example.damn_practica7

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.damn_practica7.databinding.ActivityCommunityPostsBinding
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.auth.FirebaseAuth // Necesario para obtener el usuario actual

/**
 * CommunityPostsActivity muestra las publicaciones de una comunidad específica
 * y permite a los usuarios crear nuevas publicaciones.
 */
class CommunityPostsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCommunityPostsBinding
    private lateinit var firestore: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private lateinit var communityId: String
    private lateinit var communityName: String
    private lateinit var postAdapter: PostAdapter
    private val postsList = mutableListOf<Post>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCommunityPostsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Inicializar Firebase
        firestore = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        // Obtener el ID y el nombre de la comunidad de los extras del Intent
        communityId = intent.getStringExtra("communityId") ?: ""
        communityName = intent.getStringExtra("communityName") ?: "Comunidad Desconocida"

        // Mostrar el nombre de la comunidad en el encabezado
        binding.tvCommunityNameHeader.text = "Comunidad: $communityName"

        // Configurar el RecyclerView
        setupRecyclerView()

        // Cargar las publicaciones de la comunidad
        loadCommunityPosts()

        // Configurar el FloatingActionButton para añadir nuevas publicaciones
        binding.fabAddPost.setOnClickListener {
            navigateToCreatePost()
        }
    }

    /**
     * Configura el RecyclerView para mostrar las publicaciones.
     */
    private fun setupRecyclerView() {
        postAdapter = PostAdapter(postsList)
        binding.rvPosts.apply {
            layoutManager = LinearLayoutManager(this@CommunityPostsActivity)
            adapter = postAdapter
        }
    }

    /**
     * Carga las publicaciones de la comunidad desde Cloud Firestore.
     * Utiliza un listener en tiempo real (onSnapshot) para actualizar la UI automáticamente.
     */
    private fun loadCommunityPosts() {
        if (communityId.isEmpty()) {
            Toast.makeText(this, "ID de comunidad no válido.", Toast.LENGTH_SHORT).show()
            return
        }

        firestore.collection("communities")
            .document(communityId)
            .collection("posts")
            // Ordenar publicaciones por timestamp en orden descendente para ver las más recientes primero
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshots, e ->
                if (e != null) {
                    Log.w("CommunityPostsActivity", "Listen failed.", e)
                    Toast.makeText(this, "Error al cargar publicaciones.", Toast.LENGTH_SHORT).show()
                    return@addSnapshotListener
                }

                if (snapshots != null) {
                    val newPosts = mutableListOf<Post>()
                    for (doc in snapshots.documents) {
                        val post = doc.toObject(Post::class.java)
                        post?.let {
                            newPosts.add(it.copy(id = doc.id)) // Asegurarse de que el ID del documento se guarde en el objeto Post
                        }
                    }
                    postAdapter.updatePosts(newPosts) // Actualizar el adaptador con las nuevas publicaciones
                } else {
                    Log.d("CommunityPostsActivity", "Current data: null")
                }
            }
    }

    /**
     * Navega a la actividad para crear una nueva publicación.
     * Pasa el ID y el nombre de la comunidad a la nueva actividad.
     */
    private fun navigateToCreatePost() {
        val intent = Intent(this, CreatePostActivity::class.java).apply {
            putExtra("communityId", communityId)
            putExtra("communityName", communityName)
        }
        startActivity(intent)
    }
}
