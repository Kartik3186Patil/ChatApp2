package com.example.chatapp.adapters

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.chatapp.databinding.ItemContainerBinding
import com.example.chatapp.listeners.UserListener
import com.example.chatapp.models.User


class UserAdapter(private val userList: List<User>,private val userListener: UserListener) : RecyclerView.Adapter<UserAdapter.UserViewHolder>() {

    class UserViewHolder(private val binding: ItemContainerBinding,private val userListener: UserListener) : RecyclerView.ViewHolder(binding.root) {

        fun setUserData(user: User) {
            binding.textNameItem.text=user.name
            binding.textEmailItem.text = user.email
            binding.imageProfileItem.setImageBitmap(getUserImage(user.image))
            binding.root.setOnClickListener {
                userListener.onUserClicked(user)
            }
        }
        private fun getUserImage(encodedImage:String):Bitmap{
            val bytes: ByteArray = Base64.decode(encodedImage, Base64.DEFAULT)
            return BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
        }


    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val binding=ItemContainerBinding.inflate(LayoutInflater.from(parent.context),parent,false)
        return UserViewHolder(binding,userListener)
    }

    override fun getItemCount(): Int = userList.size


    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        val user = userList[position]
        holder.setUserData(user)
    }

}