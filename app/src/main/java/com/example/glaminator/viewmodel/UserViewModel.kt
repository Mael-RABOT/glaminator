package com.example.glaminator.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.glaminator.model.User
import com.example.glaminator.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import android.util.Patterns

class UserViewModel : ViewModel() {

    private val userRepository = UserRepository()

    private val _user = MutableStateFlow<User?>(null)
    val user: StateFlow<User?> = _user

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    fun login(emailOrUsername: String, password: String) {
        val field = if (Patterns.EMAIL_ADDRESS.matcher(emailOrUsername).matches()) "email" else "username"

        userRepository.findUserBy(field, emailOrUsername).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    for (userSnapshot in snapshot.children) {
                        val user = userSnapshot.getValue(User::class.java)
                        if (user?.password == password) {
                            _user.value = user
                            return
                        }
                    }
                    _error.value = "Invalid password"
                } else {
                    _error.value = "User not found"
                }
            }

            override fun onCancelled(error: DatabaseError) {
                _error.value = error.message
            }
        })
    }

    fun register(user: User) {
        viewModelScope.launch {
            userRepository.createUser(user).addOnSuccessListener {
                _user.value = it
            }.addOnFailureListener {
                _error.value = it.message
            }
        }
    }

    fun loadUserById(userId: String) {
        userRepository.getUser(userId).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val user = snapshot.getValue(User::class.java)
                _user.value = user
            }

            override fun onCancelled(error: DatabaseError) {
                _error.value = error.message
            }
        })
    }
}
