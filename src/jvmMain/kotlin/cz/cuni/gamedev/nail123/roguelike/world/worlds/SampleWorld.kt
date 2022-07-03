package cz.cuni.gamedev.nail123.roguelike.world.worlds

import cz.cuni.gamedev.nail123.roguelike.entities.enemies.Rat
import cz.cuni.gamedev.nail123.roguelike.entities.objects.Stairs
import cz.cuni.gamedev.nail123.roguelike.entities.unplacable.FogOfWar
import cz.cuni.gamedev.nail123.roguelike.events.logMessage
import cz.cuni.gamedev.nail123.roguelike.mechanics.Pathfinding
import cz.cuni.gamedev.nail123.roguelike.world.Area
import cz.cuni.gamedev.nail123.roguelike.world.World
import cz.cuni.gamedev.nail123.roguelike.world.builders.EmptyAreaBuilder
import org.hexworks.zircon.api.data.Position3D
import org.hexworks.zircon.api.data.Size3D

/**
 * Sample world, made as a starting point for creating custom worlds.
 * It consists of separate levels - each one has one staircase, and it leads infinitely deep.
 */
class SampleWorld: World() {
    var currentLevel = 0

    override fun buildStartingArea() = buildLevel()

    /**
     * Builds one of the levels.
     */
    fun buildLevel(): Area {
        // Start with an empty area
        val areaBuilder = EmptyAreaBuilder().create()

        // Place the player at an empty location in the top-left quarter
        areaBuilder.addAtEmptyPosition(
                areaBuilder.player,
                Position3D.create(1, 1, 0),
                Size3D.create(areaBuilder.width / 2 - 2, areaBuilder.height / 2 - 2, 1)
        )
        // Place the stairs at an empty location in the top-right quarter
        areaBuilder.addAtEmptyPosition(
                Stairs(),
                Position3D.create(areaBuilder.width / 2, areaBuilder.height / 2, 0),
                Size3D.create(areaBuilder.width / 2 - 2, areaBuilder.height / 2 - 2, 1)
        )

        // TODO: add the stairs up

        // TODO: some post-processing
        var floodFill = Pathfinding.floodFill(this.player.position, currentArea);

        // Add some rats to each level
        repeat(currentLevel + 1) {
            areaBuilder.addAtEmptyPosition(Rat(), Position3D.defaultPosition(), areaBuilder.size)
        }
        // We add fog of war such that exploration is needed
//        areaBuilder.addEntity(FogOfWar(), Position3D.unknown())

        // Build it into a full Area
        return areaBuilder.build()
    }

    /**
     * Moving down - goes to a brand new level.
     */
    override fun moveDown() {
        ++currentLevel
        this.logMessage("Descended to level ${currentLevel + 1}")
        if (currentLevel >= areas.size) areas.add(buildLevel())
        goToArea(areas[currentLevel])
    }

    /**
     * Moving up would be for revisiting past levels, we do not need that. Check [DungeonWorld] for an implementation.
     */
    override fun moveUp() {
        // Not implemented
    }
}