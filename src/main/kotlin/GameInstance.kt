package io.github.dockyard.demo

import cz.lukynka.bindables.Bindable
import cz.lukynka.bindables.BindablePool
import cz.lukynka.shulkerbox.dockyard.DockyardMap
import cz.lukynka.shulkerbox.dockyard.MapFileReader
import cz.lukynka.shulkerbox.dockyard.conversion.toDockyardMap
import io.github.dockyardmc.apis.bossbar.Bossbar
import io.github.dockyardmc.apis.bossbar.BossbarColor
import io.github.dockyardmc.apis.bossbar.BossbarNotches
import io.github.dockyardmc.events.EventPool
import io.github.dockyardmc.events.system.EventFilter
import io.github.dockyardmc.inventory.clearInventory
import io.github.dockyardmc.inventory.give
import io.github.dockyardmc.item.ItemStack
import io.github.dockyardmc.location.Location
import io.github.dockyardmc.maths.Percentage
import io.github.dockyardmc.player.Player
import io.github.dockyardmc.player.systems.GameMode
import io.github.dockyardmc.registry.Attributes
import io.github.dockyardmc.registry.DimensionTypes
import io.github.dockyardmc.registry.Items
import io.github.dockyardmc.registry.PotionEffects
import io.github.dockyardmc.world.World
import io.github.dockyardmc.world.WorldManager
import io.github.dockyardmc.world.generators.VoidWorldGenerator
import java.io.File
import java.util.*

class GameInstance(val player: Player) {

    companion object {
        val sword = ItemStack(Items.WOODEN_SWORD).withDisplayName("<white><u>Basic Sword").withLore(" ", "<gray>Kill em all", "<white><bold>STARTER ITEM")
    }

    val isReady: Bindable<Boolean> = Bindable(false)
    val state: Bindable<State> = Bindable(State.GAME_LOADING)
    val readyPlayerQueue: MutableList<Player> = mutableListOf()
    val uuid = UUID.randomUUID().toString().lowercase()

    lateinit var world: World
    lateinit var map: DockyardMap
    lateinit var spawn: Location
    lateinit var center: Location

    lateinit var eventPool: EventPool
    private val bindablePool = BindablePool()

    private val bossbar = Bossbar("<aqua><bold>Loading!</b> <white>The game is loading..", 1f, BossbarColor.BLUE, BossbarNotches.NO_NOTCHES)

    var playerDamage: Float = 1f
    var playerCritRate: Percentage = Percentage(10.0)
    var playerCritDamage: Percentage = Percentage(30.0)
    var monsterSpeed: Percentage = Percentage(60.0)
    var playerDodge: Percentage = Percentage(0.0)
    var monsterHealth: Percentage = Percentage(100.0)
    var shopPriceMultiplier: Float = 1f
    var playerMaxHealth: Bindable<Int> = bindablePool.provideBindable(6)

    var money: Bindable<Int> = bindablePool.provideBindable(0)

    val controller = GameController(this)
    val shop = Shop(this)

