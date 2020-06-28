package com.ericlam.mc.achievementquests.resources

import com.hypernite.mc.hnmc.core.config.yaml.Configuration
import com.hypernite.mc.hnmc.core.config.yaml.Resource
import org.bukkit.Material

@Resource(locate = "config.yml")
data class ConfigYml(
        val status: Status
) : Configuration() {
    data class Status(
            val unavailable: Stat,
            val available: Stat,
            val accomplished: Stat
    ){
        data class Stat(
                val material: Material,
                val text: String
        )
    }
}