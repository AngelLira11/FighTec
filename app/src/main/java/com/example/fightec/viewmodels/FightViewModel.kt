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

class FightViewModel : ViewModel() {

    companion object {
        const val WORLD_WIDTH = 800f
        const val FIGHTER_WIDTH = 100f
        const val MAX_X = WORLD_WIDTH - FIGHTER_WIDTH   // límite derecho real
        const val SPEED = 6f
        const val GRAVITY = 1.2f
        const val JUMP_SPEED = 20f

        const val ATTACK_FRAMES = 18
        const val HURT_FRAMES = 20
        const val ATTACK_RANGE = 110f
        const val ATTACK_HEIGHT = 60f
        const val PUNCH_DAMAGE = 10
        const val KNOCKBACK = 25f
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

        p = tickTimers(applyPhysics(p, if (p.isHurt || over) 0 else moveDirection))
        e = tickTimers(applyPhysics(e, 0))

        if (!over && p.isAttacking && !p.hitLanded && p.attackTimer in 6..12 && inRange(p, e)) {
            val dir = if (e.x > p.x) 1 else -1
            p = p.copy(hitLanded = true)
            e = e.copy(
                hp = (e.hp - PUNCH_DAMAGE).coerceAtLeast(0),
                hurtTimer = HURT_FRAMES,
                x = (e.x + dir * KNOCKBACK).coerceIn(0f, MAX_X)
            )
            if (e.hp == 0) _winner.value = Winner.PLAYER
        }

        _player.value = p
        _enemy.value = e
    }

    private fun inRange(attacker: Fighter, defender: Fighter): Boolean =
        abs(attacker.x - defender.x) < ATTACK_RANGE &&
                abs(attacker.y - defender.y) < ATTACK_HEIGHT

    private fun tickTimers(f: Fighter) = f.copy(
        attackTimer = (f.attackTimer - 1).coerceAtLeast(0),
        hurtTimer = (f.hurtTimer - 1).coerceAtLeast(0),
        hitLanded = if (f.attackTimer <= 1) false else f.hitLanded
    )

    private fun applyPhysics(f: Fighter, dir: Int): Fighter {
        var y = f.y + f.vy
        var vy = f.vy - GRAVITY
        if (y <= 0f) { y = 0f; vy = 0f }
        val x = (f.x + dir * SPEED).coerceIn(0f, MAX_X)
        return f.copy(x = x, y = y, vy = vy)
    }
}