package kenjoohono.magicForce.event

import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.Sound
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.AsyncPlayerChatEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.plugin.Plugin
import java.io.File
import org.bukkit.scheduler.BukkitRunnable

object FinalEnhancementManager : Listener {
    var sessionActive = false
    var finalProcessed = false
    var successVotes = 0.0
    var failVotes = 0.0
    var baseSuccessChance = 0
    lateinit var item: ItemStack
    lateinit var plugin: Plugin
    var initiatingPlayer: Player? = null

    fun startSession(item: ItemStack, baseChance: Int, initiatingPlayer: Player, plugin: Plugin) {
        if (sessionActive) return

        finalProcessed = false
        successVotes = 0.0
        failVotes = 0.0
        baseSuccessChance = baseChance
        this.item = item
        this.plugin = plugin
        this.initiatingPlayer = initiatingPlayer

        initiatingPlayer.closeInventory()
        Bukkit.broadcastMessage("${ChatColor.GOLD}${initiatingPlayer.name} 님의 멋진 기도를 위해 곧 카운트다운을 시작합니다! 준비해주세요!")

        object : BukkitRunnable() {
            var countdown = 5
            override fun run() {
                if (countdown > 0) {
                    Bukkit.broadcastMessage("${ChatColor.AQUA}기도 시작까지 ${countdown}초 남았어요!")
                    countdown--
                } else {
                    cancel()
                    Bukkit.broadcastMessage("${ChatColor.GREEN}${initiatingPlayer.name} 님의 기도가 시작되었어요! 앞으로 10초간 '기도' 또는 '실패' 를 외쳐서 희망 또는 절망을 보여주세요!!")
                    sessionActive = true
                    Bukkit.getScheduler().runTaskLater(plugin, Runnable { endSession() }, 10 * 20L)
                }
            }
        }.runTaskTimer(plugin, 0L, 20L)
    }

    fun addVote(message: String) {
        if (!sessionActive) return
        when {
            message.equals("기도", ignoreCase = true) -> successVotes += 0.1
            message.equals("실패", ignoreCase = true) -> failVotes += 0.1
        }
        val successPercent = "%.1f".format(successVotes)
        val failPercent = "%.1f".format(failVotes)
        Bukkit.broadcastMessage("${ChatColor.YELLOW}지금까지의 응원 현황~! [기도 응원: ${successPercent}% / 아쉬운 실패: ${failPercent}%]")
    }

    @EventHandler
    fun onPlayerChat(event: AsyncPlayerChatEvent) {
        if (sessionActive) addVote(event.message)
    }

    private fun endSession() {
        initiatingPlayer?.closeInventory()
        sessionActive = false
        finalProcessed = true

        object : BukkitRunnable() {
            var countdown = 5
            override fun run() {
                if (countdown > 0) {
                    Bukkit.broadcastMessage("${ChatColor.AQUA}결과 발표까지 ${countdown}초 남았어요!")
                    countdown--
                } else {
                    cancel()
                    val finalChance = baseSuccessChance + successVotes - failVotes
                    val clampedChance = finalChance.coerceIn(0.0, 100.0)
                    Bukkit.broadcastMessage("${ChatColor.GOLD}뜨거운 응원이 끝났어요! ${initiatingPlayer?.name} 님의 최종 성공 확률은 ${"%.1f".format(clampedChance)}% 입니다!")
                    val outcome = if ((1..100).random() <= clampedChance) "success" else "failure"
                    if (outcome == "success") {
                        Bukkit.broadcastMessage("${ChatColor.GREEN}와우~! 기도가 빛을 발했어요! ${initiatingPlayer?.name} 님, 축하합니다! 최종 강화 성공입니다!!")
                        EnhancementLoreManager.updateItemLore(item, 10, File(plugin.dataFolder, "Probability.json"))
                        EnhancementAttributeManager.enhanceItem(item, 10, plugin)
                        val meta = item.itemMeta
                        meta.isUnbreakable = true
                        item.itemMeta = meta
                        val finalItem = cleanFinalItem(item)
                        File(plugin.dataFolder, "final_item.json").writeText(finalItem.toString())
                        Bukkit.broadcastMessage("${ChatColor.GREEN}${initiatingPlayer?.name} 님이 최종 강화에 성공했습니다! 신앙심이 대단하시네요!")
                        playSoundForPlayer(Sound.BLOCK_BEACON_ACTIVATE)
                        initiatingPlayer?.let { giveFinalItemToPlayer(finalItem, it) }
                    } else {
                        Bukkit.broadcastMessage("${ChatColor.RED}허걱스~ 아쉽지만, 기적은 오늘 함께하지 않군요 ㅠㅠ ${initiatingPlayer?.name} 님의 아이템은 9강 상태로 그대로 유지됩니다.")
                        EnhancementLoreManager.updateItemLore(item, 9, File(plugin.dataFolder, "Probability.json"))
                        EnhancementAttributeManager.enhanceItem(item, 9, plugin)
                        val finalItem = cleanFinalItem(item)
                        Bukkit.broadcastMessage("${ChatColor.RED}걱정 마세요! 다음 번엔 더 큰 행운이 있길 바라욧!")
                        playSoundForPlayer(Sound.ITEM_MACE_SMASH_AIR)
                        initiatingPlayer?.let { giveFinalItemToPlayer(finalItem, it) }
                    }
                    resetSession()
                }
            }
        }.runTaskTimer(plugin, 0L, 20L)
    }

    private fun cleanFinalItem(item: ItemStack): ItemStack {
        if (!item.hasItemMeta()) return item
        val meta = item.itemMeta
        val lore = meta.lore ?: return item
        val cleanedLore = lore.filter { line ->
            val stripped = ChatColor.stripColor(line)?.trim() ?: ""
            !(stripped.isEmpty() ||
                    stripped.equals("f", ignoreCase = true) ||
                    stripped.contains("성공 확률 :", ignoreCase = true) ||
                    stripped.contains("실패 시 하락 확률 :", ignoreCase = true) ||
                    stripped.contains("실패 시 파괴 확률 :", ignoreCase = true))
        }
        meta.lore = cleanedLore
        item.itemMeta = meta
        return item
    }

    private fun giveFinalItemToPlayer(finalItem: ItemStack, player: Player) {
        val leftover = player.inventory.addItem(finalItem)
        if (leftover.isNotEmpty()) {
            leftover.values.forEach { player.world.dropItem(player.location, it) }
        }
    }

    private fun playSoundForPlayer(sound: Sound) {
        initiatingPlayer?.playSound(initiatingPlayer!!.location, sound, 1.0f, 1.0f)
    }

    private fun resetSession() {
        sessionActive = false
        finalProcessed = false
        successVotes = 0.0
        failVotes = 0.0
        baseSuccessChance = 0
        initiatingPlayer = null
    }
}