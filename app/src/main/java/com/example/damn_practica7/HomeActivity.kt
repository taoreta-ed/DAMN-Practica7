package com.example.damn_practica7

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.damn_practica7.databinding.ActivityHomeBinding
import com.google.firebase.auth.FirebaseAuth

/**
 * HomeActivity es la actividad principal de la aplicación después de la autenticación.
 * Muestra una lista de comunidades y permite al usuario navegar a su perfil.
 */
class HomeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHomeBinding
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()

        // Mostrar el correo del usuario autenticado
        val currentUser = auth.currentUser
        currentUser?.email?.let { email ->
            binding.tvWelcomeMessage.text = "Bienvenido, $email"
        } ?: run {
            binding.tvWelcomeMessage.text = "Bienvenido"
        }

        // Configurar el botón de perfil
        binding.btnProfile.setOnClickListener {
            navigateToProfile()
        }

        // Configurar el RecyclerView de comunidades
        setupCommunitiesRecyclerView()
    }

    /**
     * Configura el RecyclerView con una lista predefinida de comunidades.
     */
    private fun setupCommunitiesRecyclerView() {
        // Lista de comunidades predefinidas
        val communities = listOf(
            Community("dev_apps", "Desarrollo de Apps", "Discute sobre programación móvil, frameworks y herramientas."),
            Community("videojuegos", "Videojuegos", "Comparte tus experiencias, noticias y trucos de videojuegos."),
            Community("anime", "Anime", "Todo lo relacionado con el mundo del anime y manga.")
        )

        // Inicializar el adaptador con la lista de comunidades y un listener de clic
        val adapter = CommunityAdapter(communities) { community ->
            // Cuando se hace clic en una comunidad, navegar a CommunityPostsActivity
            navigateToCommunityPosts(community)
        }

        // Configurar el RecyclerView
        binding.rvCommunities.layoutManager = LinearLayoutManager(this)
        binding.rvCommunities.adapter = adapter
    }

    /**
     * Navega a la actividad de perfil del usuario.
     */
    private fun navigateToProfile() {
        val intent = Intent(this, ProfileActivity::class.java)
        startActivity(intent)
    }

    /**
     * Navega a la actividad de publicaciones de una comunidad específica.
     * @param community El objeto Community al que se desea navegar.
     */
    private fun navigateToCommunityPosts(community: Community) {
        val intent = Intent(this, CommunityPostsActivity::class.java).apply {
            putExtra("communityId", community.id)
            putExtra("communityName", community.name)
        }
        startActivity(intent)
    }
}
