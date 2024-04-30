package com.dicoding.asclepius.view

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import com.dicoding.asclepius.R
import com.dicoding.asclepius.databinding.ActivityResultBinding

class ResultActivity : AppCompatActivity() {
    private lateinit var binding: ActivityResultBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityResultBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val imageUri = Uri.parse(intent.getStringExtra(EXTRA_IMAGE_URI))
        val labelResult = intent.getStringExtra(EXTRA_LABEL)
        val confidenceResult = intent.getFloatExtra(EXTRA_CONFIDANCE_SCORE, 0.0f)

        imageUri?.let {
            Log.d("Image URI", "showImage: $it")
            binding.resultImage.setImageURI(it)
        }
        val toPercentage = (confidenceResult * 100).toInt()
        binding.resultText.text = "$labelResult $toPercentage%"

        findViewById<ImageView>(R.id.arrowback).setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    companion object {
        const val EXTRA_IMAGE_URI = "extra_image_uri"
        const val EXTRA_LABEL = "extra_label"
        const val EXTRA_CONFIDANCE_SCORE = "extra_confidance_score"
        const val EXTRA_TIME = "extra_time"
    }


}