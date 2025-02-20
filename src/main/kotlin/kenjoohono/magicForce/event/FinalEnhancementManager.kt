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
import org.bukkit.scheduler.BukkitRunnable
import java.io.File
import me.clip.placeholderapi.PlaceholderAPI

object FinalEnhancementManager : Listener {
    var sessionActive = false
    var finalProcessed = false
    var successVotes = 0.0
    var failVotes = 0.0
    var baseSuccessChance = 0
    lateinit var item: ItemStack
    lateinit var plugin: Plugin
    var initiatingPlayer: Player? = null
    private val votedPlayers = mutableSetOf<Player>()
    private val prayerVotesMap = mutableMapOf<Player, Double>()
    private val failVotesMap = mutableMapOf<Player, Double>()
    private val subtitles = listOf(
        "&7★☆☆☆☆☆☆☆☆☆",
        "&7★★☆☆☆☆☆☆☆☆",
        "&7★★★☆☆☆☆☆☆☆",
        "&7★★★★☆☆☆☆☆☆",
        "&7★★★★★☆☆☆☆☆",
        "&7★★★★★★☆☆☆☆",
        "&7★★★★★★★☆☆☆",
        "&7★★★★★★★★☆☆",
        "&7★★★★★★★★★☆"
    )

    fun startSession(item: ItemStack, baseChance: Int, initiatingPlayer: Player, plugin: Plugin) {
        if (sessionActive) return
        finalProcessed = false
        successVotes = 0.0
        failVotes = 0.0
        baseSuccessChance = baseChance
        this.item = item
        this.plugin = plugin
        this.initiatingPlayer = initiatingPlayer
        votedPlayers.clear()
        prayerVotesMap.clear()
        failVotesMap.clear()
        initiatingPlayer.closeInventory()
        Bukkit.broadcastMessage("${ChatColor.GOLD}${getPlaceholderName(initiatingPlayer)} 님의 멋진 기도를 위해 곧 카운트다운을 시작합니다! 준비해주세요!")
        object : BukkitRunnable() {
            var countdown = 5
            override fun run() {
                if (countdown > 0) {
                    Bukkit.broadcastMessage("${ChatColor.AQUA}기도 시작까지 ${countdown}초 남았어요!")
                    Bukkit.getOnlinePlayers().forEach {
                        it.playSound(it.location, Sound.BLOCK_NOTE_BLOCK_HAT, 1.0f, 1.0f)
                    }
                    countdown--
                } else {
                    cancel()
                    Bukkit.broadcastMessage("${ChatColor.GREEN}${getPlaceholderName(initiatingPlayer)} 님의 기도가 시작되었어요! 앞으로 10초간 '기도' 또는 '실패' 를 외쳐서 희망 또는 절망을 보여주세요!!")
                    Bukkit.getOnlinePlayers().forEach {
                        it.playSound(it.location, Sound.BLOCK_BEACON_ACTIVATE, 1.0f, 1.0f)
                    }
                    sessionActive = true
                    Bukkit.getScheduler().runTaskLater(plugin, Runnable { endSession() }, 10 * 20L)
                }
            }
        }.runTaskTimer(plugin, 0L, 20L)
    }

    @EventHandler
    fun onPlayerChat(event: AsyncPlayerChatEvent) {
        val message = event.message
        if (sessionActive) {
            if (message.equals("기도", ignoreCase = true)) {
                event.isCancelled = true
                votedPlayers.add(event.player)
                val broadcastMsg = if (event.player == initiatingPlayer) {
                    ChatColor.translateAlternateColorCodes('&', "&6${getPlaceholderName(event.player)} &7님이 &a기도 &7하고 있습니다.")
                } else {
                    ChatColor.translateAlternateColorCodes('&', "&6${getPlaceholderName(event.player)} &7님이 같이 &a기도 &7하고 있습니다.")
                }
                Bukkit.broadcastMessage(broadcastMsg)
                Bukkit.getOnlinePlayers().forEach {
                    it.playSound(it.location, Sound.BLOCK_NOTE_BLOCK_PLING, 1.0f, 1.0f)
                }
                addVote(event.player, message)
                return
            } else if (message.equals("실패", ignoreCase = true)) {
                event.isCancelled = true
                votedPlayers.add(event.player)
                val broadcastMsg = if (event.player == initiatingPlayer) {
                    ChatColor.translateAlternateColorCodes('&', "&6${getPlaceholderName(event.player)} &7님이 자신의 무기가 &c실패 &7하길 원하고 있습니다.")
                } else {
                    ChatColor.translateAlternateColorCodes('&', "&6${getPlaceholderName(event.player)} &7님이 ${getPlaceholderName(initiatingPlayer)} 님의 무기 강화가 &c실패 &7하길 &7원하고 하고 있습니다.")
                }
                Bukkit.broadcastMessage(broadcastMsg)
                Bukkit.getOnlinePlayers().forEach {
                    it.playSound(it.location, Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f)
                }
                addVote(event.player, message)
                return
            }
        }
    }

    fun addVote(player: Player, message: String) {
        if (!sessionActive) return
        when {
            message.equals("기도", ignoreCase = true) -> {
                successVotes += 0.1
                prayerVotesMap[player] = (prayerVotesMap[player] ?: 0.0) + 0.1
            }
            message.equals("실패", ignoreCase = true) -> {
                failVotes += 0.1
                failVotesMap[player] = (failVotesMap[player] ?: 0.0) + 0.1
            }
        }
    }

