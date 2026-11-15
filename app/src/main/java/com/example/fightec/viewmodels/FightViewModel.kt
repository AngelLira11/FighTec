package com.example.fightec.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fightec.entities.Fighter
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class FightViewModel : ViewModel(){
    companion object {
        const val WORLD_WIDTH = 800f
        const val SPEED = 6f
        const val GRAVITY = 1.2f
        const val JUMP_SPEED = 20f

}

    private val _player = MutableStateFlow(Fighter(name = "Jugador", x = 150f))
    val player = _player.asStateFlow()

    private val _enemy = MutableStateFlow(Fighter(name = "CPU", x = 600f))
    val enemy = _enemy.asStateFlow()

    private var moveDirection = 0      // -1 izquierda, 0 quieto, 1 derecha
    private var loopJob: Job? = null

    fun setMoveDirection(dir: Int) { moveDirection = dir }

    fun jump() {
        _player.update { if (it.y == 0f) it.copy(vy = JUMP_SPEED) else it }
    }

    fun startFight() {
        if (loopJob?.isActive == true) return
        loopJob = viewModelScope.launch {
            while (isActive) {
                tick()
                delay(16L)             // ~60 FPS
            }
        }
    }

    private fun tick() {
        _player.update { applyPhysics(it, moveDirection) }
        _enemy.update { applyPhysics(it, 0) }
    }

    private fun applyPhysics(f: Fighter, dir: Int): Fighter {
        var y = f.y + f.vy
        var vy = f.vy - GRAVITY
        if (y <= 0f) { y = 0f; vy = 0f }
        val x = (f.x + dir * SPEED).coerceIn(0f, WORLD_WIDTH)
        return f.copy(x = x, y = y, vy = vy)
    }
}