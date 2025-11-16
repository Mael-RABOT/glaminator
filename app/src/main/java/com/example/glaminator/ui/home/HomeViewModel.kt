package com.example.glaminator.ui.home

import androidx.lifecycle.ViewModel
import com.example.glaminator.model.Post
import com.example.glaminator.repository.PostRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class HomeViewModel : ViewModel() {

    private val postRepository = PostRepository()
    private val currentUserId = FirebaseAuth.getInstance().currentUser?.uid

    private val _searchResults = MutableStateFlow<List<Post>>(emptyList())
    val searchResults: StateFlow<List<Post>> = _searchResults

    fun getUnseenPosts(onPostsReceived: (List<Post>) -> Unit) {
        postRepository.getPosts { allPosts ->
            val unseen = if (currentUserId != null) {
                allPosts.filter { !it.seenBy.contains(currentUserId) }
            } else {
                allPosts
            }
            onPostsReceived(unseen)
        }
    }

    fun markPostAsSeen(post: Post) {
        val uid = currentUserId ?: return
        postRepository.markPostAsSeen(post.id, uid)
    }

    fun searchByTag(tag: String) {
        val trimmed = tag.trim()
        if (trimmed.isEmpty()) {
            _searchResults.value = emptyList()
            return
        }

        postRepository.searchPostsByTag(trimmed) { posts ->
            _searchResults.value = posts
        }
    }
}

