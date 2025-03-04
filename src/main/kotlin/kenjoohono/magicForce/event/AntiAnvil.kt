package kenjoohono.magicForce.event

import org.bukkit.ChatColor
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryType

class AntiAnvil : Listener {
    @EventHandler
    fun onAnvilClick(event: InventoryClickEvent) {
        if (event.inventory.type != InventoryType.ANVIL) return
        val item = event.currentItem ?: return
        if (!item.hasItemMeta()) return
        val meta = item.itemMeta
        val lore = meta.lore ?: return
        if (lore.any { ChatColor.stripColor(it)?.contains("★") == true }) {
            event.isCancelled = true
            event.whoClicked.sendMessage("${ChatColor.RED}강화된 아이템은 수정할 수 없습니다.")
        }
    }
}