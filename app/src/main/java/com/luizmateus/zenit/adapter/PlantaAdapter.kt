package com.luizmateus.zenit.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.luizmateus.zenit.databinding.ItemPlantaBinding
import com.luizmateus.zenit.model.Planta

class PlantaAdapter(
    private val lista: List<Planta>,
    private val onClick: (Planta) -> Unit
) : RecyclerView.Adapter<PlantaAdapter.ViewHolder>() {

    inner class ViewHolder(val binding: ItemPlantaBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemPlantaBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val planta = lista[position]
        holder.binding.tvNomePlanta.text = planta.nome
        holder.binding.tvAmbiente.text = planta.ambiente
        holder.binding.root.setOnClickListener { onClick(planta) }
    }

    override fun getItemCount() = lista.size
}