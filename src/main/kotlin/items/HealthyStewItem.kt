package io.github.dockyard.demo.items

import io.github.dockyard.demo.GameInstance
import io.github.dockyardmc.registry.Items
import io.github.dockyardmc.registry.Sounds
import io.github.dockyardmc.registry.registries.Item
import io.github.dockyardmc.sounds.playSound

class HealthyStewItem: GameItem() {

    override fun maxCopiesInInventory(): Int {
        return 7
    }

    override fun getShopPrice(): Int {
        return 15
    }

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

    override fun onObtain(gameInstance: GameInstance) {
        gameInstance.playerMaxHealth.value += 2
        gameInstance.player.health.value += 2
        gameInstance.player.playSound(Sounds.ENTITY_GENERIC_EAT)
        gameInstance.player.playSound(Sounds.ENTITY_VILLAGER_TRADE)
        gameInstance.player.playSound(Sounds.ENTITY_VILLAGER_TRADE)
        gameInstance.player.playSound(Sounds.ENTITY_VILLAGER_TRADE)
        gameInstance.player.playSound(Sounds.ENTITY_GENERIC_DRINK)
    }
}