package com.uescbd2.protegeplus

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
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

    // --- NOVAS REFERÊNCIAS ---
    private lateinit var etBuscarSintoma: EditText
    private lateinit var btnLimparSelecao: Button

    // Lista mestra para o adapter
    private val listaCheckboxMestra = mutableListOf<SintomaCheckbox>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_verificador_sintomas)

        dbHelper = DatabaseHelper(this)
        rvSintomasCheckbox = findViewById(R.id.rvSintomasCheckbox)
        btnBuscarEnfermidades = findViewById(R.id.btnBuscarEnfermidades)

        // --- LIGANDO NOVOS BOTÕES ---
        etBuscarSintoma = findViewById(R.id.etBuscarSintoma)
        btnLimparSelecao = findViewById(R.id.btnLimparSelecao)

        // 1. Buscar os dados do banco
        val listaDeItensCiap = dbHelper.getSintomasPuros() //

        // 2. Converter para a lista mestra
        listaCheckboxMestra.addAll(listaDeItensCiap.map { SintomaCheckbox(it) })

        // 3. Configurar o Adapter e a RecyclerView
        rvSintomasCheckbox.layoutManager = LinearLayoutManager(this)
        adapter = VerificadorSintomasAdapter(listaCheckboxMestra) // Passa a lista mestra
        rvSintomasCheckbox.adapter = adapter

        // 4. Configurar o botão de busca (lógica igual)
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

        // --- NOVA LÓGICA: Botão Limpar ---
        btnLimparSelecao.setOnClickListener {
            adapter.clearSelections()
            Toast.makeText(this, "Seleção limpa", Toast.LENGTH_SHORT).show()
        }

        // --- NOVA LÓGICA: Filtro de Busca ---
        etBuscarSintoma.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                // Chama o filtro do adapter toda vez que o texto muda
                adapter.filter(s.toString())
            }

            override fun afterTextChanged(s: Editable?) {}
        })

        // Configura o logout
        findViewById<LinearLayout>(R.id.llLogout).setOnClickListener {
            Toast.makeText(this, "Logout clicado", Toast.LENGTH_SHORT).show()
        }
    }
}