package io.github.dockyard.demo.items

import io.github.dockyard.demo.GameInstance
import io.github.dockyard.demo.GameManager
import io.github.dockyard.demo.events.PlayerDoubleJumpEvent
import io.github.dockyardmc.events.Events
import io.github.dockyardmc.events.PlayerFlightToggleEvent
import io.github.dockyardmc.maths.randomFloat
import io.github.dockyardmc.maths.vectors.Vector3d
import io.github.dockyardmc.maths.vectors.Vector3f
import io.github.dockyardmc.particles.spawnParticle
import io.github.dockyardmc.protocol.packets.play.serverbound.ServerboundClientInputPacket
import io.github.dockyardmc.registry.Items
import io.github.dockyardmc.registry.Particles
import io.github.dockyardmc.registry.Sounds
import io.github.dockyardmc.registry.registries.Item
import io.github.dockyardmc.sounds.playSound
import io.github.dockyardmc.utils.getPlayerEventContext

class FloatyFeather : GameItem() {

    companion object {
        init {
            Events.on<PlayerFlightToggleEvent> { event ->
                val player = event.player
                val instance = GameManager.games[player] ?: return@on
                if (!instance.controller.items.map { item -> item::class }.contains(FloatyFeather::class)) return@on
                player.isFlying.value = false

                val doubleJumpEvent = PlayerDoubleJumpEvent(player, instance, getPlayerEventContext(player))
                Events.dispatch(doubleJumpEvent)
                if(doubleJumpEvent.cancelled) return@on

                var subtitle = ""

                var newVelocity = Vector3d(0.0, 1.0, 0.0)
                val multBy = 12.0

                val dir = player.location.getDirection(true)
                val up = Vector3d(0.0, 2.0, 0.0)
                val cross = up.cross(dir)
                val rightVector = if (cross.length() > 0.0) cross.normalized() else Vector3d(1.0, 0.0, 0.0)

                if (player.heldInputs.contains(ServerboundClientInputPacket.Input.FORWARD)) {
                    newVelocity += dir * multBy
                    subtitle += "↑"
                }
                if (player.heldInputs.contains(ServerboundClientInputPacket.Input.BACKWARDS)) {
                    newVelocity -= dir * multBy
                    subtitle += "↓"
                }
                if (player.heldInputs.contains(ServerboundClientInputPacket.Input.LEFT)) {
                    newVelocity += rightVector * multBy
                    subtitle += "←"
                }
                if (player.heldInputs.contains(ServerboundClientInputPacket.Input.RIGHT)) {
                    newVelocity -= rightVector * multBy
                    subtitle += "→"
                }

                player.setVelocity(newVelocity)
                player.playSound(Sounds.ENTITY_ENDER_DRAGON_FLAP, player.location, 1f, randomFloat(1.6f, 2f))
                player.playSound(Sounds.BLOCK_FIRE_EXTINGUISH, player.location, 0.1f, randomFloat(1.6f, 2f))
                player.world.spawnParticle(player.location, Particles.CLOUD, Vector3f(0f), 0.1f, 4)
            }
        }
    }

    override fun maxCopiesInInventory(): Int {
        return 1
    }

    override fun getShopPrice(): Int {
        return 35
    }

    override fun getItem(): Item {
        return Items.FEATHER
    }

    override fun getRarity(): Rarity {
        return Rarity.EPIC
    }

    override fun getName(): String {
        return "Floaty Feather"
    }

    override fun getDescription(): List<String> {
        return listOf("Allows you to <lime>double jump<white>. Double jumping", "consumes <red>3 energy<white>")
    }

    override fun onObtain(gameInstance: GameInstance) {
        gameInstance.player.canFly.value = true
    }

    override fun onDiscarded(gameInstance: GameInstance) {
        gameInstance.player.canFly.value = false
    }
}