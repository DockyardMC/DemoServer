package io.github.dockyard.demo.utils

import io.github.dockyard.demo.GameInstance
import io.github.dockyard.demo.GameManager
import io.github.dockyard.demo.items.GameItem
import io.github.dockyardmc.player.Player
import kotlin.reflect.KClass

fun Player.getInstance(): GameInstance? {
    return GameManager.games[this]
}

fun GameInstance.hasItem(item: KClass<out GameItem>): Boolean {
    return this.controller.items.map { gameItem -> gameItem::class }.contains(item)
}