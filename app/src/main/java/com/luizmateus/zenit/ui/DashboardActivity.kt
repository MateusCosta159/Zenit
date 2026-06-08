package com.luizmateus.zenit.ui

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.luizmateus.zenit.adapter.PlantaAdapter
import com.luizmateus.zenit.auth.UserAuth
import com.luizmateus.zenit.dao.PlantaDAO
import com.luizmateus.zenit.databinding.ActivityDashboardBinding

class DashboardActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDashboardBinding
    private val userAuth = UserAuth()
    private val plantaDAO = PlantaDAO()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // saudacao com email do usuario
        val email = userAuth.getEmailUsuarioLogado() ?: "usuário"
        binding.tvSaudacao.text = "Olá, ${email.substringBefore("@")}"

        // configura recycler
        binding.rvPlantas.layoutManager = LinearLayoutManager(this)

        // carrega plantas do firestore
        carregarPlantas()

        // navega para adicionar planta
        binding.btnAdicionarPlanta.setOnClickListener {
            startActivity(Intent(this, AdicionarPlantaActivity::class.java))
        }
    }

    override fun onResume() {
        super.onResume()
        // recarrega ao voltar da tela de adicionar
        carregarPlantas()
    }

    private fun carregarPlantas() {
        plantaDAO.listarPlantas { plantas ->
            if (plantas.isEmpty()) {
                binding.layoutVazio.visibility = View.VISIBLE
                binding.rvPlantas.visibility = View.GONE
            } else {
                binding.layoutVazio.visibility = View.GONE
                binding.rvPlantas.visibility = View.VISIBLE
                binding.rvPlantas.adapter = PlantaAdapter(plantas) { planta ->
                    val intent = Intent(this, DetalhesPlantaActivity::class.java)
                    intent.putExtra("plantaId", planta.id)
                    intent.putExtra("nome", planta.nome)
                    intent.putExtra("especie", planta.especie)
                    intent.putExtra("fotoUrl", planta.fotoUrl)
                    startActivity(intent)
                }
            }
        }
    }
}