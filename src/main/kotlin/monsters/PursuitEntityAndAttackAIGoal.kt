package io.github.dockyard.demo.monsters

import de.metaphoriker.pathetic.api.pathing.result.PathfinderResult
import io.github.dockyardmc.entity.Entity
import io.github.dockyardmc.entity.ai.AIGoal
import io.github.dockyardmc.extentions.broadcastMessage
import io.github.dockyardmc.location.Location
import io.github.dockyardmc.pathfinding.Navigator
import io.github.dockyardmc.registry.DamageTypes

class PursuitEntityAndAttackAIGoal(override var entity: Entity, override var priority: Int, val targetUnit: () -> Entity?, val navigator: Navigator, val damage: Float): AIGoal() {

    companion object {
        const val ATTACK_COOLDOWN = 20
        const val PATH_UPDATE_PERIOD_NORMAL = 1
        const val PATH_UPDATE_PERIOD_IDLE = 10
        const val PATH_UPDATE_FAIL_THRESHOLD = 6
    }

    var hasFinishedWalking = false
    var attackCooldown = 0
    var fails = 0
    var updateFrequency = PATH_UPDATE_PERIOD_NORMAL

    override fun startCondition(): Boolean {
        return targetUnit.invoke() != null
    }

    private var pathfindResultListener: ((PathfinderResult) -> Unit)? = null
    private var pathfindingStepListener: ((Navigator.PathfindingStep) -> Unit)? = null
    private var pathfindingEndListener: ((Navigator.PathfindingStep) -> Unit)? = null

    override fun start() {
        hasFinishedWalking = false

        pathfindResultListener = navigator.pathfindResultDispatcher.subscribe { result ->
            if(result.hasFailed()) {
                navigator.cancelNavigating()
                fails++
                if(fails >= PATH_UPDATE_FAIL_THRESHOLD) {
                    updateFrequency = PATH_UPDATE_PERIOD_IDLE
                }
            } else {
                if(updateFrequency != PATH_UPDATE_PERIOD_NORMAL) {
                    updateFrequency = PATH_UPDATE_PERIOD_NORMAL
                    fails = 0
                }
            }
        }

        pathfindingEndListener = navigator.navigationCompleteDispatcher.subscribe {
            hasFinishedWalking = true
        }
    }

    override fun end() {
        navigator.cancelNavigating()
        pathfindResultListener?.let { listener -> navigator.pathfindResultDispatcher.unsubscribe(listener) }
        pathfindingEndListener?.let { listener -> navigator.navigationCompleteDispatcher.unsubscribe(listener) }
        pathfindingStepListener?.let { listener -> navigator.navigationNodeStepDispatcher.unsubscribe(listener) }
    }

    override fun endCondition(): Boolean {
        return hasFinishedWalking
    }

    var lastTargetLocation: Location? = null
    var tick: Int = 0
    override fun tick() {
        entity.customNameVisible.value = true
        if(entity.isDead) return
        tick++
        val target = targetUnit.invoke()

        if(attackCooldown > 0) {
            attackCooldown--
        }

        if(target == null) {
            hasFinishedWalking = true
            return
        }

        var shouldPathfind = false
        if(entity.isDead) shouldPathfind = false
        if(lastTargetLocation == null) shouldPathfind = true
        if(lastTargetLocation != null && lastTargetLocation!!.distance(target.location) > 1.0) shouldPathfind = true
        if(tick % updateFrequency != 0) shouldPathfind = false

        if(shouldPathfind) {
            lastTargetLocation = target.location
            navigator.updatePathfindingPath(target.location.subtract(0, 1, 0))
            entity.customName.value = "<red>Pathfinding to ${target::class.simpleName}"
        } else {
            entity.customName.value = "<lime>Idle"
        }

        if(target.location.distance(entity.location) <= 1.5) {
            if(entity is Zombie) (entity as Zombie).raiseHands()
            attack(target)
        } else {
            if(entity is Zombie) (entity as Zombie).lowerHands()
        }
    }

    fun attack(target: Entity) {
        if(attackCooldown != 0) return
        attackCooldown = 20
        target.damage(damage, DamageTypes.GENERIC, this.entity)
        target.health.value -= damage
    }
}