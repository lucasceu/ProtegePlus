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

// --- VERSÃO ATUALIZADA PARA 10 ---
private const val DATABASE_NAME = "BD_Protege_v12.db"
private const val DATABASE_VERSION = 11
private const val TABLE_PESSOA = "pessoa"

class DatabaseHelper(private val context: Context) :
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    private val dbPath: String = context.getDatabasePath(DATABASE_NAME).path

    init {
        createDatabase()
    }

    @Throws(IOException::class)
    fun createDatabase() {
        if (!checkDatabase()) {
            this.readableDatabase
            this.close()
            try {
                copyDatabase()
                println("Banco de dados copiado com sucesso!")
            } catch (e: IOException) {
                throw IOException("Erro ao copiar banco de dados", e)
            }
        }
    }

    private fun checkDatabase(): Boolean {
        return File(dbPath).exists()
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

    override fun onCreate(db: SQLiteDatabase?) {}

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        if (newVersion > oldVersion) {
            try {
                File(dbPath).delete()
                copyDatabase()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    // --- USUÁRIOS E LOGIN ---

    fun adicionarUsuario(usuario: Usuario): Long {
        if (usuario.email.isBlank() || usuario.senhaPlana.isBlank() || usuario.nome.isBlank()) {
            return -1L // Retorna erro
        }

        val db = this.writableDatabase
        val values = ContentValues().apply {
            put("Nome", usuario.nome)
            put("CPF", usuario.cpf)
            put("Cargo", usuario.cargo)
            put("Empresa", usuario.empresa)
            put("Email", usuario.email)
            put("Senha", usuario.senhaPlana)
            // Os telefones novos
            put("Telefone1", usuario.telefone1)
            put("Telefone2", usuario.telefone2)
        }

        return try {
            // insertOrThrow retorna o ID da linha inserida (Long)
            val newRowId = db.insertOrThrow("pessoa", null, values)
            db.close()
            newRowId // Retorna o ID (ex: 1, 5, 42...)
        } catch (e: Exception) {
            e.printStackTrace()
            db.close()
            -1L // Retorna -1 se falhar
        }
    }

    fun adicionarTelefonePessoal(idPessoa: Long, numero: String): Boolean {
        val db = this.writableDatabase
        val values = ContentValues().apply {
            put("Numero", numero)
            put("IdPessoa_FK", idPessoa) // VINCULA AO USUÁRIO
            put("Unidadesaude", "Pessoal") // Apenas para não ficar null
        }
        return try {
            db.insertOrThrow("telefonesuteis", null, values)
            db.close()
            true
        } catch (e: Exception) {
            db.close()
            false
        }
    }

    fun adicionarTelefoneUtil(nomeLocal: String, numero: String): Boolean {
        val db = this.writableDatabase
        val values = ContentValues().apply {
            put("Numero", numero)
            put("Unidadesaude", nomeLocal)
            // Não precisa mais de IdPessoa_FK
        }
        return try {
            db.insertOrThrow("telefonesuteis", null, values)
            db.close()
            true
        } catch (e: Exception) {
            db.close()
            false
        }
    }

    fun verificarLogin(email: String, senhaPlana: String): Boolean {
        if (email.isBlank() || senhaPlana.isBlank()) return false
        val db = this.readableDatabase
        var cursor: Cursor? = null
        var loginSucesso = false
        try {
            cursor = db.query(TABLE_PESSOA, arrayOf("Senha"), "Email = ?", arrayOf(email), null, null, null)
            if (cursor != null && cursor.moveToFirst()) {
                val senhaNoBanco = cursor.getString(0)
                if (senhaPlana == senhaNoBanco) loginSucesso = true
            }
        } catch (e: Exception) { e.printStackTrace() }
        finally { cursor?.close(); db.close() }
        return loginSucesso
    }

    // --- TELEFONES ÚTEIS ---
    fun getTelefonesUteis(): List<TelefoneUtil> {
        val lista = mutableListOf<TelefoneUtil>()
        val db = this.readableDatabase
        var cursor: Cursor? = null
        try {
            // CORREÇÃO: Removemos a selection "IdPessoa_FK IS NULL"
            // Agora trazemos TUDO o que está na tabela
            cursor = db.query(
                "telefonesuteis",
                arrayOf("IdTelefone", "Numero", "Unidadesaude"),
                null, // selection = null (sem filtro)
                null,
                null,
                null,
                "Unidadesaude ASC"
            )

            if (cursor != null && cursor.moveToFirst()) {
                do {
                    val id = cursor.getInt(0)
                    val numero = cursor.getString(1)
                    val unidade = cursor.getString(2)
                    lista.add(TelefoneUtil(id, numero, unidade))
                } while (cursor.moveToNext())
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            cursor?.close()
            db.close()
        }
        return lista
    }

    // --- GRUPOS CIAP ---
    fun getGruposCiap(): List<GrupoCiap> {
        val lista = mutableListOf<GrupoCiap>()
        val db = this.readableDatabase
        var cursor: Cursor? = null
        try {
            cursor = db.query("grupo_ciap", arrayOf("id_grupo", "componente"), "id_grupo IN (?, ?, ?)", arrayOf("1", "2", "7"), null, null, "id_grupo ASC")
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    lista.add(GrupoCiap(cursor.getInt(0), cursor.getString(1)))
                } while (cursor.moveToNext())
            }
        } catch (e: Exception) { e.printStackTrace() } finally { cursor?.close(); db.close() }
        return lista
    }

    // --- SINTOMAS PUROS (Para o Verificador) ---
    fun getSintomasPuros(): List<ItemCiap> {
        val listaItens = mutableListOf<ItemCiap>()
        val db = this.readableDatabase
        var cursor: Cursor? = null
        try {
            // Busca código FK e nome (sub_enfermidade)
            cursor = db.query("tb_sintomas", arrayOf("CIAP2_Codigo_fk", "sub_enfermidade"),
                "CIAP2_Codigo_fk IS NOT NULL AND sub_enfermidade IS NOT NULL", null, null, null, "sub_enfermidade ASC")
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    val cod = cursor.getString(0)
                    val nome = cursor.getString(1)
                    // idGrupo = 1 (Sintomas)
                    listaItens.add(ItemCiap(cod, nome, 1))
                } while (cursor.moveToNext())
            }
        } catch (e: Exception) { e.printStackTrace() } finally { cursor?.close(); db.close() }
        return listaItens
    }

    // --- BUSCA HÍBRIDA: ENFERMIDADES POR SINTOMAS ---
    fun getEnfermidadesPorSintomas(codigosSintomas: List<String>): List<ItemCiap> {
        val listaItens = mutableListOf<ItemCiap>()
        if (codigosSintomas.isEmpty()) return listaItens

        val db = this.readableDatabase
        var cursor: Cursor? = null

        // Query Híbrida: Pega se for o CÓDIGO exato OU se estiver no TEXTO
        val query = StringBuilder("SELECT DISTINCT codigo_ciap2, enfermidade_leigo, id_grupo_fk, sintomas_inclusos, outros_sintomas FROM tb_ciap WHERE ")
        val args = mutableListOf<String>()

        // 1. Match Exato no Código (Pai/Filho)
        query.append(" ( codigo_ciap2 IN (")
        codigosSintomas.forEachIndexed { index, _ ->
            query.append("?")
            if (index < codigosSintomas.size - 1) query.append(",")
        }
        query.append(") ) OR ( ")

        // 2. Match no Texto (Referências Cruzadas)
        codigosSintomas.forEachIndexed { index, _ ->
            query.append(" (sintomas_inclusos LIKE ? OR outros_sintomas LIKE ?) ")
            if (index < codigosSintomas.size - 1) query.append(" OR ")
        }
        query.append(" )")

        // Argumentos
        args.addAll(codigosSintomas) // Para o IN
        codigosSintomas.forEach {
            args.add("%$it%") // Para sintomas_inclusos
            args.add("%$it%") // Para outros_sintomas
        }

        try {
            cursor = db.rawQuery(query.toString(), args.toTypedArray())
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    val item = ItemCiap(
                        codigo = cursor.getString(0),
                        nome = cursor.getString(1),
                        idGrupo = cursor.getInt(2),
                        textoSintomasInclusos = if(cursor.isNull(3)) "" else cursor.getString(3),
                        textoOutrosSintomas = if(cursor.isNull(4)) "" else cursor.getString(4)
                    )
                    listaItens.add(item)
                } while (cursor.moveToNext())
            }
        } catch (e: Exception) { println("Erro busca híbrida: ${e.message}") }
        finally { cursor?.close(); db.close() }

        return listaItens
    }

    // --- NOVO: ORGÃOS (Letras) ---
    fun getOrgaos(): List<ItemCiap> {
        val listaOrgaos = mutableListOf<ItemCiap>()
        val db = this.readableDatabase
        var cursor: Cursor? = null
        try {
            cursor = db.query("letras_ciap2", arrayOf("Letra", "Orgao_anatomico"), null, null, null, null, "Letra ASC")
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    val letra = cursor.getString(0)
                    val orgao = cursor.getString(1)
                    // Usamos ItemCiap genericamente. ID 7 = Doenças
                    listaOrgaos.add(ItemCiap(letra, orgao, 7))
                } while (cursor.moveToNext())
            }
        } catch (e: Exception) { e.printStackTrace() } finally { cursor?.close(); db.close() }
        return listaOrgaos
    }

    // --- LISTA ITENS TB_CIAP (Atualizado com filtro de letra) ---
    fun getItensFromTbCiap(grupoId: Int, letraFiltro: String? = null): List<ItemCiap> {
        val listaItens = mutableListOf<ItemCiap>()
        val db = this.readableDatabase
        var cursor: Cursor? = null

        val tabela = "tb_ciap"
        val colunas = arrayOf("codigo_ciap2", "enfermidade_leigo", "id_grupo_fk")

        var selection = "id_grupo_fk = ?"
        val argsList = mutableListOf<String>(grupoId.toString())

        if (letraFiltro != null) {
            selection += " AND letra_ciap2 = ?"
            argsList.add(letraFiltro)
        }

        try {
            cursor = db.query(tabela, colunas, selection, argsList.toTypedArray(), null, null, "enfermidade_leigo ASC")
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    val codigo = cursor.getString(0)
                    val nome = cursor.getString(1)
                    val idGrupo = cursor.getInt(2)
                    listaItens.add(ItemCiap(codigo, nome, idGrupo))
                } while (cursor.moveToNext())
            }
        } catch (e: Exception) { e.printStackTrace() } finally { cursor?.close(); db.close() }
        return listaItens
    }

    // --- LISTA ITENS PROCEDIMENTO ---
    fun getItensFromProcedimentoClinico(grupoId: Int): List<ItemCiap> {
        val listaItens = mutableListOf<ItemCiap>()
        val db = this.readableDatabase
        var cursor: Cursor? = null
        try {
            cursor = db.query("procedimento_clinico", arrayOf("codigociap2_fk", "enfermidade_leigo", "id_grupo_fk"), "id_grupo_fk = ?", arrayOf(grupoId.toString()), null, null, "enfermidade_leigo ASC")
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    val codigo = cursor.getString(0)
                    val nome = cursor.getString(1)
                    val idGrupo = cursor.getInt(2)
                    listaItens.add(ItemCiap(codigo, nome, idGrupo))
                } while (cursor.moveToNext())
            }
        } catch (e: Exception) { e.printStackTrace() } finally { cursor?.close(); db.close() }
        return listaItens
    }

    // --- DETALHES E SUB-SINTOMAS ---
    fun getDetalhesItemCiap(codigoCiap: String): DetalheCiap? {
        val db = this.readableDatabase
        var cursor: Cursor? = null
        var detalhe: DetalheCiap? = null
        try {
            cursor = db.query("tb_ciap", arrayOf("codigo_ciap2", "enfermidade_leigo", "sintomas_inclusos", "sintomas_exclusao", "possiveis_cid10", "outros_sintomas"), "codigo_ciap2 = ?", arrayOf(codigoCiap), null, null, null)
            if (cursor != null && cursor.moveToFirst()) {
                detalhe = DetalheCiap(
                    codigo = cursor.getString(0),
                    nomeLeigo = cursor.getString(1),
                    sintomasInclusos = cursor.getString(2),
                    sintomasExclusao = cursor.getString(3),
                    possiveisCid10 = cursor.getString(4),
                    outrosSintomas = cursor.getString(5)
                )
            }
        } catch (e: Exception) { e.printStackTrace() } finally { cursor?.close(); db.close() }
        return detalhe
    }

    fun getSubSintomas(codigoCiap: String): List<SubSintoma> {
        val lista = mutableListOf<SubSintoma>()
        val db = this.readableDatabase
        var cursor: Cursor? = null
        try {
            cursor = db.query("tb_sintomas", arrayOf("sub_enfermidade"), "CIAP2_Codigo_fk = ?", arrayOf(codigoCiap), null, null, "sub_enfermidade ASC")
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    val nome = cursor.getString(0)
                    if (!nome.isNullOrBlank()) lista.add(SubSintoma(nome))
                } while (cursor.moveToNext())
            }
        } catch (e: Exception) { e.printStackTrace() } finally { cursor?.close(); db.close() }
        return lista
    }

    fun getMapLetrasCiap2(): Map<String, String> {
        val mapa = mutableMapOf<String, String>()
        val db = this.readableDatabase
        var cursor: Cursor? = null
        try {
            cursor = db.query(
                "letras_ciap2",
                arrayOf("Letra", "Orgao_anatomico"),
                null, null, null, null, null
            )
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    val letra = cursor.getString(0)?.uppercase()
                    val descricao = cursor.getString(1)
                    if (letra != null && descricao != null) {
                        mapa[letra] = descricao
                    }
                } while (cursor.moveToNext())
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            cursor?.close()
            db.close()
        }
        return mapa
    }

    // --- LISTAR TODAS AS PESSOAS ---
    fun getTodasPessoas(): List<Usuario> {
        val lista = mutableListOf<Usuario>()
        val db = this.readableDatabase
        var cursor: Cursor? = null
        try {
            cursor = db.query("pessoa", null, null, null, null, null, "Nome ASC")
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    val id = cursor.getInt(cursor.getColumnIndexOrThrow("IdPessoa"))
                    val nome = cursor.getString(cursor.getColumnIndexOrThrow("Nome"))
                    val cpf = cursor.getString(cursor.getColumnIndexOrThrow("CPF"))
                    val cargo = cursor.getString(cursor.getColumnIndexOrThrow("Cargo"))
                    val empresa = cursor.getString(cursor.getColumnIndexOrThrow("Empresa"))
                    val email = cursor.getString(cursor.getColumnIndexOrThrow("Email"))
                    val senha = cursor.getString(cursor.getColumnIndexOrThrow("Senha"))
                    val tel1 = cursor.getString(cursor.getColumnIndexOrThrow("Telefone1"))
                    val tel2 = cursor.getString(cursor.getColumnIndexOrThrow("Telefone2"))

                    lista.add(Usuario(id, nome, cpf, cargo, empresa, email, senha, tel1, tel2))
                } while (cursor.moveToNext())
            }
        } catch (e: Exception) { e.printStackTrace() } finally { cursor?.close(); db.close() }
        return lista
    }

    // --- ATUALIZAR PESSOA ---
    fun atualizarPessoa(usuario: Usuario): Boolean {
        val db = this.writableDatabase
        val values = ContentValues().apply {
            put("Nome", usuario.nome)
            put("CPF", usuario.cpf)
            put("Cargo", usuario.cargo)
            put("Empresa", usuario.empresa)
            put("Email", usuario.email)
            put("Telefone1", usuario.telefone1)
            put("Telefone2", usuario.telefone2)
            // Senha opcional: só atualiza se não estiver vazia (lógica de segurança básica)
            if (usuario.senhaPlana.isNotBlank()) {
                put("Senha", usuario.senhaPlana)
            }
        }
        return try {
            val linhasAfetadas = db.update("pessoa", values, "IdPessoa = ?", arrayOf(usuario.id.toString()))
            db.close()
            linhasAfetadas > 0
        } catch (e: Exception) {
            db.close()
            false
        }
    }

    // --- DELETAR PESSOA ---
    fun deletarPessoa(idPessoa: Int): Boolean {
        val db = this.writableDatabase
        return try {
            val linhas = db.delete("pessoa", "IdPessoa = ?", arrayOf(idPessoa.toString()))
            db.close()
            linhas > 0
        } catch (e: Exception) {
            db.close()
            false
        }
    }

    fun getTodasQuestoes(): List<Questao> {
        val lista = mutableListOf<Questao>()
        val db = this.readableDatabase
        var cursor: Cursor? = null
        try {
            cursor = db.query("questao_questionario", null, null, null, null, null, "IdQuestao DESC")
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    val id = cursor.getInt(cursor.getColumnIndexOrThrow("IdQuestao"))
                    val desc = cursor.getString(cursor.getColumnIndexOrThrow("Descricao"))
                    val resp = cursor.getString(cursor.getColumnIndexOrThrow("Resposta_correta"))
                    // Ignorando FKs por enquanto para simplificar a listagem
                    lista.add(Questao(id, desc, resp))
                } while (cursor.moveToNext())
            }
        } catch (e: Exception) { e.printStackTrace() } finally { cursor?.close(); db.close() }
        return lista
    }

    fun adicionarQuestao(questao: Questao): Long {
        val db = this.writableDatabase
        val values = ContentValues().apply {
            put("Descricao", questao.descricao)
            put("Resposta_correta", questao.respostaCorreta)
            // Se quiser vincular ao usuário logado futuramente:
            // put("IdPessoa_FK", questao.idPessoaFk)
        }
        return try {
            val id = db.insertOrThrow("questao_questionario", null, values)
            db.close()
            id
        } catch (e: Exception) {
            db.close()
            -1L
        }
    }

    fun deletarQuestao(idQuestao: Int): Boolean {
        val db = this.writableDatabase
        return try {
            // 1. Deleta as alternativas primeiro (limpeza)
            db.delete("resposta_questionario", "IdQuestao_FK = ?", arrayOf(idQuestao.toString()))
            // 2. Deleta a questão
            val linhas = db.delete("questao_questionario", "IdQuestao = ?", arrayOf(idQuestao.toString()))
            db.close()
            linhas > 0
        } catch (e: Exception) {
            db.close()
            false
        }
    }

    fun getAlternativas(idQuestao: Int): List<Alternativa> {
        val lista = mutableListOf<Alternativa>()
        val db = this.readableDatabase
        var cursor: Cursor? = null
        try {
            cursor = db.query(
                "resposta_questionario",
                null,
                "IdQuestao_FK = ?",
                arrayOf(idQuestao.toString()),
                null, null, "Letra ASC"
            )
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    val id = cursor.getInt(cursor.getColumnIndexOrThrow("IdResposta"))
                    val letra = cursor.getString(cursor.getColumnIndexOrThrow("Letra"))
                    val desc = cursor.getString(cursor.getColumnIndexOrThrow("Descricao"))
                    val idFk = cursor.getInt(cursor.getColumnIndexOrThrow("IdQuestao_FK"))
                    lista.add(Alternativa(id, letra, desc, idFk))
                } while (cursor.moveToNext())
            }
        } catch (e: Exception) { e.printStackTrace() } finally { cursor?.close(); db.close() }
        return lista
    }

    fun adicionarAlternativa(alt: Alternativa): Boolean {
        val db = this.writableDatabase
        val values = ContentValues().apply {
            put("Letra", alt.letra)
            put("Descricao", alt.descricao)
            put("IdQuestao_FK", alt.idQuestaoFk)
        }
        return try {
            db.insertOrThrow("resposta_questionario", null, values)
            db.close()
            true
        } catch (e: Exception) {
            db.close()
            false
        }
    }

    fun atualizarQuestao(questao: Questao): Boolean {
        val db = this.writableDatabase
        val values = ContentValues().apply {
            put("Descricao", questao.descricao)
            put("Resposta_correta", questao.respostaCorreta)
        }
        return try {
            val rows = db.update("questao_questionario", values, "IdQuestao = ?", arrayOf(questao.id.toString()))
            db.close()
            rows > 0
        } catch (e: Exception) {
            db.close()
            false
        }
    }

    // Método auxiliar para limpar alternativas antes de salvar as novas na edição
    fun deletarAlternativasDaQuestao(idQuestao: Int) {
        val db = this.writableDatabase
        try {
            db.delete("resposta_questionario", "IdQuestao_FK = ?", arrayOf(idQuestao.toString()))
            db.close()
        } catch (e: Exception) { db.close() }
    }

    // --- MÉTODOS PARA O TESTE DE CONHECIMENTO (QUIZ) ---

    /**
     * Retorna apenas os IDs de todas as questões.
     * Usamos isso para fazer o embaralhamento (shuffle) leve, sem carregar tudo de vez.
     */
    fun getListaIdsQuestoes(): List<Int> {
        val listaIds = mutableListOf<Int>()
        val db = this.readableDatabase
        var cursor: Cursor? = null
        try {
            cursor = db.query("questao_questionario", arrayOf("IdQuestao"), null, null, null, null, null)
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    val id = cursor.getInt(0)
                    listaIds.add(id)
                } while (cursor.moveToNext())
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            cursor?.close()
            db.close()
        }
        return listaIds
    }

    /**
     * Monta o objeto completo (Questão + Alternativas) dado um ID.
     */
    fun getQuestaoCompleta(idQuestao: Int): QuestaoCompleta? {
        val db = this.readableDatabase
        var cursor: Cursor? = null
        var questaoEncontrada: Questao? = null

        // 1. Busca a Questão
        try {
            cursor = db.query(
                "questao_questionario",
                null,
                "IdQuestao = ?",
                arrayOf(idQuestao.toString()),
                null, null, null
            )
            if (cursor != null && cursor.moveToFirst()) {
                val id = cursor.getInt(cursor.getColumnIndexOrThrow("IdQuestao"))
                val desc = cursor.getString(cursor.getColumnIndexOrThrow("Descricao"))
                val resp = cursor.getString(cursor.getColumnIndexOrThrow("Resposta_correta"))
                // fkPessoa e fkProcedimento podem ser nulos, ignoramos por enquanto no Quiz
                questaoEncontrada = Questao(id, desc, resp)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        } finally {
            cursor?.close()
            // Não fecha o DB ainda, vamos usar para as alternativas!
        }

        if (questaoEncontrada == null) return null

        // 2. Busca as Alternativas dessa questão
        // Reutilizamos a função getAlternativas que já criamos antes!
        // Como ela abre e fecha o banco, vamos ter que reabrir ou confiar nela.
        // O ideal é que getAlternativas gerencie sua própria conexão, o que ela faz.
        // Mas como fechamos o cursor ali em cima, mas não o 'db' (porque getAlternativas usa 'this.readableDatabase'),
        // precisamos ter cuidado.
        // No Android SQLiteOpenHelper, chamar 'readableDatabase' várias vezes retorna a mesma instância se já estiver aberta.
        // Vamos chamar direto:

        val listaAlternativas = getAlternativas(idQuestao)

        return QuestaoCompleta(questaoEncontrada, listaAlternativas)
    }

    fun getTelefonePorId(id: Int): TelefoneUtil? {
        var telefone: TelefoneUtil? = null
        val db = this.readableDatabase
        var cursor: Cursor? = null
        try {
            cursor = db.query("telefonesuteis", arrayOf("IdTelefone", "Numero", "Unidadesaude"), "IdTelefone = ?", arrayOf(id.toString()), null, null, null)
            if (cursor != null && cursor.moveToFirst()) {
                telefone = TelefoneUtil(cursor.getInt(0), cursor.getString(1), cursor.getString(2))
            }
        } catch (e: Exception) { e.printStackTrace() } finally { cursor?.close(); db.close() }
        return telefone
    }

    fun atualizarTelefoneUtil(telefone: TelefoneUtil): Boolean {
        val db = this.writableDatabase
        val values = ContentValues().apply {
            put("Unidadesaude", telefone.unidadeSaude)
            put("Numero", telefone.numero)
        }
        return try {
            val rows = db.update("telefonesuteis", values, "IdTelefone = ?", arrayOf(telefone.id.toString()))
            db.close()
            rows > 0
        } catch (e: Exception) {
            db.close()
            false
        }
    }

    fun deletarTelefoneUtil(id: Int): Boolean {
        val db = this.writableDatabase
        return try {
            val rows = db.delete("telefonesuteis", "IdTelefone = ?", arrayOf(id.toString()))
            db.close()
            rows > 0
        } catch (e: Exception) {
            db.close()
            false
        }
    }

    // --- SUPORTE BÁSICO DE VIDA (SBV) ---
    fun getItensSBV(): List<ItemSbv> {
        val lista = mutableListOf<ItemSbv>()
        val db = this.readableDatabase
        var cursor: Cursor? = null
        try {
            cursor = db.query("sbv_acidentes", arrayOf("CATEGORIA", "ENFERMIDADE", "SINTOMAS_PROCED"), null, null, null, null, "CATEGORIA ASC, ENFERMIDADE ASC")

            if (cursor != null && cursor.moveToFirst()) {
                var index = 0
                do {
                    // ADICIONEI O .trim() AQUI EMBAIXO
                    // Ele remove espaços invisíveis e quebras de linha que duplicam categorias
                    val cat = cursor.getString(0)?.trim() ?: "Geral"
                    val enf = cursor.getString(1)?.trim() ?: "Sem título"
                    val proc = cursor.getString(2)?.trim() ?: "Sem descrição"

                    lista.add(ItemSbv(index++, cat, enf, proc))
                } while (cursor.moveToNext())
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            cursor?.close()
            db.close()
        }
        return lista
    }
}