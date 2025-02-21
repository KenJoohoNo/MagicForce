package kenjoohono.magicForce.event.finsh

import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.Material
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.player.PlayerRespawnEvent
import org.bukkit.plugin.Plugin
import org.bukkit.scheduler.BukkitTask

class NetheriteSword(private val plugin: Plugin) : Listener {

    private val restoreTasks = mutableMapOf<LivingEntity, BukkitTask>()

    @EventHandler
    fun onEntityHit(event: EntityDamageByEntityEvent) {
        val attacker = event.damager as? Player ?: return
        val item = attacker.inventory.itemInMainHand ?: return
        if (item.type != Material.NETHERITE_SWORD) return
        if (!item.hasItemMeta()) return
        val meta = item.itemMeta
        if (!meta.hasLore()) return
        val lore = meta.lore ?: return
        if (lore.any { ChatColor.stripColor(it)?.contains("★★★★★★★★★★") == true }) {
            val target = event.entity as? LivingEntity ?: return
            target.noDamageTicks = 0
            target.maximumNoDamageTicks = 0
            restoreTasks[target]?.cancel()
            val task = Bukkit.getScheduler().runTaskLater(plugin, Runnable {
                target.noDamageTicks = 20
                target.maximumNoDamageTicks = 20
                restoreTasks.remove(target)
            }, 20L)
            restoreTasks[target] = task
        }
    }

    @EventHandler
    fun onPlayerRespawn(event: PlayerRespawnEvent) {
        val player = event.player
        player.noDamageTicks = 20
        player.maximumNoDamageTicks = 20
    }
}