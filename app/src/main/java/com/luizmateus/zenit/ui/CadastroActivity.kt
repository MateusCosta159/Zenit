package com.luizmateus.zenit.ui

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.luizmateus.zenit.auth.UserAuth
import com.luizmateus.zenit.databinding.ActivityCadastroBinding

// tela de cadastro de conta
class CadastroActivity : AppCompatActivity() {

    // view binding da tela
    private lateinit var binding: ActivityCadastroBinding

    // acesso a autenticacao
    private val userAuth = UserAuth()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityCadastroBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // valida dados e tenta criar a conta
        binding.btnCriarConta.setOnClickListener {
            val email = binding.edtEmail.text.toString().trim()
            val pass = binding.edtSenha.text.toString()
            val confirm = binding.edtConfirmarSenha.text.toString()
            if (email.isEmpty() || pass.isEmpty()) {
                Toast.makeText(this, "Preencha todos os campos", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (pass != confirm) {
                Toast.makeText(this, "Senhas não conferem", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            userAuth.cadastro(email, pass) { sucesso, erro ->
                if (sucesso) {
                    startActivity(Intent(this, DashboardActivity::class.java))
                    finish()
                } else {
                    Toast.makeText(this, erro, Toast.LENGTH_LONG).show()
                }
            }
        }

        // volta para o login
        binding.btnVoltar.setOnClickListener {
            finish()
        }
    }
}