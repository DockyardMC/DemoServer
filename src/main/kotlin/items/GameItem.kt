package io.github.dockyard.demo.items

import io.github.dockyard.demo.GameInstance
import io.github.dockyardmc.entity.Entity
import io.github.dockyardmc.item.ItemStack
import io.github.dockyardmc.player.Player
import io.github.dockyardmc.registry.registries.Item

abstract class GameItem {

    companion object {
        const val AMETHYST = "<#db85ff>"
        const val LEGENDARY = "<#ffb300>"
        const val COMMON = "<#d4ceed>"
        const val UNCOMMON = "<#88ff70>"
        const val RARE = "<#70acff>"
    }

    abstract fun getItem(): Item

    abstract fun getRarity(): Rarity

    abstract fun getName(): String

    abstract fun getDescription(): List<String>

    open val discardsAfterUse: Boolean = false

    open fun onObtain(gameInstance: GameInstance) {}

    open fun onPlayerDeath(gameInstance: GameInstance) {}

    open fun onMonsterDeath(monster: Entity, gameInstance: GameInstance) {}

    open fun onPlayerDamage(gameInstance: GameInstance, damage: Float) {}

    open fun onMonsterDamage(monster: Entity, gameInstance: GameInstance, damage: Float, attacker: Player?) {}

    open fun onPhaseStart(gameInstance: GameInstance) {}

    open fun onPhaseEnd(gameInstance: GameInstance) {}

    open fun onMonsterSpawn(gameInstance: GameInstance) {}

    enum class Rarity(val color: String) {
        COMMON(GameItem.COMMON),
        UNCOMMON(GameItem.UNCOMMON),
        RARE(GameItem.RARE),
        EPIC(GameItem.AMETHYST),
        LEGENDARY(GameItem.LEGENDARY)
    }

    fun getItemStack(): ItemStack {
        val stack = ItemStack(getItem())
            .withDisplayName("${getRarity().color}<u>${getName()}<r>")
            .withLore(" ", *getDescription().toTypedArray(), "")
            .withCustomModelData(1f)
        return stack
    }
}