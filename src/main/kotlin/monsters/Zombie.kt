package io.github.dockyard.demo.monsters

import io.github.dockyard.demo.GameInstance
import io.github.dockyardmc.entity.EntityMetaValue
import io.github.dockyardmc.entity.EntityMetadata
import io.github.dockyardmc.entity.EntityMetadataType
import io.github.dockyardmc.entity.ai.goals.PlayAmbientNoiseAIGoal
import io.github.dockyardmc.entity.ai.goals.RandomLookAroundAIGoal
import io.github.dockyardmc.location.Location
import io.github.dockyardmc.maths.randomInt
import io.github.dockyardmc.registry.EntityTypes
import io.github.dockyardmc.registry.Sounds
import io.github.dockyardmc.registry.registries.EntityType
import io.github.dockyardmc.sounds.Sound

class Zombie(location: Location, instance: GameInstance) : Monster(location, instance, 3f) {

    override fun getAmountOfMoney(): Int {
        return randomInt(1, 2)
    }

    override fun getBaseDamage(): Float {
        return 1f
    }

    override fun getBaseWalkSpeed(): Int {
        return 0
    }

    override fun getDamageSound(): Sound {
        return Sound(Sounds.ENTITY_ZOMBIE_HURT)
    }

    override fun getWalkSound(): Sound {
        return Sound(Sounds.ENTITY_ZOMBIE_STEP)
    }

    override var type: EntityType = EntityTypes.ZOMBIE

    init {
        brain.addGoal(PursuitEntityAndAttackAIGoal(this, 2, ::getPlayerTarget, navigator, getBaseDamage()))
        brain.addGoal(RandomLookAroundAIGoal(this, 1, 30))
        brain.addGoal(PlayAmbientNoiseAIGoal(this, 1, 5, Sounds.ENTITY_ZOMBIE_AMBIENT))
    }

    fun lowerHands() {
        metadata[EntityMetadataType.ARMOR_STAND_BITMASK] = EntityMetadata(EntityMetadataType.ARMOR_STAND_BITMASK, EntityMetaValue.BYTE, 0x00)
    }

    fun raiseHands() {
        metadata[EntityMetadataType.ARMOR_STAND_BITMASK] = EntityMetadata(EntityMetadataType.ARMOR_STAND_BITMASK, EntityMetaValue.BYTE, 0x04)
    }
}