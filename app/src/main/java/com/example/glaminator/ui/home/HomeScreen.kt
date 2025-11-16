package com.example.glaminator.ui.home

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.glaminator.data.CurrentUser
import com.example.glaminator.model.Post
import com.example.glaminator.ui.post.CreatePostActivity
import com.example.glaminator.ui.post.PostDetailActivity
import com.example.glaminator.ui.pull.PullActivity
import com.example.glaminator.ui.theme.Background
import com.example.glaminator.ui.theme.GlaminatorTheme
import com.example.glaminator.ui.theme.Primary
import com.example.glaminator.ui.theme.ScaffoldBackground
import com.example.glaminator.ui.theme.Surface
import com.example.glaminator.ui.theme.titles
import com.example.glaminator.ui.user.UserManagementActivity
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(homeViewModel: HomeViewModel = viewModel()) {
    val context = LocalContext.current
    var posts by remember { mutableStateOf<List<Post>>(emptyList()) }
    var isRefreshing by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    val swipeRefreshState = rememberSwipeRefreshState(isRefreshing = isRefreshing)

    fun refreshPosts() {
        coroutineScope.launch {
            isRefreshing = true
            homeViewModel.getUnseenPosts { unseenPosts ->
                posts = unseenPosts
                isRefreshing = false
            }
        }
    }

    LaunchedEffect(Unit) {
        refreshPosts()
    }

    GlaminatorTheme {
        Scaffold(

            topBar = {
                TopAppBar(
                    title = { Text(text = CurrentUser.user?.username ?: "Glaminator", color = titles,  fontWeight = FontWeight.Bold) },
                    navigationIcon = {
                        IconButton(onClick = { context.startActivity(Intent(context, UserManagementActivity::class.java)) }) {
                            Icon(Icons.Filled.AccountCircle, contentDescription = "Account", tint = titles)
                        }
                    },
                    actions = {
                        IconButton(onClick = { context.startActivity(Intent(context, PullActivity::class.java)) }) {
                            Icon(Icons.Filled.CardGiftcard, contentDescription = "Pull", tint = titles)
                        }
                        IconButton(onClick = { /* TODO: Implement */ }) {
                            Icon(Icons.Filled.Search, contentDescription = "Search", tint = titles)
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = ScaffoldBackground
                    )
                )
            },
            floatingActionButton = {
                FloatingActionButton(
                    onClick = {
                        context.startActivity(Intent(context, CreatePostActivity::class.java))
                    },
                    containerColor = Primary
                ) {
                    Icon(Icons.Filled.Add, contentDescription = "Make a Post", tint = Color.White)
                }
            }
        ) { innerPadding ->
            SwipeRefresh(
                state = swipeRefreshState,
                onRefresh = ::refreshPosts,
                modifier = Modifier.padding(innerPadding)
            ) {
                if (posts.isEmpty() && !isRefreshing) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = "No unseen posts.", color = MaterialTheme.colorScheme.onBackground)
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize()
                    ) {
                        itemsIndexed(posts) { idx, post ->
                            PostItem(post = post) {
                                val intent = Intent(context, PostDetailActivity::class.java).apply {
                                    putExtra("POST_ID", post.id)
                                }
                                context.startActivity(intent)
                            }
                            if (idx < posts.count() -1) {
                                Divider()
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PostItem(post: Post, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
            .padding(8.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            AsyncImage(
                model = post.imageUrls.firstOrNull(),
                contentDescription = "Post Image",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                Color.Transparent,
                                Color.Black.copy(alpha = 0.7f)
                            )
                        )
                    )
                    .padding(16.dp),
                verticalArrangement = Arrangement.Bottom
            ) {
                Text(text = post.title, style = MaterialTheme.typography.headlineSmall, color = Color.White)
                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Filled.Favorite,
                        contentDescription = "Likes",
                        tint = Color.Red
                    )
                    Text(text = " ${post.likes.size}", style = MaterialTheme.typography.bodyMedium, color = Color.White)
                }
            }
        }
    }
}
