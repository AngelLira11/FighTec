package com.example.fightec.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.fightec.R
import com.example.fightec.entities.Fighter
import com.example.fightec.entities.Winner
import com.example.fightec.viewmodels.FightViewModel

@Composable
fun FightArenaScreen(vm: FightViewModel = viewModel()) {
    val player by vm.player.collectAsState()
    val enemy by vm.enemy.collectAsState()
    val winner by vm.winner.collectAsState()

    LaunchedEffect(Unit) { vm.startFight() }

    BoxWithConstraints(Modifier.fillMaxSize()) {
        val unit: Dp = maxWidth / FightViewModel.WORLD_WIDTH

        Image(
            painter = painterResource(R.drawable.biblioteca),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        FighterSprite(
            player, enemy,
            R.drawable.lamia_ataque, R.drawable.lamia_dano, R.drawable.lamia_win,
            isWinner = winner == Winner.PLAYER, isLoser = winner == Winner.ENEMY, unit = unit
        )
        FighterSprite(
            enemy, player,
            R.drawable.ulloa_ataque, R.drawable.ulloa_dano, R.drawable.ulloa_win,
            isWinner = winner == Winner.ENEMY, isLoser = winner == Winner.PLAYER, unit = unit
        )

        // Barras de vida
        Row(
            Modifier.align(Alignment.TopCenter).fillMaxWidth().padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            HpBar(player.hp, Modifier.weight(1f))
            HpBar(enemy.hp, Modifier.weight(1f))
        }

        // Controles de prueba
        Row(
            Modifier.align(Alignment.BottomStart).padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            HoldButton("◀") { pressed -> vm.setMoveDirection(if (pressed) -1 else 0) }
            HoldButton("▶") { pressed -> vm.setMoveDirection(if (pressed) 1 else 0) }
        }
        Row(
            Modifier.align(Alignment.BottomEnd).padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            HoldButton("⬆") { pressed -> if (pressed) vm.jump() }
            HoldButton("👊") { pressed -> if (pressed) vm.punch() }
        }

        // Mensaje de fin de pelea
        if (winner != null) {
            Column(
                Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 50.dp)
                    .background(Color.Black.copy(alpha = 0.6f))
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    if (winner == Winner.PLAYER) "¡GANASTE!" else "PERDISTE",
                    color = Color.White,
                    fontSize = 36.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(12.dp))
                Button(onClick = { vm.resetFight() }) { Text("Revancha") }
            }
        }
    }
}

@Composable
private fun BoxScope.FighterSprite(
    fighter: Fighter, opponent: Fighter,
    attackSprite: Int, hurtSprite: Int, victorySprite: Int,
    isWinner: Boolean, isLoser: Boolean, unit: Dp
) {
    val isFacingLeft = fighter.x > opponent.x
    val sprite = when {
        isWinner -> victorySprite
        isLoser || fighter.isHurt -> hurtSprite
        else -> attackSprite
    }
    val lunge = if (fighter.isAttacking) (if (isFacingLeft) -15f else 15f) else 0f
    val drawX = (fighter.x + lunge).coerceIn(0f, FightViewModel.MAX_X)

    Image(
        bitmap = ImageBitmap.imageResource(sprite),
        contentDescription = fighter.name,
        filterQuality = FilterQuality.None,
        colorFilter = if (fighter.isHurt) ColorFilter.tint(Color.Red.copy(alpha = 0.4f), BlendMode.SrcAtop) else null,
        modifier = Modifier
            .align(Alignment.BottomStart)
            .padding(bottom = 90.dp)
            .offset(x = unit * drawX, y = -(unit * fighter.y))
            .size(unit * FightViewModel.FIGHTER_WIDTH)
            .graphicsLayer(scaleX = if (isFacingLeft) -1f else 1f)
    )
}

@Composable
private fun HpBar(hp: Int, modifier: Modifier = Modifier) {
    Box(modifier.height(16.dp).background(Color.DarkGray)) {
        Box(Modifier.fillMaxHeight().fillMaxWidth(hp / 100f).background(Color.Red))
    }
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