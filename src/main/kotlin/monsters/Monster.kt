package io.github.dockyard.demo.monsters

import cz.lukynka.bindables.Bindable
import de.metaphoriker.pathetic.api.pathing.configuration.HeuristicWeights
import de.metaphoriker.pathetic.api.pathing.filter.filters.PassablePathFilter
import io.github.dockyard.demo.GameInstance
import io.github.dockyardmc.entity.Entity
import io.github.dockyardmc.entity.EntityManager.spawnEntity
import io.github.dockyardmc.entity.ItemDropEntity
import io.github.dockyardmc.entity.ai.AIManager
import io.github.dockyardmc.events.EntityDamageEvent
import io.github.dockyardmc.events.EntityNavigatorPickOffsetEvent
import io.github.dockyardmc.events.EventPool
import io.github.dockyardmc.events.WorldTickEvent
import io.github.dockyardmc.events.system.EventFilter
import io.github.dockyardmc.extentions.broadcastMessage
import io.github.dockyardmc.location.Location
import io.github.dockyardmc.maths.randomFloat
import io.github.dockyardmc.maths.vectors.Vector3f
import io.github.dockyardmc.pathfinding.Navigator
import io.github.dockyardmc.pathfinding.Pathfinder
import io.github.dockyardmc.pathfinding.RequiredHeightPathfindingFilter
import io.github.dockyardmc.player.Player
import io.github.dockyardmc.registry.Blocks
import io.github.dockyardmc.registry.Items
import io.github.dockyardmc.sounds.Sound
import io.github.dockyardmc.sounds.playSound

abstract class Monster(location: Location, instance: GameInstance, val maxHealth: Float) : Entity(location) {

    override var health: Bindable<Float> = bindablePool.provideBindable(instance.monsterHealth.getValueOf(maxHealth))
    val eventPool = EventPool().withFilter(EventFilter.containsWorld(location.world))
    override var inventorySize: Int = 0


    private val pathfinder = Pathfinder.createPathfinder {
        async(true)
        fallback(false)
        maxLength(20)
        maxIterations(256)
        heuristicWeights(HeuristicWeights.NATURAL_PATH_WEIGHTS)
    }

    private val filters = listOf(PassablePathFilter(), RequiredHeightPathfindingFilter(2))

    private val speedInMetersPerTick = calculateSpeed(instance.monsterSpeed.percentage)
    val navigator = Navigator(this, speedInMetersPerTick.toInt(), pathfinder, filters)
    val brain = AIManager(this)

    var target: Player? = null

    fun getPlayerTarget(): Entity? {
        return target
    }

    private fun calculateSpeed(percentage: Double): Int {
        val amount = ((-0.39 * percentage + 40).toInt() - getBaseWalkSpeed()).coerceAtLeast(2)
        return amount
    }

    abstract fun getDamageSound(): Sound
    abstract fun getWalkSound(): Sound
    abstract fun getAmountOfMoney(): Int
    abstract fun getBaseDamage(): Float
    abstract fun getBaseWalkSpeed(): Int

    init {
        eventPool.on<WorldTickEvent> { _ ->
            if (this::isDead.call()) return@on
            val playersInArea = world.players.filter { player ->
                player.location.distance(this.location) <= 30
                        && player.location.block.registryBlock != Blocks.WATER
            }

            if (target != null && playersInArea.isEmpty()) {
                target = null
                return@on
            }

            val player = playersInArea.firstOrNull() ?: return@on
            if (player.isFlying.value) return@on
            target = player
        }

        eventPool.on<EntityDamageEvent> { event ->
            if (event.entity != this) return@on
            if (event.entity.isDead) return@on
            world.playSound(getDamageSound(), this::location.call())
        }

        eventPool.on<EntityNavigatorPickOffsetEvent> { event ->
            if (event.entity != this) return@on
            val constraints = 0.5f
            event.location = event.location.add(Vector3f(randomFloat(constraints, -constraints), 0f, randomFloat(constraints, -constraints)))
        }

        navigator.navigationNodeStepDispatcher.subscribe { _ ->
            if (this.isDead) return@subscribe
            world.playSound(getWalkSound().identifier, this::location.call(), 0.5f, 1f)
        }
    }

    fun dropMoney() {
        val item = world.spawnEntity(ItemDropEntity(location, Items.GOLD_INGOT.toItemStack().withAmount(getAmountOfMoney()))) as ItemDropEntity
        item.pickupDistance = 5
        item.canBePickedUpAfter = 7
    }

    override fun dispose() {
        eventPool.dispose()
        navigator.dispose()
        pathfinder.abort()
        super.dispose()
    }
}