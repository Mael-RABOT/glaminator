package com.example.glaminator.model

data class Post(
    val id: String = "",
    val userId: String = "",
    val title: String = "",
    val content: String = "",
    val imageUrls: List<String> = emptyList(),
    val timestamp: Long = System.currentTimeMillis(),
    val likes: List<String> = emptyList(), // List of user IDs
    val seenBy: List<String> = emptyList() // List of user IDs
)
