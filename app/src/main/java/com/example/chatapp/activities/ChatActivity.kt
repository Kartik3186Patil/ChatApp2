package com.example.chatapp.activities

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Base64
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.example.chatapp.adapters.ChatAdapter
import com.example.chatapp.databinding.ActivityChatBinding
import com.example.chatapp.models.ChatMessage
import com.example.chatapp.models.User
import com.example.chatapp.utilities.Constants
import com.example.chatapp.utilities.PreferenceManager
import com.google.android.gms.tasks.OnCompleteListener

import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot

import com.google.firebase.firestore.EventListener
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import java.text.SimpleDateFormat
import java.util.Collections
import java.util.Date
import java.util.Locale

class ChatActivity : AppCompatActivity() {
    private lateinit var binding: ActivityChatBinding
    private lateinit var receiverUser: User
    private lateinit var chatMessages: MutableList<ChatMessage>
    private lateinit var preferenceManager: PreferenceManager
    private lateinit var chatAdapter: ChatAdapter
    private lateinit var database: FirebaseFirestore
    private var conversionId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChatBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setListeners()
        loadReceiverDetails()
        init()
        listenMessages()
    }

    private fun init() {
        preferenceManager = PreferenceManager(this)
        chatMessages = ArrayList()
        chatAdapter = ChatAdapter(
            chatMessages,
            getBitmapFromEncodedString(receiverUser.image),
            preferenceManager.getString(Constants.KEY_USER_ID) ?: return
        )
        binding.chatRecyclerView.adapter = chatAdapter
        database = FirebaseFirestore.getInstance()
    }

    private fun sendMessage() {
        // Create a HashMap to store the chat message details
        val messageSend = HashMap<String, Any>()

        // Set the sender's ID, receiver's ID, message text, and timestamp in the messageSend HashMap
        messageSend[Constants.KEY_SENDER_ID] = (preferenceManager.getString(Constants.KEY_USER_ID) ?: return)
        messageSend[Constants.KEY_RECEIVER_ID] = receiverUser.id
        messageSend[Constants.KEY_MESSAGE] = binding.inputMessage.text.toString()
        messageSend[Constants.KEY_TIMESTAMP] = Date()

        // Add the message to the Firestore collection
        database.collection(Constants.KEY_COLLECTION_CHAT).add(messageSend)

        // Check if a conversation already exists (conversionId is not null)
        if (conversionId != null) {
            // Update the conversation with the new message
            updateConversion(binding.inputMessage.text.toString())
        } else {
            // Create a new conversation in Firestore since it doesn't exist yet
            val conversion = HashMap<String, Any>()
            conversion[Constants.KEY_SENDER_ID] = (preferenceManager.getString(Constants.KEY_USER_ID) ?: return)
            conversion[Constants.KEY_SENDER_NAME] = (preferenceManager.getString(Constants.KEY_SENDER_NAME) ?: return)
            conversion[Constants.KEY_SENDER_IMAGE] = (preferenceManager.getString(Constants.KEY_SENDER_IMAGE) ?: return)
            conversion[Constants.KEY_RECEIVER_ID] = receiverUser.id
            conversion[Constants.KEY_RECEIVER_NAME] = receiverUser.name
            conversion[Constants.KEY_RECEIVER_IMAGE] = receiverUser.image
            conversion[Constants.KEY_LAST_MESSAGE] = binding.inputMessage.text.toString()
            conversion[Constants.KEY_TIMESTAMP] = Date()
            addConversion(conversion)

            // Add the new conversation to Firestore

        }

        // Clear the input message field
        binding.inputMessage.text.clear()
    }

    private fun listenMessages() {
        // Set up a snapshot listener for chat messages sent by the current user to the receiver user
        database.collection(Constants.KEY_COLLECTION_CHAT)
            .whereEqualTo(Constants.KEY_SENDER_ID, preferenceManager.getString(Constants.KEY_USER_ID))
            .whereEqualTo(Constants.KEY_RECEIVER_ID, receiverUser.id)
            .addSnapshotListener(eventListener)

        // Set up a snapshot listener for chat messages sent by the receiver user to the current user
        database.collection(Constants.KEY_COLLECTION_CHAT)
            .whereEqualTo(Constants.KEY_SENDER_ID, receiverUser.id)
            .whereEqualTo(Constants.KEY_RECEIVER_ID, preferenceManager.getString(Constants.KEY_USER_ID))
            .addSnapshotListener(eventListener)
    }


    @SuppressLint("NotifyDataSetChanged")
    private val eventListener = EventListener<QuerySnapshot> { value, error ->
        if (error != null) {
            // Handle the error
            return@EventListener
        }

        if (value != null) {
            val count = chatMessages.size
            for (documentChange:DocumentChange in value.documentChanges) {
                if (documentChange.type == DocumentChange.Type.ADDED) {
                    val chatMessage = ChatMessage("","","",""," ","","","").apply {
                        senderId = documentChange.document.getString(Constants.KEY_SENDER_ID).toString()
                        receiverId = documentChange.document.getString(Constants.KEY_RECEIVER_ID).toString()
                        message = documentChange.document.getString(Constants.KEY_MESSAGE).toString()
                        dateTime = getReadableDateTime(documentChange.document.getDate(Constants.KEY_TIMESTAMP) ?: return@EventListener)
                        dateObject = documentChange.document.getDate(Constants.KEY_TIMESTAMP).toString()!!
                    }
                    chatMessages.add(chatMessage)
                }

                chatMessages.sortWith(Comparator { obj1, obj2 -> obj1.dateObject.compareTo(obj2.dateObject) })
                if (count == 0) {
                    chatAdapter.notifyDataSetChanged()
                } else {
                    chatAdapter.notifyItemRangeInserted(chatMessages.size, chatMessages.size)
                    binding.chatRecyclerView.smoothScrollToPosition(chatMessages.size - 1)
                }

                binding.chatRecyclerView.visibility = View.VISIBLE
            }
            binding.progressBarChat.visibility = View.GONE
            if(conversionId==null){
                checkForConversion()
            }
        }
    }

    private fun getBitmapFromEncodedString(encodedImage: String): Bitmap {
        val decodedBytes = Base64.decode(encodedImage, Base64.DEFAULT)
        return BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
    }

    private fun loadReceiverDetails() {
        receiverUser = intent.getSerializableExtra(Constants.KEY_USER) as User
        binding.textNameChat.text = receiverUser.name
    }

    private fun setListeners() {
        binding.imageBackChat.setOnClickListener {
            onBackPressed()
        }
        binding.layoutSend.setOnClickListener {
            sendMessage()
        }
    }

    private fun getReadableDateTime(date: Date): String {
        return SimpleDateFormat("MMMM dd, yyyy - hh:mm a", Locale.getDefault()).format(date)
    }


    private fun addConversion(conversion: HashMap<String, Any>) {
        database.collection(Constants.KEY_COLLECTION_CONVERSATIONS)
            .add(conversion)
            .addOnSuccessListener {
                conversionId = it.id
            }
    }
    private fun updateConversion(message: String) {
        val documentReference:DocumentReference = database.collection(Constants.KEY_COLLECTION_CONVERSATIONS).document(conversionId?:return)
        documentReference.update(
            Constants.KEY_LAST_MESSAGE, message,
            Constants.KEY_TIMESTAMP, Date()
        )
    }



    private fun checkForConversion() {
        if (chatMessages.isNotEmpty()) {
            val senderUserId = preferenceManager.getString(Constants.KEY_USER_ID) ?:return
            val receiverUserId = receiverUser.id

            checkForConversionRemotely(senderUserId, receiverUserId)
            checkForConversionRemotely(receiverUserId, senderUserId)
        }
    }
    private fun checkForConversionRemotely(senderId:String,receiverId:String){
        database.collection(Constants.KEY_COLLECTION_CONVERSATIONS)
            .whereEqualTo(Constants.KEY_SENDER_ID,senderId)
            .whereEqualTo(Constants.KEY_RECEIVER_ID,receiverId)
            .get()
            .addOnCompleteListener(conversionOnCompleteListener)
    }

//


    private val conversionOnCompleteListener = OnCompleteListener<QuerySnapshot> { task ->
    if (task.isSuccessful && task.result != null && task.result!!.documents.size > 0) {
        val documentSnapshot:DocumentSnapshot= task.result.documents[0]
        conversionId=documentSnapshot.id

    }
}

}
