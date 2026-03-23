package com.softimagines.blockblastbos.data.model

data class Achievement(
    val id: String,
    val title: String,
    val description: String,
    val targetValue: Int,
    var currentValue: Int = 0,
    var isUnlocked: Boolean = false
)

object AchievementProvider {
    val allAchievements = listOf(
        Achievement("score_1000", "Pemain Pemula", "Capai skor 1.000", 1000),
        Achievement("score_5000", "Ahli Blok", "Capai skor 5.000", 5000),
        Achievement("combo_5", "Raja Combo", "Capai Combo x5", 5),
        Achievement("perfect_clear", "Papan Bersih", "Lakukan Perfect Clear", 1),
        Achievement("revive_master", "Kesempatan Kedua", "Gunakan Revive 5 kali", 5)
    )
}
