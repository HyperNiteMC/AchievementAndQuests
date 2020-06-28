package com.ericlam.mc.achievementquests

import me.clip.placeholderapi.PlaceholderAPI
import org.bukkit.OfflinePlayer

fun OfflinePlayer.stats(node: String): Int? {
    return PlaceholderAPI.setPlaceholders(this, node).toIntOrNull()
}

fun OfflinePlayer.passStat(nodes: Map<String, Int>): Boolean {
    return nodes.all { (node, pass) -> PlaceholderAPI.setPlaceholders(this, node).toIntOrNull()?.let { num -> num >= pass } ?: false }

}