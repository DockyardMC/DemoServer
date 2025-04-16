package io.github.dockyard.demo.utils

import io.github.dockyardmc.entity.Entity
import io.github.dockyardmc.location.Location
import io.github.dockyardmc.world.block.Block

fun Entity.getFloorLocation(): Pair<Block, Location>? {
    val minY = world.dimensionType.minY
    val startY = this.location.blockY

    for (y in startY downTo minY) {
        val blockLoc = world.locationAt(this.location.blockX, y, this.location.blockZ)
        val block = world.getBlock(blockLoc)
        if (!block.isAir()) {
            return block to blockLoc
        }
    }
    return null
}