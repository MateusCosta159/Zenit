package com.luizmateus.zenit.ui

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.auth.FirebaseAuth
import com.luizmateus.zenit.auth.UserAuth
import com.luizmateus.zenit.databinding.ActivityLoginBinding

// tela de login do app
class LoginActivity : AppCompatActivity() {

    // view binding da tela
    private lateinit var binding: ActivityLoginBinding

    // acesso a autenticacao
    private val userAuth = UserAuth()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // se ja estiver logado vai direto para o dashboard
        if (FirebaseAuth.getInstance().currentUser != null) {
            startActivity(Intent(this, DashboardActivity::class.java))
            finish()
            return
        }

        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // valida campos e tenta login
        binding.btnLogin.setOnClickListener {
            val email = binding.edtEmail.text.toString().trim()
            val pass = binding.edtSenha.text.toString()
            if (email.isEmpty() || pass.isEmpty()) {
                Toast.makeText(this, "Preencha e-mail e senha", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            userAuth.login(email, pass) { sucesso ->
                if (sucesso) {
                    startActivity(Intent(this, DashboardActivity::class.java))
                    finish()
                } else {
                    Toast.makeText(this, "E-mail ou senha incorretos", Toast.LENGTH_SHORT).show()
                }
            }
        }

        // abre a tela de cadastro
        binding.btnCadastrar.setOnClickListener {
            startActivity(Intent(this, CadastroActivity::class.java))
        }
    }
}