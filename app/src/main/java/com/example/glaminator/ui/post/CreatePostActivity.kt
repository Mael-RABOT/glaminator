package com.example.glaminator.ui.post

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.glaminator.data.CurrentUser
import com.example.glaminator.model.Post
import com.example.glaminator.repository.PostRepository
import com.example.glaminator.ui.theme.Background
import com.example.glaminator.ui.theme.GlaminatorTheme
import com.example.glaminator.ui.theme.Primary
import com.example.glaminator.utils.ValidationUtils

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
    var content by remember { mutableStateOf("") }
    val context = LocalContext.current
    val postRepository = PostRepository()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Create Post", color = Primary) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Background
                )
            )
        }
    ) {
        Column(
            modifier = Modifier.padding(it).padding(16.dp)
        ) {
            OutlinedTextField(
                value = content,
                onValueChange = { content = it },
                label = { Text("What's on your mind?") },
                modifier = Modifier.fillMaxWidth(),
                isError = !ValidationUtils.isValidPostContent(content),
                colors = OutlinedTextFieldDefaults.colors()
            )
            Button(
                onClick = {
                    val userId = CurrentUser.user?.id
                    if (userId != null && ValidationUtils.isValidPostContent(content)) {
                        val post = Post(userId = userId, content = content)
                        postRepository.createPost(post).addOnSuccessListener {
                            Toast.makeText(context, "Post created!", Toast.LENGTH_SHORT).show()
                            onPostCreated()
                        }.addOnFailureListener {
                            Toast.makeText(context, "Failed to create post.", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        Toast.makeText(context, "Post content is empty or too long, or user is not logged in.", Toast.LENGTH_SHORT).show()
                    }
                },
                modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
                enabled = ValidationUtils.isValidPostContent(content),
                colors = ButtonDefaults.buttonColors(containerColor = Primary)
            ) {
                Text("Post", color = Color.Black)
            }
        }
    }
}
