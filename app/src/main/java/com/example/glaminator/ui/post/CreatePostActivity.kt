package com.example.glaminator.ui.post

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.glaminator.data.CurrentUser
import com.example.glaminator.model.Post
import com.example.glaminator.model.PostTags
import com.example.glaminator.repository.PostRepository
import com.example.glaminator.ui.theme.GlaminatorTheme
import com.example.glaminator.ui.theme.Primary
import androidx.compose.foundation.layout.FlowRow

class CreatePostActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            GlaminatorTheme {
                CreatePostScreen { finish() }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreatePostScreen(onPostCreated: () -> Unit) {
    var title by remember { mutableStateOf("") }
    var content by remember { mutableStateOf("") }
    var selectedTags by remember { mutableStateOf<List<PostTags>>(emptyList()) }

    val context = LocalContext.current
    val postRepository = PostRepository()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Create Post", color = Primary) }
            )
        }
    ) { padding ->

        Column(
            modifier = Modifier.padding(padding).padding(16.dp)
        ) {

            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Title") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))

            TextField(
                value = content,
                onValueChange = { content = it },
                label = { Text("What's on your mind?") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp)
            )

            Spacer(modifier = Modifier.height(20.dp))

            Text("Select tags:", style = MaterialTheme.typography.titleMedium)

            Spacer(modifier = Modifier.height(10.dp))

            FlowRow {
                PostTags.values().forEach { tag ->
                    FilterChip(
                        selected = selectedTags.contains(tag),
                        onClick = {
                            selectedTags = if (selectedTags.contains(tag))
                                selectedTags - tag else selectedTags + tag
                        },
                        label = { Text(tag.name) },
                        modifier = Modifier.padding(end = 8.dp, bottom = 8.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    val userId = CurrentUser.user?.id
                    if (userId != null) {

                        val post = Post(
                            userId = userId,
                            title = title,
                            content = content,
                            tags = selectedTags
                        )

                        postRepository.createPost(post).addOnSuccessListener {
                            Toast.makeText(context, "Post created!", Toast.LENGTH_SHORT).show()
                            onPostCreated()
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = title.isNotBlank() && content.isNotBlank()
            ) {
                Text("Post")
            }
        }
    }
}
