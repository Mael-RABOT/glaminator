package com.example.glaminator.ui.home

import android.content.Intent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.glaminator.data.CurrentUser
import com.example.glaminator.model.Post
import com.example.glaminator.ui.post.CreatePostActivity
import com.example.glaminator.ui.theme.Background
import com.example.glaminator.ui.theme.GlaminatorTheme
import com.example.glaminator.ui.theme.Primary
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
                    title = { Text(text = CurrentUser.user?.username ?: "Glaminator", color = Primary) },
                    navigationIcon = {
                        IconButton(onClick = { /* TODO: Implement account navigation */ }) {
                            Icon(Icons.Filled.AccountCircle, contentDescription = "Account", tint = Primary)
                        }
                    },
                    actions = {
                        IconButton(onClick = { HomeViewModel().createFixtures() /* TODO: Replace with implementation */ }) {
                            Icon(Icons.Filled.Search, contentDescription = "Search", tint = Primary)
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Background
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
                    Icon(Icons.Filled.Add, contentDescription = "Make a Post", tint = Color.Black)
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
                        items(posts) { post ->
                            PostItem(post = post)
                            Divider()
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PostItem(post: Post) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
        ) {
            Text(text = post.content, style = MaterialTheme.typography.bodyLarge)
            Spacer(modifier = Modifier.height(8.dp))
            // TODO: Display images & comments
            Spacer(modifier = Modifier.height(16.dp))
            Text(text = "Comments section", style = MaterialTheme.typography.labelSmall)
        }
    }
}
