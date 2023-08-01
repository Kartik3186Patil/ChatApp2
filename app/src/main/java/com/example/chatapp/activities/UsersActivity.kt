package com.example.chatapp.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import com.example.chatapp.R
import com.example.chatapp.adapters.UserAdapter
import com.example.chatapp.databinding.ActivityUsersBinding
import com.example.chatapp.listeners.UserListener
import com.example.chatapp.models.User
import com.example.chatapp.utilities.Constants
import com.example.chatapp.utilities.PreferenceManager
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QueryDocumentSnapshot


class UsersActivity : AppCompatActivity(),UserListener {

    private lateinit var binding: ActivityUsersBinding
    private lateinit var preferenceManager: PreferenceManager
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding=ActivityUsersBinding.inflate(layoutInflater)
        setContentView(binding.root)
        preferenceManager = PreferenceManager(this)
        setListeners()
        getUsers()
    }
    private fun setListeners(){
        binding.imageBack.setOnClickListener{
            val intent:Intent=Intent(this,MainActivity::class.java)
            startActivity(intent)
        }
    }

    private fun getUsers(){
        loading(true)
        var database:FirebaseFirestore=FirebaseFirestore.getInstance()
        database.collection(Constants.KEY_COLLECTIONS_USERS)
            .get()
            .addOnCompleteListener{task->
                loading(false)
                val currentUserId: String? =preferenceManager.getString(Constants.KEY_USER_ID)
                if(task.isSuccessful && task.result!=null){
                    val users = mutableListOf<User>()
                    for (queryDocumentSnapshot in task.result) {
                        if (currentUserId == queryDocumentSnapshot.id) {
                            continue
                        }
                        var user = User("", "", "", "","").apply {
                            name = queryDocumentSnapshot.getString(Constants.KEY_NAME).toString()
                            email = queryDocumentSnapshot.getString(Constants.KEY_EMAIL).toString()
                            image = queryDocumentSnapshot.getString(Constants.KEY_IMAGE).toString()
                            token = queryDocumentSnapshot.getString(Constants.KEY_FCM_TOKEN).toString()
                            id=queryDocumentSnapshot.id

                        }
                        users.add(user)
                    }
                    if(users.size>0){
                        var userAdapter:UserAdapter= UserAdapter(users,this)
                        binding.userRecyclerView.adapter = userAdapter
                        binding.userRecyclerView.visibility=View.VISIBLE
                    }
                    else{
                        showErrorMessage()
                    }
                }else{
                    showErrorMessage()
                }
            }
    }

    private fun showErrorMessage(){
        binding.textErrorMessage.text = String.format("%s","No user available")
        binding.textErrorMessage.visibility=View.VISIBLE
    }
    private fun loading(isLoading:Boolean){
        if(isLoading){
            binding.progressBar.visibility= View.VISIBLE
        }else{
            binding.progressBar.visibility=View.INVISIBLE
        }
    }

    override fun onUserClicked(user: User) {
        val intent:Intent= Intent(this,ChatActivity::class.java)
        intent.putExtra(Constants.KEY_USER,user)
        startActivity(intent)
        finish()


    }

}