package com.ericlam.mc.achievementquests

import com.hypernite.mc.hnmc.core.main.HyperNiteMC
import com.hypernite.mc.kotlinex.schedule
import com.hypernite.mc.kotlinex.translateColor
import kotlinx.coroutines.runBlocking
import org.bukkit.OfflinePlayer
import org.bukkit.entity.Player
import org.bukkit.inventory.Inventory
import org.bukkit.persistence.PersistentDataType
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.experimental.suspendedTransactionAsync
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.*

object SQL {

    fun initSQL() {
        Database.connect(HyperNiteMC.getAPI().sqlDataSource.dataSource)
        transaction {
            addLogger(StdOutSqlLogger)
            SchemaUtils.create(PlayerData, TimedQuest)
        }
    }

    fun updateInventory(player: Player, inv: Inventory){
        val items = inv.contents.filter { item -> item?.itemMeta?.persistentDataContainer?.has(AchievementQuests.plugin.key("achievement.node"), PersistentDataType.STRING) ?: false }
        for (item in items) {
            val meta = item.itemMeta
            val node = meta.persistentDataContainer.get(AchievementQuests.plugin.key("achievement.node"), PersistentDataType.STRING)!!
            val passStat = AchievementQuests.achievementYml.items.goals[node]?.target?.let { player.passStat(it) } ?: let {
                player.sendMessage("$node is invalid achievement")
                false
            }
            val stat = runBlocking {
                val finished = suspendedTransactionAsync {
                    PlayerData.select { PlayerData.aid eq "${player.uniqueId}_$node" }.any()
                }.await()

                when{
                    finished -> AchievementQuests.configYml.status.accomplished
                    !finished && passStat -> AchievementQuests.configYml.status.available
                    !finished && !passStat -> AchievementQuests.configYml.status.unavailable
                    else -> null
                }
            } ?: continue

            meta.setDisplayName(stat.text.translateColor())
            item.itemMeta = meta
            item.type = stat.material
        }
    }

    object PlayerData : Table("AAQ_PlayerData") {
        val aid: Column<String> = varchar("achievement_id", 100).primaryKey()
        val timestamp: Column<Long> = long("timestamp")
    }

    object TimedQuest : Table("AAQ_TimedQuests") {
        val uuid: Column<UUID> = uuid("uuid").primaryKey()
        val node: Column<String> = varchar("achievement_node", 70)
        val originalStats: Column<Int> = integer("o_stats")
        val started: Column<Long> = long("started_time")
    }
}