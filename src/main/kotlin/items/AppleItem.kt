package io.github.dockyard.demo.items

import io.github.dockyard.demo.GameInstance
import io.github.dockyardmc.maths.vectors.Vector3f
import io.github.dockyardmc.particles.spawnParticle
import io.github.dockyardmc.registry.Items
import io.github.dockyardmc.registry.Particles
import io.github.dockyardmc.registry.registries.Item

class AppleItem : GameItem() {

    override fun maxCopiesInInventory(): Int {
        return -1
    }

    override fun getShopPrice(): Int {
        return 5
    }

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
        gameInstance.player.health.value += (4f.coerceAtLeast(gameInstance.playerMaxHealth.value.toFloat())).toInt()
        gameInstance.world.spawnParticle(gameInstance.player.location, Particles.HEART, Vector3f(1f), amount = 10)
    }
}