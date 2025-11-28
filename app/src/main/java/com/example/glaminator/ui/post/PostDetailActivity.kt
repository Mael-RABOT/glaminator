package com.example.glaminator.ui.post

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Divider
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.glaminator.data.CurrentUser
import com.example.glaminator.model.Comment
import com.example.glaminator.model.Post
import com.example.glaminator.model.RewardType
import com.example.glaminator.model.User
import com.example.glaminator.repository.CommentRepository
import com.example.glaminator.repository.PostRepository
import com.example.glaminator.repository.RewardRepository
import com.example.glaminator.repository.UserRepository
import com.example.glaminator.ui.common.PostStats
import com.example.glaminator.ui.theme.GlaminatorTheme
import com.example.glaminator.ui.theme.ScaffoldBackground
import com.example.glaminator.ui.theme.titles
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
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
                val rewardRepository = RewardRepository()
                val context = LocalContext.current

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
                ) { padding ->
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(padding)
                            .padding(16.dp)
                    ) {
                        post?.let { p ->
                            Column(
                                modifier = Modifier.wrapContentHeight()
                            ) {
                                Text(text = p.title, style = MaterialTheme.typography.headlineMedium)
                                Spacer(modifier = Modifier.height(8.dp))

                                Text(text = p.content, style = MaterialTheme.typography.bodyLarge)
                                Spacer(modifier = Modifier.height(16.dp))

                                PostStats(
                                    like = p.likes.size,
                                    comments = comments.size,
                                    isLiked = CurrentUser.user?.id in p.likes,
                                    onLikeClicked = {
                                        val userId = CurrentUser.user?.id ?: return@PostStats
                                        if (userId in post!!.likes) {
                                            postRepository.removeLikeFromPost(post!!.id, userId)
                                            rewardRepository.claimReward(
                                                context,
                                                com.example.glaminator.model.Reward(
                                                    type = RewardType.LIKE,
                                                    quantity = 1,
                                                    rarity = com.example.glaminator.model.Rarity.COMMON
                                                )
                                            )
                                        } else {
                                            if (rewardRepository.consumeReward(context, RewardType.LIKE, 1)) {
                                                postRepository.addLikeToPost(post!!.id, userId)
                                            } else {
                                                Toast.makeText(context, "You don't have enough rewards to like.", Toast.LENGTH_SHORT).show()
                                            }
                                        }
                                    }
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        CommentSection(
                            comments = comments,
                            onPostComment = { commentText ->
                                if (rewardRepository.consumeReward(context, RewardType.COMMENT, 1)) {
                                    val userId = CurrentUser.user?.id ?: "Anonymous"
                                    val newComment = Comment(postId = postId, userId = userId, content = commentText)
                                    commentRepository.createComment(newComment)
                                } else {
                                    Toast.makeText(context, "You don't have enough rewards to comment.", Toast.LENGTH_SHORT).show()
                                }
                            },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun LikeButton(
    isLiked: Boolean,
    onClick: () -> Unit
) {
    val icon = if (isLiked) Icons.Default.Favorite else Icons.Default.FavoriteBorder
    val tint by animateColorAsState(
        targetValue = if (isLiked) Color.Red else MaterialTheme.colorScheme.onSurface,
        animationSpec = tween(durationMillis = 300)
    )

    IconButton(onClick = onClick) {
        Icon(
            imageVector = icon,
            contentDescription = if (isLiked) "Unlike" else "Like",
            tint = tint
        )
    }
}

@Composable
fun CommentSection(
    comments: List<Comment>,
    onPostComment: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var newComment by remember { mutableStateOf("") }

    Column(modifier = Modifier.fillMaxWidth().fillMaxHeight()) {
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            items(comments) { comment ->
                CommentItem(comment = comment)
            }
        }
        Spacer(modifier = Modifier.height(8.dp))

        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            TextField(
                value = newComment,
                onValueChange = { newComment = it },
                shape = RoundedCornerShape(12.dp),
                label = { Text("Add a comment") },
                modifier = Modifier.weight(1f)
            )

            Spacer(modifier = Modifier.width(8.dp))

            Button(
                onClick = {
                    onPostComment(newComment)
                    newComment = ""
                },
                enabled = newComment.isNotBlank()
            ) {
                Text("Post")
            }
        }
    }
}

@Composable
fun CommentItem(comment: Comment) {
    var user by remember(comment.userId) { mutableStateOf<User?>(null) }
    val userRepository = remember { UserRepository() }

    LaunchedEffect(comment.userId) {
        val userRef = userRepository.getUser(comment.userId)
        val userListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                user = snapshot.getValue(User::class.java)
            }

            override fun onCancelled(error: DatabaseError) {}
        }

        userRef.addListenerForSingleValueEvent(userListener)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.onPrimary)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = user?.username ?: "...",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(4.dp))
            Divider(
                thickness = 1.dp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f)
            )
            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = comment.content,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}
