package com.ericlam.mc.achievementquests

import me.clip.placeholderapi.PlaceholderAPI
import org.bukkit.OfflinePlayer

fun OfflinePlayer.stats(node: String): Int? {
    return PlaceholderAPI.setPlaceholders(this, "%$node%").toIntOrNull()
}

fun OfflinePlayer.passStat(nodes: Map<String, Int>): Boolean {
    return nodes.all { (node, pass) -> this.stats(node)?.let { num -> num >= pass } ?: false }
}

val String.withNoStatus
    get() = this.format(*Array(30) { "" })


private val Long.timeUnit: LongArray
    get() {
        var sec = this
        var min: Long = 0
        var hour: Long = 0
        var day: Long = 0
        while (sec / 60 > 0 && sec > 0) {
            min++
            sec -= 60
        }
        while (min / 60 > 0 && min > 0) {
            hour++
            min -= 60
        }
        while (hour / 24 > 0 && hour > 0) {
            day++
            hour -= 24
        }
        return longArrayOf(day, hour, min, sec)
    }


val Long.toTimeUnit: String
    get() {
        var sec = this
        val dayUnit: String = "日"
        val hourUnit: String = "時"
        val minUnit: String = "分"
        val secUnit: String = "秒"
        val split: String = ", "
        val units = sec.timeUnit
        val day = units[0]
        val hour = units[1]
        val min = units[2]
        sec = units[3]
        val builder = StringBuilder()
        if (day > 0) builder.append(day).append(dayUnit)
        if (hour > 0) {
            if (day > 0) builder.append(split)
            builder.append(hour).append(hourUnit)
        }
        if (min > 0) {
            if (hour > 0) builder.append(split)
            builder.append(min).append(minUnit)
        }
        if (sec > 0) {
            if (min > 0) builder.append(split)
            builder.append(sec).append(secUnit)
        }
        return builder.toString()
    }
