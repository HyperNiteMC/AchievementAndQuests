package com.ericlam.mc.achievementquests

import com.hypernite.mc.hnmc.core.main.HyperNiteMC
import com.hypernite.mc.kotlinex.format
import kotlinx.coroutines.runBlocking
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

    fun updateAchievementUI(player: Player, inv: Inventory){
        val items = inv.contents.filter { item ->
            val hasNode = item?.itemMeta?.persistentDataContainer?.has(AchievementQuests.plugin.key("achievement.node"), PersistentDataType.STRING) ?: false
            val notAccomplished = item.type != AchievementQuests.configYml.status.accomplished.material
            hasNode && notAccomplished
        }
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

            meta.setDisplayName(AchievementQuests.achievementYml.items.goals[node]?.name?.format(stat.text))
            item.itemMeta = meta
            item.type = stat.material
        }
    }

    fun updateDailyUI(player: Player, inv: Inventory) {
        val items = inv.contents
        for (item in items) {
            val meta = item.itemMeta

        }

    }

    object PlayerData : Table("AAQ_PlayerData") {
        val aid: Column<String> = varchar("achievement_id", 100).primaryKey()
        val timestamp: Column<Long> = long("timestamp")
    }

    object TimedQuest : Table("AAQ_TimedQuests") {
        val aid: Column<String> = varchar("achievement_id", 100).primaryKey()
        val started: Column<Long> = long("started_time")
        val last_finished: Column<Long?> = long("last_finished_time").nullable()
    }

    object OriginalStats : Table("AAQ_TimedQuests_OriginalStats") {
        val aid: Column<String> = varchar("achievement_id", 100).references(TimedQuest.aid)
        val placeholder: Column<String> = varchar("placeholder", 30)
        val stats: Column<Int> = integer("stat")
    }
}