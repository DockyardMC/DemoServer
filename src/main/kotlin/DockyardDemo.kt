package io.github.dockyard.demo

import cz.lukynka.shulkerbox.dockyard.ShulkerboxIntegration
import io.github.dockyardmc.DockyardServer
import io.github.dockyardmc.events.Events
import io.github.dockyardmc.events.PlayerJoinEvent
import io.github.dockyardmc.events.PlayerSpawnEvent
import io.github.dockyardmc.motd.ServerStatusManager
import io.github.dockyardmc.registry.Particles
import io.github.dockyardmc.registry.registries.Biome
import io.github.dockyardmc.registry.registries.BiomeParticles
import io.github.dockyardmc.registry.registries.BiomeRegistry
import io.github.dockyardmc.registry.registries.ParticleOptions
import io.github.dockyardmc.scheduler.runLaterAsync
import io.github.dockyardmc.world.customBiome

object DockyardDemo {
    lateinit var customBiome: Biome

    fun startServer() {
        val server = DockyardServer {
            withIp("0.0.0.0")
            withPort(25565)
            withMaxPlayers(25)
            withUpdateChecker(false)
        }

        customBiome = customBiome("dockyarddemo:game") {
            withParticles(BiomeParticles(ParticleOptions(Particles.ASH.identifier), 0.01f))
            withSkyColor("#8034eb")
            withFoliageColor("#547053")
            withGrassColor("#547053")
            withFogColor("#633000")
        }
        BiomeRegistry.addEntry(customBiome, true)

        ServerStatusManager.defaultDescription.value = "<#fc0356><b>DockyardMC </b><dark_gray>▶ <#ff8fb4>Demo Server\n<#ffff82>Little demo rogue-like minigame"

        ShulkerboxIntegration.load()
        HubInstance
        GameManager

        Events.on<PlayerSpawnEvent> { event ->
            event.world = HubInstance.world
        }

        Events.on<PlayerJoinEvent> { event ->
            val player = event.player
            if(player.username == "LukynkaCZE") {
                player.permissions.add("dockyard.admin")
                player.permissions.add("dockyard.*")
                player.permissions.add("demo.admin")
            }
            runLaterAsync(2) {
                HubInstance.join(player)
            }
        }

        server.start()
    }
}

fun main() {
    DockyardDemo.startServer()
}