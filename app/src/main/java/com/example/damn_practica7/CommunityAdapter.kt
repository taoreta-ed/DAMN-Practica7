package com.example.damn_practica7

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

/**
 * Adaptador para mostrar una lista de comunidades en un RecyclerView.
 * @param communities Lista de objetos Community a mostrar.
 * @param onItemClick Listener para cuando se hace clic en un elemento de la comunidad.
 */
class CommunityAdapter(
    private val communities: List<Community>,
    private val onItemClick: (Community) -> Unit
) : RecyclerView.Adapter<CommunityAdapter.CommunityViewHolder>() {

    /**
     * ViewHolder para cada elemento de la comunidad en el RecyclerView.
     */
    class CommunityViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val communityName: TextView = itemView.findViewById(R.id.tv_community_name)
        val communityDescription: TextView = itemView.findViewById(R.id.tv_community_description)
    }

    /**
     * Crea y devuelve un nuevo ViewHolder.
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CommunityViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_community, parent, false)
        return CommunityViewHolder(view)
    }

    /**
     * Asocia los datos de una comunidad con un ViewHolder.
     */
    override fun onBindViewHolder(holder: CommunityViewHolder, position: Int) {
        val community = communities[position]
        holder.communityName.text = community.name
        holder.communityDescription.text = community.description
        // Configura el listener de clic para todo el elemento del CardView
        holder.itemView.setOnClickListener {
            onItemClick(community)
        }
    }

    /**
     * Devuelve el número total de elementos en la lista de comunidades.
     */
    override fun getItemCount(): Int {
        return communities.size
    }
}
