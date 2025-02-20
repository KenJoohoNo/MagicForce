package kenjoohono.magicForce.event

import org.bukkit.ChatColor
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryCloseEvent
import org.bukkit.inventory.ItemStack

class onMagicForceGuiClose : Listener {
    @EventHandler
    fun onInventoryClose(event: InventoryCloseEvent) {
        if (event.view.title != "MagicForce") return

        val player = event.player as? Player ?: return
        val gui = event.inventory
        val item = gui.getItem(4)

        if (item != null && item.type != Material.AIR) {
            if (item.hasItemMeta()) {
                val meta = item.itemMeta
                val lore = meta.lore
                if (!lore.isNullOrEmpty()) {
                    val filteredLore = lore.filter { line ->
                        val safeLine = line.orEmpty()
                        val stripped = ChatColor.stripColor(safeLine)?.trim() ?: ""
                        !(stripped.isEmpty() ||
                                stripped.equals("f", ignoreCase = true) ||
                                stripped.contains("성공 확률 :", ignoreCase = true) ||
                                stripped.contains("실패 시 하락 확률 :", ignoreCase = true) ||
                                stripped.contains("실패 시 파괴 확률 :", ignoreCase = true))
                    }
                    meta.lore = filteredLore
                    item.itemMeta = meta
                }
            }
            val leftover: Map<Int, ItemStack> = player.inventory.addItem(item)
            if (leftover.isNotEmpty()) {
                leftover.values.forEach { player.world.dropItem(player.location, it) }
            }
            gui.setItem(4, null)
        }
    }
}