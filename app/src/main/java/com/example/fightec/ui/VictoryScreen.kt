package com.example.fightec.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.fightec.entities.CharacterInfo

@Composable
fun VictoryScreen(
    playerChar: CharacterInfo,
    enemyChar: CharacterInfo,
    playerWon: Boolean,
    onRematch: () -> Unit,
    onMenu: () -> Unit
) {
    val winner = if (playerWon) playerChar else enemyChar

    Row(
        Modifier.fillMaxSize().background(Color(0xFF14121F)).padding(24.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        Image(
            bitmap = ImageBitmap.imageResource(winner.victory),
            contentDescription = winner.name,
            filterQuality = FilterQuality.None,
            contentScale = ContentScale.Fit,
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .graphicsLayer(scaleX = if (winner.facesRight) 1f else -1f)
        )

        Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                if (playerWon) "¡VICTORIA!" else "DERROTA",
                color = if (playerWon) Color(0xFFFFC107) else Color(0xFFE53935),
                fontSize = 44.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(8.dp))
            Text("Ganó: ${winner.name}", color = Color.White, fontSize = 20.sp)
            Spacer(Modifier.height(24.dp))
            Button(onClick = onRematch) { Text("REVANCHA", fontSize = 18.sp) }
            Spacer(Modifier.height(12.dp))
            Button(onClick = onMenu) { Text("MENÚ PRINCIPAL") }
        }
    }
}