package kenjoohono.magicForce.gui

import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.Listener
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.ItemMeta

class UpgradeGui : Listener {
    fun open(player: Player) {
        val gui: Inventory = Bukkit.createInventory(null, 9, "MagicForce")
        val pane = ItemStack(Material.BLACK_STAINED_GLASS_PANE)
        val meta: ItemMeta? = pane.itemMeta
        meta?.setDisplayName("${ChatColor.WHITE}강화할 아이템을 클릭해 주세요")
        pane.itemMeta = meta
        for (i in 0..3) {
            gui.setItem(i, pane)
        }
        for (i in 5..8) {
            gui.setItem(i, pane)
        }
        player.openInventory(gui)
    }
}