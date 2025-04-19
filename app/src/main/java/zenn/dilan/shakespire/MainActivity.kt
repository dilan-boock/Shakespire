package zenn.dilan.shakespire

import android.Manifest
import android.animation.ValueAnimator
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.DocumentsContract
import android.provider.OpenableColumns
import android.view.View
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale


class MainActivity : AppCompatActivity() {
    private lateinit var currentLanguage: String
    private var currentPdfUri: Uri? = null
    // Регистрируем контракт для выбора файла
    private val pickPdfLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            result.data?.data?.let { uri ->
                var selectedPdfUri = uri
                handleSelectedPdf(uri)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val languageButton: ImageButton = findViewById(R.id.language)
        languageButton.setOnClickListener {
            toggleLanguage()
        }

        val attachButton: ImageButton = findViewById(R.id.attach)
        attachButton.setOnClickListener {
            openFilePicker()
        }

        val removePdfButton: ImageButton = findViewById(R.id.remove_pdf)
        removePdfButton.setOnClickListener {
            removeSelectedPdf()
        }

        val downloadButton = findViewById<ImageButton>(R.id.download)
        downloadButton.setOnClickListener {
            currentPdfUri?.let { uri ->
                val fileName = getFileNameFromUri(uri)
                downloadFile(uri, fileName)
            }
        }

        val sendButton: ImageButton = findViewById(R.id.send)
        sendButton.setOnClickListener {
            sendMessage()
        }
        sendButton.isEnabled = false

    }

    private fun toggleLanguage() {
        val configuration = resources.configuration

        // Получаем текущую локаль
        val currentLocale = configuration.locales[0]
        if (currentLocale.language  == "en") {
            onbutLocalRu()
        }
        else{
            onbutLocalEn()
        }

    }

    private fun onbutLocalRu() {
        val newLocale = Locale("default") // Здесь указывается желаемая локаль, например "en" для английского языка
        val configuration = getResources().getConfiguration()
        configuration.locale = newLocale
        getResources().updateConfiguration(configuration, getResources().getDisplayMetrics())
        recreate()
    }
    private fun onbutLocalEn() {
        val newLocale = Locale("en") // Здесь указывается желаемая локаль, например "en" для английского языка
        val configuration = getResources().getConfiguration()
        configuration.locale = newLocale
        getResources().updateConfiguration(configuration, getResources().getDisplayMetrics())
        recreate()
    }

