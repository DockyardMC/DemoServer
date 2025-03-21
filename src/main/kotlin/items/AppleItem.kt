package io.github.dockyard.demo.items

import io.github.dockyard.demo.GameInstance
import io.github.dockyardmc.registry.Items
import io.github.dockyardmc.registry.registries.Item

class AppleItem : GameItem() {

    override fun getItem(): Item {
        return Items.APPLE
    }

    override fun getRarity(): Rarity {
        return Rarity.UNCOMMON
    }

    override fun getName(): String {
        return "Hearthy Apple"
    }

    override fun getDescription(): List<String> {
        return listOf("Restores <red>2❤ <white>of your health")
    }

    override val discardsAfterUse: Boolean = true

    override fun onObtain(gameInstance: GameInstance) {
        gameInstance.player.health.value += 4
    }
}