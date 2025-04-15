package io.github.dockyard.demo

import io.github.dockyard.demo.items.GameItem
import io.github.dockyard.demo.items.ItemRegistry
import io.github.dockyard.demo.shop.ShopItem
import io.github.dockyardmc.entity.Entity
import io.github.dockyardmc.entity.EntityManager.despawnEntity
import io.github.dockyardmc.entity.EntityManager.spawnEntity
import io.github.dockyardmc.entity.Interaction
import io.github.dockyardmc.entity.ItemDisplay
import io.github.dockyardmc.entity.TextDisplay
import io.github.dockyardmc.registry.Items
import kotlin.reflect.full.primaryConstructor

class Shop(val instance: GameInstance) {

    var itemsInStock = 0
    val shopItems = mutableListOf<ShopItem>()

    val cleanupEntities = mutableListOf<Entity>()

    fun spawn() {
        if (instance.state.value == GameInstance.State.SHOP_ACTIVE) return
        instance.state.value = GameInstance.State.SHOP_ACTIVE

        val items = mutableListOf<GameItem>()
        for (i in 0 until 3) {
            items.add(ItemRegistry.items.random().primaryConstructor!!.call())
        }

        spawnContinueButton()

        var i = 0
        itemsInStock = 3
        items.shuffled().forEach { item ->
            i++
            val offsetSize = item.getDescription().size * 0.3

            val location = instance.map.getPoint("shop_$i").location
            val world = location.world

            val shopItem = ShopItem(location, item, instance)
            shopItems.add(shopItem)
            shopItem.spawn()
        }
    }

    private fun spawnContinueButton() {
        val location = instance.map.getPoint("spawn").location.add(0, 1, 0)
        val continueItemDisplay = instance.world.spawnEntity(ItemDisplay(location, location.world)) as ItemDisplay
        continueItemDisplay.item.value = Items.LIME_DYE.toItemStack()

        val continueTextDisplay = instance.world.spawnEntity(TextDisplay(location.add(0.0, 0.5, 0.0))) as TextDisplay
        continueTextDisplay.text.value = "${Colors.LIME}<b>Continue"

        val continueInteraction = instance.world.spawnEntity(Interaction(location)) as Interaction
        continueInteraction.width.value = 2f
        continueInteraction.height.value = 3f

        cleanupEntities.add(continueInteraction)
        cleanupEntities.add(continueItemDisplay)
        cleanupEntities.add(continueTextDisplay)

        continueInteraction.rightClickDispatcher.subscribe { player ->
            if (!continueInteraction.responsive.value) return@subscribe
            continueInteraction.responsive.value = false
            despawn()
        }
    }

    fun despawn() {
        shopItems.forEach { shopItem -> shopItem.despawn() }

        cleanupEntities.forEach { entity ->
            instance.world.despawnEntity(entity)
        }

        instance.controller.startWave()
    }
}