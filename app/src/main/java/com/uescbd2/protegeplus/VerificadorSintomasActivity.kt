package com.uescbd2.protegeplus

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class VerificadorSintomasActivity : AppCompatActivity() {

    private lateinit var dbHelper: DatabaseHelper
    private lateinit var rvSintomasCheckbox: RecyclerView
    private lateinit var adapter: VerificadorSintomasAdapter
    private lateinit var btnBuscarEnfermidades: Button
    private lateinit var etBuscarSintoma: EditText
    private lateinit var btnLimparSelecao: Button
    private val listaCheckboxMestra = mutableListOf<SintomaCheckbox>()

    private lateinit var buttonLogin: LinearLayout
    private lateinit var buttonLogout: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_verificador_sintomas)

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

        dbHelper = DatabaseHelper(this)
        rvSintomasCheckbox = findViewById(R.id.rvSintomasCheckbox)
        btnBuscarEnfermidades = findViewById(R.id.btnBuscarEnfermidades)
        etBuscarSintoma = findViewById(R.id.etBuscarSintoma)
        btnLimparSelecao = findViewById(R.id.btnLimparSelecao)

        val listaDeItensCiap = dbHelper.getSintomasPuros()
        listaCheckboxMestra.addAll(listaDeItensCiap.map { SintomaCheckbox(it) })

        rvSintomasCheckbox.layoutManager = LinearLayoutManager(this)
        adapter = VerificadorSintomasAdapter(listaCheckboxMestra)
        rvSintomasCheckbox.adapter = adapter

        btnBuscarEnfermidades.setOnClickListener {
            val codigosSelecionados = adapter.getSelectedSymptomCodes()
            if (codigosSelecionados.isEmpty()) {
                Toast.makeText(this, "Selecione pelo menos um sintoma.", Toast.LENGTH_SHORT).show()
            } else {
                val intent = Intent(this, ResultadosEnfermidadesActivity::class.java)
                intent.putStringArrayListExtra("CODIGOS_SINTOMAS", codigosSelecionados)
                startActivity(intent)
            }
        }

        btnLimparSelecao.setOnClickListener {
            adapter.clearSelections()
            Toast.makeText(this, "Seleção limpa", Toast.LENGTH_SHORT).show()
        }

        etBuscarSintoma.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                adapter.filter(s.toString())
            }
            override fun afterTextChanged(s: Editable?) {}
        })
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
}