package com.example.glaminator.model

enum class Rarity {
    COMMON, RARE, EPIC, LEGENDARY
}

enum class RewardType {
    POST, COMMENT, LIKE
}

data class Reward(
    val type: RewardType,
    val quantity: Int,
    val rarity: Rarity
)
