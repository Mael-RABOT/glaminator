package com.example.glaminator.model

data class User(
    val id: String = "",
    val username: String = "",
    val email: String = "",
    val password: String = "",
    val rewards: List<UserReward> = emptyList()
)

data class UserReward(
    val type: RewardType = RewardType.POST,
    val quantity: Int = 0
)
