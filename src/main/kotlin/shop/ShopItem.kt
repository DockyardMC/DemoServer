package io.github.dockyard.demo.shop

import io.github.dockyard.demo.Colors
import io.github.dockyard.demo.GameInstance
import io.github.dockyard.demo.items.GameItem
import io.github.dockyardmc.apis.Hologram
import io.github.dockyardmc.apis.hologram
import io.github.dockyardmc.entity.DisplayBillboard
import io.github.dockyardmc.entity.EntityManager.despawnEntity
import io.github.dockyardmc.entity.EntityManager.spawnEntity
import io.github.dockyardmc.entity.Interaction
import io.github.dockyardmc.entity.ItemDisplay
import io.github.dockyardmc.extentions.properStrictCase
import io.github.dockyardmc.location.Location
import io.github.dockyardmc.particles.spawnParticle
import io.github.dockyardmc.player.Player
import io.github.dockyardmc.registry.Blocks
import io.github.dockyardmc.registry.Particles
import io.github.dockyardmc.registry.Sounds
import io.github.dockyardmc.runnables.ticks
import io.github.dockyardmc.scheduler.runLaterAsync
import io.github.dockyardmc.sounds.playSound
import io.github.dockyardmc.utils.Disposable
import io.github.dockyardmc.utils.vectors.Vector3f
import kotlin.math.ceil

class ShopItem(val location: Location, val item: GameItem, val instance: GameInstance): Disposable {

    val world = instance.world
    val player = instance.player
    private var itemDisplay: ItemDisplay? = null
    private var interaction: Interaction? = null
    private var hologram: Hologram? = null
    private val offsetSize = item.getDescription().size * 0.3

    val price = ceil(item.getShopPrice() * instance.shopPriceMultiplier).toInt()

    fun despawn() {
        despawnEntities()
        world.destroyNaturally(location)
    }

    private fun despawnEntities() {
        interaction?.responsive?.value = false
        itemDisplay?.let { entity -> world.despawnEntity(entity) }
        hologram?.let { entity -> world.despawnEntity(entity) }

        runLaterAsync(5) {
            interaction?.let { entity -> world.despawnEntity(entity) }
        }
    }

    fun spawn() {
        location.setBlock(Blocks.CHEST.withBlockStates("facing" to "north"))
        world.playSound(Sounds.BLOCK_WOOD_PLACE, location)
        world.scheduler.runLater(10.ticks) {
            world.players.forEach { player -> player.playChestAnimation(location, Player.ChestAnimation.OPEN) }
            world.playSound(Sounds.BLOCK_CHEST_OPEN, location)
        }

        world.scheduler.runLater(20.ticks) {
            location.world.playSound(Sounds.BLOCK_END_PORTAL_FRAME_FILL, location, volume = 0.5f)
            location.world.spawnParticle(location.add(0, 2, 0), Particles.FIREWORK, Vector3f(0f), speed = 0.1f, amount = 10)
            location.world.spawnParticle(location.add(0, 2, 0), Particles.ENCHANTED_HIT, Vector3f(0f), speed = 0.2f, amount = 10)

            itemDisplay = instance.world.spawnEntity(ItemDisplay(location.add(0.0, 3.0 + offsetSize, 0.0), location.world)) as ItemDisplay
            itemDisplay!!.item.value = item.getItemStack()
            itemDisplay!!.billboard.value = DisplayBillboard.CENTER
            itemDisplay!!.scaleTo(1.2f)

            interaction = world.spawnEntity(Interaction(location)) as Interaction
            interaction!!.height.value = 5f
            interaction!!.width.value = 3f

            hologram = hologram(location.add(0.0, 2.0 + offsetSize, 0.0)) {
                getHologramLines().forEach { line ->
                    withStaticLine(line)
                }
            }

            world.players.forEach { player -> hologram!!.addViewer(player) }

            interaction!!.rightClickDispatcher.register { _ ->
                if(!interaction!!.responsive.value) return@register

                if(purchase()) {
                    interaction!!.responsive.value = false
                    instance.shop.shopItems.forEach { shopItem -> shopItem.updateHologram() }
                }
            }
        }
    }

    fun purchase(): Boolean {
        val particleLocation = location.add(0.0, 2.5, 0.0)

        if(instance.money.value < price) {
            player.sendMessage("<red><bold>Shop <dark_gray>● <gray>You cannot afford this item! (${instance.money.value})")
            player.playSound(Sounds.ENTITY_VILLAGER_NO, 2f)
            return false
        }

        if(instance.controller.getAmountOf(item) >= item.maxCopiesInInventory()) {
            player.sendMessage("<red><bold>Shop <dark_gray>● <gray>You have maximum of this item in your inventory!")
            player.playSound(Sounds.ENTITY_VILLAGER_NO, 2f)
            return false
        }

        instance.money.value -= price

        player.sendMessage("<gold><bold>Shop <dark_gray>● <gray>You bought ${item.getRarity().color}${item.getName()}<gray> for <gold>$$price<gray>!")
        world.playSound(Sounds.ENTITY_ITEM_PICKUP, interaction!!.location, 2f, 1f)
        world.playSound(Sounds.BLOCK_VAULT_REJECT_REWARDED_PLAYER, interaction!!.location, 2f, 1f)
        world.spawnParticle(particleLocation, Particles.ENCHANTED_HIT, Vector3f(0f), 0.5f, 50)
        world.spawnParticle(particleLocation, Particles.CLOUD, Vector3f(0f), 0.1f, 10)

        instance.controller.items.add(item)
        item.onObtain(instance)

        despawnEntities()
        world.players.forEach { player -> player.playChestAnimation(location, Player.ChestAnimation.CLOSE) }
        world.playSound(Sounds.BLOCK_CHEST_CLOSE, location)
        return true
    }

    private fun getHologramLines(): MutableList<String> {
        val lines = mutableListOf<String>()
        lines.add("${item.getRarity().color}<b><u>${item.getName()}<r>")
        lines.add("<#4f4f4f>[${item.getRarity().name.properStrictCase()}] [Owned ${instance.controller.getAmountOf(item)}/${item.maxCopiesInInventory()}]")
        lines.add(" ")

        item.getDescription().forEach { line ->
            lines.add(line)
        }
        lines.add("")
        if(instance.money.value < price) {
            lines.add("${Colors.RED}▶ You don't have $${price} ◀")
        } else {
            if(instance.controller.getAmountOf(item) >= item.maxCopiesInInventory()) {
                lines.add("${Colors.RED}▶ You have maximum of this item ◀")
            } else {
                lines.add("${Colors.LIME}▶ Buy for ${Colors.LIME_HIGHLIGHT}$$price${Colors.LIME} ◀")
            }
        }

        return lines
    }

    fun updateHologram() {
        val size = getHologramLines().size
        val lines = getHologramLines()
        for (i in 0 until size) {
            hologram?.setStaticLine(i, lines[i])
        }
    }

    override fun dispose() {
        despawn()
    }
}