package com.example.glaminator.ui.mypage

import androidx.lifecycle.ViewModel
import com.example.glaminator.data.CurrentUser
import com.example.glaminator.model.Post
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class MyPageViewModel : ViewModel() {
    private val postsRef = FirebaseDatabase.getInstance().getReference("posts")
    private val _myPosts = MutableStateFlow<List<Post>>(emptyList())
    val myPosts: StateFlow<List<Post>> = _myPosts

    fun fetchMyPosts() {
        val currentUserId = CurrentUser.user?.id
        if (currentUserId == null) {
            _myPosts.value = emptyList()
            return
        }

        val query = postsRef.orderByChild("userId").equalTo(currentUserId)

        query.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val posts = snapshot.children.mapNotNull { it.getValue(Post::class.java) }
                _myPosts.value = posts.sortedByDescending { it.timestamp }
            }

            override fun onCancelled(error: DatabaseError) {
                println("Firebase Database Error in MyPageViewModel: ${error.message}")
            }
        })
    }

    fun toggleLike(postId: String, currentLikes: List<String>) {
        val userId = CurrentUser.user?.id ?: return

        val newLikes = if (currentLikes.contains(userId)) {
            currentLikes - userId
        } else {
            currentLikes + userId
        }

        postsRef.child(postId).child("likes").setValue(newLikes)
    }
}