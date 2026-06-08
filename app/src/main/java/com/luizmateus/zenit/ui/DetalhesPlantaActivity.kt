package com.luizmateus.zenit.ui

import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.luizmateus.zenit.dao.PlantaDAO
import com.luizmateus.zenit.databinding.ActivityDetalhesPlantaBinding
import com.luizmateus.zenit.utils.Base64Converter

class DetalhesPlantaActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDetalhesPlantaBinding
    private val plantaDAO = PlantaDAO()
    private var plantaId: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityDetalhesPlantaBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // recebe dados da planta via intent
        plantaId = intent.getStringExtra("plantaId") ?: ""
        val nome = intent.getStringExtra("nome") ?: ""
        val especie = intent.getStringExtra("especie") ?: ""
        val fotoBase64 = intent.getStringExtra("fotoUrl") ?: ""

        binding.tvNomeDetalhes.text = nome
        binding.tvEspecieDetalhes.text = especie

        if (fotoBase64.isNotEmpty()) {
            runCatching {
                val bitmap = Base64Converter.stringToBitmap(fotoBase64)
                binding.imgDetalhes.setImageBitmap(bitmap)
            }
        }

        binding.btnVoltar.setOnClickListener { finish() }

        binding.btnExcluir.setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle("Excluir planta")
                .setMessage("Tem certeza que deseja excluir ${nome}?")
                .setPositiveButton("Excluir") { _, _ ->
                    plantaDAO.excluirPlanta(plantaId) { sucesso ->
                        if (sucesso) {
                            Toast.makeText(this, "Planta excluída", Toast.LENGTH_SHORT).show()
                            finish()
                        } else {
                            Toast.makeText(this, "Erro ao excluir", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
                .setNegativeButton("Cancelar", null)
                .show()
        }
    }
}