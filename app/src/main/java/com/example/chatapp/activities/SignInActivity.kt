package com.example.chatapp.activities


import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Patterns
import android.view.View
import android.widget.Toast
import com.example.chatapp.R
import com.example.chatapp.databinding.ActivitySigninBinding
import com.example.chatapp.utilities.Constants
import com.example.chatapp.utilities.PreferenceManager
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import java.io.ObjectInput

class SignInActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySigninBinding
    private lateinit var preferenceManager: PreferenceManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        preferenceManager = PreferenceManager(this)
        binding = ActivitySigninBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setListeners()
    }

    private fun setListeners() {
        binding.textcreateNewAccountSignIn.setOnClickListener {
            val intent = Intent(this@SignInActivity, SignUpActivity::class.java)
            startActivity(intent)
        }
        binding.buttonSignIn.setOnClickListener{
            if(isValidSignInDetails()){
                signIn()
            }
        }

    }

    private fun signIn() {
        loading(true)
        val database:FirebaseFirestore=FirebaseFirestore.getInstance()
        database.collection(Constants.KEY_COLLECTIONS_USERS)
            .whereEqualTo(Constants.KEY_EMAIL,binding.inputEmailSignIn.text.toString())
            .whereEqualTo(Constants.KEY_PASSWORD,binding.inputPasswordSignIn.text.toString())
            .get()
            .addOnCompleteListener{task->
                if(task.isSuccessful && task.result!=null && task.result.size()>0) run {
                    val documentSnapShot: DocumentSnapshot = task.result.documents[0]
                    preferenceManager.putBoolean(Constants.KEY_IS_SIGNED_IN,true)
                    preferenceManager.putString(Constants.KEY_USER_ID,documentSnapShot.id)
                    val nameValue=documentSnapShot.getString(Constants.KEY_NAME)
                    preferenceManager.putString(Constants.KEY_NAME,nameValue?:"")
                    val imageValue=documentSnapShot.getString(Constants.KEY_IMAGE)
                    preferenceManager.putString(Constants.KEY_IMAGE,imageValue?:"")
                    val intent:Intent=Intent(this,MainActivity::class.java)
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                    startActivity(intent)
                }
                else{
                    loading(false)
                    showToast("Incorrect Password ")
                }
            }
    }

    private fun loading(isLoading:Boolean){
        if(isLoading){
            binding.buttonSignIn.visibility = View.INVISIBLE
            binding.progressBar.visibility=View.VISIBLE
        }
        else{
            binding.buttonSignIn.visibility = View.VISIBLE
            binding.progressBar.visibility=View.INVISIBLE
        }
    }

    private fun showToast(message:String){
        Toast.makeText(this,message,Toast.LENGTH_SHORT).show()
    }

    private fun isValidSignInDetails():Boolean{
        return if(binding.inputEmailSignIn.text.toString().trim().isEmpty()){
            showToast("Enter Email")
            false
        }else if(!Patterns.EMAIL_ADDRESS.matcher(binding.inputEmailSignIn.text.toString()).matches()){
            showToast("Enter valid Email")
            false
        }else if(binding.inputPasswordSignIn.text.toString().trim().isEmpty()){
            showToast("Enter password")
            false
        } else{
            true
        }

    }
}