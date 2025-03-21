package io.github.dockyard.demo.monsters

import de.metaphoriker.pathetic.api.pathing.result.PathfinderResult
import io.github.dockyardmc.entity.Entity
import io.github.dockyardmc.entity.ai.AIGoal
import io.github.dockyardmc.location.Location
import io.github.dockyardmc.pathfinding.Navigator

class PursuitEntityAndAttackAIGoal(override var entity: Entity, override var priority: Int, val targetUnit: () -> Entity?, val navigator: Navigator): AIGoal() {

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

        pathfindResultListener = navigator.pathfindResultDispatcher.register { result ->
            if(result.hasFailed()) {
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

        pathfindingEndListener = navigator.navigationCompleteDispatcher.register {
            hasFinishedWalking = true
        }
    }

    override fun end() {
        navigator.cancelNavigating()
        pathfindResultListener?.let { listener -> navigator.pathfindResultDispatcher.unregister(listener) }
        pathfindingEndListener?.let { listener -> navigator.navigationCompleteDispatcher.unregister(listener) }
        pathfindingStepListener?.let { listener -> navigator.navigationNodeStepDispatcher.unregister(listener) }
    }

    override fun endCondition(): Boolean {
        return hasFinishedWalking
    }

    var lastTargetLocation: Location? = null
    var tick: Int = 0
    override fun tick() {
        tick++
        val target = targetUnit.invoke()

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
        }

        if(target.location.distance(entity.location) < 0.3) {
            attack(target)
        }
    }

    fun attack(entity: Entity) {
        if(attackCooldown != 0) return

    }
}