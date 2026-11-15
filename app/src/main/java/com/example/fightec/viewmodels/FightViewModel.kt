package com.example.fightec.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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
import kotlin.random.Random


class FightViewModel : ViewModel() {

    companion object {
        const val WORLD_WIDTH = 800f
        const val FIGHTER_WIDTH = 100f
        const val MAX_X = WORLD_WIDTH - FIGHTER_WIDTH
        const val SPEED = 6f
        const val GRAVITY = 1.2f
        const val JUMP_SPEED = 20f

        const val ATTACK_FRAMES = 18
        const val HURT_FRAMES = 20
        const val ATTACK_RANGE = 110f
        const val ATTACK_HEIGHT = 60f
        const val KNOCKBACK = 25f

        const val PLAYER_DAMAGE = 10
        const val ENEMY_DAMAGE = 8        // dificultad: súbelo para una CPU más fuerte
        const val ENEMY_SPEED = 3.5f      // más bajo que el del jugador
        const val ENEMY_COOLDOWN = 50     // frames entre golpes de la CPU (~0.8 s)
        const val ENEMY_JUMP_CHANCE = 0.008f    // probabilidad por frame de saltar al acercarse
        const val ENEMY_DODGE_CHANCE = 0.08f    // probabilidad por frame de esquivar si le atacas
    }

    private val _player = MutableStateFlow(Fighter(name = "Jugador", x = 150f))
    val player = _player.asStateFlow()

    private val _enemy = MutableStateFlow(Fighter(name = "CPU", x = 600f))
    val enemy = _enemy.asStateFlow()

    private val _winner = MutableStateFlow<Winner?>(null)
    val winner = _winner.asStateFlow()

    private var moveDirection = 0
    private var loopJob: Job? = null

    fun setMoveDirection(dir: Int) { moveDirection = dir }

    fun jump() {
        if (_winner.value != null) return
        _player.update { if (it.y == 0f) it.copy(vy = JUMP_SPEED) else it }
    }

    fun punch() {
        if (_winner.value != null) return
        _player.update {
            if (it.isAttacking || it.isHurt) it
            else it.copy(attackTimer = ATTACK_FRAMES, hitLanded = false)
        }
    }

    fun resetFight() {
        _player.value = Fighter(name = "Jugador", x = 150f)
        _enemy.value = Fighter(name = "CPU", x = 600f)
        _winner.value = null
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

        // --- IA de la CPU ---
        var enemyDir = 0
        if (!over && !e.isHurt && !e.isAttacking) {
            val dist = abs(p.x - e.x)
            if (dist > ATTACK_RANGE * 0.8f) {
                enemyDir = if (p.x > e.x) 1 else -1                       // acercarse
                if (e.y == 0f && Random.nextFloat() < ENEMY_JUMP_CHANCE) {
                    e = e.copy(vy = JUMP_SPEED)                            // salto ocasional
                }
            } else if (e.y == 0f && p.isAttacking && Random.nextFloat() < ENEMY_DODGE_CHANCE) {
                e = e.copy(vy = JUMP_SPEED)                                // esquiva saltando
            } else if (e.cooldown == 0) {
                e = e.copy(attackTimer = ATTACK_FRAMES, hitLanded = false, cooldown = ENEMY_COOLDOWN)
            }
        }

        // --- Física y temporizadores ---
        p = tickTimers(applyPhysics(p, if (p.isHurt || over) 0 else moveDirection, SPEED))
        e = tickTimers(applyPhysics(e, enemyDir, ENEMY_SPEED))

        // --- Colisiones (golpe del jugador y golpe de la CPU) ---
        if (!over) {
            val (p1, e1) = landHit(p, e, PLAYER_DAMAGE)
            p = p1; e = e1
            val (e2, p2) = landHit(e, p, ENEMY_DAMAGE)
            e = e2; p = p2

            if (e.hp == 0) _winner.value = Winner.PLAYER
            else if (p.hp == 0) _winner.value = Winner.ENEMY
        }

        _player.value = p
        _enemy.value = e
    }

    /** Devuelve (atacante, defensor) actualizados si el golpe conecta. */
    private fun landHit(attacker: Fighter, defender: Fighter, damage: Int): Pair<Fighter, Fighter> {
        val connects = attacker.isAttacking && !attacker.hitLanded &&
                attacker.attackTimer in 6..12 && inRange(attacker, defender)
        if (!connects) return attacker to defender

        val dir = if (defender.x > attacker.x) 1 else -1
        return attacker.copy(hitLanded = true) to defender.copy(
            hp = (defender.hp - damage).coerceAtLeast(0),
            hurtTimer = HURT_FRAMES,
            attackTimer = 0,                                  // el golpe lo interrumpe
            x = (defender.x + dir * KNOCKBACK).coerceIn(0f, MAX_X)
        )
    }

    private fun inRange(attacker: Fighter, defender: Fighter): Boolean =
        abs(attacker.x - defender.x) < ATTACK_RANGE &&
                abs(attacker.y - defender.y) < ATTACK_HEIGHT

    private fun tickTimers(f: Fighter) = f.copy(
        attackTimer = (f.attackTimer - 1).coerceAtLeast(0),
        hurtTimer = (f.hurtTimer - 1).coerceAtLeast(0),
        cooldown = (f.cooldown - 1).coerceAtLeast(0),
        hitLanded = if (f.attackTimer <= 1) false else f.hitLanded
    )

    private fun applyPhysics(f: Fighter, dir: Int, speed: Float): Fighter {
        var y = f.y + f.vy
        var vy = f.vy - GRAVITY
        if (y <= 0f) { y = 0f; vy = 0f }
        val x = (f.x + dir * speed).coerceIn(0f, MAX_X)
        return f.copy(x = x, y = y, vy = vy)
    }
}