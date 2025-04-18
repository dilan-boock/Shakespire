package zenn.dilan.shakespire

import android.animation.ValueAnimator
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.media.Image
import android.net.Uri
import android.os.Bundle
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
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import java.util.Locale


class MainActivity : AppCompatActivity() {
    private lateinit var currentLanguage: String
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

    private fun sendMessage(){
        val maskotImage: ImageView = findViewById(R.id.maskot)
        val maskotText: TextView = findViewById(R.id.text_maskot)
        val gear1Image: ImageView = findViewById(R.id.gear1)
        val gear2Image: ImageView = findViewById(R.id.gear2)
        rotateAnimatorPlus(gear1Image)
        rotateAnimatorMinus(gear2Image)
        maskotImage.setBackgroundResource(R.drawable.maskot_learn)
        maskotText.setText(R.string.text_input_learn)
    }

    private fun rotateAnimatorPlus(imageView: ImageView) {
        val animator = ValueAnimator.ofFloat(0f, 360f)
        animator.duration = 5000
        animator.repeatCount = ValueAnimator.INFINITE

        animator.addUpdateListener { animation ->
            val value = animation.animatedValue as Float
            imageView.rotation = value
        }

        animator.start()
    }
    private fun rotateAnimatorMinus(imageView: ImageView) {
        val animator = ValueAnimator.ofFloat(360f, 0f)
        animator.duration = 5000
        animator.repeatCount = ValueAnimator.INFINITE

        animator.addUpdateListener { animation ->
            val value = animation.animatedValue as Float
            imageView.rotation = value
        }

        animator.start()
    }

}