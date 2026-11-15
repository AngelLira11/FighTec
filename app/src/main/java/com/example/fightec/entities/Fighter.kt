package com.example.fightec.entities

enum class FighterState { IDLE, ATTACK, HURT, VICTORY }

data class Fighter(
    val name: String,
    val x: Float,                 // posición horizontal en "unidades de mundo" (0..800)
    val y: Float = 0f,            // altura sobre el suelo (0 = en el piso)
    val vy: Float = 0f,           // velocidad vertical
    val hp: Int = 100,
    val state: FighterState = FighterState.IDLE
)