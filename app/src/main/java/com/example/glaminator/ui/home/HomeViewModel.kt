package com.example.glaminator.ui.home

import androidx.lifecycle.ViewModel
import com.example.glaminator.model.Post
import com.example.glaminator.repository.PostRepository
import com.google.firebase.auth.FirebaseAuth

class HomeViewModel : ViewModel() {

    private val postRepository = PostRepository()
    private val currentUserId = FirebaseAuth.getInstance().currentUser?.uid

    fun createFixtures() {
        val dummyPosts = listOf(
            Post(userId = "devUser1", content = "This is the first post for testing!", imageUrls = listOf("https://picsum.photos/seed/picsum/200/300"), timestamp = System.currentTimeMillis() - 10000),
            Post(userId = "devUser2", content = "A second beautiful post.", imageUrls = listOf("https://picsum.photos/seed/picsum/200/300"), timestamp = System.currentTimeMillis() - 20000),
            Post(userId = "devUser1", content = "And a third one, with a longer description to see how it renders on the screen. This should be a nice test for text wrapping.", imageUrls = emptyList(), timestamp = System.currentTimeMillis() - 30000),
            Post(userId = "devUser3", content = "Post with multiple images.", imageUrls = listOf("https://picsum.photos/id/237/200/300", "https://picsum.photos/id/238/200/300"), timestamp = System.currentTimeMillis() - 40000),
            Post(userId = "devUser2", content = "Last post for now.", timestamp = System.currentTimeMillis() - 50000)
        )

        dummyPosts.forEach { post ->
            postRepository.createPost(post)
        }
    }

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
