package com.example.glaminator.ui.pull

import android.app.Activity
import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.glaminator.model.Reward
import com.example.glaminator.repository.RewardRepository
import com.example.glaminator.ui.theme.GlaminatorTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private const val PULL_COOLDOWN_MINUTES = 1

class PullActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            GlaminatorTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    PullScreen()
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PullScreen() {
    val activity = (LocalContext.current as? Activity)
    val context = LocalContext.current
    val sharedPreferences = remember { context.getSharedPreferences("glaminator_prefs", Context.MODE_PRIVATE) }
    val rewardRepository = remember { RewardRepository() }
    var tapsRemaining by remember { mutableIntStateOf(10) }
    var isOpened by remember { mutableStateOf(false) }
    var reward by remember { mutableStateOf<Reward?>(null) }
    val coroutineScope = rememberCoroutineScope()
    val rotation = remember { Animatable(0f) }
    val scale = remember { Animatable(1f) }
    var lastPullTimestamp by remember {
        mutableLongStateOf(sharedPreferences.getLong("last_pull_timestamp", 0L))
    }
    var cooldownRemaining by remember { mutableLongStateOf(0L) }

    LaunchedEffect(Unit, lastPullTimestamp) {
        val cooldownMillis = PULL_COOLDOWN_MINUTES * 60 * 1000L
        val currentTime = System.currentTimeMillis()
        val timeSinceLastPull = currentTime - lastPullTimestamp
        if (timeSinceLastPull < cooldownMillis) {
            cooldownRemaining = (cooldownMillis - timeSinceLastPull) / 1000
            while (cooldownRemaining > 0) {
                delay(1000L)
                cooldownRemaining--
            }
        }
    }

    val isOnCooldown = cooldownRemaining > 0

    fun shake() {
        coroutineScope.launch {
            launch {
                rotation.animateTo(10f, tween(50))
                rotation.animateTo(-10f, tween(50))
                rotation.animateTo(0f, tween(50))
            }
            launch {
                scale.animateTo(1.2f, tween(75))
                scale.animateTo(1f, tween(75))
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Gacha Pull") },
                navigationIcon = {
                    IconButton(onClick = { activity?.finish() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            if (isOnCooldown) {
                Text("Next pull available in:", style = MaterialTheme.typography.headlineSmall)
                Spacer(modifier = Modifier.height(8.dp))
                val minutes = cooldownRemaining / 60
                val seconds = cooldownRemaining % 60
                Text(
                    String.format("%02d:%02d", minutes, seconds),
                    style = MaterialTheme.typography.displaySmall
                )
            } else if (isOpened) {
                reward?.let {
                    Text("Congratulations!", style = MaterialTheme.typography.displaySmall)
                    Text("You got:", style = MaterialTheme.typography.headlineSmall)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("${it.quantity} ${it.type}(s)", style = MaterialTheme.typography.headlineMedium)
                    Text("Rarity: ${it.rarity}", style = MaterialTheme.typography.bodyLarge)
                    Spacer(modifier = Modifier.height(32.dp))
                    Button(onClick = {
                        val newTimestamp = System.currentTimeMillis()
                        sharedPreferences.edit().putLong("last_pull_timestamp", newTimestamp).apply()
                        lastPullTimestamp = newTimestamp
                        isOpened = false
                        tapsRemaining = 10
                    }) {
                        Text("Awesome!")
                    }
                }
            } else {
                Text("Gacha Pull!", style = MaterialTheme.typography.displaySmall)
                Spacer(modifier = Modifier.height(8.dp))
                Text("Tap the container to open it", style = MaterialTheme.typography.headlineSmall)
                Spacer(modifier = Modifier.height(16.dp))
                Text("$tapsRemaining taps remaining", style = MaterialTheme.typography.bodyLarge)
            }

            Spacer(modifier = Modifier.height(32.dp))

            if (!isOpened && !isOnCooldown) {
                 Box(
                    modifier = Modifier
                        .size(250.dp)
                        .scale(scale.value)
                        .rotate(rotation.value)
                        .clickable(enabled = !isOpened) {
                            if (tapsRemaining > 1) {
                                tapsRemaining--
                                shake()
                            } else {
                                tapsRemaining = 0
                                isOpened = true
                                val generatedReward = rewardRepository.generateReward()
                                reward = generatedReward
                                rewardRepository.claimReward(generatedReward)
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    AsyncImage(
                        model = "file:///android_asset/CauPull1.png",
                        contentDescription = "Gacha Container",
                        modifier = Modifier.size(250.dp)
                    )
                }
            }
        }
    }
}


@Preview(showBackground = true)
@Composable
fun PullScreenPreview() {
    GlaminatorTheme {
        PullScreen()
    }
}
