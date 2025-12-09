package com.example.glaminator

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.forEach
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.BottomNavigation
import androidx.compose.material.BottomNavigationItem
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.glaminator.data.CurrentUser
import com.example.glaminator.ui.auth.LoginScreen
import com.example.glaminator.ui.auth.RegisterScreen
import com.example.glaminator.ui.home.HomeScreen
import com.example.glaminator.ui.mypage.MyPageScreen
import com.example.glaminator.ui.post.CreatePostScreen
import com.example.glaminator.ui.pull.PullActivity
import com.example.glaminator.ui.pull.PullScreen
import com.example.glaminator.ui.theme.GlaminatorTheme
import com.example.glaminator.ui.theme.Primary
import com.example.glaminator.ui.theme.ScaffoldBackground
import com.example.glaminator.ui.theme.titles
import com.example.glaminator.viewmodel.UserViewModel

sealed class Screen(val route: String, val label: String, val icon: ImageVector) {
    object AllPosts : Screen("all_posts", "", Icons.Default.Home)
    object AddPost : Screen("add_post", "", Icons.Default.AddCircle)
    object Pull : Screen("pull", "", Icons.Default.CardGiftcard)
    object MyPage : Screen("my_page", "", Icons.Default.Person)
}

val bottomNavItems = listOf(
    Screen.AllPosts,
    Screen.AddPost,
    Screen.Pull,
    Screen.MyPage,
)

class MainActivity : ComponentActivity() {

    private val userViewModel: UserViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val userPrefs = getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        val glaminatorPrefs = getSharedPreferences("glaminator_prefs", Context.MODE_PRIVATE)
        val userId = userPrefs.getString("user_id", null)
        val rememberMe = glaminatorPrefs.getBoolean("remember_me", false)

        if (rememberMe && userId != null) {
            userViewModel.loadUserById(userId)
        }

        setContent {
            GlaminatorTheme {
                val navController = rememberNavController()
                val user by userViewModel.user.collectAsState()

                val startDestination = if (user != null) {
                    CurrentUser.user = user
                    "main_screen"
                } else {
                    "login"
                }

                NavHost(navController = navController, startDestination = startDestination) {
                    composable("login") {
                        LoginScreen(
                            userViewModel = userViewModel,
                            onRegisterClick = { navController.navigate("register") },
                            onLoginSuccess = { shouldRemember ->
                                if (shouldRemember) {
                                    with(glaminatorPrefs.edit()) {
                                        putBoolean("remember_me", true)
                                        apply()
                                    }
                                }
                                navController.navigate("main_screen") { popUpTo("login") { inclusive = true } }
                            }
                        )
                    }
                    composable("register") {
                        RegisterScreen(
                            onRegisterSuccess = { navController.navigate("login") },
                            onLoginClick = { navController.navigate("login") }
                        )
                    }
                    composable("main_screen") {
                        MainScreen(userViewModel = userViewModel)
                    }
                }
            }
        }
    }
}

@Composable
fun MainScreen(userViewModel: UserViewModel) {
    val navContentController = rememberNavController()

    Scaffold(
        bottomBar = {
            Surface(
                elevation = 0.dp,
                border = BorderStroke(0.dp, Color.Transparent),
                modifier = Modifier.padding(top = 16.dp)
            ) {
                BottomNavigation(
                    backgroundColor = ScaffoldBackground,
                    elevation = 0.dp
                ) {
                    val navBackStackEntry by navContentController.currentBackStackEntryAsState()
                    val currentDestination = navBackStackEntry?.destination

                    bottomNavItems.forEach { screen ->
                        BottomNavigationItem(
                            icon = { Icon(screen.icon, contentDescription = screen.label) },
                            label = { Text(screen.label) },
                            selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true,
                            onClick = {
                                navContentController.navigate(screen.route) {
                                    popUpTo(navContentController.graph.startDestinationId)
                                    launchSingleTop = true
                                }
                            },
                            selectedContentColor = Primary,
                            unselectedContentColor = titles
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navContentController,
            startDestination = Screen.AllPosts.route,
        ) {
            composable(Screen.AllPosts.route) {
                HomeScreen()
            }
            composable(Screen.AddPost.route) {
                CreatePostScreen(
                    onPostCreated = {
                        navContentController.navigate(Screen.AllPosts.route) {
                            popUpTo(Screen.AllPosts.route) {
                                inclusive = true
                            }
                        }
                    }
                )
            }
            composable(Screen.Pull.route) {
                PullScreen()
            }
            composable(Screen.MyPage.route) {
                MyPageScreen()
            }
        }
    }
}