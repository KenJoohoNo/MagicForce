package kenjoohono.magicForce.event

import org.bukkit.attribute.AttributeModifier
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.inventory.EquipmentSlotGroup
import org.bukkit.inventory.ItemStack
import org.bukkit.NamespacedKey
import org.bukkit.plugin.Plugin
import org.json.simple.JSONArray
import org.json.simple.parser.JSONParser
import java.io.File
import java.util.*

object EnhancementAttributeManager {

    val slotGroupMap = mapOf(
        "MAINHAND" to EquipmentSlotGroup.MAINHAND,
        "OFFHAND" to EquipmentSlotGroup.OFFHAND,
        "BODY" to EquipmentSlotGroup.BODY,
        "FEET" to EquipmentSlotGroup.FEET,
        "ARMOR" to EquipmentSlotGroup.ARMOR,
        "CHEST" to EquipmentSlotGroup.CHEST,
        "HAND" to EquipmentSlotGroup.HAND,
        "LEGS" to EquipmentSlotGroup.LEGS,
        "HEAD" to EquipmentSlotGroup.HEAD,
        "ANY" to EquipmentSlotGroup.ANY
    )

    fun enhanceItem(item: ItemStack, level: Int, plugin: Plugin) {
        val itemType = item.type.toString().lowercase()
        val jsonFile = File(plugin.dataFolder, "Reinforce/$itemType.json")
        if (!jsonFile.exists()) return

        val json = JSONParser().parse(jsonFile.readText()) as? org.json.simple.JSONObject ?: return
        val attrArray = json[level.toString()] as? org.json.simple.JSONArray ?: return

        val slotGroupStr = (json["EquipmentSlotGroup"] as? String)?.trim()?.uppercase() ?: "ANY"
        val equipmentSlotGroup = slotGroupMap[slotGroupStr] ?: EquipmentSlotGroup.ANY

        val meta = item.itemMeta
        for (attr in org.bukkit.Registry.ATTRIBUTE) {
            meta.removeAttributeModifier(attr)
        }

        for (entry in attrArray) {
            val parts = entry.toString().split(":")
            if (parts.size < 2) continue
            val keyStr = parts[0].trim().lowercase()
            val value = parts[1].trim().toDoubleOrNull() ?: continue
            try {
                val attribute = org.bukkit.Registry.ATTRIBUTE.getOrThrow(NamespacedKey.minecraft(keyStr))
                val uniqueKey = "$keyStr-${UUID.randomUUID()}"
                val nsKey = NamespacedKey(plugin, uniqueKey)
                val modifier = AttributeModifier(nsKey, value, AttributeModifier.Operation.ADD_NUMBER, equipmentSlotGroup)
                meta.addAttributeModifier(attribute, modifier)
            } catch (e: Exception) {
                return
            }
        }
        item.itemMeta = meta
    }
}