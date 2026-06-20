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

        // Sensores
        sensorManager     = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        sensorLuz         = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT)
        sensorTemperatura = sensorManager.getDefaultSensor(Sensor.TYPE_AMBIENT_TEMPERATURE)

        // Saudação
        val email = userAuth.getEmailUsuarioLogado() ?: "usuário"
        binding.tvSaudacao.text = "Olá, ${email.substringBefore("@")}"

        // RecyclerView
        binding.rvPlantas.layoutManager = LinearLayoutManager(this)

        carregarPlantas()

        // Navegação — Bússola
        binding.btnBussola.setOnClickListener {
            startActivity(Intent(this, BussolaActivity::class.java))
        }

        // Navegação — Adicionar Planta
        binding.btnAdicionarPlanta.setOnClickListener {
            startActivity(Intent(this, AdicionarPlantaActivity::class.java))
        }

        // Calibrar Ambiente
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

        if (luz == null && temp == null) {
            Toast.makeText(this, "Sensores não disponíveis neste dispositivo", Toast.LENGTH_LONG).show()
            return
        }

        // Atualiza os cards da UI
        luz?.let  { binding.tvLuminosidade.text = "${it.toInt()} lx" }
        temp?.let { binding.tvTemperatura.text  = "${it.toInt()}°C"  }

        Toast.makeText(this, "Ambiente calibrado com sucesso!", Toast.LENGTH_SHORT).show()
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

    override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) { /* não utilizado */ }

    // ── Plantas ───────────────────────────────────────────────────────────────

    private fun carregarPlantas() {
        plantaDAO.listarPlantas { plantas ->
            if (plantas.isEmpty()) {
                binding.layoutVazio.visibility = View.VISIBLE
                binding.rvPlantas.visibility   = View.GONE
            } else {
                binding.layoutVazio.visibility = View.GONE
                binding.rvPlantas.visibility   = View.VISIBLE
                binding.rvPlantas.adapter = PlantaAdapter(plantas) { planta ->
                    val intent = Intent(this, DetalhesPlantaActivity::class.java)
                    intent.putExtra("plantaId", planta.id)
                    intent.putExtra("nome",     planta.nome)
                    intent.putExtra("especie",  planta.especie)
                    intent.putExtra("fotoUrl",  planta.fotoUrl)
                    startActivity(intent)
                }
            }
        }
    }
}