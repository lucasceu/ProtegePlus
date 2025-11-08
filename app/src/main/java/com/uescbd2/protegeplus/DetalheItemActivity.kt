package com.uescbd2.protegeplus

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class DetalheItemActivity : AppCompatActivity() {

    private lateinit var dbHelper: DatabaseHelper
    private lateinit var rvSubSintomas: RecyclerView
    private lateinit var tvItemNomeTitulo: TextView
    private lateinit var tvSubSintomasLabel: TextView
    private lateinit var tvEmptySubSintomas: TextView
    private lateinit var tvSintomasInclusosLabel: TextView
    private lateinit var tvSintomasInclusos: TextView
    private lateinit var tvSintomasExclusaoLabel: TextView
    private lateinit var tvSintomasExclusao: TextView
    private lateinit var tvOutrosSintomasLabel: TextView
    private lateinit var tvOutrosSintomas: TextView
    private lateinit var tvCid10Label: TextView
    private lateinit var tvCid10: TextView

    private lateinit var buttonLogin: LinearLayout
    private lateinit var buttonLogout: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detalhe_item)

        buttonLogin = findViewById(R.id.buttonLogin)
        buttonLogout = findViewById(R.id.buttonLogout)

        buttonLogin.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }

        buttonLogout.setOnClickListener {
            val sharedPreferences = getSharedPreferences("protegeplus_prefs", Context.MODE_PRIVATE)
            with(sharedPreferences.edit()) {
                putBoolean("isLoggedIn", false)
                apply()
            }
            Toast.makeText(this, "Logout realizado com sucesso", Toast.LENGTH_SHORT).show()
            updateButtonVisibility()
        }

        val itemCodigo = intent.getStringExtra("ITEM_CODIGO")
        val itemNome = intent.getStringExtra("ITEM_NOME") ?: "Detalhes"

        if (itemCodigo == null) {
            Toast.makeText(this, "Erro: Código do item não encontrado.", Toast.LENGTH_LONG).show()
            finish()
            return
        }

        dbHelper = DatabaseHelper(this)
        inicializarViews()

        tvItemNomeTitulo.text = "$itemNome ($itemCodigo)"

        val detalhes = dbHelper.getDetalhesItemCiap(itemCodigo)
        preencherDetalhes(detalhes)

        val listaSubSintomas = dbHelper.getSubSintomas(itemCodigo)
        preencherSubSintomas(listaSubSintomas)
    }

    override fun onResume() {
        super.onResume()
        updateButtonVisibility()
    }

    private fun updateButtonVisibility() {
        val sharedPreferences = getSharedPreferences("protegeplus_prefs", Context.MODE_PRIVATE)
        val isLoggedIn = sharedPreferences.getBoolean("isLoggedIn", false)

        if (isLoggedIn) {
            buttonLogin.visibility = View.GONE
            buttonLogout.visibility = View.VISIBLE
        } else {
            buttonLogin.visibility = View.VISIBLE
            buttonLogout.visibility = View.GONE
        }
    }

    private fun inicializarViews() {
        tvItemNomeTitulo = findViewById(R.id.tvItemNomeTitulo)
        rvSubSintomas = findViewById(R.id.rvSubSintomas)
        tvSubSintomasLabel = findViewById(R.id.tvSubSintomasLabel)
        tvEmptySubSintomas = findViewById(R.id.tvEmptySubSintomas)
        tvSintomasInclusosLabel = findViewById(R.id.tvSintomasInclusosLabel)
        tvSintomasInclusos = findViewById(R.id.tvSintomasInclusos)
        tvSintomasExclusaoLabel = findViewById(R.id.tvSintomasExclusaoLabel)
        tvSintomasExclusao = findViewById(R.id.tvSintomasExclusao)
        tvOutrosSintomasLabel = findViewById(R.id.tvOutrosSintomasLabel)
        tvOutrosSintomas = findViewById(R.id.tvOutrosSintomas)
        tvCid10Label = findViewById(R.id.tvCid10Label)
        tvCid10 = findViewById(R.id.tvCid10)
    }

    private fun preencherDetalhes(detalhe: DetalheCiap?) {
        if (detalhe == null) {
            tvSintomasInclusosLabel.visibility = View.GONE
            tvSintomasInclusos.visibility = View.GONE
            tvSintomasExclusaoLabel.visibility = View.GONE
            tvSintomasExclusao.visibility = View.GONE
            tvOutrosSintomasLabel.visibility = View.GONE
            tvOutrosSintomas.visibility = View.GONE
            tvCid10Label.visibility = View.GONE
            tvCid10.visibility = View.GONE
            return
        }
        exibirOuEsconderCampo(tvSintomasInclusosLabel, tvSintomasInclusos, detalhe.sintomasInclusos)
        exibirOuEsconderCampo(tvSintomasExclusaoLabel, tvSintomasExclusao, detalhe.sintomasExclusao)
        exibirOuEsconderCampo(tvOutrosSintomasLabel, tvOutrosSintomas, detalhe.outrosSintomas)
        exibirOuEsconderCampo(tvCid10Label, tvCid10, detalhe.possiveisCid10)
    }

    private fun preencherSubSintomas(lista: List<SubSintoma>) {
        if (lista.isEmpty()) {
            tvSubSintomasLabel.visibility = View.GONE
            rvSubSintomas.visibility = View.GONE
            tvEmptySubSintomas.visibility = View.VISIBLE
        } else {
            tvSubSintomasLabel.visibility = View.VISIBLE
            rvSubSintomas.visibility = View.VISIBLE
            tvEmptySubSintomas.visibility = View.GONE
            rvSubSintomas.layoutManager = LinearLayoutManager(this)
            rvSubSintomas.adapter = SubSintomaAdapter(lista)
            rvSubSintomas.isNestedScrollingEnabled = false
        }
    }

    private fun exibirOuEsconderCampo(label: TextView, campo: TextView, valor: String?) {
        if (valor.isNullOrBlank()) {
            label.visibility = View.GONE
            campo.visibility = View.GONE
        } else {
            label.visibility = View.VISIBLE
            campo.visibility = View.VISIBLE
            campo.text = valor
        }
    }
}