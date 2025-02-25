package kenjoohono.magicForce.command

import kenjoohono.magicForce.event.EnhancementAttributeManager
import org.bukkit.ChatColor
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.bukkit.plugin.Plugin

class FixedCommand(private val plugin: Plugin) : CommandExecutor {
    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if (sender !is Player) {
            sender.sendMessage("이 명령어는 플레이어만 사용할 수 있습니다.")
            return true
        }
        val item = sender.inventory.itemInMainHand
        if (item == null || !item.hasItemMeta() || !item.itemMeta.hasLore()) {
            sender.sendMessage("강화된 아이템이 아닙니다")
            return true
        }
        val lore = item.itemMeta.lore ?: run {
            sender.sendMessage("강화된 아이템이 아닙니다")
            return true
        }
        val firstLine = ChatColor.stripColor(lore.getOrNull(0) ?: "")
        val upgradeLevel = firstLine!!.takeWhile { it == '★' }.length
        if (upgradeLevel == 0) {
            sender.sendMessage("강화된 아이템이 아닙니다")
            return true
        }
        EnhancementAttributeManager.enhanceItem(item, upgradeLevel, plugin)
        sender.sendMessage("현재 아이템은 ${upgradeLevel} 강화 입니다.")
        return true
    }
}