package com.ericlam.mc.achievementquests

import com.ericlam.mc.achievementquests.models.AchievementUI
import com.ericlam.mc.achievementquests.models.TimedQuestUI
import com.hypernite.mc.kotlinex.schedule
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.bukkit.OfflinePlayer
import org.bukkit.entity.Player
import org.bukkit.inventory.Inventory
import java.util.*
import java.util.concurrent.ConcurrentHashMap

object UI {

    private val map: MutableMap<UUID, PlayerUI> = ConcurrentHashMap()

    data class PlayerUI(
            val achievement: Inventory,
            val timedQuest: Inventory
    )

    private fun createUIFor(player: Player): PlayerUI {
        val ui = PlayerUI(AchievementUI(), TimedQuestUI())
        map[player.uniqueId] = ui
        return ui
    }

    fun getUI(player: Player): PlayerUI{
        return map[player.uniqueId] ?: createUIFor(player)
    }
}