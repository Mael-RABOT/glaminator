package com.example.glaminator.ui.home

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.glaminator.data.CurrentUser
import com.example.glaminator.model.Post
import com.example.glaminator.ui.post.CreatePostActivity
import com.example.glaminator.ui.post.PostDetailActivity
import com.example.glaminator.ui.theme.Background
import com.example.glaminator.ui.theme.GlaminatorTheme
import com.example.glaminator.ui.theme.Primary
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
    var query by remember { mutableStateOf("") }

    val searchResults by homeViewModel.searchResults.collectAsState()
    val coroutineScope = rememberCoroutineScope()
    val swipeRefreshState = rememberSwipeRefreshState(isRefreshing)

    fun refreshPosts() {
        coroutineScope.launch {
            isRefreshing = true
            homeViewModel.getUnseenPosts { unseen ->
                posts = unseen
                isRefreshing = false
            }
        }
    }

    LaunchedEffect(Unit) { refreshPosts() }

    GlaminatorTheme {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            text = CurrentUser.user?.username ?: "Glaminator",
                            color = Primary
                        )
                    },
                    navigationIcon = {
                        IconButton(
                            onClick = {
                                context.startActivity(
                                    Intent(context, UserManagementActivity::class.java)
                                )
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Filled.AccountCircle,
                                contentDescription = "Account",
                                tint = Primary
                            )
                        }
                    },
                    actions = {
                        TextField(
                            value = query,
                            onValueChange = {
                                query = it
                                homeViewModel.searchByTag(it)
                            },
                            placeholder = { Text("Search tags", color = Primary) },
                            singleLine = true,
                            modifier = Modifier
                                .padding(end = 16.dp)
                                .fillMaxWidth(0.6f)
                        )
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
                    Icon(
                        imageVector = Icons.Filled.Add,
                        contentDescription = "Make a Post",
                        tint = Color.Black
                    )
                }
            }
        ) { innerPadding ->
            SwipeRefresh(
                state = swipeRefreshState,
                onRefresh = ::refreshPosts,
                modifier = Modifier.padding(innerPadding)
            ) {
                val displayList =
                    if (query.isNotBlank()) searchResults else posts

                if (displayList.isEmpty() && !isRefreshing) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (query.isNotBlank()) "No posts found." else "No unseen posts.",
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize()
                    ) {
                        itemsIndexed(displayList) { idx, post ->
                            PostItem(post = post) {
                                val intent =
                                    Intent(context, PostDetailActivity::class.java).apply {
                                        putExtra("POST_ID", post.id)
                                    }
                                context.startActivity(intent)
                            }
                            if (idx < displayList.lastIndex) {
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

                Text(
                    text = post.title,
                    style = MaterialTheme.typography.headlineSmall,
                    color = Color.White
                )

                Spacer(modifier = Modifier.height(6.dp))

                if (post.tags.isNotEmpty()) {
                    Text(
                        text = post.tags.joinToString(" · ") { "#$it" },
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.LightGray
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Filled.Favorite,
                        contentDescription = "Likes",
                        tint = Color.Red
                    )
                    Text(
                        text = " ${post.likes.size}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White
                    )
                }
            }
        }
    }
}
