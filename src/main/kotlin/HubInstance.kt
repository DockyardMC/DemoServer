package io.github.dockyard.demo

import cz.lukynka.shulkerbox.dockyard.DockyardMap
import cz.lukynka.shulkerbox.dockyard.MapFileReader
import cz.lukynka.shulkerbox.dockyard.conversion.toDockyardMap
import io.github.dockyardmc.DockyardServer
import io.github.dockyardmc.apis.bossbar.Bossbar
import io.github.dockyardmc.apis.bossbar.BossbarColor
import io.github.dockyardmc.apis.bossbar.BossbarNotches
import io.github.dockyardmc.apis.sidebar.Sidebar
import io.github.dockyardmc.events.EventPool
import io.github.dockyardmc.events.WorldTickEvent
import io.github.dockyardmc.events.system.EventFilter
import io.github.dockyardmc.inventory.clearInventory
import io.github.dockyardmc.location.Location
import io.github.dockyardmc.player.Player
import io.github.dockyardmc.player.PlayerManager
import io.github.dockyardmc.player.systems.GameMode
import io.github.dockyardmc.player.tablist
import io.github.dockyardmc.registry.DimensionTypes
import io.github.dockyardmc.registry.PotionEffects
import io.github.dockyardmc.server.ServerMetrics
import io.github.dockyardmc.world.World
import io.github.dockyardmc.world.WorldManager
import io.github.dockyardmc.world.generators.VoidWorldGenerator
import java.io.File

object HubInstance {
    lateinit var world: World
    private lateinit var map: DockyardMap
    private lateinit var spawn: Location
    private lateinit var center: Location

    private lateinit var eventPool: EventPool

    private val bossbar = Bossbar("<aqua>https://github.com/DockyardMC/Dockyard", 1f, BossbarColor.BLUE, BossbarNotches.NO_NOTCHES)

    private val sidebar = Sidebar {
        setTitle("<aqua><bold>DockyardMC Demo")
        setGlobalLine("                               ")
        setPlayerLine { player -> " Player: <cyan>$player" }
        setGlobalLine(" players online")
        setGlobalLine(" ")
        setGlobalLine(" memory usage")
        setGlobalLine(" mspt world")
        setGlobalLine(" mspt global")
        setGlobalLine(" ")
        setGlobalLine("   <aqua>demo.lukynka.cloud")
    }

    private val tablist = tablist {
        addHeaderLine(" ")
        addHeaderLine(" ")
        addHeaderLine("<aqua><b>DockyardMC Demo Server")
        addHeaderLine(" ")

        addFooterLine(" ")
        addFooterLine("<gray>Press the play button in the middle of")
        addFooterLine("<gray>the map to start the demo minigame!")
        addFooterLine("                                                                ")
    }

    init {

        WorldManager.createWithFuture("hub", VoidWorldGenerator(DockyardDemo.customBiome), DimensionTypes.OVERWORLD).thenAccept { world ->
            this.world = world

            world.freezeTime = true
            world.time.value = 13000

            map = MapFileReader.read(File("./demo_map.shulker")).toDockyardMap(world.locationAt(0, 100, 0))
            map.placeSchematicAsync().thenAccept {
                spawn = map.getPoint("spawn").location
                center = map.getPoint("center").location
                world.defaultSpawnLocation = spawn
                map.spawnProps()
            }

            eventPool = EventPool.withFilter("hub-world-filter", EventFilter.containsWorld(HubInstance.world))

            eventPool.on<WorldTickEvent> { _ ->
                sidebar.setGlobalLine(14, " Online: <cyan>${PlayerManager.players.size}/25")
                sidebar.setGlobalLine(12, " Memory Usage: <lime>${ServerMetrics.memoryUsageTruncated}mb")
                sidebar.setGlobalLine(11, " MSPT (world): <yellow>${world.scheduler.mspt}ms")
                sidebar.setGlobalLine(10, " MSPT (global): <yellow>${DockyardServer.scheduler.mspt}ms")
            }
        }
    }

    fun join(player: Player) {
        player.teleport(spawn)
        player.clearInventory()
        player.clearPotionEffects()
        player.gameMode.value = GameMode.ADVENTURE
        player.experienceLevel.value = 0
        player.experienceBar.value = 0f
        player.canFly.value = false
        player.health.value = 20f
        player.food.value = 20.0
        player.addPotionEffect(PotionEffects.NIGHT_VISION, -1, 1, showParticles = false, showBlueBorder = false, showIconOnHud = false)
        player.addPotionEffect(PotionEffects.HUNGER, -1, 1, showParticles = false, showBlueBorder = false, showIconOnHud = false)
        player.addPotionEffect(PotionEffects.WITHER, -1, 1, showParticles = false, showBlueBorder = false, showIconOnHud = false)
        bossbar.addViewer(player)
        sidebar.viewers.add(player)
        tablist.addViewer(player)
    }

    fun leave(player: Player) {
        bossbar.removeViewer(player)
        sidebar.viewers.remove(player)
        tablist.removeViewer(player)
    }
}

val Player.isInHub: Boolean get() = this.world == HubInstance.world