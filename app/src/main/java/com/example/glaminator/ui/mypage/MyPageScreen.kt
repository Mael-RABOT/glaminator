package com.example.glaminator.ui.mypage

import android.content.Intent
import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Comment
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.glaminator.data.CurrentUser
import com.example.glaminator.model.RewardType
import com.example.glaminator.ui.post.PostDetailActivity
import com.example.glaminator.ui.post.PostItem
import com.example.glaminator.ui.theme.ScaffoldBackground
import com.example.glaminator.ui.theme.titles
import com.example.glaminator.ui.user.UserManagementActivity
import com.example.glaminator.viewmodel.UserViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyPageScreen(
    myPageViewModel: MyPageViewModel = viewModel(),
) {
    val context = LocalContext.current
    val activity = context as ComponentActivity
    val userViewModel: UserViewModel = viewModel(viewModelStoreOwner = activity)
    val currentUser by userViewModel.user.collectAsState()
    val myPosts by myPageViewModel.myPosts.collectAsState()

    val postCount = currentUser?.rewards?.find { it.type == RewardType.POST }?.quantity ?: 0
    val likeCount = currentUser?.rewards?.find { it.type == RewardType.LIKE }?.quantity ?: 0
    val commentCount = currentUser?.rewards?.find { it.type == RewardType.COMMENT }?.quantity ?: 0

    LaunchedEffect(key1 = Unit) {
        myPageViewModel.fetchMyPosts()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(text = currentUser?.username ?: "Glaminator",
                        color = titles,
                        fontWeight = FontWeight.Bold)
                },
                actions = {
                    IconButton(onClick = {
                        val intent = Intent(context, UserManagementActivity::class.java)
                        context.startActivity(intent)
                    }) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Account Settings",
                            tint = titles
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = ScaffoldBackground
                )
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "My Rewards",
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp)
                )
            }
            item {
                Card(modifier = Modifier
                    .fillMaxWidth()) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        StatisticItem(
                            icon = Icons.Filled.Description,
                            label = "Posts",
                            value = postCount
                        )
                        StatisticItem(
                            icon = Icons.Filled.Favorite,
                            label = "Likes",
                            value = likeCount
                        )
                        StatisticItem(
                            icon = Icons.Filled.Comment,
                            label = "Comments",
                            value = commentCount
                        )
                    }
                }
            }

            item {
                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 8.dp),
                    thickness = 1.dp,
                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                )
            }

            item {
                Text(
                    text = "My Posts",
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier
                        .fillMaxWidth()
                )
            }
            if (myPosts.isEmpty()) {
                item {
                    Text("You haven't created any posts yet!")
                }
            } else {
                items(items = myPosts, key = { post -> post.id }) { post ->
                    PostItem(
                        post = post,
                        onPostClick = {
                            val intent = Intent(context, PostDetailActivity::class.java)
                            intent.putExtra("POST_ID", post.id)
                            context.startActivity(intent)
                        },
                        isLiked = currentUser?.id in post.likes,
                        onLikeClicked = {
                            myPageViewModel.toggleLike(post.id, post.likes)
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun StatisticItem(icon: ImageVector, label: String, value: Int) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
        Spacer(modifier = Modifier.width(16.dp))
        Text(text = label, style = MaterialTheme.typography.bodyLarge)
        Spacer(modifier = Modifier.weight(1f))
        Text(text = value.toString(), style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
    }
}