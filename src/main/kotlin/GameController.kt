package io.github.dockyard.demo

import io.github.dockyard.demo.items.GameItem
import io.github.dockyard.demo.monsters.Monster
import io.github.dockyard.demo.monsters.Zombie
import io.github.dockyardmc.entity.EntityManager
import io.github.dockyardmc.entity.EntityManager.despawnEntity
import io.github.dockyardmc.entity.EntityManager.spawnEntity
import io.github.dockyardmc.entity.Interaction
import io.github.dockyardmc.entity.ItemDisplay
import io.github.dockyardmc.events.*
import io.github.dockyardmc.extentions.sendPacket
import io.github.dockyardmc.extentions.sendTitle
import io.github.dockyardmc.player.Player
import io.github.dockyardmc.protocol.packets.play.clientbound.ClientboundPickupItemPacket
import io.github.dockyardmc.registry.DamageTypes
import io.github.dockyardmc.registry.Sounds
import io.github.dockyardmc.runnables.ticks
import io.github.dockyardmc.sounds.playSound
import io.github.dockyardmc.utils.percentOf
import io.github.dockyardmc.utils.randomInt
import io.github.dockyardmc.utils.vectors.Vector3d
import kotlin.reflect.KClass

class GameController(val instance: GameInstance) {

    var wave = 0
    var monstersRemaining = 0

    val items = mutableListOf<GameItem>()

    fun getInventoryAsMap(): Map<KClass<out GameItem>, Int> {
        val inventoryMap = mutableMapOf<KClass<out GameItem>, Int>()
        items.forEach { item ->
            val current = inventoryMap[item::class] ?: 0
            inventoryMap[item::class] = current + 1
        }
        return inventoryMap
    }

    fun getAmountOf(gameItem: GameItem): Int {
        return getInventoryAsMap()[gameItem::class] ?: 0
    }

    fun spawnMonster() {
        val random = instance.center.getBlocksInRadius(10).filter { location -> location.y == instance.center.y }.random()
        val zombie = instance.world.spawnEntity(Zombie(random, instance)) as Zombie
    }

    fun startWave() {
        wave++
        instance.world.players.sendTitle("${Colors.RED}<bold>WAVE $wave", "${Colors.RED_HIGHLIGHT}Defeat all the monsters!", 10, 20, 10)
        instance.state.value = GameInstance.State.GAME_PLAY
        items.forEach { item -> item.onPhaseStart(instance) }
        monstersRemaining = randomInt(2, 3) * wave
        for (i in 0 until monstersRemaining) {
            spawnMonster()
        }
    }

    fun endWave() {
        instance.world.players.sendTitle("${Colors.LIME}<bold>SURVIVED", "${Colors.LIME_HIGHLIGHT}Buy items from the shop!", 10, 20, 10)
        items.forEach { item -> item.onPhaseEnd(instance) }
        instance.shop.spawn()
    }

    fun registerController() {
        val pool = instance.eventPool

        pool.on<PlayerDamageEntityEvent> { event ->
            if (event.entity !is Monster) {
                event.cancel()
                return@on
            }
            var damage = instance.playerDamage
            val chance = randomInt(0, 100)
            if(chance <= instance.playerCritRate) {
                val crit = percentOf(instance.playerCritDamage.toFloat(), instance.playerDamage.toDouble()).toFloat()
                damage += crit
                event.player.sendMessage("<red>crit for $crit dmg")
            }

            event.entity.damage(instance.playerDamage, DamageTypes.GENERIC, event.player)
        }

        pool.on<EntityDamageEvent> { event ->
            if (event.entity !is Monster) return@on
            if (event.attacker !is Player) return@on
            items.forEach { item -> item.onMonsterDamage(event.entity, instance, event.damage, event.attacker as Player?) }
        }

        pool.on<PlayerDamageEvent> { event ->
            val chance = randomInt(0, 100)
            if(chance <= instance.playerDodge) {
                event.player.sendMessage("<gray>Dodged attack")
                event.player.playSound(Sounds.ITEM_SHIELD_BLOCK)
                event.cancel()
            }
            items.forEach { item -> item.onPlayerDamage(instance, event.damage) }
        }

        pool.on<PlayerDeathEvent> { event ->
            items.forEach { item -> item.onPlayerDeath(instance) }
        }

        pool.on<EntityDeathEvent> { event ->
            if (event.entity is Player) return@on
            if (event.entity is Interaction) return@on
            items.forEach { item -> item.onMonsterDeath(event.entity, instance) }
            (event.entity as Monster).navigator.cancelNavigating()
            (event.entity as Monster).navigator.dispose()
            (event.entity as Monster).dropMoney()

            monstersRemaining--
            if (monstersRemaining <= 0) {
                endWave()
            }
        }

        Events.on<EntityPickupItemEvent> { event ->
            if (event.entity !is Player) return@on
            event.cancel()
            event.itemDropEntity.canBePickedUp = false
            instance.money.value += event.itemDropEntity.itemStack.value.amount
            val packet = ClientboundPickupItemPacket(event.itemDropEntity, event.entity, event.itemDropEntity.itemStack.value)
            event.itemDropEntity.viewers.sendPacket(packet)

            instance.world.scheduler.runLater(5.ticks) {
                event.itemDropEntity.autoViewable = false
                instance.world.despawnEntity(event.itemDropEntity)
                instance.world.despawnEntity(event.itemDropEntity)
            }
        }

        val glowingEntities = mutableListOf<ItemDisplay>()
        pool.on<PlayerMoveEvent> { event ->
            if (instance.state.value != GameInstance.State.SHOP_ACTIVE) {
                if (glowingEntities.isNotEmpty()) {
                    glowingEntities.forEach { entity ->
                        EntityManager.despawnEntity(entity)
                    }
                    glowingEntities.clear()
                }
                return@on
            }

            val player = event.player
            for (i in 1 until 6) {
                val direction = player.location.getDirection() * Vector3d(i.toDouble() - 1.0)
                val location = player.location.add(0.0, player.type.dimensions.eyeHeight.toDouble(), 0.0).add(direction)
                val nearDisplays = location.world.entities.filter { entity -> entity is ItemDisplay && entity.item.value.customModelData.floats.contains(1f) && entity.location.distance(location) <= 4.0 }

                nearDisplays.forEach { entity ->
                    if (!glowingEntities.contains(entity)) {
                        entity.isGlowing.value = true
                        glowingEntities.add(entity as ItemDisplay)
                    }
                }

                glowingEntities.toList().forEach { entity ->
                    if (!nearDisplays.contains(entity)) {
                        entity.isGlowing.value = false
                        glowingEntities.remove(entity)
                    }
                }
            }
        }
    }
}