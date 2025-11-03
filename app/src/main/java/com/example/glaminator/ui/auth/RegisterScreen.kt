package com.example.glaminator.ui.auth

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.glaminator.model.User
import com.example.glaminator.ui.theme.GlaminatorTheme
import com.example.glaminator.ui.theme.Primary
import com.example.glaminator.utils.ValidationUtils
import com.example.glaminator.viewmodel.UserViewModel
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(userViewModel: UserViewModel = viewModel(), onRegisterSuccess: () -> Unit, onLoginClick: () -> Unit) {
    var username by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    val user by userViewModel.user.collectAsState()
    val error by userViewModel.error.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(user) {
        if (user != null) {
            Toast.makeText(context, "Registration successful", Toast.LENGTH_SHORT).show()
            onRegisterSuccess()
        }
    }

    LaunchedEffect(error) {
        if (error != null) {
            Toast.makeText(context, error, Toast.LENGTH_SHORT).show()
        }
    }

    GlaminatorTheme {
        Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("GLAMINATOR", style = MaterialTheme.typography.headlineLarge, color = Primary)
                Spacer(modifier = Modifier.height(32.dp))
                OutlinedTextField(
                    value = username,
                    onValueChange = { username = it },
                    label = { Text("Username") },
                    modifier = Modifier.fillMaxWidth(),
                )
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email") },
                    modifier = Modifier.fillMaxWidth(),
                )
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Password") },
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth(),
                )
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(
                    value = confirmPassword,
                    onValueChange = { confirmPassword = it },
                    label = { Text("Confirm Password") },
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth(),
                )
                Spacer(modifier = Modifier.height(32.dp))
                Button(
                    onClick = {
                        val errorMessage = ValidationUtils.validateRegistration(username, email, password, confirmPassword)
                        if (errorMessage != null) {
                            Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show()
                        } else {
                            val hashedPassword = ValidationUtils.toMD5(password)
                            userViewModel.register(User(username = username, email = email, password = hashedPassword))
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Primary)
                ) {
                    Text("Register")
                }
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = onLoginClick, modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent)
                ) {
                    Text("Go to Login", color = Primary)
                }
            }
        }
    }
}
