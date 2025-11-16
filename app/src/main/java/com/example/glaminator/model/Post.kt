package com.example.glaminator.model

data class Post(
    val id: String = "",
    val userId: String = "",
    val title: String = "",
    val content: String = "",
    val imageUrls: List<String> = emptyList(),
    val timestamp: Long = System.currentTimeMillis(),
    val likes: List<String> = emptyList(),
    val seenBy: List<String> = emptyList(),
    val tags: List<String> = emptyList()
)
