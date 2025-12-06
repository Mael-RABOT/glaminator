package com.example.glaminator.data

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.example.glaminator.model.User

object CurrentUser {
    var user by mutableStateOf<User?>(null)
}
