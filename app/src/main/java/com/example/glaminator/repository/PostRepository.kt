package com.example.glaminator.repository

import com.example.glaminator.model.Post
import com.example.glaminator.model.PostTags
import com.example.glaminator.services.FirebaseService
import com.google.android.gms.tasks.Task
import com.google.firebase.database.*

class PostRepository {
    private val firebaseService = FirebaseService()
    private val databaseReference: DatabaseReference =
        firebaseService.getDatabaseReference().child("posts")
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

    fun createPost(post: Post): Task<Void> {
        val newPostId = databaseReference.push().key
            ?: throw Exception("Failed to generate post ID")

        val data = hashMapOf<String, Any>(
            "id" to newPostId,
            "userId" to post.userId,
            "title" to post.title,
            "content" to post.content,
            "imageUrls" to post.imageUrls,
            "timestamp" to post.timestamp,
            "likes" to post.likes,
            "seenBy" to post.seenBy,
            "tags" to post.tags.map { it.name }
        )

        return databaseReference.child(newPostId).setValue(data)
    }

    fun getPost(id: String): DatabaseReference {
        return databaseReference.child(id)
    }

    fun getPosts(onPostsReceived: (List<Post>) -> Unit) {
        databaseReference.orderByChild("timestamp")
            .addValueEventListener(object : ValueEventListener {

                override fun onDataChange(snapshot: DataSnapshot) {
                    val posts = snapshot.children.mapNotNull { snap ->
                        val basePost = snap.getValue(Post::class.java) ?: return@mapNotNull null

                        val tagStrings =
                            snap.child("tags").children.mapNotNull { it.getValue(String::class.java) }

                        val enumTags = tagStrings.mapNotNull {
                            try { PostTags.valueOf(it) } catch (_: Exception) { null }
                        }

                        basePost.copy(tags = enumTags)
                    }

                    onPostsReceived(posts.reversed())
                }

                override fun onCancelled(error: DatabaseError) {}
            })
    }

    fun updatePost(id: String, post: Post): Task<Void> {
        val data = hashMapOf<String, Any>(
            "id" to post.id,
            "userId" to post.userId,
            "title" to post.title,
            "content" to post.content,
            "imageUrls" to post.imageUrls,
            "timestamp" to post.timestamp,
            "likes" to post.likes,
            "seenBy" to post.seenBy,
            "tags" to post.tags.map { it.name }
        )

        return databaseReference.child(id).setValue(data)
    }

    fun deletePost(id: String): Task<Void> {
        return databaseReference.child(id).removeValue()
    }

    fun markPostAsSeen(postId: String, userId: String) {
        getPost(postId)
            .addListenerForSingleValueEvent(object : ValueEventListener {

                override fun onDataChange(snapshot: DataSnapshot) {
                    val post = snapshot.getValue(Post::class.java) ?: return

                    if (!post.seenBy.contains(userId)) {
                        val newSeen = post.seenBy.toMutableList()
                        newSeen.add(userId)

                        updatePost(postId, post.copy(seenBy = newSeen))
                    }
                }

                override fun onCancelled(error: DatabaseError) {}
            })
    }

    fun searchPostsByTags(tags: List<PostTags>, onPostsReceived: (List<Post>) -> Unit) {
        databaseReference
            .addValueEventListener(object : ValueEventListener {

                override fun onDataChange(snapshot: DataSnapshot) {
                    val results = snapshot.children.mapNotNull { snap ->

                        val tagStrings =
                            snap.child("tags").children.mapNotNull { it.getValue(String::class.java) }

                        val postTags = tagStrings.mapNotNull {
                            try {
                                PostTags.valueOf(it.uppercase())
                            } catch (_: Exception) {
                                null
                            }
                        }

                        if (tags.isEmpty() || postTags.containsAll(tags)) {
                            val basePost = snap.getValue(Post::class.java) ?: return@mapNotNull null
                            basePost.copy(tags = postTags)
                        } else {
                            null
                        }
                    }

                    onPostsReceived(results)
                }

                override fun onCancelled(error: DatabaseError) {}
            })
    }

}
