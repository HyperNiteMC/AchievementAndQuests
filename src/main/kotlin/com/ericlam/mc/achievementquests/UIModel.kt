package com.ericlam.mc.achievementquests

import com.hypernite.mc.kotlinex.dsl.inventory
import com.hypernite.mc.kotlinex.dsl.item
import com.hypernite.mc.kotlinex.format
import com.hypernite.mc.kotlinex.insertOrUpdate
import com.hypernite.mc.kotlinex.translateColor
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.bukkit.entity.Player
import org.bukkit.inventory.Inventory
import org.bukkit.persistence.PersistentDataType
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.jetbrains.exposed.sql.transactions.experimental.suspendedTransactionAsync
import org.jetbrains.exposed.sql.transactions.transaction

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
                                when(m){
                                    AchievementQuests.configYml.status.accomplished.material -> {
                                        e.whoClicked.sendMessage(AchievementQuests.langYml["accomplished"])
                                        return@click
                                    }
                                    AchievementQuests.configYml.status.available.material -> runBlocking {
                                        val result = suspendedTransactionAsync {
                                            SQL.PlayerData.insertIgnore{
                                                it[aid] = "${e.whoClicked.uniqueId}_$node"
                                                it[timestamp] = System.currentTimeMillis()
                                            }
                                        }.await()

                                        val stat =  AchievementQuests.configYml.status.accomplished
                                        item.type = stat.material

                                        if (result.isIgnore){
                                            e.whoClicked.sendMessage(AchievementQuests.langYml["accomplished"])
                                        }else{
                                            val meta = item.itemMeta
                                            meta.setDisplayName(AchievementQuests.achievementYml.items.goals[node]?.name?.format(stat.text))
                                            item.itemMeta = meta
                                            e.whoClicked.sendMessage(AchievementQuests.langYml["finished"].format(goal.name.withNoStatus))
                                        }
                                    }
                                    AchievementQuests.configYml.status.unavailable.material -> e.whoClicked.sendMessage(AchievementQuests.langYml["condition-not-pass"])
                                    else -> return@click
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

                        event {

                            click {
                                e ->
                                e.isCancelled = true
                                val item = e.currentItem ?: return@click
                                val player = e.whoClicked as Player
                                val achievementId = "${player.uniqueId}_$node"
                                when(item.type){
                                    AchievementQuests.configYml.status.accomplished.material -> {
                                        e.whoClicked.sendMessage(AchievementQuests.langYml["accomplished"])
                                        return@click
                                    }
                                    AchievementQuests.configYml.status.unaccepted.material -> {
                                        GlobalScope.launch {
                                            suspendedTransactionAsync {
                                                SQL.TimedQuest.insertOrUpdate {
                                                    it[aid] = achievementId
                                                    it[started] = System.currentTimeMillis()
                                                    it[last_finished] = null
                                                }

                                                SQL.OriginalStats.batchInsert(goal.target.keys) { stat ->
                                                    this[SQL.OriginalStats.aid] = achievementId
                                                    this[SQL.OriginalStats.placeholder] = stat
                                                    this[SQL.OriginalStats.stats] = player.stats(stat)
                                                            ?: throw IllegalStateException("placeholder $stat for ${player.name} is null or not an integer.")
                                                }
                                            }.await()
                                        }.invokeOnCompletion {
                                            val stat = AchievementQuests.configYml.status.unavailable
                                            item.type = stat.material
                                            val meta = item.itemMeta
                                            meta.setDisplayName(AchievementQuests.achievementYml.items.goals[node]?.name?.format(stat.text))
                                            item.itemMeta = meta
                                            player.sendMessage(AchievementQuests.langYml["accepted"].format(goal.name.withNoStatus, goal.timer))
                                        }
                                    }
                                    AchievementQuests.configYml.status.available.material -> {
                                        val finishedTime = System.currentTimeMillis()
                                        GlobalScope.launch {
                                            suspendedTransactionAsync {
                                                SQL.TimedQuest.update({ SQL.TimedQuest.aid eq achievementId }) {
                                                    it[last_finished] = finishedTime
                                                }
                                                SQL.OriginalStats.deleteWhere { SQL.OriginalStats.aid eq achievementId }
                                            }.await()
                                        }.invokeOnCompletion {
                                            val stat = AchievementQuests.configYml.status.accomplished
                                            item.type = stat.material
                                            val meta = item.itemMeta
                                            meta.setDisplayName(AchievementQuests.achievementYml.items.goals[node]?.name?.format(stat.text))
                                            meta.persistentDataContainer.set(AchievementQuests.plugin.key("achievement.finished"), PersistentDataType.LONG, finishedTime)
                                            item.itemMeta = meta
                                            player.sendMessage(AchievementQuests.langYml["finished"].format(goal.name.withNoStatus))
                                        }
                                    }
                                    AchievementQuests.configYml.status.cooling.material -> {
                                        val finished = item.itemMeta.persistentDataContainer.get(AchievementQuests.plugin.key("achievement.finished"), PersistentDataType.LONG) ?: let {
                                            val stat = AchievementQuests.configYml.status.unavailable
                                            item.type = stat.material
                                            val meta = item.itemMeta
                                            meta.setDisplayName(AchievementQuests.achievementYml.items.goals[node]?.name?.format(stat.text))
                                            item.itemMeta = meta
                                            return@click
                                        }
                                        val remain = System.currentTimeMillis() - finished
                                        player.sendMessage(AchievementQuests.langYml["cooling"].format(remain.toTimeUnit))
                                    }
                                    AchievementQuests.configYml.status.unavailable.material -> e.whoClicked.sendMessage(AchievementQuests.langYml["condition-not-pass"])
                                    else -> return@click
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
}