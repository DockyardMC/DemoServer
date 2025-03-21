package io.github.dockyard.demo.items

import io.github.dockyardmc.registry.Items
import io.github.dockyardmc.registry.registries.Item

class HealthyStewItem: GameItem() {

    override fun getItem(): Item {
        return Items.MUSHROOM_STEW
    }

    override fun getRarity(): Rarity {
        return Rarity.RARE
    }

    override fun getName(): String {
        return "Healthy Stew"
    }

    override fun getDescription(): List<String> {
        return listOf("Increase max health by <red>1❤", "<white>and heal <red>1❤")
    }
}