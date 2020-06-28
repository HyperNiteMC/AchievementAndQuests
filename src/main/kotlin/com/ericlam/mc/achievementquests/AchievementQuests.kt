package com.ericlam.mc.achievementquests

import com.ericlam.mc.achievementquests.resources.AchievementYml
import com.ericlam.mc.achievementquests.resources.ConfigYml
import com.ericlam.mc.achievementquests.resources.LangYml
import com.ericlam.mc.achievementquests.resources.TimedQuestYml
import com.hypernite.mc.hnmc.core.main.HyperNiteMC
import com.hypernite.mc.kotlinex.dsl.command
import com.hypernite.mc.kotlinex.dsl.listener
import com.hypernite.mc.kotlinex.forKotlin
import com.hypernite.mc.kotlinex.schedule
import org.bukkit.NamespacedKey
import org.bukkit.entity.Player
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.plugin.java.JavaPlugin

class AchievementQuests : JavaPlugin() {

    companion object {
        lateinit var configYml: ConfigYml
        lateinit var achievementYml: AchievementYml
        lateinit var timedQuestYml: TimedQuestYml
        lateinit var langYml: LangYml
        lateinit var plugin: AchievementQuests
    }

    fun key(string: String): NamespacedKey {
        return NamespacedKey(this, string)
    }

    override fun onEnable() {
        plugin = this

        val manager = HyperNiteMC.getAPI().factory.getConfigFactory(this).forKotlin
                .register(ConfigYml::class.java)
                .register(AchievementYml::class.java)
                .register(TimedQuestYml::class.java)
                .register(LangYml::class.java)
                .dump()

        configYml = manager.getConfigAs(ConfigYml::class.java)
        achievementYml = manager.getConfigAs(AchievementYml::class.java)
        timedQuestYml = manager.getConfigAs(TimedQuestYml::class.java)
        langYml = manager.getConfigAs(LangYml::class.java)

        listener(this) {
            listen<PlayerJoinEvent> {
                val ui = UI.getUI(player)
                schedule {
                    SQL.updateInventory(player, ui.achievement, UI.Type.Achievement)
                    SQL.updateInventory(player, ui.timedQuest, UI.Type.Quest)
                }
            }
        }

        val achieveUICommand = command("achieve") {

            description("打開成就界面")

            alias {
                -"achievement"
                -"accomplishment"
            }

            execute {
                val player = sender as? Player ?: let {
                    sender.sendMessage("not player")
                    return@execute true
                }
                player.openInventory(UI.getUI(player).achievement)
                true
            }
        }

        val questUICommand = command("quests") {

            description("打開限時任務界面")

            alias {
                -"timedquests"
                -"quest"
            }

            execute {
                val player = sender as? Player ?: let {
                    sender.sendMessage("not player")
                    return@execute true
                }
                player.openInventory(UI.getUI(player).timedQuest)
                true
            }
        }

        HyperNiteMC.getAPI().commandRegister.registerCommand(this, achieveUICommand)
        HyperNiteMC.getAPI().commandRegister.registerCommand(this, questUICommand)
    }
}