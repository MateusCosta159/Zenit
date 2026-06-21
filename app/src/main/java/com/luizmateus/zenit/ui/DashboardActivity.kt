package com.luizmateus.zenit.ui

import android.content.Context
import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.luizmateus.zenit.adapter.PlantaAdapter
import com.luizmateus.zenit.auth.UserAuth
import com.luizmateus.zenit.dao.PlantaDAO
import com.luizmateus.zenit.databinding.ActivityDashboardBinding
import com.luizmateus.zenit.model.Planta
import com.luizmateus.zenit.utils.NotificacaoHelper

class DashboardActivity : AppCompatActivity(), SensorEventListener {

    private lateinit var binding: ActivityDashboardBinding
    private val userAuth  = UserAuth()
    private val plantaDAO = PlantaDAO()

    // ── Sensores ──────────────────────────────────────────────────────────────
    private lateinit var sensorManager: SensorManager
    private var sensorLuz: Sensor?         = null
    private var sensorTemperatura: Sensor? = null

    private var leituraLuz: Float?         = null
    private var leituraTemperatura: Float? = null
    private var calibrandoAtivo: Boolean   = false

    // ── Lifecycle ─────────────────────────────────────────────────────────────

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

        NotificacaoHelper.criarCanal(this)

        sensorManager     = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        sensorLuz         = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT)
        sensorTemperatura = sensorManager.getDefaultSensor(Sensor.TYPE_AMBIENT_TEMPERATURE)

        val email = userAuth.getEmailUsuarioLogado() ?: "usuário"
        binding.tvSaudacao.text = "Olá, ${email.substringBefore("@")}"

        binding.rvPlantas.layoutManager = LinearLayoutManager(this)

        carregarPlantas()

        binding.btnBussola.setOnClickListener {
            startActivity(Intent(this, BussolaActivity::class.java))
        }

        binding.btnAdicionarPlanta.setOnClickListener {
            startActivity(Intent(this, AdicionarPlantaActivity::class.java))
        }

        binding.btnConfiguracoes.setOnClickListener {
            startActivity(Intent(this, ConfiguracoesActivity::class.java))
        }

        binding.btnCalibrar.setOnClickListener {
            iniciarCalibracao()
        }
    }

    override fun onResume() {
        super.onResume()
        carregarPlantas()
    }

    override fun onPause() {
        super.onPause()
        pararSensores()
    }

    // ── Calibração ────────────────────────────────────────────────────────────

    private fun iniciarCalibracao() {
        if (calibrandoAtivo) return

        calibrandoAtivo    = true
        leituraLuz         = null
        leituraTemperatura = null

        binding.btnCalibrar.isEnabled = false
        binding.btnCalibrar.text      = "Calibrando..."

        sensorLuz?.let         { sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_NORMAL) }
        sensorTemperatura?.let { sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_NORMAL) }

        binding.root.postDelayed({ finalizarCalibracao() }, 3000)
    }

    private fun finalizarCalibracao() {
        pararSensores()
        calibrandoAtivo = false

        binding.btnCalibrar.isEnabled = true
        binding.btnCalibrar.text      = "Calibrar Ambiente"

        val luz  = leituraLuz
        val temp = leituraTemperatura

        // ── Dados mockados para demo quando sensor não está disponível ──
        val luzFinal  = luz  ?: 3200f   // lux típico de ambiente interno bem iluminado
        val tempFinal = temp ?: 24f     // temperatura ambiente confortável

        binding.tvLuminosidade.text = "${luzFinal.toInt()} lx"
        binding.tvTemperatura.text  = "${tempFinal.toInt()}°C"

        if (luz == null && temp == null) {
            Toast.makeText(this, "Sensor indisponível — exibindo dados de exemplo", Toast.LENGTH_SHORT).show()
        }

        NotificacaoHelper.tocarSomConfirmacao()
        NotificacaoHelper.notificarAmbiente(this, tempFinal, luzFinal)
    }

    private fun pararSensores() {
        sensorManager.unregisterListener(this)
    }

    // ── SensorEventListener ───────────────────────────────────────────────────

    override fun onSensorChanged(event: SensorEvent) {
        when (event.sensor.type) {
            Sensor.TYPE_LIGHT               -> leituraLuz         = event.values[0]
            Sensor.TYPE_AMBIENT_TEMPERATURE -> leituraTemperatura = event.values[0]
        }
    }

    override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {}

    // ── Plantas ───────────────────────────────────────────────────────────────

    private fun carregarPlantas() {
        plantaDAO.listarPlantas { plantas ->
            val listagem = plantas.ifEmpty { plantasMock() }

            if (listagem === plantasMock() && plantas.isEmpty()) {
                // Firestore vazio — exibe mocks com flag visual
                binding.layoutVazio.visibility = View.GONE
                binding.rvPlantas.visibility   = View.VISIBLE
            } else if (plantas.isEmpty()) {
                binding.layoutVazio.visibility = View.VISIBLE
                binding.rvPlantas.visibility   = View.GONE
                return@listarPlantas
            } else {
                binding.layoutVazio.visibility = View.GONE
                binding.rvPlantas.visibility   = View.VISIBLE
            }

            binding.rvPlantas.adapter = PlantaAdapter(listagem) { planta ->
                val intent = Intent(this, DetalhesPlantaActivity::class.java)
                intent.putExtra("plantaId", planta.id)
                intent.putExtra("nome",     planta.nome)
                intent.putExtra("especie",  planta.especie)
                intent.putExtra("fotoUrl",  planta.fotoUrl)
                startActivity(intent)
            }
        }
    }

    /**
     * Plantas de exemplo para demo e testes quando o Firestore está vazio.
     * Remove ou substitua após popular o banco real.
     */
    private fun plantasMock(): List<Planta> = listOf(
        Planta(
            id      = "mock_1",
            nome    = "Samambaia",
            especie = "Nephrolepis exaltata",
            fotoUrl = ""
        ),
        Planta(
            id      = "mock_2",
            nome    = "Espada-de-são-jorge",
            especie = "Dracaena trifasciata",
            fotoUrl = ""
        ),
        Planta(
            id      = "mock_3",
            nome    = "Lírio-da-paz",
            especie = "Spathiphyllum wallisii",
            fotoUrl = ""
        )
    )
}