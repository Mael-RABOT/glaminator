package com.example.glaminator.ui.common

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Comment
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.glaminator.ui.post.LikeButton

@Composable
fun PostStats(
    like: Int,
    comments: Int,
    isLiked: Boolean = false,
    onLikeClicked: () -> Unit = {}
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Like button and count
        LikeButton(
            isLiked = isLiked,
            onClick = onLikeClicked
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = like.toString(),
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.width(16.dp))

        // Comment icon and count
        Icon(
            imageVector = Icons.Filled.Comment,
            contentDescription = "Comments",
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = comments.toString(),
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold
        )
    }
}
