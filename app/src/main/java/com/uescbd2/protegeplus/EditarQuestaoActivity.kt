package com.uescbd2.protegeplus

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class EditarQuestaoActivity : AppCompatActivity() {

    private lateinit var dbHelper: DatabaseHelper
    private lateinit var etEnunciado: EditText
    private lateinit var etAltA: EditText
    private lateinit var etAltB: EditText
    private lateinit var etAltC: EditText
    private lateinit var etAltD: EditText
    private lateinit var etAltE: EditText
    private lateinit var spinnerGabarito: Spinner
    private lateinit var btnSalvar: Button

    private var idQuestao: Int = -1 // -1 = Criando Nova

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_editar_questao)

        dbHelper = DatabaseHelper(this)
        inicializarViews()
        configurarSpinner()

        idQuestao = intent.getIntExtra("ID_QUESTAO", -1)

        if (idQuestao != -1) {
            carregarDados(idQuestao)
            btnSalvar.text = "Atualizar Questão"
        }

        btnSalvar.setOnClickListener {
            salvarQuestao()
        }
    }

    private fun inicializarViews() {
        etEnunciado = findViewById(R.id.etEnunciado)
        etAltA = findViewById(R.id.etAltA)
        etAltB = findViewById(R.id.etAltB)
        etAltC = findViewById(R.id.etAltC)
        etAltD = findViewById(R.id.etAltD)
        etAltE = findViewById(R.id.etAltE)
        spinnerGabarito = findViewById(R.id.spinnerGabarito)
        btnSalvar = findViewById(R.id.btnSalvarQuestao)
    }

    private fun configurarSpinner() {
        val letras = arrayOf("Selecione...", "A", "B", "C", "D", "E")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, letras)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerGabarito.adapter = adapter
    }

    private fun carregarDados(id: Int) {
        val questoes = dbHelper.getTodasQuestoes() // Busca na lista (poderia ser busca direta por ID)
        val questao = questoes.find { it.id == id } ?: return

        etEnunciado.setText(questao.descricao)

        // Seleciona o gabarito no spinner
        val gabarito = questao.respostaCorreta
        val letras = arrayOf("Selecione...", "A", "B", "C", "D", "E")
        val index = letras.indexOf(gabarito)
        if (index >= 0) spinnerGabarito.setSelection(index)

        // Carrega as alternativas
        val alternativas = dbHelper.getAlternativas(id)
        alternativas.forEach { alt ->
            when (alt.letra) {
                "A" -> etAltA.setText(alt.descricao)
                "B" -> etAltB.setText(alt.descricao)
                "C" -> etAltC.setText(alt.descricao)
                "D" -> etAltD.setText(alt.descricao)
                "E" -> etAltE.setText(alt.descricao)
            }
        }
    }

    private fun salvarQuestao() {
        val enunciado = etEnunciado.text.toString().trim()
        val gabarito = spinnerGabarito.selectedItem.toString()

        // Validação simples
        if (enunciado.isEmpty()) {
            etEnunciado.error = "Digite a pergunta"
            return
        }
        if (gabarito == "Selecione...") {
            Toast.makeText(this, "Selecione a alternativa correta", Toast.LENGTH_SHORT).show()
            return
        }
        if (etAltA.text.isEmpty() || etAltB.text.isEmpty()) {
            Toast.makeText(this, "Preencha pelo menos as alternativas A e B", Toast.LENGTH_SHORT).show()
            return
        }

        // 1. Salva ou Atualiza a Questão (Cabeçalho)
        val questaoObj = Questao(
            id = if (idQuestao == -1) 0 else idQuestao,
            descricao = enunciado,
            respostaCorreta = gabarito
        )

        var idSalvo: Long = -1

        if (idQuestao == -1) {
            // Nova Questão
            idSalvo = dbHelper.adicionarQuestao(questaoObj)
        } else {
            // Atualizar Questão
            if (dbHelper.atualizarQuestao(questaoObj)) {
                idSalvo = idQuestao.toLong()
                // Limpa alternativas antigas para regravar as novas
                dbHelper.deletarAlternativasDaQuestao(idQuestao)
            }
        }

        if (idSalvo != -1L) {
            // 2. Salva as Alternativas vinculadas ao ID
            salvarAlternativa("A", etAltA.text.toString(), idSalvo.toInt())
            salvarAlternativa("B", etAltB.text.toString(), idSalvo.toInt())
            salvarAlternativa("C", etAltC.text.toString(), idSalvo.toInt())
            salvarAlternativa("D", etAltD.text.toString(), idSalvo.toInt())
            salvarAlternativa("E", etAltE.text.toString(), idSalvo.toInt())

            Toast.makeText(this, "Questão salva com sucesso!", Toast.LENGTH_SHORT).show()
            finish()
        } else {
            Toast.makeText(this, "Erro ao salvar a questão.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun salvarAlternativa(letra: String, texto: String, idQ: Int) {
        if (texto.isNotBlank()) {
            dbHelper.adicionarAlternativa(Alternativa(letra = letra, descricao = texto, idQuestaoFk = idQ))
        }
    }
}