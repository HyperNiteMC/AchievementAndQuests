package com.ericlam.mc.achievementquests.resources

import com.hypernite.mc.hnmc.core.config.yaml.Configuration
import com.hypernite.mc.hnmc.core.config.yaml.Resource
import org.bukkit.Material

@Resource(locate = "timedquests.yml")
data class TimedQuestYml(
        val title: String,
        val rows: Int,
        val items: InterfaceSetUp
) : Configuration() {
    data class InterfaceSetUp(
            val headers: Map<String, HeaderItem>,
            val goals: Map<String, GoalItem>
    ){
        data class HeaderItem(
                val name: String,
                val slot: Int,
                val lore: List<String>,
                val material: Material
        )

        data class GoalItem(
                val name: String,
                val slot: Int,
                val lore: List<String>,
                val timer: Long,
                val target: Map<String, Int>
        )
    }
}