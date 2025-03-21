package io.github.dockyard.demo

import io.github.dockyard.demo.items.GameItem
import io.github.dockyard.demo.items.ItemRegistry
import io.github.dockyard.demo.monsters.Monster
import io.github.dockyard.demo.monsters.Zombie
import io.github.dockyardmc.apis.hologram
import io.github.dockyardmc.entity.DisplayBillboard
import io.github.dockyardmc.entity.EntityManager
import io.github.dockyardmc.entity.EntityManager.spawnEntity
import io.github.dockyardmc.entity.Interaction
import io.github.dockyardmc.entity.ItemDisplay
import io.github.dockyardmc.events.*
import io.github.dockyardmc.extentions.broadcastMessage
import io.github.dockyardmc.extentions.sendTitle
import io.github.dockyardmc.particles.spawnParticle
import io.github.dockyardmc.player.Player
import io.github.dockyardmc.registry.Blocks
import io.github.dockyardmc.registry.DamageTypes
import io.github.dockyardmc.registry.Particles
import io.github.dockyardmc.registry.Sounds
import io.github.dockyardmc.runnables.ticks
import io.github.dockyardmc.sounds.playSound
import io.github.dockyardmc.utils.randomInt
import io.github.dockyardmc.utils.vectors.Vector3d
import io.github.dockyardmc.utils.vectors.Vector3f
import kotlin.reflect.full.primaryConstructor

class GameController(val instance: GameInstance) {

    var wave = 0
    var monstersRemaining = 0

    val items = mutableListOf<GameItem>()

    fun spawnMonster() {
        val random = instance.center.getBlocksInRadius(10).filter { location -> location.y == instance.center.y }.random()
        val zombie = instance.world.spawnEntity(Zombie(random, instance)) as Zombie
    }

    fun startWave() {
        wave++
        instance.world.players.sendTitle("${Colors.RED}<bold>WAVE $wave", "${Colors.RED_HIGHLIGHT}Defeat all the monsters!", 10, 40, 10)
        instance.state.value = GameInstance.State.GAME_PLAY
        items.forEach { item -> item.onPhaseStart(instance) }
        monstersRemaining = randomInt(2, 3) * wave
        for (i in 0 until monstersRemaining) {
            spawnMonster()
        }
    }

    fun endWave() {
        instance.world.players.sendTitle("${Colors.LIME}<bold>SURVIVED", "${Colors.LIME_HIGHLIGHT}Buy items from the shop!", 10, 40, 10)
        items.forEach { item -> item.onPhaseEnd(instance) }
        instance.shop.spawn()
    }


    fun registerController() {
        val pool = instance.eventPool

        pool.on<PlayerDamageEntityEvent> { event ->
            event.entity.damage(instance.playerDamage, DamageTypes.GENERIC, event.player)
        }

        pool.on<EntityDamageEvent> { event ->
            if(event.attacker !is Player) return@on
            items.forEach { item -> item.onMonsterDamage(event.entity, instance, event.damage, event.attacker as Player?) }
        }

        pool.on<PlayerDamageEvent> { event ->
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

            monstersRemaining--
            if(monstersRemaining <= 0) {
                endWave()
            }
        }

        val glowingEntities = mutableListOf<ItemDisplay>()
        pool.on<PlayerMoveEvent> { event ->
            if(instance.state.value != GameInstance.State.SHOP_ACTIVE) {
                if(glowingEntities.isNotEmpty()) {
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
                    if(!glowingEntities.contains(entity)) {
                        entity.isGlowing.value = true
                        glowingEntities.add(entity as ItemDisplay)
                    }
                }

                glowingEntities.toList().forEach { entity ->
                    if(!nearDisplays.contains(entity)) {
                        entity.isGlowing.value = false
                        glowingEntities.remove(entity)
                    }
                }
            }
        }
    }
}