package com.luizmateus.zenit.dao

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.luizmateus.zenit.model.Planta

class PlantaDAO {
    private val db = FirebaseFirestore.getInstance()
    private val uid get() = FirebaseAuth.getInstance().currentUser?.uid ?: ""

    // busca todas as plantas do usuario logado
    fun listarPlantas(callback: (List<Planta>) -> Unit) {
        db.collection("usuarios").document(uid)
            .collection("plantas")
            .get()
            .addOnSuccessListener { result ->
                val lista = result.documents.mapNotNull { doc ->
                    doc.toObject(Planta::class.java)?.copy(id = doc.id)
                }
                callback(lista)
            }
            .addOnFailureListener { callback(emptyList()) }
    }

    // salva uma nova planta
    fun salvarPlanta(planta: Planta, callback: (Boolean) -> Unit) {
        db.collection("usuarios").document(uid)
            .collection("plantas")
            .add(planta)
            .addOnSuccessListener { callback(true) }
            .addOnFailureListener { callback(false) }
    }
}