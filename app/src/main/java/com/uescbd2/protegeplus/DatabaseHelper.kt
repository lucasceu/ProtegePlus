package com.uescbd2.protegeplus

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteException
import android.database.sqlite.SQLiteOpenHelper
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream

// --- MUDANÇA AQUI ---
private const val DATABASE_NAME = "BD_Protege_v9.db" //
private const val DATABASE_VERSION = 5 // <-- MUDANÇA CRÍTICA (era 3)
// --- FIM DA MUDANÇA ---

private const val TABLE_PESSOA = "pessoa"

class DatabaseHelper(private val context: Context) :
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    private val dbPath: String = context.getDatabasePath(DATABASE_NAME).path

    init {
        createDatabase()
    }

    // ... (createDatabase, checkDatabase, copyDatabase, close continuam iguais) ...

    @Throws(IOException::class)
    fun createDatabase() {
        if (!checkDatabase()) {
            this.readableDatabase
            this.close()
            try {
                copyDatabase()
                println("Banco de dados v9 copiado com sucesso!")
            } catch (e: IOException) {
                println("Erro ao copiar banco de dados v9: ${e.message}")
                throw IOException("Erro ao copiar banco de dados", e)
            }
        } else {
            println("Banco de dados v9 já existe.")
        }
    }

    private fun checkDatabase(): Boolean {
        val dbFile = File(dbPath)
        val exists = dbFile.exists()
        println("Verificando se o banco existe em $dbPath: $exists")
        return exists
    }

    @Throws(IOException::class)
    private fun copyDatabase() {
        val inputStream: InputStream = context.assets.open(DATABASE_NAME)
        val outFileName = dbPath
        val outputStream: OutputStream = FileOutputStream(outFileName)
        val buffer = ByteArray(1024)
        var length: Int
        while (inputStream.read(buffer).also { length = it } > 0) {
            outputStream.write(buffer, 0, length)
        }
        outputStream.flush()
        outputStream.close()
        inputStream.close()
    }

    @Synchronized
    override fun close() {
        super.close()
    }

    override fun onCreate(db: SQLiteDatabase?) {
        // Vazio, pois o banco é copiado.
    }

    // Esta função será chamada por causa da mudança de versão (3 -> 4)
    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        if (newVersion > oldVersion) {
            println("Atualizando banco de dados da versão $oldVersion para $newVersion.")
            try {
                File(dbPath).delete() // Deleta o banco antigo (v3)
                copyDatabase() // Copia o novo (v9)
                println("Banco de dados v9 atualizado com sucesso.")
            } catch (e: IOException) {
                println("Erro ao atualizar o banco de dados v9: ${e.message}")
            }
        }
    }

    // --- FUNÇÃO DE CADASTRO (Já estava correta) ---
    fun adicionarUsuario(usuario: Usuario): Boolean {
        // ... (código de adicionarUsuario que já funciona) ...
        if (usuario.email.isBlank() || usuario.senhaPlana.isBlank() || usuario.nome.isBlank()) {
            println("Erro ao adicionar: Email, Senha ou Nome não podem ser vazios.")
            return false
        }

        val db = this.writableDatabase
        val values = ContentValues().apply {
            put("Nome", usuario.nome)
            put("CPF", usuario.cpf)
            put("Cargo", usuario.cargo)
            put("Telefone", usuario.telefone)
            put("Empresa", usuario.empresa)
            put("Email", usuario.email)
            put("Senha", usuario.senhaPlana)
        }

        return try {
            val newRowId = db.insertOrThrow(TABLE_PESSOA, null, values)
            println("Usuário inserido com sucesso na tabela '$TABLE_PESSOA'. Novo ID: $newRowId")
            db.close()
            newRowId != -1L
        } catch (e: SQLiteException) {
            println("Erro ao inserir usuário na tabela '$TABLE_PESSOA': ${e.message}")
            db.close()
            false
        }
    }

    // --- FUNÇÃO DE LOGIN (Já estava correta) ---
    fun verificarLogin(email: String, senhaPlana: String): Boolean {
        // ... (código de verificarLogin que já funciona) ...
        if (email.isBlank() || senhaPlana.isBlank()) {
            return false
        }

        val db = this.readableDatabase
        var cursor: Cursor? = null
        var loginSucesso = false

        try {
            val selection = "Email = ?"
            val selectionArgs = arrayOf(email)
            val columns = arrayOf("Senha")

            cursor = db.query(TABLE_PESSOA, columns, selection, selectionArgs, null, null, null)

            if (cursor != null && cursor.moveToFirst()) {
                val senhaNoBanco = cursor.getString(cursor.getColumnIndexOrThrow("Senha"))
                if (senhaPlana == senhaNoBanco) {
                    loginSucesso = true
                }
            }
        } catch (e: Exception) {
            println("Erro ao verificar login na tabela '$TABLE_PESSOA': ${e.message}")
            loginSucesso = false
        } finally {
            cursor?.close()
            db.close()
        }
        return loginSucesso
    }

    // --- FUNÇÃO DE TELEFONES ÚTEIS (CORRIGIDA) ---
    fun getTelefonesUteis(): List<TelefoneUtil> {
        val listaTelefones = mutableListOf<TelefoneUtil>()
        val db = this.readableDatabase
        var cursor: Cursor? = null

        // Colunas da tabela "fonte da verdade"
        val tabela = "telefonesuteis"
        val colunas = arrayOf("IdTelefone", "Numero", "Unidadesaude")

        try {
            cursor = db.query(tabela, colunas, null, null, null, null, "Unidadesaude ASC")

            if (cursor != null && cursor.moveToFirst()) {
                val idCol = cursor.getColumnIndexOrThrow("IdTelefone")
                val numCol = cursor.getColumnIndexOrThrow("Numero")
                val unidadeCol = cursor.getColumnIndexOrThrow("Unidadesaude")

                do {
                    val id = cursor.getInt(idCol)

                    // Lê Numero como String, pois é varchar(14) no banco
                    val numero = if (cursor.isNull(numCol)) null else cursor.getString(numCol)
                    val unidade = if (cursor.isNull(unidadeCol)) null else cursor.getString(unidadeCol)

                    listaTelefones.add(TelefoneUtil(id, numero, unidade))
                } while (cursor.moveToNext())
            }
        } catch (e: Exception) {
            println("Erro ao buscar telefones úteis: ${e.message}")
        } finally {
            cursor?.close()
            db.close()
        }
        println("Telefones encontrados: ${listaTelefones.size}")
        return listaTelefones
    }

    fun getGruposCiap(): List<GrupoCiap> {
        val listaGrupos = mutableListOf<GrupoCiap>()
        val db = this.readableDatabase
        var cursor: Cursor? = null

        val tabela = "grupo_ciap"
        val colunas = arrayOf("id_grupo", "componente")

        val selection = "id_grupo IN (?, ?, ?)"
        val selectionArgs = arrayOf("1", "2", "7")

        try {
            // Consulta a tabela 'grupo_ciap'
            cursor = db.query(
                tabela,
                colunas,
                selection,  // <-- MUDANÇA AQUI
                selectionArgs, // <-- MUDANÇA AQUI
                null,
                null,
                "id_grupo ASC"
            )

            if (cursor != null && cursor.moveToFirst()) {
                val idCol = cursor.getColumnIndexOrThrow("id_grupo")
                val compCol = cursor.getColumnIndexOrThrow("componente")

                do {
                    val id = cursor.getInt(idCol)
                    val componente = if (cursor.isNull(compCol)) null else cursor.getString(compCol)

                    listaGrupos.add(GrupoCiap(id, componente))
                } while (cursor.moveToNext())
            }
        } catch (e: Exception) {
            println("Erro ao buscar grupos CIAP filtrados: ${e.message}")
        } finally {
            cursor?.close()
            db.close()
        }
        println("Grupos CIAP filtrados encontrados: ${listaGrupos.size}")
        return listaGrupos
    }

    fun getItensFromTbCiap(grupoId: Int): List<ItemCiap> {
        val listaItens = mutableListOf<ItemCiap>()
        val db = this.readableDatabase
        var cursor: Cursor? = null

        val tabela = "tb_ciap"
        val colunas = arrayOf("codigo_ciap2", "enfermidade_leigo", "id_grupo_fk")
        val selection = "id_grupo_fk = ?"
        val selectionArgs = arrayOf(grupoId.toString())

        try {
            cursor = db.query(tabela, colunas, selection, selectionArgs, null, null, "enfermidade_leigo ASC")

            if (cursor != null && cursor.moveToFirst()) {
                val codCol = cursor.getColumnIndexOrThrow("codigo_ciap2")
                val nomeCol = cursor.getColumnIndexOrThrow("enfermidade_leigo")
                val grupoCol = cursor.getColumnIndexOrThrow("id_grupo_fk")

                do {
                    val codigo = cursor.getString(codCol)
                    val nome = if (cursor.isNull(nomeCol)) null else cursor.getString(nomeCol)
                    val idGrupo = cursor.getInt(grupoCol)

                    listaItens.add(ItemCiap(codigo, nome, idGrupo))
                } while (cursor.moveToNext())
            }
        } catch (e: Exception) {
            println("Erro ao buscar itens da tb_ciap: ${e.message}")
        } finally {
            cursor?.close()
            db.close()
        }
        println("Itens da tb_ciap (Grupo $grupoId) encontrados: ${listaItens.size}")
        return listaItens
    }

    // --- NOVA FUNÇÃO PARA BUSCAR ITENS DE PROCEDIMENTO_CLINICO ---
    fun getItensFromProcedimentoClinico(grupoId: Int): List<ItemCiap> {
        val listaItens = mutableListOf<ItemCiap>()
        val db = this.readableDatabase
        var cursor: Cursor? = null

        val tabela = "procedimento_clinico"
        // Colunas com nomes diferentes, mas mesmo propósito
        val colunas = arrayOf("codigociap2_fk", "enfermidade_leigo", "id_grupo_fk")
        val selection = "id_grupo_fk = ?"
        val selectionArgs = arrayOf(grupoId.toString())

        try {
            cursor = db.query(tabela, colunas, selection, selectionArgs, null, null, "enfermidade_leigo ASC")

            if (cursor != null && cursor.moveToFirst()) {
                val codCol = cursor.getColumnIndexOrThrow("codigociap2_fk")
                val nomeCol = cursor.getColumnIndexOrThrow("enfermidade_leigo")
                val grupoCol = cursor.getColumnIndexOrThrow("id_grupo_fk")

                do {
                    val codigo = cursor.getString(codCol)
                    val nome = if (cursor.isNull(nomeCol)) null else cursor.getString(nomeCol)
                    val idGrupo = cursor.getInt(grupoCol)

                    listaItens.add(ItemCiap(codigo, nome, idGrupo))
                } while (cursor.moveToNext())
            }
        } catch (e: Exception) {
            println("Erro ao buscar itens de procedimento_clinico: ${e.message}")
        } finally {
            cursor?.close()
            db.close()
        }
        println("Itens de procedimento_clinico (Grupo $grupoId) encontrados: ${listaItens.size}")
        return listaItens
    }

    fun getDetalhesItemCiap(codigoCiap: String): DetalheCiap? {
        val db = this.readableDatabase
        var cursor: Cursor? = null
        var detalhe: DetalheCiap? = null

        val tabela = "tb_ciap"
        val colunas = arrayOf(
            "codigo_ciap2", "enfermidade_leigo", "sintomas_inclusos",
            "sintomas_exclusao", "possiveis_cid10", "outros_sintomas"
        )
        val selection = "codigo_ciap2 = ?"
        val selectionArgs = arrayOf(codigoCiap)

        try {
            cursor = db.query(tabela, colunas, selection, selectionArgs, null, null, null)

            if (cursor != null && cursor.moveToFirst()) {
                val codCol = cursor.getColumnIndexOrThrow("codigo_ciap2")
                val nomeCol = cursor.getColumnIndexOrThrow("enfermidade_leigo")
                val inclCol = cursor.getColumnIndexOrThrow("sintomas_inclusos")
                val exclCol = cursor.getColumnIndexOrThrow("sintomas_exclusao")
                val cidCol = cursor.getColumnIndexOrThrow("possiveis_cid10")
                val outrosCol = cursor.getColumnIndexOrThrow("outros_sintomas")

                detalhe = DetalheCiap(
                    codigo = cursor.getString(codCol),
                    nomeLeigo = if (cursor.isNull(nomeCol)) null else cursor.getString(nomeCol),
                    sintomasInclusos = if (cursor.isNull(inclCol)) null else cursor.getString(inclCol),
                    sintomasExclusao = if (cursor.isNull(exclCol)) null else cursor.getString(exclCol),
                    possiveisCid10 = if (cursor.isNull(cidCol)) null else cursor.getString(cidCol),
                    outrosSintomas = if (cursor.isNull(outrosCol)) null else cursor.getString(outrosCol)
                )
            }
        } catch (e: Exception) {
            println("Erro ao buscar detalhes do item $codigoCiap: ${e.message}")
        } finally {
            cursor?.close()
            db.close()
        }
        return detalhe
    }

    // --- NOVA FUNÇÃO PARA BUSCAR OS SUB-SINTOMAS (DA TB_SINTOMAS) ---
    fun getSubSintomas(codigoCiap: String): List<SubSintoma> {
        val listaSubSintomas = mutableListOf<SubSintoma>()
        val db = this.readableDatabase
        var cursor: Cursor? = null

        val tabela = "tb_sintomas"
        val colunas = arrayOf("sub_enfermidade")
        val selection = "CIAP2_Codigo_fk = ?" //
        val selectionArgs = arrayOf(codigoCiap)

        try {
            cursor = db.query(tabela, colunas, selection, selectionArgs, null, null, "sub_enfermidade ASC")

            if (cursor != null && cursor.moveToFirst()) {
                val nomeCol = cursor.getColumnIndexOrThrow("sub_enfermidade")

                do {
                    val nome = if (cursor.isNull(nomeCol)) null else cursor.getString(nomeCol)
                    if (nome != null) { // Adiciona apenas se não for nulo
                        listaSubSintomas.add(SubSintoma(nome))
                    }
                } while (cursor.moveToNext())
            }
        } catch (e: Exception) {
            println("Erro ao buscar sub-sintomas do item $codigoCiap: ${e.message}")
        } finally {
            cursor?.close()
            db.close()
        }
        println("Sub-sintomas de $codigoCiap encontrados: ${listaSubSintomas.size}")
        return listaSubSintomas
    }

    fun getEnfermidadesPorSintomas(codigosSintomas: List<String>): List<ItemCiap> {
        val listaItens = mutableListOf<ItemCiap>()
        if (codigosSintomas.isEmpty()) {
            return listaItens
        }

        val db = this.readableDatabase
        var cursor: Cursor? = null
        val tabela = "tb_ciap"
        val colunas = arrayOf("codigo_ciap2", "enfermidade_leigo", "id_grupo_fk",
            "sintomas_inclusos", "outros_sintomas")

        // --- MUDANÇA AQUI ---
        // Removemos o "id_grupo_fk = 7" para pesquisar em TUDO
        var selection = "("
        // --- FIM DA MUDANÇA ---

        val selectionArgs = mutableListOf<String>()
        codigosSintomas.forEachIndexed { index, codigo ->
            val likeQuery = " (sintomas_inclusos LIKE ? OR outros_sintomas LIKE ?) "
            selection += likeQuery
            selectionArgs.add("% ${codigo}%")
            selectionArgs.add("% ${codigo}%")
            if (index < codigosSintomas.size - 1) {
                selection += " OR "
            }
        }
        selection += ")"

        try {
            cursor = db.query(tabela, colunas, selection, selectionArgs.toTypedArray(), null, null, null)

            if (cursor != null && cursor.moveToFirst()) {
                val codCol = cursor.getColumnIndexOrThrow("codigo_ciap2")
                val nomeCol = cursor.getColumnIndexOrThrow("enfermidade_leigo")
                val grupoCol = cursor.getColumnIndexOrThrow("id_grupo_fk")
                val inclCol = cursor.getColumnIndexOrThrow("sintomas_inclusos")
                val outrosCol = cursor.getColumnIndexOrThrow("outros_sintomas")

                do {
                    val item = ItemCiap(
                        codigo = cursor.getString(codCol),
                        nome = if (cursor.isNull(nomeCol)) null else cursor.getString(nomeCol),
                        idGrupo = cursor.getInt(grupoCol),
                        textoSintomasInclusos = if (cursor.isNull(inclCol)) null else cursor.getString(inclCol),
                        textoOutrosSintomas = if (cursor.isNull(outrosCol)) null else cursor.getString(outrosCol)
                    )
                    listaItens.add(item)
                } while (cursor.moveToNext())
            }
        } catch (e: Exception) {
            println("Erro ao buscar enfermidades por sintomas (rede de pesca v2): ${e.message}")
        } finally {
            cursor?.close()
            db.close()
        }
        println("Enfermidades (suspeitas v2) encontradas: ${listaItens.size}")
        return listaItens
    }

    fun getSintomasPuros(): List<ItemCiap> {
        val listaItens = mutableListOf<ItemCiap>()
        val db = this.readableDatabase
        var cursor: Cursor? = null

        val tabela = "tb_sintomas"
        val colunas = arrayOf("CIAP2_Codigo_fk", "sub_enfermidade")

        // O filtro que você sugeriu!
        val selection = "CIAP2_Codigo_fk IS NOT NULL AND CIAP2_Codigo_fk NOT LIKE '*%' AND sub_enfermidade IS NOT NULL AND sub_enfermidade != ''"

        try {
            // Usamos GROUP BY para não listar "Febre alta (A03)" e "Febre baixa (A03)"
            // como dois sintomas, mas sim como sub-sintomas de A03.
            // ... pensando bem, é melhor listar todos. O usuário quer ser específico.
            // Vamos remover o GROUP BY e pegar tudo.
            cursor = db.query(tabela, colunas, selection, null,
                "CIAP2_Codigo_fk, sub_enfermidade", // GROUP BY para evitar duplicatas exatas
                null, "sub_enfermidade ASC")

            if (cursor != null && cursor.moveToFirst()) {
                val codCol = cursor.getColumnIndexOrThrow("CIAP2_Codigo_fk")
                val nomeCol = cursor.getColumnIndexOrThrow("sub_enfermidade")

                do {
                    // Mapeia os dados da tb_sintomas para o nosso ItemCiap
                    val item = ItemCiap(
                        codigo = cursor.getString(codCol),
                        nome = cursor.getString(nomeCol),
                        idGrupo = 1 // Apenas para constar que é um sintoma
                    )
                    listaItens.add(item)
                } while (cursor.moveToNext())
            }
        } catch (e: Exception) {
            println("Erro ao buscar sintomas puros (tb_sintomas): ${e.message}")
        } finally {
            cursor?.close()
            db.close()
        }
        println("Sintomas puros encontrados: ${listaItens.size}")
        return listaItens
    }
}