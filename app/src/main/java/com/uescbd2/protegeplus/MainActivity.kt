package com.uescbd2.protegeplus

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import java.io.IOException

class MainActivity : AppCompatActivity() {

    private lateinit var dbHelper: DatabaseHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        dbHelper = DatabaseHelper(this)
        try {
            dbHelper.readableDatabase.close()
            println("DatabaseHelper inicializado e banco pronto.")
        } catch (e: IOException) {
            println("Erro ao inicializar DatabaseHelper: ${e.message}")
        }

        val goToRegisterButton = findViewById<Button>(R.id.buttonRegister)
        goToRegisterButton.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }

        val loginButton = findViewById<Button>(R.id.buttonLogin)
        val emailEditText = findViewById<EditText>(R.id.editTextEmail)
        val passwordEditText = findViewById<EditText>(R.id.editTextPassword)

        loginButton.setOnClickListener {
            val email = emailEditText.text.toString().trim()
            val senha = passwordEditText.text.toString()

            if (email.isNotEmpty() && senha.isNotEmpty()) {
                val loginSucesso = dbHelper.verificarLogin(email, senha)

                if (loginSucesso) {
                    Toast.makeText(this, "Login bem-sucedido!", Toast.LENGTH_SHORT).show()

                    // *** A CORREÇÃO ESTÁ AQUI ***
                    // Salva o estado de login no SharedPreferences
                    val sharedPreferences = getSharedPreferences("protegeplus_prefs", Context.MODE_PRIVATE)
                    with(sharedPreferences.edit()) {
                        putBoolean("isLoggedIn", true)
                        apply()
                    }

                    val intent = Intent(this, HomeActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                    finish()
                } else {
                    Toast.makeText(this, "E-mail ou senha incorretos.", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "Digite o e-mail e a senha.", Toast.LENGTH_SHORT).show()
            }
        }

        val visitorTextView = findViewById<TextView>(R.id.textViewVisitor)
        visitorTextView.setOnClickListener {
            Toast.makeText(this, "Entrando como visitante...", Toast.LENGTH_SHORT).show()
            val intent = Intent(this, HomeActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }

        val forgotPasswordTextView = findViewById<TextView>(R.id.textViewForgotPassword)
        forgotPasswordTextView.setOnClickListener {
            Toast.makeText(this, "Funcionalidade 'Esqueceu Senha' ainda não implementada.", Toast.LENGTH_SHORT).show()
        }
    }
}