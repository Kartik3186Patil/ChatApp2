package com.example.chatapp.models

data class ChatMessage(
    var senderId: String,
    var receiverId: String,
    var message: String,
    var dateTime: String,
    var dateObject: String,
    var conversionId:String,
    var conversionName:String,
    var conversionImage:String
)
