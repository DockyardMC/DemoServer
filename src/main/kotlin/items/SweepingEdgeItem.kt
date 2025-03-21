package io.github.dockyard.demo.items

import io.github.dockyard.demo.GameInstance
import io.github.dockyardmc.entity.Entity
import io.github.dockyardmc.particles.spawnParticle
import io.github.dockyardmc.player.Player
import io.github.dockyardmc.registry.DamageTypes
import io.github.dockyardmc.registry.Items
import io.github.dockyardmc.registry.Particles
import io.github.dockyardmc.registry.Sounds
import io.github.dockyardmc.registry.registries.Item
import io.github.dockyardmc.sounds.playSound
import io.github.dockyardmc.utils.vectors.Vector3d

class SweepingEdgeItem: GameItem() {

    override fun getItem(): Item {
        return Items.ENCHANTED_BOOK
    }

    override fun getRarity(): Rarity {
        return Rarity.COMMON
    }

    override fun getName(): String {
        return "Book of Sweeping Edge"
    }

    override fun onMonsterDamage(monster: Entity, gameInstance: GameInstance, damage: Float, attacker: Player?) {
        if(attacker == null) return
        if(!attacker.mainHandItem.isEmpty()) return
        monster.location.world.entities.filter { entity -> entity.location.distance(monster.location) >= 0.3 }.forEach { entity ->
            monster.damage(damage / 1.3f, DamageTypes.GENERIC, null)
        }
        monster.world.playSound(Sounds.ENTITY_PLAYER_ATTACK_SWEEP, monster.location)
        val direction = attacker.location.getDirection() * Vector3d(1.5)
        monster.world.spawnParticle(attacker.location.add(0.0, attacker.type.dimensions.eyeHeight.toDouble(), 0.0).add(direction), Particles.SWEEP_ATTACK)
    }

    override fun getDescription(): List<String> {
        return listOf("Adds the <pink>Sweeping Edge<white> enchant to", "your weapon!")
    }
}