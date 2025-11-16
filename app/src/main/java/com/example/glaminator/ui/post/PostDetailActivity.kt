package com.example.glaminator.ui.post

import android.content.Context
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.Button
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.glaminator.data.CurrentUser
import com.example.glaminator.model.Comment
import com.example.glaminator.model.Post
import com.example.glaminator.model.RewardType
import com.example.glaminator.model.User
import com.example.glaminator.model.UserReward
import com.example.glaminator.repository.CommentRepository
import com.example.glaminator.repository.PostRepository
import com.example.glaminator.repository.RewardRepository
import com.example.glaminator.repository.UserRepository
import com.example.glaminator.ui.theme.GlaminatorTheme
import com.example.glaminator.ui.theme.ScaffoldBackground
import com.example.glaminator.ui.theme.titles
import com.example.glaminator.ui.theme.heart
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.Query
import com.google.firebase.database.ValueEventListener


class PostDetailActivity : ComponentActivity() {
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val postId = intent.getStringExtra("POST_ID") ?: return

        setContent {
            GlaminatorTheme {
                var post by remember { mutableStateOf<Post?>(null) }
                var comments by remember { mutableStateOf<List<Comment>>(emptyList()) }
                val postRepository = PostRepository()
                val commentRepository = CommentRepository()

                postRepository.getPost(postId).addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        post = snapshot.getValue(Post::class.java)
                    }

                    override fun onCancelled(error: DatabaseError) { /* Handle error */ }
                })

                commentRepository.getCommentsForPost(postId).addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        comments = snapshot.children.mapNotNull { it.getValue(Comment::class.java) }
                    }

                    override fun onCancelled(error: DatabaseError) { /* Handle error */ }
                })

                Scaffold(
                    topBar = {
                        TopAppBar(
                            title = { Text(post?.title ?: "Post Details", color = titles) },
                            navigationIcon = {
                                IconButton(onClick = { finish() }) {
                                    Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = titles)
                                }
                            },
                            colors = TopAppBarDefaults.topAppBarColors(
                                containerColor = ScaffoldBackground
                            )
                        )
                    }
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(it)
                            .padding(16.dp)
                    ) {
                        post?.let {
                            Text(text = it.title, style = MaterialTheme.typography.headlineMedium)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(text = it.content, style = MaterialTheme.typography.bodyLarge)
                            Spacer(modifier = Modifier.height(16.dp))
                            LikeButton(post = it, postRepository = postRepository)
                            Spacer(modifier = Modifier.height(16.dp))
                            CommentSection(postId = postId, comments = comments, commentRepository = commentRepository)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun LikeButton(post: Post, postRepository: PostRepository) {
    val isLiked = CurrentUser.user?.id in post.likes
    val context = LocalContext.current
    val rewardRepository = remember { RewardRepository() }

    IconButton(onClick = {
        val userId = CurrentUser.user?.id ?: ""
        if (isLiked) {
            postRepository.removeLikeFromPost(post.id, userId)
        } else {
            if (rewardRepository.consumeReward(context, RewardType.LIKE, 1)) {
                postRepository.addLikeToPost(post.id, userId)
            } else {
                Toast.makeText(context, "You don\'t have enough likes. Try a pull!", Toast.LENGTH_SHORT).show()
            }
        }
    }) {
        Icon(
            imageVector = if (isLiked) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
            contentDescription = "Like",
            tint = if (isLiked) MaterialTheme.colorScheme.heart else MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
fun CommentSection(postId: String, comments: List<Comment>, commentRepository: CommentRepository) {
    var newComment by remember { mutableStateOf("") }
    val context = LocalContext.current
    val rewardRepository = remember { RewardRepository() }

    Column(modifier = Modifier.fillMaxWidth()) {
        Text("Comments", style = MaterialTheme.typography.headlineSmall)
        Spacer(modifier = Modifier.height(8.dp))
        LazyColumn(modifier = Modifier.weight(1f)) {
            items(comments) { comment ->
                CommentItem(comment = comment)
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            TextField(
                value = newComment,
                onValueChange = { newComment = it },
                label = { Text("Add a comment") },
                modifier = Modifier.weight(1f)
            )
            Button(onClick = {
                val userId = CurrentUser.user?.id ?: "Anonymous"

                if (rewardRepository.consumeReward(context, RewardType.COMMENT, 1)) {
                    val comment = Comment(
                        postId = postId,
                        userId = userId,
                        content = newComment
                    )
                    commentRepository.createComment(comment)
                    newComment = ""
                } else {
                    Toast.makeText(context, "You don\'t have enough comment rewards. Try a pull!", Toast.LENGTH_SHORT).show()
                }
            }) {
                Text("Post", fontSize = 18.sp)
            }
        }
    }
}

@Composable
fun CommentItem(comment: Comment) {
    val userRepository = UserRepository()
    var user by remember { mutableStateOf<User?>(null) }

    userRepository.getUser(comment.userId).addValueEventListener(object : ValueEventListener {
        override fun onDataChange(snapshot: DataSnapshot) {
            user = snapshot.getValue(User::class.java)
        }

        override fun onCancelled(error: DatabaseError) {
            // Handle error
        }
    })

    Column(modifier = Modifier.padding(vertical = 4.dp)) {
        Text(
            text = user?.username ?: "Loading...",
            style = MaterialTheme.typography.bodyMedium
        )
        Text(
            text = comment.content,
            style = MaterialTheme.typography.bodyLarge
        )
    }
}
