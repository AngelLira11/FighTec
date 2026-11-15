package com.example.fightec.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.fightec.R
import com.example.fightec.entities.Fighter
import com.example.fightec.viewmodels.FightViewModel
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.res.imageResource

@Composable
fun FightArenaScreen(vm: FightViewModel = viewModel()) {
    val player by vm.player.collectAsState()
    val enemy by vm.enemy.collectAsState()

    LaunchedEffect(Unit) { vm.startFight() }

    BoxWithConstraints(Modifier.fillMaxSize()) {
        val unit: Dp = maxWidth / FightViewModel.WORLD_WIDTH   // cuánto mide 1 unidad de mundo

        Image(
            painter = painterResource(R.drawable.biblioteca),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        FighterSprite(player, enemy, R.drawable.lamia_ataque, unit)
        FighterSprite(enemy, player, R.drawable.ulloa_ataque, unit)

        // Controles de prueba
        Row(Modifier.align(Alignment.BottomStart).padding(16.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            HoldButton("◀") { pressed -> vm.setMoveDirection(if (pressed) -1 else 0) }
            HoldButton("▶") { pressed -> vm.setMoveDirection(if (pressed) 1 else 0) }
        }
        HoldButton("⬆", Modifier.align(Alignment.BottomEnd).padding(16.dp)) { pressed -> if (pressed) vm.jump() }
    }
}

@Composable
private fun BoxScope.FighterSprite(fighter: Fighter, opponent: Fighter, sprite: Int, unit: Dp) {
    val isFacingLeft = fighter.x > opponent.x      // el espejo horizontal
    val bitmap = ImageBitmap.imageResource(sprite)
    Image(
        bitmap = bitmap,
        contentDescription = fighter.name,
        filterQuality = FilterQuality.None,        // mantiene el pixel art nítido
        modifier = Modifier
            .align(Alignment.BottomStart)
            .padding(bottom = 90.dp)               // altura del "piso"
            .offset(x = unit * fighter.x, y = -(unit * fighter.y))
            .size(120.dp)
            .graphicsLayer(scaleX = if (isFacingLeft) -1f else 1f)
    )
}

@Composable
private fun HoldButton(label: String, modifier: Modifier = Modifier, onPress: (Boolean) -> Unit) {
    Box(
        modifier = modifier
            .size(64.dp)
            .background(Color.Black.copy(alpha = 0.5f))
            .pointerInput(Unit) {
                detectTapGestures(onPress = {
                    onPress(true)
                    tryAwaitRelease()
                    onPress(false)
                })
            },
        contentAlignment = Alignment.Center
    ) { Text(label, color = Color.White) }
}