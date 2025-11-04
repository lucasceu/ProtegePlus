package com.uescbd2.protegeplus

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class SintomasCategoriaActivity : AppCompatActivity() {

    private lateinit var dbHelper: DatabaseHelper
    private lateinit var rvCategorias: RecyclerView
    private lateinit var adapter: CategoriaAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sintomas_categoria)

        dbHelper = DatabaseHelper(this)
        rvCategorias = findViewById(R.id.rvCategorias)

        // 1. Buscar os dados do banco
        //val listaDeCategorias = dbHelper.getCategorias()

        // 2. Configurar o Adapter e a RecyclerView
        rvCategorias.layoutManager = LinearLayoutManager(this)
        //adapter = CategoriaAdapter(this, listaDeCategorias) { categoriaClicada ->
            // --- Ação de clique ---
            // Por enquanto, só um Toast. No futuro, abre a Tela 2
            //Toast.makeText(this, "Clicou em: ${categoriaClicada.nome}", Toast.LENGTH_SHORT).show()

            // TODO: Criar a SintomaListActivity e passar o ID
            // val intent = Intent(this, SintomaListActivity::class.java)
            // intent.putExtra("CATEGORIA_ID", categoriaClicada.id)
            // intent.putExtra("CATEGORIA_NOME", categoriaClicada.nome)
            // startActivity(intent)
        //}
        rvCategorias.adapter = adapter

        // Configura o logout (igual às outras telas)
        findViewById<android.widget.LinearLayout>(R.id.llLogout).setOnClickListener {
            // TODO: Adicionar lógica real de logout (voltar pra MainActivity)
            Toast.makeText(this, "Logout clicado", Toast.LENGTH_SHORT).show()
        }
    }
}