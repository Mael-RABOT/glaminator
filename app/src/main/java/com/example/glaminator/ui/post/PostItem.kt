package com.example.glaminator.ui.post

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Divider
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.glaminator.model.Post
import com.example.glaminator.model.User
import com.example.glaminator.repository.CommentRepository
import com.example.glaminator.repository.UserRepository
import com.example.glaminator.ui.common.PostStats
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener

@Composable
fun PostItem (
    post: Post,
    onPostClick: (Post) -> Unit,
    isLiked: Boolean,
    onLikeClicked: () -> Unit
) {
    var user by remember { mutableStateOf<User?>(null) }
    var commentCount by remember { mutableStateOf(0) }

    LaunchedEffect(post.userId) {
        val commentsQuery = CommentRepository().getCommentsForPost(post.id)
        commentsQuery.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                commentCount = snapshot.childrenCount.toInt()
            }
            override fun onCancelled(error: DatabaseError) { /* Handle error */ }
        })

        val userRef = UserRepository().getUser(post.userId)
        userRef.addListenerForSingleValueEvent(object :
            ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                user = snapshot.getValue(User::class.java)
            }

            override fun onCancelled(error: DatabaseError) { /* Handle error */
            }
        })
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp, horizontal = 16.dp)
            .clickable { onPostClick(post) },
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            PostHeader(user = user)
            Spacer(modifier = Modifier.height(12.dp))
            Divider(
                thickness = 1.dp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f)
            )
            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = post.title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = post.content,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 4,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(12.dp))
            Divider(
                thickness = 1.dp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f)
            )
            Spacer(modifier = Modifier.height(4.dp))

            PostStats(
                like = post.likes.size,
                comments = commentCount,
                isLiked = isLiked,
                onLikeClicked = onLikeClicked
            )
        }
    }
}

@Composable
private fun PostHeader(user: User?) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
            text = user?.username ?: "Loading...",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )
    }
}