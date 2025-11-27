package com.example.glaminator.ui.home

import androidx.lifecycle.ViewModel
import com.example.glaminator.model.Post
import com.example.glaminator.model.PostTags
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
        postRepository.getPosts { posts ->
            val unseen = posts.filter { !it.seenBy.contains(currentUserId) }
            onPostsReceived(unseen)
        }
    }

    fun markPostAsSeen(post: Post) {
        currentUserId?.let {
            postRepository.markPostAsSeen(post.id, it)
        }
    }

    fun searchByTag(tag: PostTags?) {
        if (tag == null) {
            _searchResults.value = emptyList()
            return
        }

        postRepository.searchPostsByTag(tag) { results ->
            _searchResults.value = results
        }
    }

}

