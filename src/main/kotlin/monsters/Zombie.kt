package io.github.dockyard.demo.monsters

import cz.lukynka.bindables.Bindable
import io.github.dockyard.demo.GameInstance
import io.github.dockyardmc.entity.ai.goals.PlayAmbientNoiseAIGoal
import io.github.dockyardmc.entity.ai.goals.RandomLookAroundAIGoal
import io.github.dockyardmc.events.EntityDamageEvent
import io.github.dockyardmc.location.Location
import io.github.dockyardmc.registry.EntityTypes
import io.github.dockyardmc.registry.Sounds
import io.github.dockyardmc.registry.registries.EntityType
import io.github.dockyardmc.sounds.Sound
import io.github.dockyardmc.sounds.playSound

class Zombie(location: Location, instance: GameInstance) : Monster(location, instance) {

    override fun getDamageSound(): Sound {
        return Sound(Sounds.ENTITY_ZOMBIE_HURT)
    }

    override var health: Bindable<Float> = bindablePool.provideBindable(3f * instance.monsterHealthMultiplier)
    override var type: EntityType = EntityTypes.ZOMBIE

    init {
        brain.addGoal(PursuitEntityAndAttackAIGoal(this, 2, ::getPlayerTarget, navigator))
        brain.addGoal(RandomLookAroundAIGoal(this, 1, 30))
        brain.addGoal(PlayAmbientNoiseAIGoal(this, 1, 5, Sounds.ENTITY_ZOMBIE_AMBIENT))

    }
}