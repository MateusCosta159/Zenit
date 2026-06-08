package com.luizmateus.zenit.ui

import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.luizmateus.zenit.dao.PlantaDAO
import com.luizmateus.zenit.databinding.ActivityAdicionarPlantaBinding
import com.luizmateus.zenit.utils.Base64Converter
import com.luizmateus.zenit.model.Planta

class AdicionarPlantaActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAdicionarPlantaBinding
    private val plantaDAO = PlantaDAO()

    // string base64 da foto selecionada
    private var fotoBase64: String? = null

    // launcher da galeria usando PickVisualMedia (igual ao nativ)
    private val galeriaLauncher = registerForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            binding.imgFotoPlanta.setImageURI(uri)
            binding.imgFotoPlanta.drawable?.let {
                fotoBase64 = Base64Converter.drawableToString(it)
            }
        }
    }

    // launcher da câmera
    private val cameraLauncher = registerForActivityResult(
        ActivityResultContracts.TakePicturePreview()
    ) { bitmap ->
        if (bitmap != null) {
            binding.imgFotoPlanta.setImageBitmap(bitmap)
            fotoBase64 = Base64Converter.bitmapToString(bitmap)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityAdicionarPlantaBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        binding.btnVoltar.setOnClickListener { finish() }

        // abre dialog para escolher câmera ou galeria (igual ao nativ)
        binding.btnSelecionarFoto.setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle("Selecionar foto")
                .setItems(arrayOf("Câmera", "Galeria")) { _, which ->
                    if (which == 0) {
                        cameraLauncher.launch(null)
                    } else {
                        galeriaLauncher.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                    }
                }
                .show()
        }

        binding.btnSalvar.setOnClickListener { salvarPlanta() }
    }

    private fun salvarPlanta() {
        val nome = binding.edtNome.text.toString().trim()
        val especie = binding.edtEspecie.text.toString().trim()

        if (nome.isEmpty() || especie.isEmpty()) {
            Toast.makeText(this, "Preencha todos os campos", Toast.LENGTH_SHORT).show()
            return
        }

        // salva foto como base64 no Firestore (igual ao nativ)
        val planta = Planta(
            nome = nome,
            especie = especie,
            fotoUrl = fotoBase64 ?: ""
        )

        plantaDAO.salvarPlanta(planta) { sucesso ->
            if (sucesso) {
                Toast.makeText(this, "Planta salva!", Toast.LENGTH_SHORT).show()
                finish()
            } else {
                Toast.makeText(this, "Erro ao salvar", Toast.LENGTH_SHORT).show()
            }
        }
    }
}