package com.example.glaminator

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.*
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.glaminator.ui.auth.LoginScreen
import com.example.glaminator.ui.auth.RegisterScreen
import com.example.glaminator.ui.home.HomeScreen
import com.example.glaminator.ui.theme.GlaminatorTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            GlaminatorTheme {
                val navController = rememberNavController()
                NavHost(navController = navController, startDestination = "login") {
                    composable("login") {
                        LoginScreen(
                            onRegisterClick = { navController.navigate("register") },
                            onLoginSuccess = { navController.navigate("home") })
                    }
                    composable("register") {
                        RegisterScreen(
                            onRegisterSuccess = { navController.navigate("login") }, // Force login to validate account
                            onLoginClick = { navController.navigate("login") })
                    }
                    composable("home") {
                        HomeScreen()
                    }
                }
            }
        }
    }
}
