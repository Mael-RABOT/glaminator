package com.example.glaminator

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.*
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.glaminator.data.CurrentUser
import com.example.glaminator.ui.auth.LoginScreen
import com.example.glaminator.ui.auth.RegisterScreen
import com.example.glaminator.ui.home.HomeScreen
import com.example.glaminator.ui.theme.GlaminatorTheme
import com.example.glaminator.viewmodel.UserViewModel

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
                    "home"
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
                                navController.navigate("home") { popUpTo("login") { inclusive = true } }
                            }
                        )
                    }
                    composable("register") {
                        RegisterScreen(
                            onRegisterSuccess = { navController.navigate("login") },
                            onLoginClick = { navController.navigate("login") }
                        )
                    }
                    composable("home") {
                        HomeScreen()
                    }
                }
            }
        }
    }
}
