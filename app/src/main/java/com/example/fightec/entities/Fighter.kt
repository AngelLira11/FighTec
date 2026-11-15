package com.example.fightec.entities

enum class FighterState { IDLE, ATTACK, HURT, VICTORY }

data class Fighter(
    val name: String,
    val x: Float,                 // posición horizontal en "unidades de mundo" (0..800)
    val y: Float = 0f,            // altura sobre el suelo (0 = en el piso)
    val vy: Float = 0f,           // velocidad vertical
    val hp: Int = 100,
    val attackTimer: Int = 0,      // frames que le quedan al ataque
    val hurtTimer: Int = 0,        // frames que le quedan en estado herido
    val hitLanded: Boolean = false // evita que un solo golpe pegue varias veces
){
    val isAttacking get() = attackTimer > 0
    val isHurt get() = hurtTimer > 0
}

enum class Winner { PLAYER, ENEMY }