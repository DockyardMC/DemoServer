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
        add(RuneOfCrit::class)
        add(HealthyStewItem::class)
        add(PikachusTailItem::class)
    }
}