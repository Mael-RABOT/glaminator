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
import androidx.compose.material.icons.filled.Tag
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
import com.example.glaminator.model.PostTags
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
    var showSearchDialog by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }

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

    LaunchedEffect(Unit) {
        refreshPosts()
    }

    GlaminatorTheme {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text(CurrentUser.user?.username ?: "Glaminator", color = Primary) },
                    navigationIcon = {
                        IconButton(
                            onClick = { context.startActivity(Intent(context, UserManagementActivity::class.java)) }
                        ) {
                            Icon(Icons.Filled.AccountCircle, contentDescription = "Account", tint = Primary)
                        }
                    },
                    actions = {
                        IconButton(onClick = { showSearchDialog = true }) {
                            Icon(
                                imageVector = Icons.Filled.Tag,
                                contentDescription = "Search by Tag",
                                tint = Primary
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Background)
                )
            },
            floatingActionButton = {
                FloatingActionButton(
                    onClick = { context.startActivity(Intent(context, CreatePostActivity::class.java)) },
                    containerColor = Primary
                ) {
                    Icon(Icons.Filled.Add, contentDescription = "Create Post", tint = Color.Black)
                }
            }
        ) { padding ->

            if (showSearchDialog) {
                AlertDialog(
                    onDismissRequest = { showSearchDialog = false },
                    title = { Text("Search posts by tag") },
                    text = {
                        TextField(
                            value = searchQuery,
                            onValueChange = { text ->
                                searchQuery = text

                                val tagEnum = try {
                                    PostTags.valueOf(text.trim().uppercase())
                                } catch (_: Exception) {
                                    null
                                }

                                homeViewModel.searchByTag(tagEnum)
                            },
                            placeholder = { Text("Enter tag (e.g. FOOD)") },
                            singleLine = true
                        )
                    },
                    confirmButton = {
                        TextButton(onClick = { showSearchDialog = false }) {
                            Text("Close")
                        }
                    }
                )
            }

            SwipeRefresh(
                state = swipeRefreshState,
                onRefresh = ::refreshPosts,
                modifier = Modifier.padding(padding)
            ) {

                val listToDisplay =
                    if (searchQuery.isNotBlank()) searchResults else posts

                if (listToDisplay.isEmpty() && !isRefreshing) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (searchQuery.isNotBlank()) "No posts found." else "No unseen posts.",
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    }
                } else {
                    LazyColumn(Modifier.fillMaxSize()) {
                        itemsIndexed(listToDisplay) { idx, post ->
                            PostItem(post) {
                                val intent = Intent(context, PostDetailActivity::class.java)
                                intent.putExtra("POST_ID", post.id)
                                context.startActivity(intent)
                            }
                            if (idx < listToDisplay.lastIndex) {
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
            .height(220.dp)
            .padding(8.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Box(Modifier.fillMaxSize()) {

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
                        Brush.verticalGradient(
                            listOf(Color.Transparent, Color.Black.copy(alpha = 0.75f))
                        )
                    )
                    .padding(16.dp),
                verticalArrangement = Arrangement.Bottom
            ) {

                Text(post.title, style = MaterialTheme.typography.headlineSmall, color = Color.White)

                Spacer(Modifier.height(6.dp))

                if (post.tags.isNotEmpty()) {
                    Text(
                        text = post.tags.joinToString(" · ") { "#${it.name}" },
                        color = Color.LightGray,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(Modifier.height(6.dp))
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.Favorite, contentDescription = "Likes", tint = Color.Red)
                    Text(" ${post.likes.size}", color = Color.White)
                }
            }
        }
    }
}
