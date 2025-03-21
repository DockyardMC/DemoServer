package io.github.dockyard.demo.items

import io.github.dockyardmc.registry.Items
import io.github.dockyardmc.registry.registries.Item

class PikachusTailItem: GameItem() {

    override fun getItem(): Item {
        return Items.HORN_CORAL
    }

    override fun getRarity(): Rarity {
        return Rarity.EPIC
    }

    override fun getName(): String {
        return "P1k0chu's Tail"
    }

    override fun getDescription(): List<String> {
        return listOf("Has <green>1 in 15<white> chance to strike a", "<yellow>lighting <white>on death, dealing AoE damage", "but <red>+2% Enemy Health")
    }
}