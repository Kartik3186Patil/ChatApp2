package com.example.chatapp.activities

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Binder
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import androidx.annotation.RequiresApi
import com.example.chatapp.R
import com.example.chatapp.databinding.ActivityMainBinding
import com.example.chatapp.databinding.ActivitySignupBinding
import com.example.chatapp.utilities.Constants
import com.example.chatapp.utilities.PreferenceManager
import android.util.Base64
import android.widget.Toast
import com.example.chatapp.adapters.RecentConversionAdapter
import com.example.chatapp.models.ChatMessage
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessaging

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var preferenceManager: PreferenceManager
    private lateinit var conversations: MutableList<ChatMessage>
    private lateinit var conversationsAdapter:RecentConversionAdapter
    private lateinit var database:FirebaseFirestore




    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        preferenceManager= PreferenceManager(this)
     init()
        loadUserDetails()
        getToken()
        setListeners()

    }

    private fun init() {
        conversations = mutableListOf() // Correct way to initialize a MutableList
        conversationsAdapter = RecentConversionAdapter(conversations)
        binding.conversionRecyclerView.adapter = conversationsAdapter
        database = FirebaseFirestore.getInstance()
    }
    private fun setListeners(){
        binding.imageSignOut.setOnClickListener{
            signOut()
        }
        binding.fabNewChat.setOnClickListener{
            val intent:Intent=Intent(this,UsersActivity::class.java)
            startActivity(intent)
        }
    }



    private fun loadUserDetails(){
        binding.textName.text = preferenceManager.getString(Constants.KEY_NAME)
        val base64String = preferenceManager.getString(Constants.KEY_IMAGE)
        val decodedBytes = Base64.decode(base64String,Base64.DEFAULT)
        val bitmap: Bitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
        binding.imageProfile.setImageBitmap(bitmap)

    }
    private fun showToast(message:String){
        Toast.makeText(this,message,Toast.LENGTH_SHORT).show()
    }

    private fun getToken(){
        FirebaseMessaging.getInstance().token.addOnSuccessListener(this::updateToken)
    }

    private fun updateToken(Token: String) {
        val database: FirebaseFirestore = FirebaseFirestore.getInstance()
        val userId: String = preferenceManager.getString(Constants.KEY_USER_ID) ?: return

        val documentReference: DocumentReference = database.collection(Constants.KEY_COLLECTIONS_USERS)
            .document(userId)

        documentReference.update(Constants.KEY_FCM_TOKEN, Token)

            .addOnFailureListener {
                showToast("Unable to update token")
            }
    }
    private fun signOut(){
        showToast("Signing Out...")
        val database: FirebaseFirestore = FirebaseFirestore.getInstance()
        val userId: String = preferenceManager.getString(Constants.KEY_USER_ID) ?: return

        val documentReference: DocumentReference = database.collection(Constants.KEY_COLLECTIONS_USERS)
            .document(userId)
        val updates = HashMap<String, Any>()
        updates[Constants.KEY_FCM_TOKEN] = FieldValue.delete()
        documentReference.update(updates)
            .addOnSuccessListener {
                preferenceManager.clear()
                val intent:Intent=Intent(this,SignInActivity::class.java)
                startActivity(intent)
                finish()
            }
            .addOnFailureListener {
                showToast("Unable to Sign out")
            }


    }

}
