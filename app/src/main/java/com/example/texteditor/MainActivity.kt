package com.example.texteditor // У вас будет свой пакет

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter

class MainActivity : AppCompatActivity() {

    private lateinit var editText: EditText
    private var currentFileUri: android.net.Uri? = null  // Хранит путь к открытому файлу

    // 1. Запускаем системный выборщик для ОТКРЫТИЯ файла
    private val openFileLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.data?.let { uri ->
                // Сохраняем разрешение на постоянный доступ
                contentResolver.takePersistableUriPermission(
                    uri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                )
                currentFileUri = uri
                loadFileContent(uri)
                Toast.makeText(this, "Файл открыт", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // 2. Запускаем системный выборщик для СОЗДАНИЯ нового файла
    private val saveFileLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.data?.let { uri ->
                currentFileUri = uri
                saveContentToUri(uri)
                Toast.makeText(this, "Файл сохранён", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        editText = findViewById(R.id.edit_text)
        val btnOpen = findViewById<Button>(R.id.btn_open)
        val btnSave = findViewById<Button>(R.id.btn_save)

        // Кнопка "Открыть файл"
        btnOpen.setOnClickListener {
            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                addCategory(Intent.CATEGORY_OPENABLE)
                type = "text/plain"  // Показываем только .txt файлы
            }
            openFileLauncher.launch(intent)
        }

        // Кнопка "Сохранить"
        btnSave.setOnClickListener {
            if (currentFileUri == null) {
                // Если файл ещё не открыт - создаём новый
                createNewFile()
            } else {
                // Если файл открыт - сохраняем в него
                saveContentToUri(currentFileUri!!)
                Toast.makeText(this, "Сохранено", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Создание нового файла
    private fun createNewFile() {
        val intent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "text/plain"
            putExtra(Intent.EXTRA_TITLE, "Новый_файл.txt")  // Имя по умолчанию
        }
        saveFileLauncher.launch(intent)
    }

    // Загрузка текста из файла в EditText
    private fun loadFileContent(uri: android.net.Uri) {
        try {
            contentResolver.openInputStream(uri)?.use { inputStream ->
                val reader = BufferedReader(InputStreamReader(inputStream))
                val stringBuilder = StringBuilder()
                var line: String? = reader.readLine()
                while (line != null) {
                    stringBuilder.append(line)
                    stringBuilder.append("\n")
                    line = reader.readLine()
                }
                editText.setText(stringBuilder.toString())
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "Ошибка при загрузке файла", Toast.LENGTH_SHORT).show()
        }
    }

    // Сохранение текста из EditText в файл
    private fun saveContentToUri(uri: android.net.Uri) {
        try {
            contentResolver.openOutputStream(uri)?.use { outputStream ->
                val writer = OutputStreamWriter(outputStream)
                writer.write(editText.text.toString())
                writer.flush()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "Ошибка при сохранении", Toast.LENGTH_SHORT).show()
        }
    }
}