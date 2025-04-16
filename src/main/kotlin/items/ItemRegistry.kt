package io.github.dockyard.demo.items

import io.github.dockyard.demo.GameInstance
import io.github.dockyard.demo.GameManager
import io.github.dockyardmc.commands.CommandException
import io.github.dockyardmc.commands.Commands
import io.github.dockyardmc.commands.IntArgument
import io.github.dockyardmc.commands.StringArgument
import io.github.dockyardmc.player.Player
import kotlin.reflect.KClass
import kotlin.reflect.full.primaryConstructor

object ItemRegistry {
    val items: MutableMap<String, KClass<out GameItem>> = mutableMapOf()

    fun add(item: KClass<out GameItem>): KClass<out GameItem> {
        val name = item.simpleName!!
        items[name] = item
        return item
    }

    init {
        add(AppleItem::class)
        add(SweepingEdgeItem::class)
        add(RuneOfCritRate::class)
        add(RuneOfCritDamage::class)
        add(HealthyStewItem::class)
//        add(PikachusTailItem::class)
        add(TurtleShellItem::class)
        add(SuperchardgedItem::class)
        add(PiercingAxe::class)
        add(BloodstainedSwordItem::class)
        add(FloatyFeather::class)
        add(WindChargedItem::class)
        add(RecycleItem::class)

        registerCommands()
    }

    private fun suggestItems(player: Player): List<String> {
        return items.keys.toList()
    }

    private fun registerCommands() {
        Commands.add("/item") {
            withPermission("dd.admin")

            addSubcommand("give") {
                addArgument("item", StringArgument(), ::suggestItems)
                execute { ctx ->
                    val player = ctx.getPlayerOrThrow()
                    val item = items[getArgument("item")] ?: throw CommandException("No item with that class name exists!")
                    val instance = GameManager.games[player] ?: throw CommandException("You are not in game")
                    val itemObject = item.primaryConstructor!!.call()
                    instance.controller.items.add(itemObject)
                    itemObject.onObtain(instance)
                }
            }
        }

        Commands.add("/money") {
            withPermission("dd.admin")

            addSubcommand("set") {
                addArgument("amount", IntArgument())
                execute { ctx ->
                    val player = ctx.getPlayerOrThrow()
                    val amount = getArgument<Int>("amount")
                    val instance = GameManager.games[player] ?: throw CommandException("You are not in game")
                    instance.money.value = amount
                }
            }
        }
    }
}