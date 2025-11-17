package com.example.glaminator.ui.user

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Comment
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.glaminator.MainActivity
import com.example.glaminator.data.CurrentUser
import com.example.glaminator.model.RewardType
import com.example.glaminator.repository.UserRepository
import com.example.glaminator.ui.theme.GlaminatorTheme
import com.example.glaminator.ui.theme.ScaffoldBackground
import com.example.glaminator.ui.theme.heart
import com.example.glaminator.ui.theme.titles
import kotlinx.coroutines.launch

class UserManagementActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            GlaminatorTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    UserManagementScreen()
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserManagementScreen() {
    val activity = (LocalContext.current as? Activity)
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val userRepository = remember { UserRepository() }
    val userPrefs = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
    val glaminatorPrefs = context.getSharedPreferences("glaminator_prefs", Context.MODE_PRIVATE)

    var currentUsername by remember { mutableStateOf(CurrentUser.user?.username ?: "") }
    var newUsername by remember { mutableStateOf("") }
    var confirmationUsername by remember { mutableStateOf("") }
    var isGachaDisabled by remember {
        mutableStateOf(glaminatorPrefs.getBoolean("gacha_disabled", false))
    }

    val user = CurrentUser.user
    val postCount = user?.rewards?.find { it.type == RewardType.POST }?.quantity ?: 0
    val likeCount = user?.rewards?.find { it.type == RewardType.LIKE }?.quantity ?: 0
    val commentCount = user?.rewards?.find { it.type == RewardType.COMMENT }?.quantity ?: 0

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Account Management", color = titles) },
                navigationIcon = {
                    IconButton(onClick = { activity?.finish() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = titles)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = ScaffoldBackground
                )
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Text(
                    text = "Welcome $currentUsername",
                    style = MaterialTheme.typography.headlineSmall
                )
                Spacer(modifier = Modifier.height(16.dp))
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(4.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(text = "Change Username", style = MaterialTheme.typography.titleLarge)
                        Spacer(modifier = Modifier.height(16.dp))
                        OutlinedTextField(
                            value = newUsername,
                            onValueChange = { newUsername = it },
                            label = { Text("New Username") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(
                            onClick = {
                                if (newUsername.isNotBlank()) {
                                    coroutineScope.launch {
                                        CurrentUser.user?.let { currentUser ->
                                            val updatedUser = currentUser.copy(username = newUsername)
                                            userRepository.updateUser(currentUser.id, updatedUser)
                                                .addOnSuccessListener {
                                                    CurrentUser.user = updatedUser
                                                    currentUsername = newUsername // update state to trigger recomposition
                                                    Toast.makeText(context, "Username updated successfully", Toast.LENGTH_SHORT).show()
                                                    newUsername = ""
                                                }
                                                .addOnFailureListener { e ->
                                                    Toast.makeText(context, "Failed to update username: ${e.message}", Toast.LENGTH_SHORT).show()
                                                }
                                        }
                                    }
                                } else {
                                    Toast.makeText(context, "New username cannot be empty", Toast.LENGTH_SHORT).show()
                                }
                            },
                            modifier = Modifier.align(Alignment.End)
                        ) {
                            Text("Save")
                        }
                    }
                }
            }

            // Gacha Toggle Section
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(4.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(text = "Disable Gacha", style = MaterialTheme.typography.titleLarge)
                            Switch(
                                checked = isGachaDisabled,
                                onCheckedChange = {
                                    isGachaDisabled = it
                                    with(glaminatorPrefs.edit()) {
                                        putBoolean("gacha_disabled", it)
                                        apply()
                                    }
                                }
                            )
                        }
                    }
                }
            }


            // Logout Section
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(4.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.AutoMirrored.Filled.ExitToApp,
                                contentDescription = "Logout",
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Logout",
                                style = MaterialTheme.typography.titleLarge
                            )
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = {
                                CurrentUser.user = null
                                with(userPrefs.edit()) {
                                    remove("user_id")
                                    apply()
                                }
                                with(glaminatorPrefs.edit()) {
                                    putBoolean("remember_me", false)
                                    apply()
                                }
                                val intent = Intent(context, MainActivity::class.java).apply {
                                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                                }
                                context.startActivity(intent)
                                activity?.finish()
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Logout")
                        }
                    }
                }
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(4.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(text = "Your Statistics", style = MaterialTheme.typography.titleLarge)
                        Spacer(modifier = Modifier.height(16.dp))
                        StatisticItem(icon = Icons.Filled.Description, label = "Posts", value = postCount)
                        StatisticItem(icon = Icons.Filled.Favorite, label = "Likes", value = likeCount)
                        StatisticItem(icon = Icons.Filled.Comment, label = "Comments", value = commentCount)
                    }
                }
            }

            // Delete Account Section
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(4.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Filled.Warning,
                                contentDescription = "Warning",
                                tint = MaterialTheme.colorScheme.heart
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Delete Account",
                                style = MaterialTheme.typography.titleLarge,

                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "This action is irreversible and will delete all your data. Please type your username to confirm.",
                            style = MaterialTheme.typography.bodyMedium,

                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        OutlinedTextField(
                            value = confirmationUsername,
                            onValueChange = { confirmationUsername = it },
                            label = { Text("Confirm '$currentUsername'") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = {
                                if (confirmationUsername == currentUsername && currentUsername.isNotEmpty()) {
                                    coroutineScope.launch {
                                        CurrentUser.user?.let { currentUser ->
                                            userRepository.deleteAllUserData(currentUser.id)
                                                .addOnSuccessListener {
                                                    Toast.makeText(context, "Account deleted successfully", Toast.LENGTH_SHORT).show()
                                                    CurrentUser.user = null
                                                    with(userPrefs.edit()) {
                                                        remove("user_id")
                                                        apply()
                                                    }
                                                    with(glaminatorPrefs.edit()) {
                                                        putBoolean("remember_me", false)
                                                        apply()
                                                    }
                                                    val intent = Intent(context, MainActivity::class.java).apply {
                                                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                                                    }
                                                    context.startActivity(intent)
                                                    activity?.finish()
                                                }
                                                .addOnFailureListener { e ->
                                                    Toast.makeText(context, "Failed to delete account: ${e.message}", Toast.LENGTH_SHORT).show()
                                                }
                                        }
                                    }
                                } else {
                                    Toast.makeText(context, "Username does not match or is empty!", Toast.LENGTH_SHORT).show()
                                }
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.error,
                                contentColor = MaterialTheme.colorScheme.onError
                            ),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Filled.DeleteForever, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("DELETE MY ACCOUNT")
                        }
                    }
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

@Preview(showBackground = true)
@Composable
fun UserManagementScreenPreview() {
    GlaminatorTheme {
        UserManagementScreen()
    }
}
