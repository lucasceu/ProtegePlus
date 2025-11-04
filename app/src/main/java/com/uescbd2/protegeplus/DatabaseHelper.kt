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

// Assumindo que você vai renomear o "Verdadeiro.sql" para este nome de .db
private const val DATABASE_NAME = "Bd_protege_v7_SQLite.db"
// --- MUDANÇA CRÍTICA ---
// Mudei para 3 para forçar o onUpgrade
private const val DATABASE_VERSION = 4
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
                println("Banco de dados v7 (Verdadeiro) copiado com sucesso!")
            } catch (e: IOException) {
                println("Erro ao copiar banco de dados v7 (Verdadeiro): ${e.message}")
                throw IOException("Erro ao copiar banco de dados", e)
            }
        } else {
            println("Banco de dados v7 (Verdadeiro) já existe.")
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

    // Esta função será chamada por causa da mudança de versão (2 -> 3)
    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        if (newVersion > oldVersion) {
            println("Atualizando banco de dados da versão $oldVersion para $newVersion.")
            try {
                File(dbPath).delete() // Deleta o banco antigo (v7 bugado)
                copyDatabase() // Copia o novo (v7 verdadeiro)
                println("Banco de dados v7 (Verdadeiro) atualizado com sucesso.")
            } catch (e: IOException) {
                println("Erro ao atualizar o banco de dados v7 (Verdadeiro): ${e.message}")
            }
        }
    }

    // --- FUNÇÃO DE CADASTRO CORRIGIDA ---
    fun adicionarUsuario(usuario: Usuario): Boolean {
        if (usuario.email.isBlank() || usuario.senhaPlana.isBlank() || usuario.nome.isBlank()) {
            println("Erro ao adicionar: Email, Senha ou Nome não podem ser vazios.")
            return false
        }

        val db = this.writableDatabase
        val values = ContentValues().apply {
            // Usa os nomes da tabela "fonte da verdade"
            put("Nome", usuario.nome)
            put("CPF", usuario.cpf)
            put("Cargo", usuario.cargo)
            put("Telefone", usuario.telefone)
            put("Empresa", usuario.empresa)
            put("Email", usuario.email)
            put("Senha", usuario.senhaPlana) // O objeto Usuario chama de senhaPlana

            // NÃO colocamos o IdPessoa, pois ele é AUTOINCREMENT
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

    // --- FUNÇÃO DE LOGIN CORRIGIDA ---
    fun verificarLogin(email: String, senhaPlana: String): Boolean {
        if (email.isBlank() || senhaPlana.isBlank()) {
            return false
        }

        val db = this.readableDatabase
        var cursor: Cursor? = null
        var loginSucesso = false

        try {
            // Usa os nomes da tabela "fonte da verdade"
            val selection = "Email = ?" // <-- MUDANÇA (Maiúscula)
            val selectionArgs = arrayOf(email)
            val columns = arrayOf("Senha") // <-- MUDANÇA (Maiúscula)

            cursor = db.query(TABLE_PESSOA, columns, selection, selectionArgs, null, null, null)

            if (cursor != null && cursor.moveToFirst()) {
                val senhaNoBanco = cursor.getString(cursor.getColumnIndexOrThrow("Senha")) // <-- MUDANÇA
                if (senhaPlana == senhaNoBanco) {
                    loginSucesso = true
                    println("Login bem-sucedido para: $email")
                } else {
                    println("Senha incorreta para: $email")
                }
            } else {
                println("Email não encontrado na tabela '$TABLE_PESSOA': $email")
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

    fun getTelefonesUteis(): List<TelefoneUtil> {
        val listaTelefones = mutableListOf<TelefoneUtil>()
        val db = this.readableDatabase
        var cursor: Cursor? = null

        val tabela = "telefonesuteis"
        val colunas = arrayOf("IdTelefone", "Numero", "Unidadesaude")

        try {
            // Consulta a tabela 'telefonesuteis'
            cursor = db.query(tabela, colunas, null, null, null, null, "Unidadesaude ASC")

            if (cursor != null && cursor.moveToFirst()) {
                val idCol = cursor.getColumnIndexOrThrow("IdTelefone")
                val numCol = cursor.getColumnIndexOrThrow("Numero")
                val unidadeCol = cursor.getColumnIndexOrThrow("Unidadesaude")

                do {
                    val id = cursor.getInt(idCol)

                    // Lê como String, mesmo sendo INTEGER no DB, para ser mais seguro
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


}