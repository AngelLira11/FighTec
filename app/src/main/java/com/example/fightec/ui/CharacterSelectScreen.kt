package com.example.fightec.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.fightec.entities.CharacterInfo
import com.example.fightec.entities.GameCatalog

@Composable
fun CharacterSelectScreen(
    onFight: (playerId: Int, arenaId: Int) -> Unit,
    onBack: () -> Unit
) {
    var selectedId by remember { mutableIntStateOf(GameCatalog.characters.first().id) }
    var arenaIndex by remember { mutableIntStateOf(0) }
    val arenas = GameCatalog.arenas
    val arena = arenas[arenaIndex]

    Column(
        Modifier.fillMaxSize().background(Color(0xFF14121F)).padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("ELIGE TU LUCHADOR", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(8.dp))

        Row(
            Modifier.weight(1f).fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            GameCatalog.characters.forEach { c ->
                CharacterCard(
                    character = c,
                    selected = c.id == selectedId,
                    modifier = Modifier.weight(1f).fillMaxHeight()
                ) { selectedId = c.id }
            }
        }

        Spacer(Modifier.height(8.dp))

        Row(
            Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Button(onClick = onBack) { Text("Atrás") }

            Spacer(Modifier.weight(1f))

            if (arenas.size > 1) {
                TextButton(onClick = { arenaIndex = (arenaIndex - 1 + arenas.size) % arenas.size }) {
                    Text("◀", color = Color.White, fontSize = 20.sp)
                }
            }
            Image(
                painter = painterResource(arena.image),
                contentDescription = arena.name,
                contentScale = ContentScale.Crop,
                modifier = Modifier.width(96.dp).height(54.dp)
            )
            Text(arena.name, color = Color.White, fontSize = 16.sp)
            if (arenas.size > 1) {
                TextButton(onClick = { arenaIndex = (arenaIndex + 1) % arenas.size }) {
                    Text("▶", color = Color.White, fontSize = 20.sp)
                }
            }

            Spacer(Modifier.weight(1f))

            Button(onClick = { onFight(selectedId, arena.id) }) {
                Text("¡PELEAR!", fontSize = 18.sp)
            }
        }
    }
}

@Composable
private fun CharacterCard(
    character: CharacterInfo,
    selected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Column(
        modifier
            .background(Color.White.copy(alpha = if (selected) 0.18f else 0.07f))
            .border(3.dp, if (selected) Color(0xFFFFC107) else Color.Transparent)
            .clickable(onClick = onClick)
            .padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            bitmap = ImageBitmap.imageResource(character.attack),
            contentDescription = character.name,
            filterQuality = FilterQuality.None,
            contentScale = ContentScale.Fit,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .graphicsLayer(scaleX = if (character.facesRight) 1f else -1f)
        )
        Text(character.name, color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
    }
}