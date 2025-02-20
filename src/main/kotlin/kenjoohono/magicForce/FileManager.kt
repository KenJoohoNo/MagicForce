package kenjoohono.magicForce

import org.bukkit.plugin.java.JavaPlugin
import java.io.File
import java.io.IOException

class FileManager(private val plugin: JavaPlugin) {
    fun setupFiles() {
        val dataFolder: File = plugin.dataFolder
        if (!dataFolder.exists()) {
            dataFolder.mkdirs()
        }
        val reinforceFolder = File(dataFolder, "Reinforce")
        if (!reinforceFolder.exists()) {
            reinforceFolder.mkdirs()
        }
        val ticketFile = File(dataFolder, "Ticket.json")
        if (!ticketFile.exists()) {
            try {
                ticketFile.createNewFile()
                ticketFile.writeText("{}")
            } catch (e: IOException) {
                plugin.logger.severe("Ticket.json 파일 생성 실패: ${e.message}")
            }
        }
        val probabilityFile = File(dataFolder, "Probability.json")
        if (!probabilityFile.exists()) {
            try {
                probabilityFile.createNewFile()
                probabilityFile.writeText(
                    """{
    "success": {
        "1": 100,
        "2": 90,
        "3": 80,
        "4": 70,
        "5": 60,
        "6": 50,
        "7": 40,
        "8": 30,
        "9": 20,
        "10": 10
    },
    "Decline": {
        "1": 0,
        "2": 10,
        "3": 20,
        "4": 30,
        "5": 40,
        "6": 50,
        "7": 50,
        "8": 50,
        "9": 50,
        "10": 50
    },
    "Destruction": {
        "1": 0,
        "2": 0,
        "3": 0,
        "4": 0,
        "5": 0,
        "6": 0,
        "7": 10,
        "8": 20,
        "9": 30,
        "10": 40
    }
}""".trimIndent()
                )
            } catch (e: IOException) {
                plugin.logger.severe("Probability.json 파일 생성 실패: ${e.message}")
            }
        }
    }
}