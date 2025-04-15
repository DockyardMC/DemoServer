package io.github.dockyard.demo.items

import io.github.dockyard.demo.GameInstance
import io.github.dockyard.demo.monsters.Monster
import io.github.dockyardmc.entity.Entity
import io.github.dockyardmc.maths.Percentage
import io.github.dockyardmc.maths.vectors.Vector3d
import io.github.dockyardmc.particles.spawnParticle
import io.github.dockyardmc.player.Player
import io.github.dockyardmc.registry.DamageTypes
import io.github.dockyardmc.registry.Items
import io.github.dockyardmc.registry.Particles
import io.github.dockyardmc.registry.Sounds
import io.github.dockyardmc.registry.registries.Item
import io.github.dockyardmc.sounds.playSound

class SweepingEdgeItem : GameItem() {

    override fun maxCopiesInInventory(): Int {
        return 2
    }

    override fun getShopPrice(): Int {
        return 10
    }

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
        if (attacker == null) return
        if (attacker.mainHandItem.isEmpty()) return

        val nearEntities = gameInstance.world.entities.filter { entity ->
            entity is Monster
            && entity.location.distance(monster.location) <= 1.3
            && entity != monster
        }

        nearEntities.forEach { entity ->
            entity.damage(Percentage(35.0).getValueOf(damage), DamageTypes.GENERIC, null)
        }

        monster.world.playSound(Sounds.ENTITY_PLAYER_ATTACK_SWEEP, monster.location)
        val direction = attacker.location.getDirection() * Vector3d(1.5)
        monster.world.spawnParticle(attacker.location.add(0.0, attacker.type.dimensions.eyeHeight.toDouble(), 0.0).add(direction), Particles.SWEEP_ATTACK)
    }

    override fun getDescription(): List<String> {
        return listOf("Adds the <pink>Sweeping Edge<white> enchant to", "your weapon!")
    }
}