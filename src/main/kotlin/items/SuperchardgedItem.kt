package io.github.dockyard.demo.items

import io.github.dockyard.demo.GameInstance
import io.github.dockyard.demo.monsters.Monster
import io.github.dockyardmc.entity.Entity
import io.github.dockyardmc.particles.spawnParticle
import io.github.dockyardmc.registry.DamageTypes
import io.github.dockyardmc.registry.Items
import io.github.dockyardmc.registry.Particles
import io.github.dockyardmc.registry.Sounds
import io.github.dockyardmc.registry.registries.Item
import io.github.dockyardmc.sounds.playSound
import io.github.dockyardmc.utils.percentOf
import io.github.dockyardmc.utils.randomFloat
import io.github.dockyardmc.utils.randomInt
import io.github.dockyardmc.utils.vectors.Vector3f

class SuperchardgedItem: GameItem() {

    override fun maxCopiesInInventory(): Int {
        return 3
    }

    override fun getShopPrice(): Int {
        return 30
    }

    override fun getItem(): Item {
        return Items.TNT_MINECART
    }

    override fun getRarity(): Rarity {
        return Rarity.EPIC
    }

    override fun getName(): String {
        return "Supercharged"
    }

    override fun getDescription(): List<String> {
        return listOf("Enemies have <lime>1 in 7<white> chance to explode on", "death, dealing <lime>30%<white> of their max health to", "nearby enemies but enemies have <red>10% more health<white>")
    }

    override fun onMonsterDeath(monster: Entity, gameInstance: GameInstance) {
        val random = randomInt(1, 7)
        if(random != 1) return

        val location = monster.location
        val world = monster.world

        world.spawnParticle(location, Particles.EXPLOSION, Vector3f(0.5f), amount = 5)
        world.playSound(Sounds.ENTITY_GENERIC_EXPLODE, volume = 1f, pitch = randomFloat(0.8f, 1.3f))

        val nearEntities = gameInstance.world.entities.filter { entity ->
            entity is Monster
            && entity.location.distance(monster.location) <= 2.0
            && entity != monster
        }

        nearEntities.forEach { entity ->
            val damage = percentOf(30f, (entity as Monster).maxHealth.toDouble()).toFloat() / 100
            entity.damage(damage, DamageTypes.GENERIC, null)
        }
    }
}