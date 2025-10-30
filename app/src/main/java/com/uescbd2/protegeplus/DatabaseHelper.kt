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

// Nome exato do arquivo no assets e nome do banco no dispositivo
private const val DATABASE_NAME = "Bd_protege_v6_SQLite.db"
// Incremente se atualizar o .db nos assets
private const val DATABASE_VERSION = 1
// Nome da tabela de usuários/pessoas
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
                println("Erro ao copiar banco de dados: ${e.message}")
                throw IOException("Erro ao copiar banco de dados", e)
            }
        } else {
            println("Banco de dados já existe.")
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

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        if (newVersion > oldVersion) {
            println("Atualizando banco de dados da versão $oldVersion para $newVersion.")
            try {
                File(dbPath).delete()
                copyDatabase()
                println("Banco de dados atualizado com sucesso.")
            } catch (e: IOException) {
                println("Erro ao atualizar o banco de dados: ${e.message}")
            }
        }
    }

    // --- FUNÇÃO DE CADASTRO CORRIGIDA (usa "IdPessoa") ---
    fun adicionarUsuario(usuario: Usuario): Boolean {
        if (usuario.email.isBlank() || usuario.senhaPlana.isBlank() || usuario.nome.isBlank()) {
            println("Erro ao adicionar: Email, Senha ou Nome não podem ser vazios.")
            return false
        }
        // Validação de teste: recusa ID 0 ou negativo
        if (usuario.IdPessoa <= 0) { // NOME CORRIGIDO
            println("Erro ao adicionar: ID de teste inválido (<= 0).")
            return false
        }

        val db = this.writableDatabase
        val values = ContentValues().apply {
            // Usa o nome correto da coluna "IdPessoa"
            put("IdPessoa", usuario.IdPessoa) // NOME CORRIGIDO (maiúsculas)
            put("nome", usuario.nome)
            put("cpf", usuario.cpf)
            put("cargo", usuario.cargo)
            put("telefone", usuario.telefone)
            put("empresa", usuario.empresa)
            put("email", usuario.email)
            put("senha", usuario.senhaPlana) // **INSEGURO!**
        }

        return try {
            val newRowId = db.insertOrThrow(TABLE_PESSOA, null, values)
            println("Usuário inserido com sucesso na tabela '$TABLE_PESSOA'. ID fornecido: ${usuario.IdPessoa}, Resultado: $newRowId") // NOME CORRIGIDO
            db.close()
            newRowId != -1L
        } catch (e: SQLiteException) {
            println("Erro ao inserir usuário na tabela '$TABLE_PESSOA': ${e.message}")
            db.close()
            false
        }
    }

    // --- FUNÇÃO DE LOGIN (Sem alterações, já estava ok) ---
    fun verificarLogin(email: String, senhaPlana: String): Boolean {
        // ... (código igual ao anterior) ...
        if (email.isBlank() || senhaPlana.isBlank()) {
            return false
        }

        val db = this.readableDatabase
        var cursor: Cursor? = null
        var loginSucesso = false

        try {
            val selection = "email = ?"
            val selectionArgs = arrayOf(email)
            val columns = arrayOf("senha") // **INSEGURO!**

            cursor = db.query(TABLE_PESSOA, columns, selection, selectionArgs, null, null, null)

            if (cursor != null && cursor.moveToFirst()) {
                val senhaNoBanco = cursor.getString(cursor.getColumnIndexOrThrow("senha"))
                if (senhaPlana == senhaNoBanco) { // **INSEGURO!**
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
}