package com.example.fightec.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
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
import com.example.fightec.entities.ArenaInfo
import com.example.fightec.entities.AttackType
import com.example.fightec.entities.CharacterInfo
import com.example.fightec.entities.Fighter
import com.example.fightec.entities.Winner
import com.example.fightec.viewmodels.FightViewModel
import kotlinx.coroutines.delay

@Composable
fun FightArenaScreen(
    playerChar: CharacterInfo,
    enemyChar: CharacterInfo,
    arena: ArenaInfo,
    onFightEnd: (playerWon: Boolean) -> Unit,
    vm: FightViewModel = viewModel()
) {
    val player by vm.player.collectAsState()
    val enemy by vm.enemy.collectAsState()
    val winner by vm.winner.collectAsState()
    val shake by vm.shake.collectAsState()

    LaunchedEffect(Unit) { vm.startFight() }

    // Al terminar la pelea, deja ver la pose de victoria 2.5 s y pasa a la pantalla de resultado
    LaunchedEffect(winner) {
        if (winner != null) {
            delay(2500)
            onFightEnd(winner == Winner.PLAYER)
        }
    }

    BoxWithConstraints(Modifier.fillMaxSize()) {
        val unit: Dp = maxWidth / FightViewModel.WORLD_WIDTH

        // Escena (arena + luchadores): es lo que se sacude con el especial
        val shakeOffset = if (shake > 0) (if (shake % 2 == 0) 10f else -10f) else 0f
        Box(
            Modifier
                .fillMaxSize()
                .graphicsLayer(translationX = shakeOffset, translationY = shakeOffset / 2)
        ) {
            Image(
                painter = painterResource(arena.image),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )

            FighterSprite(
                player, enemy, playerChar,
                isWinner = winner == Winner.PLAYER, isLoser = winner == Winner.ENEMY, unit = unit
            )
            FighterSprite(
                enemy, player, enemyChar,
                isWinner = winner == Winner.ENEMY, isLoser = winner == Winner.PLAYER, unit = unit
            )
        }

        // Barras de vida y energía
        Row(
            Modifier.align(Alignment.TopCenter).fillMaxWidth().padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                StatBar(player.hp, Color.Red, 16.dp)
                StatBar(player.energy, Color(0xFFFFC107), 8.dp)
            }
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                StatBar(enemy.hp, Color.Red, 16.dp)
                StatBar(enemy.energy, Color(0xFFFFC107), 8.dp)
            }
        }

        // Controles: izquierda (movimiento), un poco más hacia la derecha
        Row(
            Modifier
                .align(Alignment.BottomStart)
                .padding(start = 56.dp, top = 16.dp, end = 16.dp, bottom = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            HoldButton("◀") { pressed -> vm.setMoveDirection(if (pressed) -1 else 0) }
            HoldButton("▶") { pressed -> vm.setMoveDirection(if (pressed) 1 else 0) }
        }

        // Controles: derecha (acciones)
        Row(
            Modifier.align(Alignment.BottomEnd).padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            HoldButton("🛡") { pressed -> vm.setBlocking(pressed) }
            HoldButton("⬆") { pressed -> if (pressed) vm.jump() }
            HoldButton("👊") { pressed -> if (pressed) vm.punch() }
            HoldButton("🦶") { pressed -> if (pressed) vm.kick() }
            HoldButton(
                "⭐",
                background = if (player.energy >= FightViewModel.MAX_ENERGY)
                    Color(0xFFFFC107).copy(alpha = 0.9f) else Color.Black.copy(alpha = 0.5f)
            ) { pressed -> if (pressed) vm.special() }
        }

        // Mensaje de fin de pelea
        if (winner != null) {
            Text(
                if (winner == Winner.PLAYER) "¡GANASTE!" else "PERDISTE",
                color = Color.White,
                fontSize = 44.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 60.dp)
                    .background(Color.Black.copy(alpha = 0.6f))
                    .padding(horizontal = 32.dp, vertical = 12.dp)
            )
        }
    }
}

@Composable
private fun BoxScope.FighterSprite(
    fighter: Fighter, opponent: Fighter, character: CharacterInfo,
    isWinner: Boolean, isLoser: Boolean, unit: Dp
) {
    val isFacingLeft = fighter.x > opponent.x
    val flip = if (character.facesRight) isFacingLeft else !isFacingLeft
    val sprite = when {
        isWinner -> character.victory
        isLoser || fighter.isHurt -> character.hurt
        else -> character.attack
    }
    val lungeSize = when (fighter.attackType) {
        AttackType.SPECIAL -> 40f
        AttackType.KICK -> 25f
        else -> 15f
    }
    val lunge = if (fighter.isAttacking) (if (isFacingLeft) -lungeSize else lungeSize) else 0f
    val drawX = (fighter.x + lunge).coerceIn(0f, FightViewModel.MAX_X)

    val tint = when {
        fighter.isHurt -> ColorFilter.tint(Color.Red.copy(alpha = 0.4f), BlendMode.SrcAtop)
        fighter.isBlocking -> ColorFilter.tint(Color(0xFF3399FF).copy(alpha = 0.45f), BlendMode.SrcAtop)
        fighter.attackType == AttackType.SPECIAL ->
            ColorFilter.tint(Color(0xFFFFC107).copy(alpha = 0.4f), BlendMode.SrcAtop)
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
private fun StatBar(value: Int, color: Color, height: Dp, modifier: Modifier = Modifier) {
    Box(modifier.fillMaxWidth().height(height).background(Color.DarkGray)) {
        Box(Modifier.fillMaxHeight().fillMaxWidth(value / 100f).background(color))
    }
}

@Composable
private fun HoldButton(
    label: String,
    modifier: Modifier = Modifier,
    background: Color = Color.Black.copy(alpha = 0.5f),
    onPress: (Boolean) -> Unit
) {
    Box(
        modifier = modifier
            .size(72.dp)
            .background(background)
            .pointerInput(Unit) {
                detectTapGestures(onPress = {
                    onPress(true)
                    tryAwaitRelease()
                    onPress(false)
                })
            },
        contentAlignment = Alignment.Center
    ) { Text(label, color = Color.White, fontSize = 26.sp) }
}