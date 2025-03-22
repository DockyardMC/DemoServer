package io.github.dockyard.demo.items

import io.github.dockyard.demo.GameInstance
import io.github.dockyardmc.registry.Items
import io.github.dockyardmc.registry.registries.Item

class TurtleShellItem: GameItem() {

    override fun maxCopiesInInventory(): Int {
        return 5
    }

    override fun getShopPrice(): Int {
        return 25
    }

    override fun getItem(): Item {
        return Items.TURTLE_HELMET
    }

    override fun getRarity(): Rarity {
        return Rarity.RARE
    }

    override fun getName(): String {
        return "Turtle Shell"
    }

    override fun getDescription(): List<String> {
        return listOf("<green>+5% dodge chance<white> but <red>+1 monster speed")
    }

    override fun onObtain(gameInstance: GameInstance) {
        gameInstance.monsterSpeed -= 1
        gameInstance.playerDodge += 5
    }
}