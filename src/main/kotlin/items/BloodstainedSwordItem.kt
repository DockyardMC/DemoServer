package io.github.dockyard.demo.items

import io.github.dockyard.demo.GameInstance
import io.github.dockyardmc.registry.Items
import io.github.dockyardmc.registry.registries.Item

class BloodstainedSwordItem: GameItem() {

    override fun maxCopiesInInventory(): Int {
        return 3
    }

    override fun getShopPrice(): Int {
        return 5
    }

    override fun getItem(): Item {
        return Items.IRON_SWORD
    }

    override fun getRarity(): Rarity {
        return Rarity.UNCOMMON
    }

    override fun getName(): String {
        return "Bloodstained Sword"
    }

    override fun getDescription(): List<String> {
        return listOf("<lime>+1 player damage<white> but <red>+1 monster speed")
    }

    override fun onObtain(gameInstance: GameInstance) {
        gameInstance.playerDamage += 1
        gameInstance.monsterSpeed -= 1
    }
}