package com.uescbd2.protegeplus

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class NovoTelefoneUtilActivity : AppCompatActivity() {

    private lateinit var dbHelper: DatabaseHelper
    private lateinit var etNomeLocal: EditText
    private lateinit var etNumero: EditText
    private lateinit var btnSalvar: Button
    private lateinit var btnCancelar: Button
    private lateinit var tvTitulo: TextView

    private var idTelefone: Int = -1 // -1 significa NOVO CADASTRO

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_novo_telefone_util)

        dbHelper = DatabaseHelper(this)
        etNomeLocal = findViewById(R.id.etNomeLocal)
        etNumero = findViewById(R.id.etNumero)
        btnSalvar = findViewById(R.id.btnSalvarTelefone)
        btnCancelar = findViewById(R.id.btnCancelar)
        tvTitulo = findViewById(R.id.tvTitulo)

        // Verifica se veio um ID para Edição
        idTelefone = intent.getIntExtra("ID_TELEFONE", -1)

        if (idTelefone != -1) {
            // MODO EDIÇÃO
            tvTitulo.text = "Editar Telefone"
            btnSalvar.text = "Atualizar Dados"
            carregarDados(idTelefone)
        }

        btnSalvar.setOnClickListener {
            salvarOuAtualizar()
        }

        btnCancelar.setOnClickListener {
            finish()
        }
    }

    private fun carregarDados(id: Int) {
        val telefone = dbHelper.getTelefonePorId(id)
        telefone?.let {
            etNomeLocal.setText(it.unidadeSaude)
            etNumero.setText(it.numero)
        }
    }

    private fun salvarOuAtualizar() {
        val nome = etNomeLocal.text.toString().trim()
        val numero = etNumero.text.toString().trim()

        if (nome.isEmpty() || numero.isEmpty()) {
            Toast.makeText(this, "Preencha todos os campos.", Toast.LENGTH_SHORT).show()
            return
        }

        val sucesso: Boolean
        if (idTelefone == -1) {
            // Criar Novo
            sucesso = dbHelper.adicionarTelefoneUtil(nome, numero)
        } else {
            // Atualizar Existente
            val telAtualizado = TelefoneUtil(idTelefone, numero, nome)
            sucesso = dbHelper.atualizarTelefoneUtil(telAtualizado)
        }

        if (sucesso) {
            Toast.makeText(this, "Salvo com sucesso!", Toast.LENGTH_SHORT).show()
            finish()
        } else {
            Toast.makeText(this, "Erro ao salvar.", Toast.LENGTH_SHORT).show()
        }
    }
}