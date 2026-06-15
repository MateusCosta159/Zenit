package com.luizmateus.zenit.ui

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.view.animation.Animation
import android.view.animation.RotateAnimation
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
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

    // ── Lifecycle ─────────────────────────────────────────────────────────────

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_bussola)

        setupToolbar()
        bindViews()
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

    private fun setupToolbar() {
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(true)
        toolbar.setNavigationOnClickListener { finish() }
    }

    private fun bindViews() {
        ivAgulha     = findViewById(R.id.ivAgulha)
        tvGraus      = findViewById(R.id.tvGraus)
        tvDirecao    = findViewById(R.id.tvDirecao)
        tvDicaSolar  = findViewById(R.id.tvDicaSolar)
        tvIconeDica  = findViewById(R.id.tvIconeDica)
        tvCalibration = findViewById(R.id.tvCalibration)
    }

    private fun setupSensors() {
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        magnetometer  = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
    }

    // ── SensorEventListener ───────────────────────────────────────────────────

    override fun onSensorChanged(event: SensorEvent) {
        // Filtro de baixo-passa para suavizar leituras
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

        // Direção cardinal e dica solar
        val (direcao, emoji, dica) = calcularDirecaoEDica(azimute)
        tvDirecao.text  = direcao
        tvIconeDica.text = emoji
        tvDicaSolar.text = dica

        // Animação suave da agulha
        // A agulha aponta para o Norte real, então rotaciona no sentido oposto ao azimute
        val rotacaoAlvo = -azimute
        val rotacaoAnterior = -azimuleAnterior

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
     * Voltado para jardinagem urbana — contexto do Zenit.
     */
    private fun calcularDirecaoEDica(azimute: Float): Triple<String, String, String> {
        return when {
            azimute < 22.5 || azimute >= 337.5 -> Triple(
                "Norte",
                "🌿",
                "Face Norte: ideal para plantas que preferem sombra parcial ou luz indireta, como samambaias e lírios-da-paz."
            )
            azimute < 67.5 -> Triple(
                "Nordeste",
                "🌤",
                "Face Nordeste: sol da manhã com tarde amena. Ótimo para ervas aromáticas como manjericão e hortelã."
            )
            azimute < 112.5 -> Triple(
                "Leste",
                "☀️",
                "Face Leste: sol matinal suave. Excelente para begônias, violetas e plantas floríferas de interior."
            )
            azimute < 157.5 -> Triple(
                "Sudeste",
                "🌻",
                "Face Sudeste: boa luminosidade durante a manhã. Adequado para cactos e suculentas em climas tropicais."
            )
            azimute < 202.5 -> Triple(
                "Sul",
                "🔆",
                "Face Sul (Hemisfério Sul): maior incidência solar durante o dia. Ideal para plantas que exigem pleno sol."
            )
            azimute < 247.5 -> Triple(
                "Sudoeste",
                "🌵",
                "Face Sudoeste: sol da tarde intenso. Adequado para cactos, aloe vera e plantas resistentes ao calor."
            )
            azimute < 292.5 -> Triple(
                "Oeste",
                "🍃",
                "Face Oeste: sol vespertino forte. Prefira plantas adaptadas ao calor, como bougainvilleas e lantanas."
            )
            else -> Triple(
                "Noroeste",
                "🌱",
                "Face Noroeste: luz moderada à tarde. Boa opção para roseiras e plantas que toleram sol parcial."
            )
        }
    }
}