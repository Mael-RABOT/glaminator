package com.example.glaminator.repository

import com.example.glaminator.model.User
import com.example.glaminator.services.FirebaseService
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.TaskCompletionSource
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.Query

class UserRepository {

    private val firebaseService = FirebaseService()
    private val databaseReference: DatabaseReference = firebaseService.getDatabaseReference().child("users")

    fun createUser(user: User): Task<User> {
        val taskCompletionSource = TaskCompletionSource<User>()
        val newUserId = databaseReference.push().key
        if (newUserId == null) {
            taskCompletionSource.setException(Exception("Couldn't get push key for users"))
            return taskCompletionSource.task
        }
        val userWithId = user.copy(id = newUserId)
        databaseReference.child(newUserId).setValue(userWithId)
            .addOnSuccessListener {
                taskCompletionSource.setResult(userWithId)
            }
            .addOnFailureListener {
                taskCompletionSource.setException(it)
            }
        return taskCompletionSource.task
    }

    fun findUserBy(field: String, value: String): Query {
        return databaseReference.orderByChild(field).equalTo(value)
    }

    fun getUser(id: String): DatabaseReference {
        return databaseReference.child(id)
    }

    fun updateUser(id: String, user: User): Task<Void> {
        return databaseReference.child(id).setValue(user)
    }

    fun deleteUser(id: String): Task<Void> {
        return databaseReference.child(id).removeValue()
    }
}
