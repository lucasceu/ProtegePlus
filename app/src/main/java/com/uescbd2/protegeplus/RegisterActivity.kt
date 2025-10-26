package com.uescbd2.protegeplus

import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class RegisterActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register) // Carrega o layout que acabamos de criar

        // Encontra o botão "Fazer login" pelo ID
        val goToLoginButton = findViewById<Button>(R.id.buttonGoToLogin)

        // Adiciona um "ouvinte" de clique
        goToLoginButton.setOnClickListener {
            // Fecha esta tela (RegisterActivity) e volta para a anterior (MainActivity)
            finish()
        }

        // Adicionar a lógica do botão "Cadastre-se" aqui mais tarde
        val registerButton = findViewById<Button>(R.id.buttonRegister)
        registerButton.setOnClickListener {
            // Lógica de cadastro (ex: verificar campos, salvar no banco)
            // Caso a gente decida apresentar apenas para fechar a tela, descomentar linha abaixo:
            // finish()
        }
    }
}
