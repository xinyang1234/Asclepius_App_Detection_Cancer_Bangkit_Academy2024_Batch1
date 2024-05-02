package com.dicoding.asclepius.view

import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.dicoding.asclepius.R
import com.dicoding.asclepius.databinding.ActivityMainBinding
import com.dicoding.asclepius.helper.ImageClassifierHelper
import com.yalantis.ucrop.UCrop
import org.tensorflow.lite.task.vision.classifier.Classifications
import java.io.File
import java.util.Date

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    private var currentImageUri: Uri? = null
    private var imageClassifierHelper: ImageClassifierHelper? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        checkInternetConnection()

        binding.galleryButton.setOnClickListener { startGallery() }
        binding.analyzeButton.setOnClickListener {
            currentImageUri?.let {
                analyzeImage(it)
            } ?: run {
                showToast(getString(R.string.image_warning))
            }
        }
    }

    private fun startGallery() {
        launcherGallery.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
    }

    private fun showImage() {
        currentImageUri?.let {
            Log.d("Image URI", "showImage: $it")
            binding.previewImageView.setImageURI(it)
        }
    }

    private fun analyzeImage(uri: Uri) {
        try {
            imageClassifierHelper = ImageClassifierHelper(context = this,
                classifierListener = object : ImageClassifierHelper.ClassifierListener {
                    override fun onError(error: String) {
                        Toast.makeText(
                            this@MainActivity,
                            "Something went error!",
                            Toast.LENGTH_SHORT
                        ).show()
                    }

                    override fun onResults(results: List<Classifications>?, inferenceTime: Long) {
                        results?.let { listClassification ->
                            val topResult = listClassification[0]
                            val result = topResult.categories[0].label
                            val confidence = topResult.categories[0].score
                            showToast("Done Analyzing Image!")
                            moveToResult(uri.toString(), result, confidence, inferenceTime)
                        }
                    }
                })
        } catch (e: Exception) {
            showToast("Error: ${e.message}")
        }

        imageClassifierHelper?.classifyStaticImage(uri)
    }

    private fun moveToResult(
        currentImageUri: String,
        label: String,
        confidence: Float,
        inferenceTime: Long
    ) {
        val intent = Intent(this, ResultActivity::class.java)
        intent.putExtra(ResultActivity.EXTRA_IMAGE_URI, currentImageUri)
        intent.putExtra(ResultActivity.EXTRA_LABEL, label)
        intent.putExtra(ResultActivity.EXTRA_CONFIDANCE_SCORE, confidence)
        intent.putExtra(ResultActivity.EXTRA_TIME, inferenceTime)
        startActivity(intent)
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    private val launcherGallery = registerForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            currentImageUri = uri
            cropperImage(uri)
        } else {
            Log.d("Photo Picker", "No media selected")
        }
    }

    private fun cropperImage(uri: Uri) {
        UCrop.of(uri, Uri.fromFile(File(cacheDir, "cropped_image_${Date().time}.jpg")))
            .withAspectRatio(1f, 1f).getIntent(this).apply {
                imageCropperLauncher.launch(this)
            }
    }

    private val imageCropperLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { value ->
        if (value.resultCode == RESULT_OK) {
            val resultValue = UCrop.getOutput(value.data!!)
            if (resultValue != null) {
                currentImageUri = resultValue
                showImage()
            }
        } else if (value.resultCode == UCrop.RESULT_ERROR) {
            showToast("Error caused by : ${UCrop.getError(value.data!!)?.localizedMessage}")
        }
    }

    private fun checkInternetConnection() {
        val connectivityManager =
            getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val networkInfo = connectivityManager.activeNetworkInfo

        if (networkInfo == null || !networkInfo.isConnected) {
            // Tampilkan pesan Toast jika tidak ada koneksi internet
            showToast("Tidak ada koneksi internet")
        }
    }
}