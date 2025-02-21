package kenjoohono.magicForce.event.finsh

import org.bukkit.ChatColor
import org.bukkit.Material
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.plugin.Plugin

class Mace(private val plugin: Plugin) : Listener {
    @EventHandler
    fun onEntityHit(event: EntityDamageByEntityEvent) {
        val attacker = event.damager as? Player ?: return
        val item = attacker.inventory.itemInMainHand ?: return
        if (item.type != Material.MACE) return
        if (!item.hasItemMeta()) return
        val meta = item.itemMeta
        if (!meta.hasLore()) return
        val lore = meta.lore ?: return
        if (lore.any { ChatColor.stripColor(it)?.contains("★★★★★★★★★★") == true }) {
            val target = event.entity as? LivingEntity ?: return
            val additionalDamage = target.maxHealth * 0.1
            event.damage += additionalDamage
        }
    }
}