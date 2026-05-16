package com.example.grama_khata

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.example.grama_khata.data.model.Customer
import com.example.grama_khata.databinding.ActivityAddCustomerBinding
import com.example.grama_khata.viewmodel.GramaKhataViewModel
import com.example.grama_khata.viewmodel.GramaKhataViewModelFactory
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class AddCustomerActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddCustomerBinding
    private lateinit var viewModel: GramaKhataViewModel
    private var selectedPhotoPath: String = ""
    private var cameraPhotoPath: String = ""

    private val galleryLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.data?.let { uri ->
                selectedPhotoPath = copyImageToAppStorage(uri)
                loadPhotoPreview(selectedPhotoPath)
            }
        }
    }

    private val cameraLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            selectedPhotoPath = cameraPhotoPath
            loadPhotoPreview(selectedPhotoPath)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddCustomerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.toolbar.setNavigationOnClickListener { finish() }

        val factory = GramaKhataViewModelFactory(application)
        viewModel = ViewModelProvider(this, factory)[GramaKhataViewModel::class.java]

        setupClickListeners()
    }

    private fun setupClickListeners() {
        binding.btnSelectPhoto.setOnClickListener { showPhotoOptions() }
        binding.btnSaveCustomer.setOnClickListener { saveCustomer() }
    }

    private fun showPhotoOptions() {
        val options = arrayOf("📷 Take Photo", "🖼️ Choose from Gallery")
        AlertDialog.Builder(this)
            .setTitle("Add Customer Photo")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> openCamera()
                    1 -> openGallery()
                }
            }
            .show()
    }

    private fun openGallery() {
        val intent = Intent(
            Intent.ACTION_PICK,
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        )
        galleryLauncher.launch(intent)
    }

    private fun openCamera() {
        val photoFile = createImageFile()
        cameraPhotoPath = photoFile.absolutePath
        val photoUri = FileProvider.getUriForFile(
            this, "${packageName}.fileprovider", photoFile
        )
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        intent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri)
        cameraLauncher.launch(intent)
    }

    private fun createImageFile(): File {
        val timestamp = SimpleDateFormat(
            "yyyyMMdd_HHmmss", Locale.getDefault()
        ).format(Date())
        val storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile("CUSTOMER_${timestamp}", ".jpg", storageDir)
    }

    private fun copyImageToAppStorage(uri: Uri): String {
        val timestamp = SimpleDateFormat(
            "yyyyMMdd_HHmmss", Locale.getDefault()
        ).format(Date())
        val storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        val destFile = File(storageDir, "CUSTOMER_${timestamp}.jpg")
        contentResolver.openInputStream(uri)?.use { input ->
            destFile.outputStream().use { output ->
                input.copyTo(output)
            }
        }
        return destFile.absolutePath
    }

    private fun loadPhotoPreview(path: String) {
        Glide.with(this)
            .load(File(path))
            .circleCrop()
            .placeholder(R.drawable.ic_person_placeholder)
            .into(binding.ivCustomerPhoto)
    }

    private fun saveCustomer() {
        val name = binding.etCustomerName.text.toString().trim()
        val phone = binding.etPhoneNumber.text.toString().trim()
        val village = binding.etVillage.text.toString().trim()

        if (name.isEmpty()) {
            binding.etCustomerName.error = "Please enter customer name"
            binding.etCustomerName.requestFocus()
            return
        }
        if (phone.isEmpty()) {
            binding.etPhoneNumber.error = "Please enter phone number"
            binding.etPhoneNumber.requestFocus()
            return
        }
        if (phone.length < 10) {
            binding.etPhoneNumber.error = "Enter valid 10-digit number"
            binding.etPhoneNumber.requestFocus()
            return
        }

        val customer = Customer(
            name = name,
            phone = phone,
            village = village,
            photoPath = selectedPhotoPath
        )

        viewModel.insertCustomer(customer) {
            runOnUiThread {
                Toast.makeText(
                    this,
                    "✅ $name added successfully!",
                    Toast.LENGTH_SHORT
                ).show()
                finish()
            }
        }
    }
}