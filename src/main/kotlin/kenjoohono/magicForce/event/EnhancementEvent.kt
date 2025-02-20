package kenjoohono.magicForce.event

import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.Material
import org.bukkit.Sound
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.ClickType
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryDragEvent
import org.bukkit.inventory.ItemStack
import org.json.simple.JSONObject
import org.json.simple.parser.JSONParser
import java.io.File

class EnhancementEvent : Listener {

    @EventHandler
    fun handleInventoryClick(event: InventoryClickEvent) {
        if (event.view.title != "MagicForce") return
        if (event.clickedInventory != event.view.topInventory) return
        if (event.click == ClickType.NUMBER_KEY) {
            event.isCancelled = true
            return
        }

        if (event.slot != 4) {
            event.isCancelled = true
            return
        }

        if (FinalEnhancementManager.finalProcessed) {
            event.isCancelled = true
            return
        }

        val itemInSlot4 = event.currentItem ?: return
        if (itemInSlot4.type == Material.AIR) {
            event.isCancelled = true
            return
        }

        val itemKey = itemInSlot4.type.toString().lowercase()
        if (!onInventoryClick.reinforcementItems.contains(itemKey)) {
            event.isCancelled = true
            return
        }

        val player = event.whoClicked as? Player ?: return
        val currentLevel: Int = onInventoryClick.getEnhanceLevel(itemInSlot4)
        val pluginInstance = Bukkit.getPluginManager().getPlugin("MagicForce") ?: return
        val probabilityFile = File(pluginInstance.dataFolder, "Probability.json")
        if (!probabilityFile.exists()) return
        val parser = JSONParser()
        val probabilityData = parser.parse(probabilityFile.readText()) as JSONObject

        EnhancementLoreManager.updateItemLore(itemInSlot4, currentLevel, probabilityFile)
        player.updateInventory()

        if (currentLevel < 9) {
            if (useTicket(player, "확정강화권", pluginInstance)) {
                applyEnhancementResult("success", itemInSlot4, currentLevel, probabilityData, pluginInstance, player)
                event.isCancelled = true
                return
            } else if (!useTicket(player, "강화권", pluginInstance)) {
                player.sendMessage("${ChatColor.RED}강화권이 없습니다.")
                event.isCancelled = true
                return
            }
        } else {
            if (!useTicket(player, "최종강화권", pluginInstance)) {
                player.sendMessage("${ChatColor.RED}최종강화권이 없습니다.")
                event.isCancelled = true
                return
            }
        }

        if (currentLevel == 9) {
            if (FinalEnhancementManager.sessionActive && player != FinalEnhancementManager.initiatingPlayer) {
                player.sendMessage("${ChatColor.RED}다른 유저가 최종 강화 기도 중입니다.")
                event.isCancelled = true
                return
            }
            event.view.setItem(4, null)
            player.closeInventory()
            FinalEnhancementManager.startSession(
                itemInSlot4,
                (probabilityData["success"] as JSONObject)["10"].toString().toInt(),
                player,
                pluginInstance
            )
            event.isCancelled = true
            return
        }
        if (currentLevel >= 10) {
            event.isCancelled = true
            return
        }
        val nextLevel = currentLevel + 1

        val successJson = probabilityData["success"] as JSONObject
        val declineJson = probabilityData["Decline"] as JSONObject
        val destructionJson = probabilityData["Destruction"] as JSONObject
        val nextLevelStr = nextLevel.toString()
        val successChance = (successJson[nextLevelStr] as Long).toInt()
        val declineChance = (declineJson[nextLevelStr] as Long).toInt()
        val destructionChance = (destructionJson[nextLevelStr] as Long).toInt()

        val roll = (1..100).random()
        val outcome = if (roll <= successChance) {
            "success"
        } else {
            val remainingRoll = roll - successChance
            when {
                remainingRoll <= declineChance -> "decline"
                remainingRoll <= (declineChance + destructionChance) -> "destruction"
                else -> "noChange"
            }
        }
        applyEnhancementResult(outcome, itemInSlot4, currentLevel, probabilityData, pluginInstance, player)
        player.updateInventory()
        event.isCancelled = true
    }

