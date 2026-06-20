package com.luizmateus.zenit.utils

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.media.AudioManager
import android.media.ToneGenerator
import android.os.Build
import androidx.core.app.NotificationCompat
import com.luizmateus.zenit.R

object NotificacaoHelper {

    private const val CHANNEL_ID   = "zenit_ambiente"
    private const val CHANNEL_NAME = "Alertas de Ambiente"
    private const val NOTIF_ID     = 1001

    fun criarCanal(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val canal = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Notificações sobre temperatura e luminosidade do ambiente"
            }
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(canal)
        }
    }

    fun notificarAmbiente(context: Context, temperatura: Float?, luminosidade: Float) {
        val (titulo, mensagem) = avaliarAmbiente(temperatura, luminosidade)

        val notif = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_sun_zenit)
            .setContentTitle(titulo)
            .setContentText(mensagem)
            .setStyle(NotificationCompat.BigTextStyle().bigText(mensagem))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .build()

        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(NOTIF_ID, notif)
    }

    fun tocarSomConfirmacao() {
        try {
            val toneGen = ToneGenerator(AudioManager.STREAM_NOTIFICATION, 80)
            toneGen.startTone(ToneGenerator.TONE_PROP_BEEP, 150)
        } catch (e: Exception) {
            // Dispositivos sem saída de áudio não devem crashar o app
        }
    }

    private fun avaliarAmbiente(temperatura: Float?, luminosidade: Float): Pair<String, String> {
        if (temperatura != null && temperatura > 35f) {
            return Pair(
                "🔥 Calor excessivo detectado",
                "Temperatura de ${temperatura.toInt()}°C pode prejudicar suas plantas. Considere umidificar o ambiente ou mover os vasos para a sombra."
            )
        }
        if (temperatura != null && temperatura < 10f) {
            return Pair(
                "🥶 Temperatura baixa detectada",
                "Temperatura de ${temperatura.toInt()}°C pode estressar plantas tropicais. Considere levá-las para ambientes mais aquecidos."
            )
        }
        return when {
            luminosidade < 100f -> Pair(
                "🌑 Luz insuficiente",
                "Luminosidade de ${luminosidade.toInt()} lx está abaixo do ideal para a maioria das plantas. Aproxime-as de janelas ou use luz artificial."
            )
            luminosidade > 50_000f -> Pair(
                "☀️ Luz solar intensa",
                "Luminosidade de ${luminosidade.toInt()} lx pode queimar folhas sensíveis. Considere usar sombrite ou mover os vasos."
            )
            else -> Pair(
                "✅ Ambiente adequado",
                "Condições ideais detectadas — ${temperatura?.let { "${it.toInt()}°C · " } ?: ""}${luminosidade.toInt()} lx. Suas plantas estão em boas condições!"
            )
        }
    }
}