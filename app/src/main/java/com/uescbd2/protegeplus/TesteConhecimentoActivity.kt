package com.uescbd2.protegeplus

import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat

class TesteConhecimentoActivity : AppCompatActivity() {

    private lateinit var dbHelper: DatabaseHelper

    // Views
    private lateinit var tvContador: TextView
    private lateinit var tvEnunciado: TextView
    private lateinit var rgAlternativas: RadioGroup
    private lateinit var btnAcao: Button
    private lateinit var tvFeedback: TextView
    private lateinit var scrollView: ScrollView

    // Estado do Jogo
    private var listaIdsQuestoes: List<Int> = emptyList() // O "Baralho"
    private var indiceAtual = 0 // Qual carta estamos vendo
    private var acertos = 0
    private var questaoAtualObj: QuestaoCompleta? = null
    private var isRespondido = false // Controla o estado do botão (Confirmar vs Próxima)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_teste_conhecimento)

        dbHelper = DatabaseHelper(this)
        inicializarViews()

        // Começa o jogo
        iniciarJogo()

        btnAcao.setOnClickListener {
            if (!isRespondido) {
                verificarResposta()
            } else {
                proximaQuestao()
            }
        }
    }

    private fun inicializarViews() {
        tvContador = findViewById(R.id.tvContadorQuestao)
        tvEnunciado = findViewById(R.id.tvEnunciado)
        rgAlternativas = findViewById(R.id.rgAlternativas)
        btnAcao = findViewById(R.id.btnConfirmarResposta)
        tvFeedback = findViewById(R.id.tvFeedback)
        scrollView = findViewById(R.id.scrollView) // Certifique-se de ter colocado ID no ScrollView do XML

        // Se o XML não tiver ID no ScrollView, adicione android:id="@+id/scrollView" lá
        // Se não quiser mexer no XML agora, pode remover a linha do scrollView, é apenas para rolar pro topo
    }

    private fun iniciarJogo() {
        // 1. Busca todos os IDs
        val todosIds = dbHelper.getListaIdsQuestoes()

        if (todosIds.isEmpty()) {
            Toast.makeText(this, "Nenhuma questão cadastrada no banco!", Toast.LENGTH_LONG).show()
            finish()
            return
        }

        // 2. Embaralha (Shuffle) - A mágica do "Aleatório sem repetição"
        listaIdsQuestoes = todosIds.shuffled()

        // 3. Reseta contadores
        indiceAtual = 0
        acertos = 0

        // 4. Carrega a primeira
        carregarQuestao()
    }

    private fun carregarQuestao() {
        // Reset visual
        isRespondido = false
        btnAcao.text = "Confirmar Resposta"
        tvFeedback.visibility = View.GONE
        rgAlternativas.removeAllViews() // Limpa opções anteriores
        rgAlternativas.clearCheck()

        // Pega o ID da vez
        val idDaVez = listaIdsQuestoes[indiceAtual]

        // Busca os dados completos no banco
        questaoAtualObj = dbHelper.getQuestaoCompleta(idDaVez)

        val q = questaoAtualObj
        if (q == null) {
            // Se der erro ao buscar (ex: deletaram no meio do jogo), pula
            proximaQuestao()
            return
        }

        // Preenche a tela
        tvContador.text = "Questão ${indiceAtual + 1}/${listaIdsQuestoes.size}"
        tvEnunciado.text = q.questao.descricao

        // Cria os RadioButtons dinamicamente
        for (alt in q.alternativas) {
            val rb = RadioButton(this)
            rb.id = View.generateViewId()
            rb.text = "${alt.letra}) ${alt.descricao}"
            rb.textSize = 16f
            rb.setPadding(16, 16, 16, 16)

            // Layout Params para dar margem
            val params = RadioGroup.LayoutParams(
                RadioGroup.LayoutParams.MATCH_PARENT,
                RadioGroup.LayoutParams.WRAP_CONTENT
            )
            params.setMargins(0, 0, 0, 16) // Margem inferior
            rb.layoutParams = params

            // Estilo visual (opcional, borda ou cor)
            // rb.setBackgroundResource(R.drawable.list_item_border)

            // G GUARDAR A LETRA NA TAG PARA CONFERIR DEPOIS
            rb.tag = alt.letra

            rgAlternativas.addView(rb)
        }
    }

    private fun verificarResposta() {
        val idSelecionado = rgAlternativas.checkedRadioButtonId

        if (idSelecionado == -1) {
            Toast.makeText(this, "Por favor, selecione uma alternativa.", Toast.LENGTH_SHORT).show()
            return
        }

        val rbSelecionado = findViewById<RadioButton>(idSelecionado)
        val letraSelecionada = rbSelecionado.tag.toString() // Pegamos "A", "B"... da tag
        val gabarito = questaoAtualObj?.questao?.respostaCorreta?.trim() ?: ""

        isRespondido = true

        // Bloqueia alterações
        for (i in 0 until rgAlternativas.childCount) {
            rgAlternativas.getChildAt(i).isEnabled = false
        }

        // Lógica de Acerto/Erro
        if (letraSelecionada.equals(gabarito, ignoreCase = true)) {
            // ACERTOU
            acertos++
            estilizarOpcao(rbSelecionado, true) // Pinta de Verde
            tvFeedback.text = "Resposta Correta! Muito bem."
            tvFeedback.setTextColor(ContextCompat.getColor(this, R.color.protegeTeal)) // Teal/Verde
        } else {
            // ERROU
            estilizarOpcao(rbSelecionado, false) // Pinta de Vermelho

            // Encontra a certa para mostrar ao usuário
            val rbCorreto = encontrarRadioButtonPorLetra(gabarito)
            if (rbCorreto != null) {
                estilizarOpcao(rbCorreto, true) // Pinta a certa de Verde
            }

            tvFeedback.text = "Resposta Incorreta. O gabarito é a letra $gabarito."
            tvFeedback.setTextColor(Color.RED)
        }

        tvFeedback.visibility = View.VISIBLE
        btnAcao.text = "Próxima Questão"
    }

    private fun proximaQuestao() {
        indiceAtual++

        if (indiceAtual < listaIdsQuestoes.size) {
            carregarQuestao()
            // Rola para o topo (opcional, se tiver o ID no XML)
            // scrollView.fullScroll(ScrollView.FOCUS_UP)
        } else {
            finalizarJogo()
        }
    }

    private fun finalizarJogo() {
        val total = listaIdsQuestoes.size
        val porcentagem = (acertos * 100) / total

        val mensagem = if (porcentagem >= 70) {
            "Parabéns! Você tem um ótimo conhecimento."
        } else {
            "Continue estudando para melhorar sua pontuação."
        }

        AlertDialog.Builder(this)
            .setTitle("Teste Finalizado")
            .setMessage("Você acertou $acertos de $total questões.\n\n$mensagem")
            .setCancelable(false)
            .setPositiveButton("Tentar Novamente") { _, _ ->
                iniciarJogo() // Reinicia tudo
            }
            .setNegativeButton("Sair") { _, _ ->
                finish()
            }
            .show()
    }

    // --- Auxiliares Visuais ---

    private fun estilizarOpcao(rb: RadioButton, isCorreto: Boolean) {
        if (isCorreto) {
            rb.setTextColor(Color.parseColor("#2E7D32")) // Verde Escuro
            rb.setTypeface(null, android.graphics.Typeface.BOLD)
            // Se quiser mudar a bolinha:
            rb.buttonTintList = ColorStateList.valueOf(Color.parseColor("#2E7D32"))
        } else {
            rb.setTextColor(Color.RED)
            rb.buttonTintList = ColorStateList.valueOf(Color.RED)
        }
    }

    private fun encontrarRadioButtonPorLetra(letra: String): RadioButton? {
        for (i in 0 until rgAlternativas.childCount) {
            val rb = rgAlternativas.getChildAt(i) as RadioButton
            if (rb.tag.toString().equals(letra, ignoreCase = true)) {
                return rb
            }
        }
        return null
    }
}