package com.luizmateus.zenit.ui

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.view.animation.Animation
import android.view.animation.RotateAnimation
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.luizmateus.zenit.R
import kotlin.math.roundToInt

class BussolaActivity : AppCompatActivity(), SensorEventListener {

    // ── Sensor ────────────────────────────────────────────────────────────────
    private lateinit var sensorManager: SensorManager
    private var magnetometer: Sensor? = null
    private var accelerometer: Sensor? = null

    // Dados brutos dos sensores
    private val gravity    = FloatArray(3)
    private val geomagnetic = FloatArray(3)
    private var hasMag  = false
    private var hasAccel = false

    // Controle de rotação da agulha
    private var azimuleAtual: Float = 0f
    private var azimuleAnterior: Float = 0f

    // ── Views ─────────────────────────────────────────────────────────────────
    private lateinit var ivAgulha: ImageView
    private lateinit var tvGraus: TextView
    private lateinit var tvDirecao: TextView
    private lateinit var tvDicaSolar: TextView
    private lateinit var tvIconeDica: TextView
    private lateinit var tvCalibration: TextView
    private lateinit var btnVoltarBussola: ImageButton

    // ── Lifecycle ─────────────────────────────────────────────────────────────

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_bussola)

        bindViews()
        setupClickListeners()
        setupSensors()
    }

    override fun onResume() {
        super.onResume()
        magnetometer?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_UI)
        }
        accelerometer?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_UI)
        }
        if (magnetometer == null || accelerometer == null) {
            tvCalibration.text = "⚠  Sensor de bússola não disponível neste dispositivo"
        }
    }

    override fun onPause() {
        super.onPause()
        sensorManager.unregisterListener(this)
    }

    // ── Setup ─────────────────────────────────────────────────────────────────

    private fun bindViews() {
        ivAgulha         = findViewById(R.id.ivAgulha)
        tvGraus          = findViewById(R.id.tvGraus)
        tvDirecao        = findViewById(R.id.tvDirecao)
        tvDicaSolar      = findViewById(R.id.tvDicaSolar)
        tvIconeDica      = findViewById(R.id.tvIconeDica)
        tvCalibration    = findViewById(R.id.tvCalibration)
        btnVoltarBussola = findViewById(R.id.btnVoltarBussola) // ID atualizado do novo XML
    }

    private fun setupClickListeners() {
        btnVoltarBussola.setOnClickListener {
            finish()
        }
    }

    private fun setupSensors() {
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        magnetometer  = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
    }

    // ── SensorEventListener ───────────────────────────────────────────────────

    override fun onSensorChanged(event: SensorEvent) {
        // Filtro passa-baixas para suavizar leituras
        val alpha = 0.15f

        when (event.sensor.type) {
            Sensor.TYPE_ACCELEROMETER -> {
                gravity[0] = alpha * event.values[0] + (1 - alpha) * gravity[0]
                gravity[1] = alpha * event.values[1] + (1 - alpha) * gravity[1]
                gravity[2] = alpha * event.values[2] + (1 - alpha) * gravity[2]
                hasAccel = true
            }
            Sensor.TYPE_MAGNETIC_FIELD -> {
                geomagnetic[0] = alpha * event.values[0] + (1 - alpha) * geomagnetic[0]
                geomagnetic[1] = alpha * event.values[1] + (1 - alpha) * geomagnetic[1]
                geomagnetic[2] = alpha * event.values[2] + (1 - alpha) * geomagnetic[2]
                hasMag = true
            }
        }

        if (!hasMag || !hasAccel) return

        val rotationMatrix = FloatArray(9)
        val inclinationMatrix = FloatArray(9)
        val success = SensorManager.getRotationMatrix(
            rotationMatrix, inclinationMatrix, gravity, geomagnetic
        )

        if (!success) return

        val orientation = FloatArray(3)
        SensorManager.getOrientation(rotationMatrix, orientation)

        // Azimute em graus (0° = Norte, 90° = Leste, 180° = Sul, 270° = Oeste)
        azimuleAtual = Math.toDegrees(orientation[0].toDouble()).toFloat()
        azimuleAtual = (azimuleAtual + 360) % 360

        atualizarUI(azimuleAtual)
    }

    override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {
        val statusTexto = when (accuracy) {
            SensorManager.SENSOR_STATUS_ACCURACY_HIGH   -> "⬤  Alta precisão"
            SensorManager.SENSOR_STATUS_ACCURACY_MEDIUM -> "⬤  Precisão média — gire o celular em 8 para calibrar"
            SensorManager.SENSOR_STATUS_ACCURACY_LOW    -> "⬤  Precisão baixa — afaste-se de superfícies metálicas"
            else                                         -> "⬤  Calibrando sensor..."
        }
        tvCalibration.text = statusTexto
    }

    // ── UI ────────────────────────────────────────────────────────────────────

    private fun atualizarUI(azimute: Float) {
        // Texto do ângulo
        tvGraus.text = "${azimute.roundToInt()}°"

        // Direção cardinal e dica solar corrigida
        val (direcao, emoji, dica) = calcularDirecaoEDica(azimute)
        tvDirecao.text  = direcao
        tvIconeDica.text = emoji
        tvDicaSolar.text = dica

        // Animação suave da agulha
        val rotacaoAlvo = -azimute
        val  rotacaoAnterior = -azimuleAnterior

        val anim = RotateAnimation(
            rotacaoAnterior,
            rotacaoAlvo,
            Animation.RELATIVE_TO_SELF, 0.5f,
            Animation.RELATIVE_TO_SELF, 0.5f
        ).apply {
            duration      = 250
            fillAfter     = true
            interpolator  = android.view.animation.DecelerateInterpolator()
        }

        ivAgulha.startAnimation(anim)
        azimuleAnterior = azimute
    }

    /**
     * Retorna Triple<direcaoCardinal, emoji, dicaSolar> com base no azimute.
     * Corrigido para a realidade geográfica do Hemisfério Sul (Brasil).
     */
    private fun calcularDirecaoEDica(azimute: Float): Triple<String, String, String> {
        return when {
            azimute < 22.5 || azimute >= 337.5 -> Triple(
                "Norte",
                "🔆",
                "Face Norte: Maior incidência solar o ano todo. Ideal para plantas de pleno sol, como cactos, suculentas e frutíferas."
            )
            azimute < 67.5 -> Triple(
                "Nordeste",
                "🌻",
                "Face Nordeste: Excelente sol da manhã e ótima luminosidade. Muito boa para a maioria das plantas de horta."
            )
            azimute < 112.5 -> Triple(
                "Leste",
                "☀️",
                "Face Leste: Sol matinal suave e ameno. Perfeito para begônias, samambaias, violetas e orquídeas."
            )
            azimute < 157.5 -> Triple(
                "Sudeste",
                "🌤",
                "Face Sudeste: Recebe luz clara pela manhã. Adequado para plantas de meia-sombra que evitam o calor forte."
            )
            azimute < 202.5 -> Triple(
                "Sul",
                "🌿",
                "Face Sul: Menor incidência de sol direto. Ideal para plantas que preferem sombra ou luz indireta, como Lírio-da-paz."
            )
            azimute < 247.5 -> Triple(
                "Sudoeste",
                "🍃",
                "Face Sudoeste: Luz indireta pela manhã e sol mais forte no fim do dia. Boa para plantas resistentes."
            )
            azimute < 292.5 -> Triple(
                "Oeste",
                "🌵",
                "Face Oeste: Recebe o sol forte e intenso da tarde. Prefira plantas que toleram calor extremo, como Bougainvillea."
            )
            else -> Triple(
                "Noroeste",
                "🌱",
                "Face Noroeste: Muito iluminada no período da tarde. Boa opção para roseiras e arbustos que gostam de calor."
            )
        }
    }
}