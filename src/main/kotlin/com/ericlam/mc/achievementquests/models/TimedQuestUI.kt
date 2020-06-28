package com.ericlam.mc.achievementquests.models

import com.ericlam.mc.achievementquests.AchievementQuests
import com.hypernite.mc.kotlinex.dsl.inventory
import com.hypernite.mc.kotlinex.dsl.item
import com.hypernite.mc.kotlinex.translateColor
import org.bukkit.inventory.Inventory
import org.bukkit.persistence.PersistentDataType

class TimedQuestUI : Inventory by with(AchievementQuests.timedQuestYml, {
    val t = title
    inventory(rows) {

        title = t

        items.headers.map { (_, header) ->
            slot(header.slot) {

                item(header.material) {

                    name(header.name.translateColor())

                    lore {
                        header.lore.forEach { line -> -(line.translateColor()) }
                    }

                }
            }
        }

        items.goals.map { (node, goal) ->
            slot(goal.slot) {

                item(AchievementQuests.configYml.status.unavailable.material) {

                    name(goal.name.translateColor())

                    lore {
                        goal.lore.forEach { line -> -(line.translateColor()) }
                    }

                }.apply {
                    itemMeta.persistentDataContainer.set(AchievementQuests.plugin.key("achievement.node"), PersistentDataType.STRING, node)
                }

            }
        }

    }
})