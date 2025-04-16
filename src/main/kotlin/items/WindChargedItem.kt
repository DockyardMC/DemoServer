package io.github.dockyard.demo.items

import io.github.dockyard.demo.events.PlayerDoubleJumpEvent
import io.github.dockyard.demo.utils.getInstance
import io.github.dockyard.demo.utils.hasItem
import io.github.dockyardmc.events.Events
import io.github.dockyardmc.registry.Items
import io.github.dockyardmc.registry.PotionEffects
import io.github.dockyardmc.registry.registries.Item

class WindChargedItem: GameItem() {

    companion object {
        init {
            Events.on<PlayerDoubleJumpEvent> { event ->
                val player = event.player
                val instance = player.getInstance() ?: return@on
                if(!instance.hasItem(FloatyFeather::class)) return@on
                if(player.potionEffects.contains(PotionEffects.SPEED)) return@on
                player.addPotionEffect(PotionEffects.SPEED, 2 * 20, 2, false, true, true)
            }
        }
    }

    override fun maxCopiesInInventory(): Int {
        return 1
    }

    override fun getShopPrice(): Int {
        return 30
    }

    override fun getItem(): Item {
        return Items.WIND_CHARGE
    }

    override fun getRarity(): Rarity {
        return Rarity.RARE
    }

    override fun getName(): String {
        return "Wind Charged"
    }

    override fun getDescription(): List<String> {
        return listOf("After double jumping, gain a short burst of <lime>speed")
    }
}