    private fun openFilePicker() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "application/pdf"
            flags = Intent.FLAG_GRANT_READ_URI_PERMISSION or
                    Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION
        }
        pickPdfLauncher.launch(intent)
    }

    private fun handleSelectedPdf(uri: Uri) {
        currentPdfUri = uri
        // Получаем имя файла
        val fileName = getFileNameFromUri(uri)

        // Показываем уведомление
        Toast.makeText(this, "Выбран файл: $fileName", Toast.LENGTH_SHORT).show()
        // Показываем превью файла
        val pdfPreviewContainer = findViewById<LinearLayout>(R.id.pdf_preview_container)
        val pdfFileName = findViewById<TextView>(R.id.pdf_file_name)
        val attachButton = findViewById<ImageButton>(R.id.attach)

        pdfFileName.text = fileName
        pdfPreviewContainer.visibility = View.VISIBLE

        val sendButton: ImageButton = findViewById(R.id.send)

        sendButton.setBackgroundResource(R.drawable.send)
        sendButton.isEnabled = true
        attachButton.setBackgroundResource(R.drawable.attach_off)
        attachButton.isEnabled = false




        // Здесь можно добавить обработку PDF:
        // - Отправить на сервер
        // - Показать превью
        // - Сохранить ссылку для дальнейшего использования
    }
    private fun removeSelectedPdf() {
        var selectedPdfUri = null

        val pdfPreviewContainer = findViewById<LinearLayout>(R.id.pdf_preview_container)
        val attachButton = findViewById<ImageButton>(R.id.attach)

        pdfPreviewContainer.visibility = View.GONE
        val sendButton: ImageButton = findViewById(R.id.send)
        // Восстанавливаем кнопку скрепки
        attachButton.isEnabled = true
        sendButton.isEnabled = false
        sendButton.setBackgroundResource(R.drawable.send_off)
        attachButton.setBackgroundResource(R.drawable.attach)

        Toast.makeText(this, "PDF файл удалён", Toast.LENGTH_SHORT).show()
    }

    private fun getFileNameFromUri(uri: Uri): String {
        return contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            cursor.moveToFirst()
            cursor.getString(nameIndex)
        } ?: uri.path?.substringAfterLast('/') ?: "file.pdf"
    }

    private var rotationAnimator: ValueAnimator? = null
    private var animationJob: Job? = null
    private var rotationAnimator2: ValueAnimator? = null
    private var animationJob2: Job? = null

    private fun sendMessage(){
        val maskotImage: ImageView = findViewById(R.id.maskot)
        val maskotText: TextView = findViewById(R.id.text_maskot)
        val gearView: LinearLayout = findViewById(R.id.gearView)
        val pdfPreviewContainer = findViewById<LinearLayout>(R.id.pdf_preview_container)
        val downloadButton: ImageButton = findViewById(R.id.download)
        downloadButton.visibility = View.GONE
        pdfPreviewContainer.visibility = View.GONE
        gearView.visibility = View.VISIBLE
        val gear1Image: ImageView = findViewById(R.id.gear1)
        val gear2Image: ImageView = findViewById(R.id.gear2)
        rotateAnimatorPlus(gear1Image)
        rotateAnimatorMinus(gear2Image)
        maskotImage.setBackgroundResource(R.drawable.maskot_learn)
        maskotText.setText(R.string.text_input_learn)
    }

    private fun rotateAnimatorPlus(imageView: ImageView) {
        // Отменяем предыдущую анимацию, если есть
        rotationAnimator?.cancel()
        animationJob?.cancel()

        // Создаем новый аниматор
        rotationAnimator = ValueAnimator.ofFloat(0f, 360f).apply {
            duration = 2000
            repeatCount = ValueAnimator.INFINITE

            addUpdateListener { animation ->
                val value = animation.animatedValue as Float
                imageView.rotation = value
            }
        }

        val sendButton: ImageButton = findViewById(R.id.send)
        sendButton.setBackgroundResource(R.drawable.send_off)
        sendButton.isEnabled = false

        // Запускаем анимацию
        rotationAnimator?.start()

        // Запускаем корутину для остановки через 30 секунд
        animationJob = lifecycleScope.launch {
            delay(10000) // Ждем 30 секунд

            // Останавливаем анимацию в основном потоке
            withContext(Dispatchers.Main) {
                rotationAnimator?.cancel()
                imageView.rotation = 0f // Сбрасываем rotation в исходное положение
            }
        }
    }
    private fun rotateAnimatorMinus(imageView: ImageView) {
        // Отменяем предыдущую анимацию, если есть
        rotationAnimator2?.cancel()
        animationJob2?.cancel()

        // Создаем новый аниматор
        rotationAnimator2 = ValueAnimator.ofFloat(360f, 0f).apply {
            duration = 2000
            repeatCount = ValueAnimator.INFINITE

            addUpdateListener { animation ->
                val value = animation.animatedValue as Float
                imageView.rotation = value
            }
        }

        val sendButton: ImageButton = findViewById(R.id.send)
        sendButton.setBackgroundResource(R.drawable.send_off)
        sendButton.isEnabled = false

        // Запускаем анимацию
        rotationAnimator2?.start()

        // Запускаем корутину для остановки через 30 секунд
        animationJob2 = lifecycleScope.launch {
            delay(5000) // Ждем 30 секунд
            val maskotImage: ImageView = findViewById(R.id.maskot)
            maskotImage.setBackgroundResource(R.drawable.maskot_magik)

            val maskotText: TextView = findViewById(R.id.text_maskot)
            maskotText.setText(R.string.text_input_learn2)
            delay(5000) // Ждем 30 секунд

            // Останавливаем анимацию в основном потоке
            withContext(Dispatchers.Main) {
                rotationAnimator2?.cancel()
                imageView.rotation = 0f // Сбрасываем rotation в исходное положение

                maskotImage.setBackgroundResource(R.drawable.shekspire)
                val maskotText: TextView = findViewById(R.id.text_maskot)
                maskotText.setText(R.string.text_input_download)

                // Восстанавливаем кнопку send
                val attachButton: ImageButton = findViewById(R.id.attach)
                sendButton.setBackgroundResource(R.drawable.send)
                attachButton.setBackgroundResource(R.drawable.attach)
                sendButton.isEnabled = true
                attachButton.isEnabled = true

                // Показываем кнопку скачивания (если нужно)
                val downloadButton = findViewById<ImageButton>(R.id.download)
                val gearImage: LinearLayout = findViewById(R.id.gearView)
                downloadButton.visibility = View.VISIBLE
                gearImage.visibility = View.GONE
            }
        }
    }

    private fun downloadFile(uri: Uri, fileName: String) {
        val intent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "application/pdf"
            putExtra(Intent.EXTRA_TITLE, fileName)

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                putExtra(DocumentsContract.EXTRA_INITIAL_URI, uri)
            }
        }

        // Для Android 10+ (API 29+) разрешение не требуется
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startActivityForResult(intent, DOWNLOAD_REQUEST_CODE)
        } else {
            // Для старых версий проверяем разрешение
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                startActivityForResult(intent, DOWNLOAD_REQUEST_CODE)
            } else {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                    STORAGE_PERMISSION_CODE
                )
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        when (requestCode) {
            STORAGE_PERMISSION_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    currentPdfUri?.let { uri ->
                        val fileName = getFileNameFromUri(uri)
                        downloadFile(uri, fileName)
                    }
                } else {
                    Toast.makeText(
                        this,
                        "Для сохранения файла необходимо разрешение",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    // Обработка результата выбора места сохранения
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == DOWNLOAD_REQUEST_CODE && resultCode == RESULT_OK) {
            data?.data?.let { destinationUri ->
                currentPdfUri?.let { sourceUri ->
                    // Копируем файл в выбранное место
                    copyFileToDestination(this, sourceUri, destinationUri)
                } ?: run {
                    Toast.makeText(this, "Файл не выбран", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun copyFileToDestination(context: Context, sourceUri: Uri, destinationUri: Uri) {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                context.contentResolver.openInputStream(sourceUri)?.use { inputStream ->
                    context.contentResolver.openOutputStream(destinationUri)?.use { outputStream ->
                        inputStream.copyTo(outputStream)

                        withContext(Dispatchers.Main) {
                            Toast.makeText(
                                context,
                                "Файл успешно сохранен",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        context,
                        "Ошибка при сохранении: ${e.localizedMessage}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }

    // Константы
    companion object {
        private const val DOWNLOAD_REQUEST_CODE = 1002
        private const val STORAGE_PERMISSION_CODE = 1003
    }

}