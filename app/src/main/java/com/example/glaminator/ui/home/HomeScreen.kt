package com.example.glaminator.ui.home

import android.content.Intent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.FilterList
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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.glaminator.data.CurrentUser
import com.example.glaminator.model.Post
import com.example.glaminator.model.PostTags
import com.example.glaminator.ui.post.CreatePostActivity
import com.example.glaminator.ui.post.PostDetailActivity
import com.example.glaminator.ui.theme.GlaminatorTheme
import com.example.glaminator.ui.theme.Primary
import com.example.glaminator.ui.user.UserManagementActivity
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import kotlinx.coroutines.launch
import com.example.glaminator.ui.post.PostItem
import com.example.glaminator.repository.PostRepository
import com.example.glaminator.ui.components.TagSelectionDialog
import com.example.glaminator.ui.pull.PullActivity
import com.example.glaminator.ui.theme.ScaffoldBackground
import com.example.glaminator.ui.theme.titles

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(homeViewModel: HomeViewModel = viewModel()) {
    val context = LocalContext.current
    var posts by remember { mutableStateOf<List<Post>>(emptyList()) }
    var isRefreshing by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()
    var showSearchDialog by remember { mutableStateOf(false) }
    var selectedTags by remember { mutableStateOf<Set<PostTags>>(emptySet()) }

    val searchResults by homeViewModel.searchResults.collectAsState()
    val postRepository = remember { PostRepository() }

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
                        IconButton(onClick = { showSearchDialog = true }) {
                            Icon(Icons.Filled.FilterList, contentDescription = "Filter by Tag", tint = titles)
                        }
                        IconButton(
                            onClick = {
                                selectedTags = emptySet()
                                homeViewModel.searchByTags(emptyList())
                            }
                        ) {
                            Icon(
                                Icons.Filled.Clear,
                                contentDescription = "Clear Filter",
                                tint = titles
                            )
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
                    Icon(
                        Icons.Filled.Add,
                        contentDescription = "Make a Post",
                        tint = titles
                    )
                }
            }
        ) { innerPadding ->
            if (showSearchDialog) {
                TagSelectionDialog(
                    onDismiss = { showSearchDialog = false },
                    onConfirm = { 
                        selectedTags = it
                        homeViewModel.searchByTags(it.toList())
                        showSearchDialog = false 
                    },
                    initialSelectedTags = selectedTags
                )
            }
            SwipeRefresh(
                state = swipeRefreshState,
                onRefresh = ::refreshPosts,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                val listToDisplay =
                    if (selectedTags.isNotEmpty()) searchResults else posts

                if (posts.isEmpty() && !isRefreshing) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = "No unseen posts.", color = MaterialTheme.colorScheme.onBackground)
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                    ) {
                        itemsIndexed(listToDisplay) { idx, post ->
                            val currentUserId = CurrentUser.user?.id
                            PostItem(
                                post = post,
                                onPostClick = {
                                    val intent = Intent(context, PostDetailActivity::class.java)
                                    intent.putExtra("POST_ID", post.id)
                                    context.startActivity(intent)
                                },
                                isLiked = CurrentUser.user?.id in post.likes,
                                onLikeClicked = {
                                    if (currentUserId != null) {
                                        if (currentUserId in post.likes) {
                                            postRepository.removeLikeFromPost(post.id, currentUserId)
                                        } else {
                                            postRepository.addLikeToPost(post.id, currentUserId)
                                        }
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}
