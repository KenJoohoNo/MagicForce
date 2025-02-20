package kenjoohono.magicForce.event

import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.inventory.ItemStack
import org.json.simple.JSONObject
import org.json.simple.parser.JSONParser
import java.io.File

open class onInventoryClick {
    companion object {
        val reinforcementItems: Set<String> = run {
            val items = mutableSetOf<String>()
            val plugin = Bukkit.getPluginManager().getPlugin("MagicForce")
            if (plugin != null) {
                val reinforceFolder = File(plugin.dataFolder, "Reinforce")
                if (reinforceFolder.exists() && reinforceFolder.isDirectory) {
                    reinforceFolder.listFiles { file -> file.extension.lowercase() == "json" }?.forEach {
                        items.add(it.nameWithoutExtension.lowercase())
                    }
                }
            }
            items
        }

        fun getEnhanceLevel(item: ItemStack): Int {
            val meta = item.itemMeta ?: return 0
            if (meta.hasLore()) {
                val loreLine = meta.lore?.get(0) ?: ""
                val plainLore = ChatColor.stripColor(loreLine) ?: ""
                return plainLore.count { it == '★' }
            }
            return 0
        }

        fun updateLore(item: ItemStack) {
            val plugin = Bukkit.getPluginManager().getPlugin("MagicForce") ?: return
            val probFile = File(plugin.dataFolder, "Probability.json")
            if (!probFile.exists()) return

            val parser = JSONParser()
            val obj = parser.parse(probFile.readText()) as JSONObject

            val currentLevel = getEnhanceLevel(item)
            val nextAttempt = if (currentLevel < 10) currentLevel + 1 else 10
            val nextAttemptStr = nextAttempt.toString()

            val successJson = obj["success"] as JSONObject
            val declineJson = obj["Decline"] as JSONObject
            val destructionJson = obj["Destruction"] as JSONObject

            val successChance = (successJson[nextAttemptStr] as Long).toInt()
            val declineChance = (declineJson[nextAttemptStr] as Long).toInt()
            val destructionChance = (destructionJson[nextAttemptStr] as Long).toInt()

            val loreList = mutableListOf<String>()
            loreList.add(getStarsLine(currentLevel))

            if (currentLevel < 10) {
                if (currentLevel != 0 && successChance != 0) {
                    loreList.add(ChatColor.translateAlternateColorCodes('&', "&f"))
                }
                if (successChance != 0) {
                    loreList.add(ChatColor.translateAlternateColorCodes('&', "&7성공 확률 : &f &a&l${successChance}%"))
                }
                if (declineChance != 0) {
                    loreList.add(ChatColor.translateAlternateColorCodes('&', "&7실패 시 하락 확률 : &c&l${declineChance}%"))
                }
                if (destructionChance != 0) {
                    loreList.add(ChatColor.translateAlternateColorCodes('&', "&7실패 시 파괴 확률 : &4&l${destructionChance}%"))
                }
            }
            val meta = item.itemMeta
            meta.lore = loreList
            item.itemMeta = meta
        }

        fun getStarsLine(level: Int): String {
            return if (level == 0) {
                ChatColor.translateAlternateColorCodes('&', "&f")
            } else {
                val filled = "★".repeat(level)
                val empty = "☆".repeat(10 - level)
                ChatColor.translateAlternateColorCodes('&', "&6$filled$empty")
            }
        }
    }
}