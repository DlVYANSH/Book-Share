package com.example.bookstore

class LatestMessage(
    val textMessage: String = "",
    val receiver: User = User(),
    val sender: User = User(),
    val createdAt: String = ""
)