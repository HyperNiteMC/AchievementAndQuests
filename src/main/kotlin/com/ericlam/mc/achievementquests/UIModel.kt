package com.ericlam.mc.achievementquests

import com.hypernite.mc.kotlinex.dsl.inventory
import com.hypernite.mc.kotlinex.dsl.item
import com.hypernite.mc.kotlinex.format
import com.hypernite.mc.kotlinex.translateColor
import kotlinx.coroutines.runBlocking
import org.bukkit.inventory.Inventory
import org.bukkit.persistence.PersistentDataType
import org.jetbrains.exposed.sql.insertIgnore
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction

object UIModel {
    fun createAchievement(): Inventory = with(AchievementQuests.achievementYml){
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

                        event {
                            click {
                                it.isCancelled = true
                            }
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

                        event {

                            click {
                                e ->
                                e.isCancelled = true
                                val item = e.currentItem ?: return@click
                                val m = item.type
                                if (m == AchievementQuests.configYml.status.available.material){
                                    runBlocking {
                                        val result = newSuspendedTransaction {
                                            SQL.PlayerData.insertIgnore{
                                                it[aid] = "${e.whoClicked.uniqueId}_$node"
                                                it[timestamp] = System.currentTimeMillis()
                                            }
                                        }

                                        val stat =  AchievementQuests.configYml.status.accomplished
                                        item.type = stat.material

                                        if (result.isIgnore){
                                            e.whoClicked.sendMessage(AchievementQuests.langYml["accomplished"])
                                        }else{
                                            val meta = item.itemMeta
                                            meta.setDisplayName(AchievementQuests.achievementYml.items.goals[node]?.name?.format(stat.text))
                                            item.itemMeta = meta
                                        }
                                    }
                                }else{
                                    e.whoClicked.sendMessage(AchievementQuests.langYml["condition-not-pass"])
                                }
                            }

                        }

                    }.apply {
                        itemMeta.persistentDataContainer.set(AchievementQuests.plugin.key("achievement.node"), PersistentDataType.STRING, node)
                    }

                }
            }

        }
    }

    fun createTimedQuests(): Inventory = with(AchievementQuests.timedQuestYml){
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
    }
}