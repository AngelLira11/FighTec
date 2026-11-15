package com.example.fightec.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun MenuScreen(onPlay: () -> Unit) {
    Box(
        Modifier.fillMaxSize().background(Color(0xFF14121F)),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("FIGHTEC", color = Color(0xFFFFC107), fontSize = 56.sp, fontWeight = FontWeight.Bold)
            Text("Torneo Arcade Escolar", color = Color.White, fontSize = 18.sp)
            Spacer(Modifier.height(32.dp))
            Button(onClick = onPlay) { Text("JUGAR", fontSize = 22.sp) }
        }
    }
}