package io.github.dockyard.demo.events

import io.github.dockyard.demo.GameInstance
import io.github.dockyardmc.events.CancellableEvent
import io.github.dockyardmc.events.Event
import io.github.dockyardmc.player.Player

data class PlayerDoubleJumpEvent(val player: Player, val instance: GameInstance, override val context: Event.Context): CancellableEvent()