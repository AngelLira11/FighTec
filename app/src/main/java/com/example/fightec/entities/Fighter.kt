package com.example.fightec.entities

/** Cada ataque define su propia duración, ventana de impacto, daño, alcance y empuje. */
enum class AttackType(
    val frames: Int,       // duración total
    val hitFrom: Int,      // el golpe conecta cuando attackTimer está entre hitFrom y hitTo
    val hitTo: Int,
    val damage: Int,
    val range: Float,
    val knockback: Float
) {
    PUNCH(frames = 18, hitFrom = 6, hitTo = 12, damage = 10, range = 110f, knockback = 25f),
    KICK(frames = 28, hitFrom = 10, hitTo = 18, damage = 18, range = 130f, knockback = 45f)
}

data class Fighter(
    val name: String,
    val x: Float,
    val y: Float = 0f,
    val vy: Float = 0f,
    val hp: Int = 100,
    val attackTimer: Int = 0,
    val attackType: AttackType? = null,
    val hurtTimer: Int = 0,
    val cooldown: Int = 0,
    val hitLanded: Boolean = false,
    val isBlocking: Boolean = false
) {
    val isAttacking get() = attackTimer > 0
    val isHurt get() = hurtTimer > 0
}

enum class Winner { PLAYER, ENEMY }