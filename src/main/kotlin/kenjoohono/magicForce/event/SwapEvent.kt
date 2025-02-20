package kenjoohono.magicForce.event

import org.bukkit.ChatColor
import org.bukkit.Material
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.ClickType
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.inventory.ItemStack

class SwapEvent : Listener {
    @EventHandler
    fun onInventoryClick(event: InventoryClickEvent) {
        if (event.view.title != "MagicForce") return
        if (event.clickedInventory == event.view.topInventory) return
        if (event.click == ClickType.NUMBER_KEY) {
            event.isCancelled = true
            return
        }
        val clickedItem = event.currentItem ?: return
        if (clickedItem.type == Material.AIR) {
            event.isCancelled = true
            return
        }
        val itemKey = clickedItem.type.toString().lowercase()
        if (!onInventoryClick.reinforcementItems.contains(itemKey)) {
            event.isCancelled = true
            return
        }
        val topInv = event.view.topInventory
        val slot4Item = topInv.getItem(4)
        if (slot4Item == null || slot4Item.type == Material.AIR) {
            onInventoryClick.updateLore(clickedItem)
            topInv.setItem(4, clickedItem)
            event.clickedInventory?.setItem(event.slot, ItemStack(Material.AIR))
        } else {
            val cleanedSlot4Item = removeLore(slot4Item)
            onInventoryClick.updateLore(clickedItem)
            topInv.setItem(4, clickedItem)
            event.clickedInventory?.setItem(event.slot, cleanedSlot4Item)
        }
        event.isCancelled = true
    }

    private fun removeLore(item: ItemStack): ItemStack {
        if (!item.hasItemMeta()) return item
        val meta = item.itemMeta
        val lore = meta.lore ?: return item
        val filteredLore = lore.filter { line ->
            val safeLine = line.orEmpty()
            val stripped = ChatColor.stripColor(safeLine)?.trim() ?: ""
            !(stripped.isBlank() ||
                    stripped.equals("f", ignoreCase = true) ||
                    stripped.startsWith("성공 확률 :", ignoreCase = true) ||
                    stripped.startsWith("실패 시 하락 확률 :", ignoreCase = true) ||
                    stripped.startsWith("실패 시 파괴 확률 :", ignoreCase = true))
        }
        meta.lore = filteredLore
        item.itemMeta = meta
        return item
    }
}