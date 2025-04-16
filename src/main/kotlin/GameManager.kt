package io.github.dockyard.demo

import io.github.dockyardmc.commands.Commands
import io.github.dockyardmc.events.Events
import io.github.dockyardmc.events.PlayerLeaveEvent
import io.github.dockyardmc.player.Player

object GameManager {

    val games: MutableMap<Player, GameInstance> = mutableMapOf()

    init {
        Commands.add("/play") {
            execute { ctx ->
                val player = ctx.getPlayerOrThrow()
                val instance = GameInstance(player)
                games[player] = instance
                instance.join(player)
            }
        }

        Events.on<PlayerLeaveEvent> { event ->
            val player = event.player
            games[player]?.dispose()
            games.remove(player)
        }
    }
}