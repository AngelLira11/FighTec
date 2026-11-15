package com.example.fightec.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fightec.entities.AttackType
import com.example.fightec.entities.Fighter
import com.example.fightec.entities.Winner
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.roundToInt
import kotlin.random.Random

class FightViewModel : ViewModel() {

    companion object {
        const val WORLD_WIDTH = 800f
        const val FIGHTER_WIDTH = 100f
        const val MAX_X = WORLD_WIDTH - FIGHTER_WIDTH
        const val SPEED = 6f
        const val GRAVITY = 1.2f
        const val JUMP_SPEED = 20f

        const val HURT_FRAMES = 20
        const val ATTACK_HEIGHT = 60f

        // Bloqueo
        const val BLOCK_DAMAGE_FACTOR = 0.2f   // el bloqueo deja pasar el 20% del daño
        const val BLOCK_KNOCKBACK = 8f

        // Dificultad de la CPU
        const val ENEMY_DAMAGE_MULT = 0.8f
        const val ENEMY_SPEED = 3.5f
        const val ENEMY_COOLDOWN = 50
        const val ENEMY_KICK_CHANCE = 0.3f
        const val ENEMY_JUMP_CHANCE = 0.008f
        const val ENEMY_DODGE_CHANCE = 0.08f
        const val ENEMY_BLOCK_CHANCE = 0.06f
        const val ENEMY_BLOCK_FRAMES = 30
    }

    private val _player = MutableStateFlow(Fighter(name = "Jugador", x = 150f))
    val player = _player.asStateFlow()

    private val _enemy = MutableStateFlow(Fighter(name = "CPU", x = 600f))
    val enemy = _enemy.asStateFlow()

    private val _winner = MutableStateFlow<Winner?>(null)
    val winner = _winner.asStateFlow()

    private var moveDirection = 0
    private var blockHeld = false
    private var enemyBlockFrames = 0
    private var loopJob: Job? = null

    fun setMoveDirection(dir: Int) { moveDirection = dir }
    fun setBlocking(pressed: Boolean) { blockHeld = pressed }

    fun jump() {
        if (_winner.value != null) return
        _player.update { if (it.y == 0f && !it.isBlocking) it.copy(vy = JUMP_SPEED) else it }
    }

    fun punch() = playerAttack(AttackType.PUNCH)
    fun kick() = playerAttack(AttackType.KICK)

    private fun playerAttack(type: AttackType) {
        if (_winner.value != null) return
        _player.update {
            if (it.isAttacking || it.isHurt || blockHeld) it else it.startAttack(type)
        }
    }

    private fun Fighter.startAttack(type: AttackType) =
        copy(attackType = type, attackTimer = type.frames, hitLanded = false)

    fun resetFight() {
        _player.value = Fighter(name = "Jugador", x = 150f)
        _enemy.value = Fighter(name = "CPU", x = 600f)
        _winner.value = null
        enemyBlockFrames = 0
    }

    fun startFight() {
        if (loopJob?.isActive == true) return
        loopJob = viewModelScope.launch {
            while (isActive) {
                tick()
                delay(16L)
            }
        }
    }

