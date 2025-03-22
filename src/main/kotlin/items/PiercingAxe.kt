package io.github.dockyard.demo.items

import io.github.dockyard.demo.GameInstance
import io.github.dockyardmc.entity.Entity
import io.github.dockyardmc.player.Player
import io.github.dockyardmc.registry.DamageTypes
import io.github.dockyardmc.registry.Items
import io.github.dockyardmc.registry.registries.Item
import io.github.dockyardmc.utils.percentOf

class PiercingAxe: GameItem() {

    override fun maxCopiesInInventory(): Int {
        return 1
    }

    override fun getShopPrice(): Int {
        return 25
    }

    override fun getItem(): Item {
        return Items.GOLDEN_AXE
    }

    override fun getRarity(): Rarity {
        return Rarity.RARE
    }

    override fun getName(): String {
        return "Piercing Axe"
    }

    override fun getDescription(): List<String> {
        return listOf("Every 10th attack will deal <orange>2x<white> damage but <red>+5% enemy health<white>")
    }

    override fun onObtain(gameInstance: GameInstance) {
        val current = gameInstance.monsterHealthMultiplier.toDouble()
        val percent = (percentOf(5f, current).toFloat() / 100f) + 1f
        gameInstance.player.sendMessage("<red>buffed enemy health by $percent")
        gameInstance.monsterHealthMultiplier += percent
    }

    private var hit = 0
    override fun onMonsterDamage(monster: Entity, gameInstance: GameInstance, damage: Float, attacker: Player?) {
        if(hit % 10 == 0) {
            monster.damage(damage, DamageTypes.GENERIC, null)
        } else {
            hit++
            attacker?.sendMessage("<orange>2x damage from piercing axe")
        }
    }
}