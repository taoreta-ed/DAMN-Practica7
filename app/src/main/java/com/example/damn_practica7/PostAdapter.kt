package com.example.damn_practica7

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.text.SimpleDateFormat
import java.util.Locale

/**
 * Adaptador para mostrar una lista de publicaciones en un RecyclerView.
 * @param posts Lista mutable de objetos Post a mostrar.
 */
class PostAdapter(private val posts: MutableList<Post>) : RecyclerView.Adapter<PostAdapter.PostViewHolder>() {

    /**
     * ViewHolder para cada elemento de publicación en el RecyclerView.
     */
    class PostViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val postTitle: TextView = itemView.findViewById(R.id.tv_post_title)
        val postDescription: TextView = itemView.findViewById(R.id.tv_post_description)
        val postAuthor: TextView = itemView.findViewById(R.id.tv_post_author)
        val postDate: TextView = itemView.findViewById(R.id.tv_post_date)
    }

    /**
     * Crea y devuelve un nuevo ViewHolder.
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_post, parent, false)
        return PostViewHolder(view)
    }

    /**
     * Asocia los datos de una publicación con un ViewHolder.
     */
    override fun onBindViewHolder(holder: PostViewHolder, position: Int) {
        val post = posts[position]
        holder.postTitle.text = post.title
        holder.postDescription.text = post.description
        holder.postAuthor.text = "Publicado por: ${post.authorEmail}"

        // Formatear la fecha si está disponible
        post.timestamp?.let {
            val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
            holder.postDate.text = "Fecha: ${sdf.format(it)}"
        } ?: run {
            holder.postDate.text = "Fecha: N/A"
        }
    }

    /**
     * Devuelve el número total de elementos en la lista de publicaciones.
     */
    override fun getItemCount(): Int {
        return posts.size
    }

    /**
     * Actualiza la lista de publicaciones y notifica al adaptador sobre los cambios.
     * @param newPosts La nueva lista de publicaciones.
     */
    fun updatePosts(newPosts: List<Post>) {
        posts.clear()
        posts.addAll(newPosts)
        notifyDataSetChanged() // Notifica al RecyclerView que los datos han cambiado
    }
}
