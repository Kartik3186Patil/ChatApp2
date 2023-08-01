package com.example.chatapp.adapters

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.chatapp.databinding.ItemContainerRecentConversationBinding
import com.example.chatapp.models.ChatMessage

class RecentConversionAdapter(private val chatMessages: List<ChatMessage>) : RecyclerView.Adapter<RecentConversionAdapter.ConversionViewHolder>() {

    class ConversionViewHolder(private val binding: ItemContainerRecentConversationBinding) : RecyclerView.ViewHolder(binding.root) {
        fun setData(chatMessage: ChatMessage){
            binding.imageProfileItem.setImageBitmap(getConversionImage(chatMessage.conversionImage))
            binding.textNameItem.text = chatMessage.conversionId
            binding.textRecentMessage.text = chatMessage.message
        }

        private fun getConversionImage(encodedImage:String): Bitmap {
            val bytes: ByteArray = Base64.decode(encodedImage, Base64.DEFAULT)
            return BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ConversionViewHolder {
        val binding = ItemContainerRecentConversationBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ConversionViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return chatMessages.size
    }

    override fun onBindViewHolder(holder: ConversionViewHolder, position: Int) {
        val chatMessage = chatMessages[position]
        holder.setData(chatMessage)
    }

}
