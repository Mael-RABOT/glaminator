package com.example.glaminator.ui.home

import androidx.lifecycle.ViewModel
import com.example.glaminator.model.Post
import com.example.glaminator.repository.PostRepository
import com.google.firebase.auth.FirebaseAuth

class HomeViewModel : ViewModel() {

    private val postRepository = PostRepository()
    private val currentUserId = FirebaseAuth.getInstance().currentUser?.uid

    fun getUnseenPosts(onPostsReceived: (List<Post>) -> Unit) {
        postRepository.getPosts { allPosts ->
            val unseenPosts = allPosts.filter { !it.seenBy.contains(currentUserId) }
            onPostsReceived(unseenPosts)
        }
    }

    fun markPostAsSeen(post: Post) {
        if (currentUserId != null) {
            postRepository.markPostAsSeen(post.id, currentUserId)
        }
    }
}
