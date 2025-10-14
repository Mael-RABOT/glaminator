package com.example.glaminator.utils

import android.util.Patterns
import java.math.BigInteger
import java.security.MessageDigest

object ValidationUtils {

    fun toMD5(input: String): String {
        val md = MessageDigest.getInstance("MD5")
        return BigInteger(1, md.digest(input.toByteArray())).toString(16).padStart(32, '0')
    }

    fun validateRegistration(
        username: String,
        email: String,
        password: String,
        confirmPassword: String
    ): String? {
        if (username.isBlank()) {
            return "Username cannot be empty"
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            return "Invalid email address"
        }
        if (password.length < 6) {
            return "Password must be at least 6 characters long"
        }
        if (password != confirmPassword) {
            return "Passwords do not match"
        }
        return null
    }

    fun validateLogin(emailOrUsername: String, password: String): String? {
        if (emailOrUsername.isBlank()) {
            return "Email or username cannot be empty"
        }
        if (password.isBlank()) {
            return "Password cannot be empty"
        }
        return null
    }
}
