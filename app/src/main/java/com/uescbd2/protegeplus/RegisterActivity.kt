package com.uescbd2.protegeplus

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class RegisterActivity : AppCompatActivity() {

    private lateinit var dbHelper: DatabaseHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        dbHelper = DatabaseHelper(this)

        val goToLoginButton = findViewById<Button>(R.id.buttonGoToLogin)
        goToLoginButton.setOnClickListener {
            finish()
        }

        val registerButton = findViewById<Button>(R.id.buttonRegister)
        val matriculaEditText = findViewById<EditText>(R.id.editTextMatricula) // Mapeia para CPF
        val nomeEditText = findViewById<EditText>(R.id.editTextNome)
        val emailEditText = findViewById<EditText>(R.id.editTextEmail)
        val areaEditText = findViewById<EditText>(R.id.editTextArea) // Não será salvo
        val cargoEditText = findViewById<EditText>(R.id.editTextCargo)
        val senhaEditText = findViewById<EditText>(R.id.editTextPasswordRegister)
        val confirmaSenhaEditText = findViewById<EditText>(R.id.editTextConfirmPasswordRegister)

        registerButton.setOnClickListener {
            val cpfOuMatricula = matriculaEditText.text.toString().trim()
            val nome = nomeEditText.text.toString().trim()
            val email = emailEditText.text.toString().trim()
            // val descricaoOuArea = areaEditText.text.toString().trim() // Ignorado
            val cargo = cargoEditText.text.toString().trim()
            val senha = senhaEditText.text.toString()
            val confirmaSenha = confirmaSenhaEditText.text.toString()

            // --- Validações ---
            if (nome.isEmpty() || email.isEmpty() || senha.isEmpty()) {
                Toast.makeText(this, "Nome, E-mail e Senha são obrigatórios.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (senha != confirmaSenha) {
                Toast.makeText(this, "As senhas não coincidem.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // --- NOVA VALIDAÇÃO (devido ao varchar(8)) ---
            if (senha.length > 8) {
                Toast.makeText(this, "A senha deve ter no máximo 8 caracteres.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            // --- FIM DA NOVA VALIDAÇÃO ---


            // --- MUDANÇA NA CRIAÇÃO DO USUÁRIO ---
            // Removemos o testId
            // O IdPessoa = 0 (padrão) indica que é um novo usuário
            // O DatabaseHelper vai ignorar o ID 0 e deixar o AUTOINCREMENT funcionar.
            val novoUsuario = Usuario(
                // IdPessoa = 0 (padrão)
                nome = nome,
                cpf = cpfOuMatricula.ifEmpty { null },
                cargo = cargo.ifEmpty { null },
                telefone = null, // Você não tinha campo de telefone no form
                empresa = null, // Você não tinha campo de empresa no form
                email = email,
                senhaPlana = senha
            )
            // --- FIM DA MUDANÇA ---

            // Tenta adicionar ao banco
            val sucesso = dbHelper.adicionarUsuario(novoUsuario)

            // Feedback
            if (sucesso) {
                Toast.makeText(this, "Cadastro realizado com sucesso!", Toast.LENGTH_SHORT).show()
                finish()
            } else {
                Toast.makeText(this, "Erro ao cadastrar. Verifique os dados ou tente outro e-mail.", Toast.LENGTH_LONG).show()
            }
        }
    }
}