package com.example.glaminator.repository

import com.example.glaminator.data.CurrentUser
import com.example.glaminator.model.Rarity
import com.example.glaminator.model.Reward
import com.example.glaminator.model.RewardType
import com.example.glaminator.model.UserReward

class RewardRepository {

    private val userRepository = UserRepository()

    fun generateReward(): Reward {
        val rarity = when ((1..100).random()) {
            in 1..5 -> Rarity.LEGENDARY
            in 6..15 -> Rarity.EPIC
            in 16..40 -> Rarity.RARE
            else -> Rarity.COMMON
        }

        val rewardType = RewardType.values().random()

        val quantity = when (rarity) {
            Rarity.COMMON -> (1..5).random()
            Rarity.RARE -> (6..10).random()
            Rarity.EPIC -> (11..15).random()
            Rarity.LEGENDARY -> (16..20).random()
        }

        return Reward(rewardType, quantity, rarity)
    }

    fun claimReward(reward: Reward) {
        CurrentUser.user?.let { user ->
            val existingReward = user.rewards.find { it.type == reward.type }
            val updatedRewards = if (existingReward != null) {
                user.rewards.map {
                    if (it.type == reward.type) {
                        it.copy(quantity = it.quantity + reward.quantity)
                    } else {
                        it
                    }
                }
            } else {
                user.rewards + UserReward(reward.type, reward.quantity)
            }
            val updatedUser = user.copy(rewards = updatedRewards)
            userRepository.updateUser(user.id, updatedUser)
            CurrentUser.user = updatedUser // Update current user in memory
        }
    }

    fun consumeReward(rewardType: RewardType, quantity: Int): Boolean {
        val user = CurrentUser.user ?: return false

        val userReward = user.rewards.find { it.type == rewardType }

        if (userReward != null && userReward.quantity >= quantity) {
            val updatedRewards = user.rewards.map {
                if (it.type == rewardType) {
                    it.copy(quantity = it.quantity - quantity)
                } else {
                    it
                }
            }
            val updatedUser = user.copy(rewards = updatedRewards)
            userRepository.updateUser(user.id, updatedUser)
            CurrentUser.user = updatedUser // Update current user in memory
            return true
        }
        return false
    }
}