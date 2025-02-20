package kenjoohono.magicForce

import kenjoohono.magicForce.command.UpgradeCommand
import kenjoohono.magicForce.command.TicketCommand
import kenjoohono.magicForce.event.onMagicForceGuiClose
import kenjoohono.magicForce.event.EnhancementEvent
import kenjoohono.magicForce.event.SwapEvent
import kenjoohono.magicForce.event.FinalEnhancementManager
import org.bukkit.plugin.java.JavaPlugin

class MagicForce : JavaPlugin() {
    override fun onEnable() {
        FileManager(this).setupFiles()
        getCommand("강화")?.setExecutor(UpgradeCommand())
        getCommand("강화권등록")?.setExecutor(TicketCommand())
        getCommand("확정강화권등록")?.setExecutor(TicketCommand())
        getCommand("최종강화권등록")?.setExecutor(TicketCommand())
        server.pluginManager.registerEvents(EnhancementEvent(), this)
        server.pluginManager.registerEvents(SwapEvent(), this)
        server.pluginManager.registerEvents(onMagicForceGuiClose(), this)
        server.pluginManager.registerEvents(FinalEnhancementManager, this)
    }

    override fun onDisable() {}
}