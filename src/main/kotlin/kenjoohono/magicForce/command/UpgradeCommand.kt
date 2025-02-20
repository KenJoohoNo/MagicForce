package kenjoohono.magicForce.command

import kenjoohono.magicForce.gui.UpgradeGui
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

class UpgradeCommand : CommandExecutor {

    private val upgradeGui = UpgradeGui()

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if (sender !is Player) return true
        if (command.name.equals("강화", ignoreCase = true)) {
            upgradeGui.open(sender)
        }
        return true
    }
}