    @EventHandler
    fun handleInventoryDrag(event: InventoryDragEvent) {
        if (event.view.title != "MagicForce") return
        if (!event.rawSlots.contains(4)) return
        val pluginInstance = Bukkit.getPluginManager().getPlugin("MagicForce") ?: return
        val probabilityFile = File(pluginInstance.dataFolder, "Probability.json")
        if (!probabilityFile.exists()) return
        val parser = JSONParser()
        val probabilityData = parser.parse(probabilityFile.readText()) as JSONObject
        val itemInSlot4 = event.view.topInventory.getItem(4) ?: return
        if (itemInSlot4.type == Material.AIR) return
        val currentLevel: Int = onInventoryClick.getEnhanceLevel(itemInSlot4)
        EnhancementLoreManager.updateItemLore(itemInSlot4, currentLevel, probabilityFile)
        (event.whoClicked as? Player)?.updateInventory()
    }

    private fun applyEnhancementResult(
        outcome: String,
        item: ItemStack,
        currentLevel: Int,
        probabilityData: JSONObject,
        pluginInstance: org.bukkit.plugin.Plugin,
        player: Player
    ) {
        when (outcome) {
            "success" -> {
                val nextLevel = currentLevel + 1
                EnhancementLoreManager.updateItemLore(item, nextLevel, File(pluginInstance.dataFolder, "Probability.json"))
                EnhancementAttributeManager.enhanceItem(item, nextLevel, pluginInstance)
                if (nextLevel == 10) {
                    val meta = item.itemMeta
                    meta.isUnbreakable = true
                    item.itemMeta = meta
                    player.playSound(player.location, Sound.BLOCK_BEACON_ACTIVATE, 1.0f, 1.0f)
                } else {
                    player.playSound(player.location, Sound.BLOCK_VAULT_OPEN_SHUTTER, 1.0f, 1.0f)
                }
            }
            "decline" -> {
                val newLevel = if (currentLevel > 0) currentLevel - 1 else 0
                EnhancementLoreManager.updateItemLore(item, newLevel, File(pluginInstance.dataFolder, "Probability.json"))
                EnhancementAttributeManager.enhanceItem(item, newLevel, pluginInstance)
                player.playSound(player.location, Sound.ITEM_MACE_SMASH_AIR, 1.0f, 1.0f)
            }
            "destruction" -> {
                item.type = Material.AIR
                player.playSound(player.location, Sound.ENTITY_ALLAY_DEATH, 1.0f, 1.0f)
            }
            "noChange" -> {
                EnhancementLoreManager.updateItemLore(item, currentLevel, File(pluginInstance.dataFolder, "Probability.json"))
                EnhancementAttributeManager.enhanceItem(item, currentLevel, pluginInstance)
                player.playSound(player.location, Sound.BLOCK_TRIAL_SPAWNER_SPAWN_ITEM, 1.0f, 1.0f)
            }
        }
    }

    private fun useTicket(player: Player, ticketType: String, pluginInstance: org.bukkit.plugin.Plugin): Boolean {
        val ticketFile = File(pluginInstance.dataFolder, "ticket.json")
        if (!ticketFile.exists()) return false
        val ticketJson = JSONParser().parse(ticketFile.readText()) as JSONObject
        val ticketData = ticketJson[ticketType] as? JSONObject ?: return false
        val expectedMaterial = ticketData["material"].toString().uppercase()
        val expectedName = ticketData["name"].toString()
        val expectedLore = (ticketData["lore"] as? org.json.simple.JSONArray)?.map { it.toString() } ?: emptyList<String>()
        for (slot in 0 until player.inventory.size) {
            val item = player.inventory.getItem(slot) ?: continue
            if (item.type.name.equals(expectedMaterial, ignoreCase = true)) {
                val meta = item.itemMeta ?: continue
                if (!meta.hasDisplayName() || meta.displayName != expectedName) continue
                val lore = meta.lore ?: emptyList()
                if (lore == expectedLore) {
                    item.amount -= 1
                    if (item.amount <= 0) {
                        player.inventory.setItem(slot, null)
                    }
                    player.updateInventory()
                    return true
                }
            }
        }
        return false
    }
}