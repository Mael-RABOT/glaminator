package com.example.glaminator.model

data class Comment(
    val id: String = "",
    val postId: String = "",
    val userId: String = "",
    val content: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val likes: List<String> = emptyList() // List of user IDs
)
