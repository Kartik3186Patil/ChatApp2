package com.example.chatapp.activities
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.util.Base64
import android.util.Patterns
import android.view.View
import android.widget.Toast

import androidx.activity.result.contract.ActivityResultContracts
import com.example.chatapp.activities.MainActivity
import com.example.chatapp.databinding.ActivitySignupBinding
import com.example.chatapp.utilities.Constants
import com.example.chatapp.utilities.PreferenceManager
import com.google.firebase.firestore.FirebaseFirestore
import java.io.ByteArrayOutputStream
import java.io.FileNotFoundException
import java.io.InputStream

class SignUpActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySignupBinding
    private lateinit var preferenceManager: PreferenceManager
    private lateinit var encodedImage: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignupBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Get an instance of PreferenceManager with the current activity context
        preferenceManager = PreferenceManager(this)

        setListeners()
    }

    private fun setListeners() {
        binding.buttonSignUp.setOnClickListener {
            if (isValidSignUpDetails()) {
                signUp()
            }
        }
        binding.layoutImage.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            pickImage.launch(intent)
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    private fun signUp() {
        loading(true)
        val database: FirebaseFirestore = FirebaseFirestore.getInstance()
        val user = HashMap<String, Any>()
        user[Constants.KEY_NAME] = binding.inputNameSignUp.text.toString()
        user[Constants.KEY_EMAIL] = binding.inputEmailSignUp.text.toString()
        user[Constants.KEY_PASSWORD] = binding.inputPasswordSignUp.text.toString()
        user[Constants.KEY_IMAGE] = encodedImage

        database.collection(Constants.KEY_COLLECTIONS_USERS)
            .add(user)
            .addOnSuccessListener { documentReference ->
                loading(false)
                // Save user info in preferences after successful signup
                preferenceManager.putBoolean(Constants.KEY_IS_SIGNED_IN, true)
                preferenceManager.putString(Constants.KEY_USER_ID, documentReference.id)
                preferenceManager.putString(Constants.KEY_NAME, binding.inputNameSignUp.text.toString())
                preferenceManager.putString(Constants.KEY_IMAGE, encodedImage)

                val intent = Intent(this, MainActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                startActivity(intent)
            }
            .addOnFailureListener { exception ->
                loading(false)
                exception.message?.let { showToast(it) }
            }
    }

    private fun encodeImage(bitmap: Bitmap): String? {
        val previewWidth = 150
        val previewHeight: Int = bitmap.height * previewWidth / bitmap.width
        val previewBitmap: Bitmap = Bitmap.createScaledBitmap(bitmap, previewWidth, previewHeight, false)
        val byteArrayOutputStream = ByteArrayOutputStream()
        previewBitmap.compress(Bitmap.CompressFormat.JPEG, 50, byteArrayOutputStream)
        val bytes: ByteArray = byteArrayOutputStream.toByteArray()
        return Base64.encodeToString(bytes, Base64.DEFAULT)
    }

    private val pickImage =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                val data: Intent? = result.data
                if (data != null) {
                    val imageUri: Uri? = data.data
                    try {
                        val inputStream: InputStream? = contentResolver.openInputStream(imageUri!!)
                        val bitmap: Bitmap? = BitmapFactory.decodeStream(inputStream)
                        binding.imageProfile.setImageBitmap(bitmap)
                        binding.textAddImage.visibility = View.GONE

                        // Encode the selected image to Base64 String
                        if (bitmap != null) {
                            encodedImage = encodeImage(bitmap) ?: ""
                        }
                    } catch (e: FileNotFoundException) {
                        e.printStackTrace()
                    }
                }
            }
        }

    private fun isValidSignUpDetails(): Boolean {
        if (encodedImage.isEmpty()) {
            showToast("Select profile image")
            return false
        } else if (binding.inputNameSignUp.text.toString().trim().isEmpty()) {
            showToast("Enter name")
            return false
        } else if (binding.inputEmailSignUp.text.toString().trim().isEmpty()) {
            showToast("Enter email")
            return false
        } else if (!Patterns.EMAIL_ADDRESS.matcher(binding.inputEmailSignUp.text.toString()).matches()) {
            showToast("Enter a valid email")
            return false
        } else if (binding.inputPasswordSignUp.text.toString().trim().isEmpty()) {
            showToast("Enter password")
            return false
        } else if (binding.inputConfirmPasswordSignUp.text.toString().trim().isEmpty()) {
            showToast("Confirm your password")
            return false
        } else if (binding.inputPasswordSignUp.text.toString() != binding.inputConfirmPasswordSignUp.text.toString()) {
            showToast("Password and confirm password must be the same")
            return false
        }
        return true
    }

    private fun loading(isLoading: Boolean) {
        if (isLoading) {
            binding.buttonSignUp.visibility = View.INVISIBLE
            binding.progressBar.visibility = View.VISIBLE
        } else {
            binding.buttonSignUp.visibility = View.VISIBLE
            binding.progressBar.visibility = View.INVISIBLE
        }
    }
}
