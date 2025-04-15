package io.github.dockyard.demo.items

import kotlin.reflect.KClass

object ItemRegistry {
    val items: MutableSet<KClass<out GameItem>> = mutableSetOf()

    fun add(item: KClass<out GameItem>): KClass<out GameItem> {
        items.add(item)
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
    }
}