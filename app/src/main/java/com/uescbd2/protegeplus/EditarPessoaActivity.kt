package com.uescbd2.protegeplus

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class EditarPessoaActivity : AppCompatActivity() {

    private lateinit var dbHelper: DatabaseHelper
    private lateinit var etNome: EditText
    private lateinit var etCpf: EditText
    private lateinit var etCargo: EditText
    private lateinit var etEmpresa: EditText
    private lateinit var etEmail: EditText
    private lateinit var etTelefone1: EditText
    private lateinit var etTelefone2: EditText
    private lateinit var btnSalvar: Button
    private var idPessoa: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register) // Reutilizando layout de registro para facilitar!

        // Pequenos ajustes no layout via código para parecer edição
        findViewById<TextView>(R.id.textViewVisitorRegister).text = "Editando Usuário"
        findViewById<Button>(R.id.buttonGoToLogin).isEnabled = false
        findViewById<Button>(R.id.buttonGoToLogin).alpha = 0f // Esconde botão de login

        dbHelper = DatabaseHelper(this)
        inicializarCampos()

        idPessoa = intent.getIntExtra("ID_PESSOA", -1)
        if (idPessoa != -1) {
            carregarDados(idPessoa)
        } else {
            finish()
        }

        btnSalvar.text = "Salvar Alterações"
        btnSalvar.setOnClickListener {
            salvarAlteracoes()
        }
    }

    private fun inicializarCampos() {
        etNome = findViewById(R.id.editTextNome)
        etCpf = findViewById(R.id.editTextMatricula) // Usando campo matricula como CPF
        etCargo = findViewById(R.id.editTextCargo)
        etEmpresa = findViewById(R.id.editTextArea) // Usando Area como Empresa
        etEmail = findViewById(R.id.editTextEmail)
        etTelefone1 = findViewById(R.id.editTextTelefone) // Telefone 1
        // etTelefone2 -> Se não tiver no layout Register, ignoramos ou adicionamos lá
        btnSalvar = findViewById(R.id.buttonRegister)
    }

    private fun carregarDados(id: Int) {
        val pessoas = dbHelper.getTodasPessoas() // Método não otimizado, mas funcional pra agora
        val pessoa = pessoas.find { it.id == id }

        pessoa?.let {
            etNome.setText(it.nome)
            etCpf.setText(it.cpf)
            etCargo.setText(it.cargo)
            etEmpresa.setText(it.empresa)
            etEmail.setText(it.email)
            etTelefone1.setText(it.telefone1)
        }
    }

    private fun salvarAlteracoes() {
        val usuarioAtualizado = Usuario(
            id = idPessoa,
            nome = etNome.text.toString(),
            cpf = etCpf.text.toString(),
            cargo = etCargo.text.toString(),
            empresa = etEmpresa.text.toString(),
            email = etEmail.text.toString(),
            senhaPlana = "", // Senha vazia significa "não alterar" no DatabaseHelper
            telefone1 = etTelefone1.text.toString()
        )

        if (dbHelper.atualizarPessoa(usuarioAtualizado)) {
            Toast.makeText(this, "Dados atualizados!", Toast.LENGTH_SHORT).show()
            finish()
        } else {
            Toast.makeText(this, "Erro ao atualizar.", Toast.LENGTH_SHORT).show()
        }
    }
}