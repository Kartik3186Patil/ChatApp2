package com.example.chatapp.adapters

import android.graphics.Bitmap
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.chatapp.databinding.ItemContainerSentMessageBinding
import com.example.chatapp.models.ChatMessage
import com.example.chatapp.databinding.ItemContainerReceivedMessageBinding


class ChatAdapter(private val chatMessages:List<ChatMessage>,private val receiverProfileImage: Bitmap,private val senderId:String) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    // View types for sent and received messages
    private val VIEW_TYPE_SENT = 1
    private val VIEW_TYPE_RECEIVED = 2
    class SentMessageViewHolder(private val binding: ItemContainerSentMessageBinding) : RecyclerView.ViewHolder(binding.root){
        fun setDataSent(chatMessage: ChatMessage){
            binding.textMessageSent.text = chatMessage.message
            binding.textDateTime.text = chatMessage.dateTime
        }
    }
    class ReceivedMessageViewHolder(private val binding: ItemContainerReceivedMessageBinding) : RecyclerView.ViewHolder(binding.root) {
            fun setDataReceive(chatMessage: ChatMessage, receiverProfileImage: Bitmap){
                binding.receivedTextMessage.text=chatMessage.message
                binding.textDateTimeReceived.text=chatMessage.dateTime
                binding.imageProfileReceived.setImageBitmap(receiverProfileImage)
            }
    }

    // Implement onCreateViewHolder
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            VIEW_TYPE_SENT -> {
                val binding = ItemContainerSentMessageBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                SentMessageViewHolder(binding)
            }
            VIEW_TYPE_RECEIVED -> {
                val binding = ItemContainerReceivedMessageBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                ReceivedMessageViewHolder(binding)
            }
            else -> throw IllegalArgumentException("Invalid view type: $viewType")
        }
    }

    override fun getItemCount(): Int {
        return chatMessages.size
    }
    // Implement getItemViewType to determine the view type for each item
    override fun getItemViewType(position: Int): Int {
        val chatMessage = chatMessages[position]
        return if (chatMessage.senderId == senderId) {
            VIEW_TYPE_SENT
        } else {
            VIEW_TYPE_RECEIVED
        }
    }


    // Implement onBindViewHolder to bind data to the views based on view type
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val chatMessage = chatMessages[position]
        when (holder) {
            is SentMessageViewHolder -> {
                holder.setDataSent(chatMessage)
            }

            is ReceivedMessageViewHolder -> {
                holder.setDataReceive(chatMessage, receiverProfileImage)
            }
        }

    }
}
