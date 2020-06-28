package com.ericlam.mc.achievementquests

import org.bukkit.entity.Player
import org.bukkit.inventory.Inventory
import java.util.*
import java.util.concurrent.ConcurrentHashMap

object UI {

    enum class Type {
        Achievement,
        Quest
    }

    private val map: MutableMap<UUID, PlayerUI> = ConcurrentHashMap()

    data class PlayerUI(
            val achievement: Inventory,
            val timedQuest: Inventory
    )

    private fun createUIFor(player: Player): PlayerUI {
        val ui = PlayerUI(achievement = UIModel.createAchievement(), timedQuest = UIModel.createTimedQuests())
        map[player.uniqueId] = ui
        return ui
    }

    fun getUI(player: Player): PlayerUI {
        return map[player.uniqueId] ?: createUIFor(player)
    }
}