    init {
        monsterHealth.min = 15.0
        monsterSpeed.max = 100.0
        playerCritDamage.min = 0.0
        playerCritRate.min = 0.0
        playerCritRate.max = 100.0

        WorldManager.createWithFuture("game_${player.username.lowercase()}_${uuid}", VoidWorldGenerator(DockyardDemo.customBiome), DimensionTypes.OVERWORLD).thenAccept { world ->
            this.world = world

            world.freezeTime = true
            world.time.value = 13000

            map = MapFileReader.read(File("./demo_map.shulker")).toDockyardMap(world.locationAt(0, 100, 0))
            map.placeSchematicAsync().thenAccept {
                spawn = map.getPoint("spawn").location
                center = map.getPoint("center").location
                world.defaultSpawnLocation = spawn
                map.spawnProps()

                isReady.value = true
                readyPlayerQueue.forEach { player ->
                    this.join(player)
                }

                controller.registerController()
            }

            eventPool = EventPool.withFilter("world-filter", EventFilter.containsWorld(world))

//            eventPool.on<WorldTickEvent> { _ ->
//                sidebar.setGlobalLine(14, " Online: <cyan>${PlayerManager.players.size}/25")
//                sidebar.setGlobalLine(12, " Memory Usage: <lime>${ServerMetrics.memoryUsageTruncated}mb")
//                sidebar.setGlobalLine(11, " MSPT (world): <yellow>${world.scheduler.mspt}ms")
//                sidebar.setGlobalLine(10, " MSPT (global): <yellow>${DockyardServer.scheduler.mspt}ms")
//            }

            state.valueChanged { event ->
                updateBossbar(event.newValue)
            }

            playerMaxHealth.valueChanged { event ->
                player.attributes[Attributes.MAX_HEALTH].base.value = playerMaxHealth.value.toDouble()
            }

            money.valueChanged { event ->
                val diff = event.oldValue - event.newValue
                val diffString = if (diff > 0) {
                    "-${diff}"
                } else if (diff < 0) {
                    "+${-diff}"
                } else {
                    "0"
                }
                player.sendActionBar("<gold>You have $${event.newValue} <yellow>($diffString)")
            }

            world.time.value = 13000
        }
    }

    fun updateBossbar(state: State) {
        when (state) {
            State.GAME_LOADING -> {} // no need, it will already be in loading state
            State.INTRO -> {
                bossbar.color.value = BossbarColor.YELLOW
                bossbar.title.value = "<yellow><bold>Tutorial! <white>Lets learn how to play"
            }

            State.SHOP_ACTIVE -> {
                bossbar.color.value = BossbarColor.GREEN
                bossbar.title.value = "<lime><bold>Shop Active! <white>Right Click items to purchase them"
            }

            State.GAME_PLAY -> {
                bossbar.color.value = BossbarColor.RED
                bossbar.title.value = "<red><bold>They are here! <white>Defeat all the monsters!"
            }

            State.GAME_OVER -> {
                bossbar.color.value = BossbarColor.RED
                bossbar.title.value = "<red><bold>Game Over! <white>womp womp"
            }
        }
    }

    fun join(player: Player) {
        if (!isReady.value) {
            readyPlayerQueue.add(player)
            return
        }

        HubInstance.leave(player)
        player.sendMessage("<aqua><bold>Instance <dark_gray>● <gray>Joining instance <cyan>${uuid.substring(0, 8)}")
        player.teleport(spawn)
        player.clearInventory()
        player.clearPotionEffects()
        player.gameMode.value = GameMode.ADVENTURE
        player.experienceLevel.value = 0
        player.experienceBar.value = 0f
        player.canFly.value = false
        player.health.value = playerMaxHealth.value.toFloat()
        player.food.value = 20.0
        player.give(sword)
        player.addPotionEffect(PotionEffects.NIGHT_VISION, -1, 1, showParticles = false, showBlueBorder = false, showIconOnHud = false)
        bossbar.addViewer(player)

        controller.startWave()

//        state.value = State.INTRO
//        world.sendMessage(" ")
//        world.sendMessage(" ")
//        world.sendMessage(" <white>Welcome to the DockyardMC demo minigame! This is a")
//        world.sendMessage(" <yellow>rogue-like<white> where you <red>fight against waves of monsters<white>.")
//        world.sendMessage(" <white>After every round, you will be able to buy passive")
//        world.sendMessage(" <white>items in the shop! <lime>Good luck!")
//        world.sendMessage(" ")
//        world.sendMessage(" ")
//        world.playSound(Sounds.ENTITY_CHICKEN_EGG, volume = 1f, pitch = 0.8f)
//
//        world.scheduler.runLater(3.seconds) {
//            controller.startWave()
//        }

        playerMaxHealth.triggerUpdate()
    }

    enum class State {
        GAME_LOADING,
        INTRO,
        SHOP_ACTIVE,
        GAME_PLAY,
        GAME_OVER
    }
}