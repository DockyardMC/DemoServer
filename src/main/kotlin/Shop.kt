package io.github.dockyard.demo

import io.github.dockyard.demo.items.GameItem
import io.github.dockyard.demo.items.ItemRegistry
import io.github.dockyardmc.apis.hologram
import io.github.dockyardmc.entity.*
import io.github.dockyardmc.entity.EntityManager.despawnEntity
import io.github.dockyardmc.entity.EntityManager.spawnEntity
import io.github.dockyardmc.particles.spawnParticle
import io.github.dockyardmc.player.Player
import io.github.dockyardmc.registry.Blocks
import io.github.dockyardmc.registry.Items
import io.github.dockyardmc.registry.Particles
import io.github.dockyardmc.registry.Sounds
import io.github.dockyardmc.runnables.ticks
import io.github.dockyardmc.scheduler.runLaterAsync
import io.github.dockyardmc.sounds.playSound
import io.github.dockyardmc.utils.vectors.Vector3f
import kotlin.reflect.full.primaryConstructor

class Shop(val instance: GameInstance) {

    var itemsInStock = 0
    val cleanupEntities = mutableListOf<Entity>()

    fun spawn() {
        instance.state.value = GameInstance.State.SHOP_ACTIVE

        val items = mutableListOf<GameItem>()
        for (i in 0 until 3) {
            items.add(ItemRegistry.items.random().primaryConstructor!!.call())
        }


        var i = 0
        itemsInStock = 3
        items.shuffled().forEach { item ->
            i++
            val offsetSize = item.getDescription().size * 0.3

            val location = instance.map.getPoint("shop_$i").location
            val world = location.world
            location.setBlock(Blocks.CHEST.withBlockStates("facing" to "north"))
            world.playSound(Sounds.BLOCK_WOOD_PLACE, location)
            world.scheduler.runLater(10.ticks) {
                world.players.forEach { player -> player.playChestAnimation(location, Player.ChestAnimation.OPEN) }
                world.playSound(Sounds.BLOCK_CHEST_OPEN, location)
            }

            spawnContinueButton()

            world.scheduler.runLater(20.ticks) {
                location.world.playSound(Sounds.BLOCK_END_PORTAL_FRAME_FILL, location, volume = 0.5f)
                location.world.spawnParticle(location.add(0, 2, 0), Particles.FIREWORK, Vector3f(0f), speed = 0.2f, amount = 20)
                location.world.spawnParticle(location.add(0, 2, 0), Particles.ENCHANTED_HIT, Vector3f(0f), speed = 0.2f, amount = 10)

                val itemDisplay = instance.world.spawnEntity(ItemDisplay(location.add(0.0, 3.0 + offsetSize, 0.0), location.world)) as ItemDisplay
                itemDisplay.item.value = item.getItemStack()
                itemDisplay.billboard.value = DisplayBillboard.CENTER
                itemDisplay.scaleTo(1.2f)

                val interaction = world.spawnEntity(Interaction(location)) as Interaction
                interaction.height.value = 5f
                interaction.width.value = 3f

                val holo = hologram(location.add(0.0, 2.0 + offsetSize, 0.0)) {
                    val description = item.getDescription()
                    withStaticLine("${item.getRarity().color}<b><u>${item.getName()}<r>")
                    description.forEach { line ->
                        withStaticLine(line)
                    }
                    withStaticLine(" ")
                    withStaticLine("<yellow><b>></b> Buy for <gold>5$<yellow> <b><<")
                }

                cleanupEntities.add(interaction)
                cleanupEntities.add(holo)
                cleanupEntities.add(itemDisplay)

                interaction.generalInteractionDispatcher.register { player ->
                    if(!interaction.responsive.value) return@register
                    itemsInStock--
                    player.sendMessage("<yellow>bought ${item.getRarity().color}${item.getName()}<yellow>!")
                    interaction.responsive.value = false
                    world.despawnEntity(itemDisplay)
                    world.despawnEntity(holo)
                    world.players.forEach { loopPlayer -> loopPlayer.playChestAnimation(location, Player.ChestAnimation.CLOSE) }
                    world.playSound(Sounds.BLOCK_CHEST_CLOSE, location)
                    runLaterAsync(5.ticks) {
                        world.despawnEntity(interaction)
                    }
                }

                location.world.players.forEach { player -> holo.addViewer(player) }
            }
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

        continueInteraction.rightClickDispatcher.register { player ->
            despawn()
        }
    }

    fun despawn() {
        for (i in 1 until 4) {
            val location = instance.map.getPoint("shop_$i").location
            location.world.destroyNaturally(location)
        }
        runLaterAsync(1.ticks) {
            cleanupEntities.forEach { entity ->
                instance.world.despawnEntity(entity)
            }
        }
        instance.controller.startWave()
    }
}