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
import com.example.fightec.entities.AttackType
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
            painter = painterResource(R.drawable.postgrados),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        FighterSprite(
            player, enemy,
            R.drawable.chespi_ataque, R.drawable.chespi_dano, R.drawable.chespi_win,
            isWinner = winner == Winner.PLAYER, isLoser = winner == Winner.ENEMY, unit = unit
        )
        FighterSprite(
            enemy, player,
            R.drawable.oswi_qt_ataque, R.drawable.oswi_qt_dano, R.drawable.oswi_qt_win,
            isWinner = winner == Winner.ENEMY, isLoser = winner == Winner.PLAYER, unit = unit,
            spriteFacesRight = true
        )

        // Barras de vida
        Row(
            Modifier.align(Alignment.TopCenter).fillMaxWidth().padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            HpBar(player.hp, Modifier.weight(1f))
            HpBar(enemy.hp, Modifier.weight(1f))
        }

        // Controles: izquierda (movimiento)
        Row(
            Modifier.align(Alignment.BottomStart).padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            HoldButton("◀") { pressed -> vm.setMoveDirection(if (pressed) -1 else 0) }
            HoldButton("▶") { pressed -> vm.setMoveDirection(if (pressed) 1 else 0) }
        }

        // Controles: derecha (acciones)
        Row(
            Modifier.align(Alignment.BottomEnd).padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            HoldButton("🛡") { pressed -> vm.setBlocking(pressed) }
            HoldButton("⬆") { pressed -> if (pressed) vm.jump() }
            HoldButton("👊") { pressed -> if (pressed) vm.punch() }
            HoldButton("🦶") { pressed -> if (pressed) vm.kick() }
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
    isWinner: Boolean, isLoser: Boolean, unit: Dp,
    spriteFacesRight: Boolean = true
) {
    val isFacingLeft = fighter.x > opponent.x
    val flip = if (spriteFacesRight) isFacingLeft else !isFacingLeft
    val sprite = when {
        isWinner -> victorySprite
        isLoser || fighter.isHurt -> hurtSprite
        else -> attackSprite
    }
    val lungeSize = if (fighter.attackType == AttackType.KICK) 25f else 15f
    val lunge = if (fighter.isAttacking) (if (isFacingLeft) -lungeSize else lungeSize) else 0f
    val drawX = (fighter.x + lunge).coerceIn(0f, FightViewModel.MAX_X)

    val tint = when {
        fighter.isHurt -> ColorFilter.tint(Color.Red.copy(alpha = 0.4f), BlendMode.SrcAtop)
        fighter.isBlocking -> ColorFilter.tint(Color(0xFF3399FF).copy(alpha = 0.45f), BlendMode.SrcAtop)
        else -> null
    }

    Image(
        bitmap = ImageBitmap.imageResource(sprite),
        contentDescription = fighter.name,
        filterQuality = FilterQuality.None,
        colorFilter = tint,
        modifier = Modifier
            .align(Alignment.BottomStart)
            .padding(bottom = 90.dp)
            .offset(x = unit * drawX, y = -(unit * fighter.y))
            .size(unit * FightViewModel.FIGHTER_WIDTH)
            .graphicsLayer(scaleX = if (flip) -1f else 1f)
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
            .size(60.dp)
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