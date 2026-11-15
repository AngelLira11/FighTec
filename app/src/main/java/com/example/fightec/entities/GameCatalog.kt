package com.example.fightec.entities

import androidx.annotation.DrawableRes
import com.example.fightec.R

data class CharacterInfo(
    val id: Int,
    val name: String,
    @DrawableRes val attack: Int,
    @DrawableRes val hurt: Int,
    @DrawableRes val victory: Int,
    val facesRight: Boolean = true      // hacia dónde mira la imagen original
)

data class ArenaInfo(
    val id: Int,
    val name: String,
    @DrawableRes val image: Int
)

object GameCatalog {

    val characters = listOf(
        CharacterInfo(1, "Ing. Lamia Hamda", R.drawable.lamia_ataque, R.drawable.lamia_dano, R.drawable.lamia_win),
        CharacterInfo(2, "Ing. Ulloa", R.drawable.ulloa_ataque, R.drawable.ulloa_dano, R.drawable.ulloa_win, facesRight = false),
        CharacterInfo(3, "Captain QT", R.drawable.oswi_qt_ataque, R.drawable.oswi_qt_dano, R.drawable.oswi_qt_win),
        CharacterInfo(4, "Oswaldo", R.drawable.oswi_ataque, R.drawable.oswi_dano, R.drawable.oswi_win),
        CharacterInfo(5, "Ing. Chespi", R.drawable.chespi_ataque, R.drawable.chespi_dano, R.drawable.chespi_win)


    )

    val arenas = listOf(
        ArenaInfo(1, "Arena 1", R.drawable.ed1_entrada),
        // Descomenta y ajusta el nombre del archivo cuando agregues más arenas:
        // ArenaInfo(2, "Arena 2", R.drawable.arena_2),
        // ArenaInfo(3, "Arena 3", R.drawable.arena_3),
    )

    fun character(id: Int) = characters.first { it.id == id }
    fun arena(id: Int) = arenas.first { it.id == id }
}