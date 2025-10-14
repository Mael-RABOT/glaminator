package com.example.glaminator.repository

import com.example.glaminator.model.Comment
import com.example.glaminator.services.FirebaseService
import com.google.android.gms.tasks.Task
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener

class CommentRepository {

    private val firebaseService = FirebaseService()
    private val databaseReference: DatabaseReference = firebaseService.getDatabaseReference().child("comments")

    fun createComment(comment: Comment): Task<Void> {
        val newCommentId = databaseReference.push().key
        val newComment = comment.copy(id = newCommentId!!)
        return databaseReference.child(newCommentId).setValue(newComment)
    }

    fun getCommentsForPost(postId: String): DatabaseReference {
        return databaseReference.orderByChild("postId").equalTo(postId).ref
    }

    fun getComment(id: String): DatabaseReference {
        return databaseReference.child(id)
    }

    fun updateComment(id: String, comment: Comment): Task<Void> {
        return databaseReference.child(id).setValue(comment)
    }

    fun deleteComment(id: String): Task<Void> {
        return databaseReference.child(id).removeValue()
    }

    fun addLikeToComment(commentId: String, userId: String) {
        getComment(commentId).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val comment = snapshot.getValue(Comment::class.java)
                if (comment != null) {
                    val newLikes = comment.likes.toMutableList()
                    if (!newLikes.contains(userId)) {
                        newLikes.add(userId)
                        updateComment(commentId, comment.copy(likes = newLikes))
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }

    fun removeLikeFromComment(commentId: String, userId: String) {
        getComment(commentId).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val comment = snapshot.getValue(Comment::class.java)
                if (comment != null) {
                    val newLikes = comment.likes.toMutableList()
                    if (newLikes.contains(userId)) {
                        newLikes.remove(userId)
                        updateComment(commentId, comment.copy(likes = newLikes))
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }
}
