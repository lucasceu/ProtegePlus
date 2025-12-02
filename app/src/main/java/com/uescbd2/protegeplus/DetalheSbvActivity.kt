package com.uescbd2.protegeplus

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class DetalheSbvActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detalhe_sbv)

        val item = intent.getSerializableExtra("ITEM_SBV") as? ItemSbv

        if (item != null) {
            findViewById<TextView>(R.id.tvTituloSbv).text = item.enfermidade
            findViewById<TextView>(R.id.tvCategoriaSbv).text = item.categoria
            // Exibe o texto longo dos procedimentos
            findViewById<TextView>(R.id.tvProcedimentoTexto).text = item.procedimentos
        }
    }
}