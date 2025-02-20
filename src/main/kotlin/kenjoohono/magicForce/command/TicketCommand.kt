package kenjoohono.magicForce.command

import org.bukkit.ChatColor
import org.bukkit.Material
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.json.simple.JSONObject
import org.json.simple.parser.JSONParser
import java.io.File
import java.io.FileReader
import java.io.FileWriter

class TicketCommand : CommandExecutor {
    private val ticketFilePath = "plugins/MagicForce/ticket.json"

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if (sender !is Player) {
            sender.sendMessage("플레이어만 이 명령어를 사용할 수 있습니다.")
            return true
        }
        if (!sender.isOp) {
            sender.sendMessage("${ChatColor.RED}권한이 없습니다")
            return true
        }

        val heldItem = sender.inventory.itemInMainHand
        if (heldItem.type == Material.AIR) {
            sender.sendMessage("${ChatColor.RED}등록할 아이템을 손에 들어주세요")
            return true
        }

        val ticketKey = resolveTicketKey(label) ?: run {
            sender.sendMessage("${ChatColor.RED}알 수 없는 명령어")
            return true
        }

        val ticketData = buildTicketData(heldItem)
        val ticketJson = readTicketJson(ticketFilePath)
        ticketJson[ticketKey] = ticketData
        writeTicketJson(ticketJson, ticketFilePath)

        sender.sendMessage("${ChatColor.GREEN}$ticketKey 등록 완료")
        return true
    }

    private fun resolveTicketKey(label: String): String? = when (label) {
        "강화권등록" -> "강화권"
        "확정강화권등록" -> "확정강화권"
        "최종강화권등록" -> "최종강화권"
        else -> null
    }

    private fun buildTicketData(item: org.bukkit.inventory.ItemStack): JSONObject {
        val itemName = if (item.hasItemMeta() && item.itemMeta.hasDisplayName())
            item.itemMeta.displayName
        else
            item.type.toString()

        val loreList = if (item.hasItemMeta() && item.itemMeta.hasLore())
            item.itemMeta.lore
        else
            listOf<String>()

        return JSONObject().apply {
            put("name", itemName)
            put("material", item.type.toString())
            put("lore", loreList)
        }
    }

    private fun readTicketJson(path: String): JSONObject {
        val file = File(path)
        return if (file.exists()) {
            FileReader(file).use { reader ->
                JSONParser().parse(reader) as JSONObject
            }
        } else {
            JSONObject()
        }
    }

    private fun writeTicketJson(json: JSONObject, path: String) {
        FileWriter(File(path)).use { writer ->
            writer.write(json.toJSONString())
        }
    }
}