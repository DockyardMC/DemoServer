package io.github.dockyard.demo.items

import io.github.dockyard.demo.GameInstance
import io.github.dockyardmc.registry.Items
import io.github.dockyardmc.registry.registries.Item

class RuneOfCritRate : GameItem() {

    override fun maxCopiesInInventory(): Int {
        return 15
    }

    override fun getShopPrice(): Int {
        return 10
    }

    override fun getItem(): Item {
        return Items.RAISER_ARMOR_TRIM_SMITHING_TEMPLATE
    }

    override fun getRarity(): Rarity {
        return Rarity.RARE
    }

    override fun getName(): String {
        return "Rune of Uruz"
    }

    override fun onObtain(gameInstance: GameInstance) {
        gameInstance.playerCritDamage.percentage -= 3
        gameInstance.playerCritRate.percentage += 3
    }

    override fun onDiscarded(gameInstance: GameInstance) {
        gameInstance.playerCritDamage.percentage += 3
        gameInstance.playerCritRate.percentage -= 3
    }

    override fun getDescription(): List<String> {
        return listOf("Crit Rate <lime>+3%<white> but Crit Damage <red>-3%")
    }
}