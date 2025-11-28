package com.example.glaminator.model

enum class PostTags {
    TIPS,
    DISCUSSION,
    FOOD,
    PLACES,
    EVENTS,
    OFFERS,
    GENERAL,
    FASHION,
    TECHNOLOGY,
    COURSE,
    HEALTH,
    TRAVEL,
    ENTERTAINMENT
}

data class Post(
    val id: String = "",
    val userId: String = "",
    val title: String = "",
    val content: String = "",
    val imageUrls: List<String> = emptyList(),
    val timestamp: Long = System.currentTimeMillis(),
    val likes: List<String> = emptyList(),
    val seenBy: List<String> = emptyList(),
    val tags: List<PostTags> = emptyList()
)
