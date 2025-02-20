package kenjoohono.magicForce.event

import org.bukkit.ChatColor
import org.bukkit.inventory.ItemStack
import org.json.simple.parser.JSONParser
import java.io.File

object EnhancementLoreManager {
    fun updateItemLore(item: ItemStack, level: Int, probabilityFile: File) {
        if (!probabilityFile.exists()) return
        val parser = JSONParser()
        val probabilityData = parser.parse(probabilityFile.readText()) as org.json.simple.JSONObject

        val newLore = mutableListOf<String>()
        newLore.add(formatStarsLine(level))

        if (level < 10) {
            val nextLevel = level + 1
            val nextLevelStr = nextLevel.toString()
            val successData = probabilityData["success"] as org.json.simple.JSONObject
            val declineData = probabilityData["Decline"] as org.json.simple.JSONObject
            val destructionData = probabilityData["Destruction"] as org.json.simple.JSONObject

            val successChance = (successData[nextLevelStr] as Long).toInt()
            val declineChance = (declineData[nextLevelStr] as Long).toInt()
            val destructionChance = (destructionData[nextLevelStr] as Long).toInt()

            if (level != 0 && successChance != 0) {
                newLore.add(ChatColor.translateAlternateColorCodes('&', "&f"))
            }
            if (successChance != 0) {
                newLore.add(ChatColor.translateAlternateColorCodes('&', "&7성공 확률 : &f &a&l${successChance}%"))
            }
            if (declineChance != 0) {
                newLore.add(ChatColor.translateAlternateColorCodes('&', "&7실패 시 하락 확률 : &c&l${declineChance}%"))
            }
            if (destructionChance != 0) {
                newLore.add(ChatColor.translateAlternateColorCodes('&', "&7실패 시 파괴 확률 : &4&l${destructionChance}%"))
            }
        }
        val meta = item.itemMeta
        meta.lore = newLore
        item.itemMeta = meta
    }

    private fun formatStarsLine(level: Int): String {
        return if (level == 0) {
            ChatColor.translateAlternateColorCodes('&', "&f")
        } else {
            val filledStars = "★".repeat(level)
            val emptyStars = "☆".repeat(10 - level)
            ChatColor.translateAlternateColorCodes('&', "&6$filledStars$emptyStars")
        }
    }
}