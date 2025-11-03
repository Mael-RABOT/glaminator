package com.example.glaminator.repository

import com.example.glaminator.model.User
import com.example.glaminator.services.FirebaseService
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.TaskCompletionSource
import com.google.android.gms.tasks.Tasks
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.Query
import com.google.firebase.database.ValueEventListener

class UserRepository {

    private val firebaseService = FirebaseService()
    private val databaseReference: DatabaseReference = firebaseService.getDatabaseReference().child("users")
    private val postsReference: DatabaseReference = firebaseService.getDatabaseReference().child("posts")
    private val commentsReference: DatabaseReference = firebaseService.getDatabaseReference().child("comments")

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

    fun deleteAllUserData(id: String): Task<Void> {
        val taskCompletionSource = TaskCompletionSource<Void>()

        val userPostsQuery = postsReference.orderByChild("userId").equalTo(id)
        userPostsQuery.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val deletePostTasks = mutableListOf<Task<Void>>()
                for (postSnapshot in snapshot.children) {
                    deletePostTasks.add(postSnapshot.ref.removeValue())
                }

                Tasks.whenAll(deletePostTasks).addOnCompleteListener {
                    val userCommentsQuery = commentsReference.orderByChild("userId").equalTo(id)
                    userCommentsQuery.addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(commentSnapshot: DataSnapshot) {
                            val deleteCommentTasks = mutableListOf<Task<Void>>()
                            for (comment in commentSnapshot.children) {
                                deleteCommentTasks.add(comment.ref.removeValue())
                            }

                            Tasks.whenAll(deleteCommentTasks).addOnCompleteListener { 
                                deleteUser(id).addOnCompleteListener { userDeleteTask ->
                                    if (userDeleteTask.isSuccessful) {
                                        taskCompletionSource.setResult(null)
                                    } else {
                                        taskCompletionSource.setException(userDeleteTask.exception!!)
                                    }
                                }
                            }
                        }

                        override fun onCancelled(error: DatabaseError) {
                            taskCompletionSource.setException(error.toException())
                        }
                    })
                }
            }

            override fun onCancelled(error: DatabaseError) {
                taskCompletionSource.setException(error.toException())
            }
        })

        return taskCompletionSource.task
    }
}