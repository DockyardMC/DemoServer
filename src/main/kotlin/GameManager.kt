package io.github.dockyard.demo

import io.github.dockyardmc.commands.Commands

object GameManager {

    init {
        Commands.add("/play") {
            execute { ctx ->
                val player = ctx.getPlayerOrThrow()
                val instance = GameInstance(player)
                instance.join(player)
            }
        }
    }

}