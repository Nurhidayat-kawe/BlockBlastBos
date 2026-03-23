package com.softimagines.blockblastbos.data.model

enum class GameMode {
    CLASSIC,
    ZEN,
    TIME_ATTACK,
    PUZZLE,
    BOSS_BATTLE
}

data class LevelData(
    val id: Int,
    val targetScore: Int,
    val initialGrid: List<Position> = emptyList(),
    val description: String = ""
)

object LevelProvider {
    val levels = listOf(
        LevelData(1, 500, description = "Mudah: Capai 500 skor"),
        LevelData(2, 1200, initialGrid = listOf(Position(3,3), Position(3,4), Position(4,3), Position(4,4)), description = "Ada rintangan di tengah"),
        LevelData(3, 2500, description = "Tantangan: 2500 skor"),
        LevelData(4, 5000, initialGrid = (0..7).map { Position(it, it) }, description = "Diagonal blocked!"),
        LevelData(5, 10000, description = "Grand Master")
    )
}