    private fun tick() {
        var p = _player.value
        var e = _enemy.value
        val over = _winner.value != null

        // --- Estado de bloqueo del jugador ---
        p = p.copy(isBlocking = blockHeld && !over && !p.isAttacking && !p.isHurt && p.y == 0f)

        // --- IA de la CPU ---
        if (enemyBlockFrames > 0) enemyBlockFrames--
        e = e.copy(isBlocking = !over && enemyBlockFrames > 0 && !e.isHurt && !e.isAttacking)

        var enemyDir = 0
        if (!over && !e.isHurt && !e.isAttacking && !e.isBlocking) {
            val dist = abs(p.x - e.x)
            if (dist > AttackType.PUNCH.range * 0.8f) {
                enemyDir = if (p.x > e.x) 1 else -1
                if (e.y == 0f && Random.nextFloat() < ENEMY_JUMP_CHANCE) {
                    e = e.copy(vy = JUMP_SPEED)
                }
            } else {
                val r = Random.nextFloat()
                if (e.y == 0f && p.isAttacking && r < ENEMY_DODGE_CHANCE) {
                    e = e.copy(vy = JUMP_SPEED)                               // esquiva saltando
                } else if (e.y == 0f && p.isAttacking && r < ENEMY_DODGE_CHANCE + ENEMY_BLOCK_CHANCE) {
                    enemyBlockFrames = ENEMY_BLOCK_FRAMES                     // se cubre
                } else if (e.cooldown == 0) {
                    val type = if (Random.nextFloat() < ENEMY_KICK_CHANCE) AttackType.KICK else AttackType.PUNCH
                    val extraWait = if (type == AttackType.KICK) 20 else 0
                    e = e.startAttack(type).copy(cooldown = ENEMY_COOLDOWN + extraWait)
                }
            }
        }

        // --- Física y temporizadores ---
        val playerDir = if (p.isHurt || p.isBlocking || over) 0 else moveDirection
        p = tickTimers(applyPhysics(p, playerDir, SPEED))
        e = tickTimers(applyPhysics(e, enemyDir, ENEMY_SPEED))

        // --- Colisiones ---
        if (!over) {
            val (p1, e1) = landHit(p, e, 1f)
            p = p1; e = e1
            val (e2, p2) = landHit(e, p, ENEMY_DAMAGE_MULT)
            e = e2; p = p2

            if (e.hp == 0) _winner.value = Winner.PLAYER
            else if (p.hp == 0) _winner.value = Winner.ENEMY
        }

        _player.value = p
        _enemy.value = e
    }

    /** Devuelve (atacante, defensor) actualizados si el golpe conecta. */
    private fun landHit(attacker: Fighter, defender: Fighter, damageMult: Float): Pair<Fighter, Fighter> {
        val type = attacker.attackType ?: return attacker to defender
        val connects = attacker.isAttacking && !attacker.hitLanded &&
                attacker.attackTimer in type.hitFrom..type.hitTo &&
                abs(attacker.x - defender.x) < type.range &&
                abs(attacker.y - defender.y) < ATTACK_HEIGHT
        if (!connects) return attacker to defender

        val dir = if (defender.x > attacker.x) 1 else -1
        val fullDamage = type.damage * damageMult

        return if (defender.isBlocking) {
            // Bloqueado: daño reducido, empuje pequeño, sin estado herido
            val damage = (fullDamage * BLOCK_DAMAGE_FACTOR).roundToInt().coerceAtLeast(1)
            attacker.copy(hitLanded = true) to defender.copy(
                hp = (defender.hp - damage).coerceAtLeast(0),
                x = (defender.x + dir * BLOCK_KNOCKBACK).coerceIn(0f, MAX_X)
            )
        } else {
            attacker.copy(hitLanded = true) to defender.copy(
                hp = (defender.hp - fullDamage.roundToInt()).coerceAtLeast(0),
                hurtTimer = HURT_FRAMES,
                attackTimer = 0,                       // el golpe interrumpe su ataque
                attackType = null,
                x = (defender.x + dir * type.knockback).coerceIn(0f, MAX_X)
            )
        }
    }

    private fun tickTimers(f: Fighter): Fighter {
        val newAttackTimer = (f.attackTimer - 1).coerceAtLeast(0)
        return f.copy(
            attackTimer = newAttackTimer,
            attackType = if (newAttackTimer == 0) null else f.attackType,
            hurtTimer = (f.hurtTimer - 1).coerceAtLeast(0),
            cooldown = (f.cooldown - 1).coerceAtLeast(0),
            hitLanded = if (newAttackTimer == 0) false else f.hitLanded
        )
    }

    private fun applyPhysics(f: Fighter, dir: Int, speed: Float): Fighter {
        var y = f.y + f.vy
        var vy = f.vy - GRAVITY
        if (y <= 0f) { y = 0f; vy = 0f }
        val x = (f.x + dir * speed).coerceIn(0f, MAX_X)
        return f.copy(x = x, y = y, vy = vy)
    }
}