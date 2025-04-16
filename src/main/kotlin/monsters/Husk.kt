package io.github.dockyard.demo.monsters

import io.github.dockyard.demo.GameInstance
import io.github.dockyardmc.attributes.AttributeModifier
import io.github.dockyardmc.attributes.AttributeOperation
import io.github.dockyardmc.entity.EntityMetaValue
import io.github.dockyardmc.entity.EntityMetadata
import io.github.dockyardmc.entity.EntityMetadataType
import io.github.dockyardmc.entity.ai.goals.PlayAmbientNoiseAIGoal
import io.github.dockyardmc.entity.ai.goals.RandomLookAroundAIGoal
import io.github.dockyardmc.location.Location
import io.github.dockyardmc.maths.randomInt
import io.github.dockyardmc.player.Player
import io.github.dockyardmc.protocol.packets.play.clientbound.ClientboundUpdateAttributesPacket
import io.github.dockyardmc.registry.Attributes
import io.github.dockyardmc.registry.EntityTypes
import io.github.dockyardmc.registry.Sounds
import io.github.dockyardmc.registry.registries.EntityType
import io.github.dockyardmc.sounds.Sound

class Husk(location: Location, instance: GameInstance): Monster(location, instance, 3f) {
    override var type: EntityType = EntityTypes.HUSK

    override fun addViewer(player: Player) {
        super.addViewer(player)
        val modifier = AttributeModifier("dockyard:size", 0.3, AttributeOperation.ADD)
        val property = ClientboundUpdateAttributesPacket.Property(Attributes.SCALE, Attributes.SCALE.defaultValue, listOf(modifier))
        player.sendPacket(ClientboundUpdateAttributesPacket(this, listOf(property)))
    }

    override fun getAmountOfMoney(): Int {
        return randomInt(2, 4)
    }

    override fun getBaseDamage(): Float {
        return 1.5f
    }

    override fun getBaseWalkSpeed(): Int {
        return 1
    }

    override fun getDamageSound(): Sound {
        return Sound(Sounds.ENTITY_HUSK_HURT)
    }

    override fun getWalkSound(): Sound {
        return Sound(Sounds.ENTITY_HUSK_STEP)
    }

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