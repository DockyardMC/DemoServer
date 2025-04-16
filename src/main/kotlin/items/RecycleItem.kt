package io.github.dockyard.demo.items

import io.github.dockyard.demo.GameInstance
import io.github.dockyardmc.registry.Items
import io.github.dockyardmc.registry.registries.Item
import kotlin.reflect.full.createInstance

class RecycleItem: GameItem() {

    override fun maxCopiesInInventory(): Int {
        return -1
    }

    override fun getShopPrice(): Int {
        return 25
    }

    override fun getItem(): Item {
        return Items.FLOWER_BANNER_PATTERN
    }

    override fun getRarity(): Rarity {
        return Rarity.RARE
    }

    override fun getName(): String {
        return "Recycler"
    }

    override fun getDescription(): List<String> {
        return listOf("Destroy <red>1 random item<white> from your", "inventory but <lime>create another one <white>of", "the same rarity")
    }

    override fun onObtain(gameInstance: GameInstance) {
        val item = gameInstance.controller.items.random()

        gameInstance.controller.items.remove(item)
        item.onDiscarded(gameInstance)

        val rarity = item.getRarity()
        val newItem = ItemRegistry.items.values.shuffled().first { gameItem -> gameItem.createInstance().getRarity() == rarity }
        val newItemInstance = newItem.createInstance()
        gameInstance.controller.items.add(newItemInstance)
        newItemInstance.onObtain(gameInstance)
        gameInstance.player.sendMessage("<green><bold>Recycler <dark_gray>● <gray>Destroyed ${item.getRarity().color}${item.getName()}<gray> and replaced it with ${newItemInstance.getRarity().color}${newItemInstance.getName()}<gray>!")
    }
}