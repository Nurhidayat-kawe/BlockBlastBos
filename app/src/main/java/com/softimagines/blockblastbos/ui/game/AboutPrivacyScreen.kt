package com.softimagines.blockblastbos.ui.game

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutPrivacyScreen(
    onBack: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("About & Privacy", color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF1A1A1A))
            )
        },
        containerColor = Color(0xFF121212)
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // About Section
            SectionHeader("About Block Blast Bos")
            Text(
                "Block Blast Bos is an addictive block puzzle game with various modes including Classic, Boss Battle, Time Attack, Zen, and Puzzle levels. Strategy and quick thinking are key to achieving high scores and defeating bosses.",
                color = Color.White.copy(alpha = 0.8f),
                fontSize = 14.sp,
                lineHeight = 20.sp
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            Text("Version: 1.0.0", color = Color.Gray, fontSize = 12.sp)
            Text("Developer: SoftImagines", color = Color.Gray, fontSize = 12.sp)

            Divider(modifier = Modifier.padding(vertical = 24.dp), color = Color.White.copy(alpha = 0.1f))

            // Privacy Policy Section
            SectionHeader("Privacy Policy")
            PrivacyText("1. Information Collection", "We do not collect any personal identification information from our users. The game may store progress and high scores locally on your device.")
            PrivacyText("2. Advertising", "We use AdMob to serve advertisements. AdMob may collect and use personal data for its own purposes, such as to personalize the advertisements it serves to you.")
            PrivacyText("3. Data Usage", "The data stored locally (high scores, coins) is only used for game functionality and is not shared with third parties.")
            PrivacyText("4. Consent", "By using Block Blast Bos, you hereby consent to our Privacy Policy and agree to its terms.")
            
            Spacer(modifier = Modifier.height(32.dp))
            
            Text(
                "© 2024 SoftImagines. All rights reserved.",
                modifier = Modifier.fillMaxWidth(),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                color = Color.White.copy(alpha = 0.3f),
                fontSize = 12.sp
            )
        }
    }
}

@Composable
fun SectionHeader(text: String) {
    Text(
        text = text,
        color = Color.Yellow,
        fontSize = 20.sp,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(bottom = 8.dp)
    )
}

@Composable
fun PrivacyText(title: String, content: String) {
    Column(modifier = Modifier.padding(bottom = 12.dp)) {
        Text(title, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
        Spacer(modifier = Modifier.height(4.dp))
        Text(content, color = Color.White.copy(alpha = 0.7f), fontSize = 14.sp, lineHeight = 20.sp)
    }
}
