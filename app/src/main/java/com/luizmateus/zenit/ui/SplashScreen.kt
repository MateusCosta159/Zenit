package com.luizmateus.zenit.ui


import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.luizmateus.zenit.R
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


@SuppressLint("CustomSplashScreen")
class SplashActivity : AppCompatActivity() {

    companion object {
        private const val SPLASH_DELAY_MS = 2500L
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        setupAnimations()
        navigateAfterDelay()
    }


    private fun setupAnimations() {
        val ivLogo     = findViewById<ImageView>(R.id.iv_splash_logo)
        val tvAppName  = findViewById<TextView>(R.id.tv_splash_name)
        val tvTagline  = findViewById<TextView>(R.id.tv_splash_tagline)

        val fadeIn      = AnimationUtils.loadAnimation(this, R.anim.fade_in)
        val slideUp     = AnimationUtils.loadAnimation(this, R.anim.slide_up)

        ivLogo.startAnimation(fadeIn)
        tvAppName.startAnimation(slideUp)
        tvTagline.startAnimation(slideUp)
    }



    private fun navigateAfterDelay() {
        lifecycleScope.launch {
            delay(SPLASH_DELAY_MS)

            val isUserLoggedIn = checkUserSession()

            val destination = if (isUserLoggedIn) {
                MainActivity::class.java      // Dashboard / Home
            } else {
                //LoginActivity::class.java     // Tela de Login
            }

            startActivity(Intent(this@SplashActivity, destination))
            finish() // Remove a Splash da pilha de back-stack
        }
    }


    private fun checkUserSession(): Boolean {
        // Retorna false por padrão → sempre vai para o Login
        return false
    }
}