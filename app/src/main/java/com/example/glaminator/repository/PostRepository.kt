package com.example.glaminator.repository

import com.example.glaminator.model.Post
import com.example.glaminator.services.FirebaseService
import com.google.android.gms.tasks.Task
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener

class PostRepository {

    private val firebaseService = FirebaseService()
    private val databaseReference: DatabaseReference = firebaseService.getDatabaseReference().child("posts")

    fun createPost(post: Post): Task<Void> {
        val newPostId = databaseReference.push().key
        val newPost = post.copy(id = newPostId!!)
        return databaseReference.child(newPostId).setValue(newPost)
    }

    fun getPost(id: String): DatabaseReference {
        return databaseReference.child(id)
    }

    fun getPosts(onPostsReceived: (List<Post>) -> Unit) {
        databaseReference.orderByChild("timestamp").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val posts = snapshot.children.mapNotNull { it.getValue(Post::class.java) }
                onPostsReceived(posts.reversed())
            }

            override fun onCancelled(error: DatabaseError) {
            }
        })
    }

    fun updatePost(id: String, post: Post): Task<Void> {
        return databaseReference.child(id).setValue(post)
    }

    fun deletePost(id: String): Task<Void> {
        return databaseReference.child(id).removeValue()
    }

    fun addLikeToPost(postId: String, userId: String) {
        getPost(postId).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val post = snapshot.getValue(Post::class.java)
                if (post != null) {
                    val newLikes = post.likes.toMutableList()
                    if (!newLikes.contains(userId)) {
                        newLikes.add(userId)
                        updatePost(postId, post.copy(likes = newLikes))
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }

    fun removeLikeFromPost(postId: String, userId: String) {
        getPost(postId).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val post = snapshot.getValue(Post::class.java)
                if (post != null) {
                    val newLikes = post.likes.toMutableList()
                    if (newLikes.contains(userId)) {
                        newLikes.remove(userId)
                        updatePost(postId, post.copy(likes = newLikes))
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }

    fun markPostAsSeen(postId: String, userId: String) {
        getPost(postId).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val post = snapshot.getValue(Post::class.java)
                if (post != null) {
                    if (!post.seenBy.contains(userId)) {
                        val newSeenBy = post.seenBy.toMutableList()
                        newSeenBy.add(userId)
                        updatePost(postId, post.copy(seenBy = newSeenBy))
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }
}