    private fun endSession() {
        Bukkit.getOnlinePlayers().forEach {
            it.playSound(it.location, Sound.BLOCK_NOTE_BLOCK_BELL, 1.0f, 1.0f)
        }
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
                    val outcome = (1..100).random() <= clampedChance
                    sendResultTitles(outcome)
                    Bukkit.getScheduler().runTaskLater(plugin, Runnable {
                        Bukkit.broadcastMessage("${ChatColor.GOLD}뜨거운 응원이 끝났어요! ${getPlaceholderName(initiatingPlayer)} 님의 최종 성공 확률은 ${"%.1f".format(clampedChance)}% 입니다!")
                        if (outcome) {
                            Bukkit.broadcastMessage("${ChatColor.GREEN}와우~! 기도가 빛을 발했어요! ${getPlaceholderName(initiatingPlayer)} 님, 축하합니다! 최종 강화 성공입니다!!")
                            EnhancementLoreManager.updateItemLore(item, 10, File(plugin.dataFolder, "Probability.json"))
                            EnhancementAttributeManager.enhanceItem(item, 10, plugin)
                            val meta = item.itemMeta
                            meta.isUnbreakable = true
                            item.itemMeta = meta
                            val finalItem = cleanFinalItem(item)
                            File(plugin.dataFolder, "final_item.json").writeText(finalItem.toString())
                            Bukkit.broadcastMessage("${ChatColor.GREEN}${getPlaceholderName(initiatingPlayer)} 님이 최종 강화에 성공했습니다! 신앙심이 대단하시네요!")
                            playSoundForPlayer(Sound.BLOCK_BEACON_ACTIVATE)
                            initiatingPlayer?.let { giveFinalItemToPlayer(finalItem, it) }
                        } else {
                            Bukkit.broadcastMessage("${ChatColor.RED}허걱스~ 아쉽지만, 기적은 오늘 함께하지 않군요 ㅠㅠ ${getPlaceholderName(initiatingPlayer)} 님의 아이템은 9강 상태로 그대로 유지됩니다.")
                            EnhancementLoreManager.updateItemLore(item, 9, File(plugin.dataFolder, "Probability.json"))
                            EnhancementAttributeManager.enhanceItem(item, 9, plugin)
                            val finalItem = cleanFinalItem(item)
                            Bukkit.broadcastMessage("${ChatColor.RED}걱정 마세요! 다음 번엔 더 큰 행운이 있길 바라욧!")
                            playSoundForPlayer(Sound.ITEM_MACE_SMASH_AIR)
                            initiatingPlayer?.let { giveFinalItemToPlayer(finalItem, it) }
                        }
                        val participants = votedPlayers.map { getPlaceholderName(it) }.joinToString(", ")
                        val topPrayer = prayerVotesMap.maxByOrNull { it.value }
                        val topFailure = failVotesMap.maxByOrNull { it.value }
                        Bukkit.broadcastMessage("${ChatColor.YELLOW}참여한 유저: $participants")
                        topPrayer?.let {
                            Bukkit.broadcastMessage("${ChatColor.YELLOW}가장 열심히 기도한 유저: ${getPlaceholderName(it.key)} (${String.format("%.1f", it.value)} %)")
                        }
                        topFailure?.let {
                            Bukkit.broadcastMessage("${ChatColor.YELLOW}가장 많이 실패를 원한 유저: ${getPlaceholderName(it.key)} (${String.format("%.1f", it.value)} %)")
                        }
                        resetSession()
                    }, subtitles.size * 10L + 60L)
                }
            }
        }.runTaskTimer(plugin, 0L, 20L)
    }

    private fun sendResultTitles(outcome: Boolean) {
        val recipients = mutableSetOf<Player>().apply {
            initiatingPlayer?.let { add(it) }
            addAll(votedPlayers)
        }
        subtitles.forEachIndexed { index, bar ->
            Bukkit.getScheduler().runTaskLater(plugin, Runnable {
                recipients.forEach { player ->
                    player.sendTitle("과연..?", ChatColor.translateAlternateColorCodes('&', bar), 0, 0, 0)
                    player.playSound(player.location, Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.0f)
                }
            }, index * 10L)
        }
        Bukkit.getScheduler().runTaskLater(plugin, Runnable {
            if (outcome) {
                recipients.forEach { player ->
                    player.sendTitle("와우!", ChatColor.translateAlternateColorCodes('&', "&6★★★★★★★★★★"), 0, 0, 0)
                }
            } else {
                recipients.forEach { player ->
                    player.sendTitle("앗..", ChatColor.translateAlternateColorCodes('&', "&c★★★★★★★★★★"), 0, 0, 0)
                }
            }
        }, subtitles.size * 10L + 60L)
    }

    private fun cleanFinalItem(item: ItemStack): ItemStack {
        if (!item.hasItemMeta()) return item
        val meta = item.itemMeta
        val lore = meta.lore ?: return item
        val cleanedLore = lore.filter { line ->
            val stripped = ChatColor.stripColor(line)?.trim() ?: ""
            !(stripped.isEmpty() || stripped.equals("f", ignoreCase = true) || stripped.contains("성공 확률 :", ignoreCase = true) || stripped.contains("실패 시 하락 확률 :", ignoreCase = true) || stripped.contains("실패 시 파괴 확률 :", ignoreCase = true))
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
        votedPlayers.clear()
        prayerVotesMap.clear()
        failVotesMap.clear()
    }

    private fun getPlaceholderName(player: Player?): String {
        if (player == null) return "Unknown"
        val placeholder = PlaceholderAPI.setPlaceholders(player, "%cmi_p_${player.name}_display_name%")
        return if (placeholder.isBlank() || placeholder == "Unknown") player.name else placeholder
    }
}