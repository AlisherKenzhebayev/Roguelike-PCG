package cz.cuni.gamedev.nail123.roguelike.world.worlds;

import cz.cuni.gamedev.nail123.roguelike.GameConfig;
import cz.cuni.gamedev.nail123.roguelike.blocks.*;
import cz.cuni.gamedev.nail123.roguelike.entities.GameEntity;
import cz.cuni.gamedev.nail123.roguelike.entities.enemies.Chest;
import cz.cuni.gamedev.nail123.roguelike.entities.enemies.Orc;
import cz.cuni.gamedev.nail123.roguelike.entities.enemies.Rat;
import cz.cuni.gamedev.nail123.roguelike.entities.objects.Stairs;
import cz.cuni.gamedev.nail123.roguelike.events.LoggedEvent;
import cz.cuni.gamedev.nail123.roguelike.mechanics.Pathfinding;
import cz.cuni.gamedev.nail123.roguelike.world.Area;
import cz.cuni.gamedev.nail123.roguelike.world.World;
import cz.cuni.gamedev.nail123.roguelike.world.builders.AreaBuilder;
import cz.cuni.gamedev.nail123.roguelike.world.builders.wavefunctioncollapse.WFCAreaBuilder;
import org.hexworks.zircon.api.data.Position3D;
import org.hexworks.zircon.api.data.Size3D;
import org.hexworks.zircon.api.data.Tile;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class SampleJavaWorld extends World {
    int currentLevel = 0;
    Random randomGen;

    public SampleJavaWorld() {
    }

    @NotNull
    @Override
    public Area buildStartingArea() {
        randomGen = new Random();

        Area retVal = buildLevel();
        return retVal;
    }

    private Area fixEntities(Area area, List<GameEntity> entities) {
        for (var t : entities)
        {
            // First try updating the tilemap
            t.getBlock().updateTileMap();

            // That doesnt help -> try adding the stairs back.
            var tile = t.getTile().asGraphicTile();
            if(tile.isEmpty()){
                continue;
            }
            
            if(tile.get().getName().contains("tairs "))
                area.addEntity(t, t.getPosition());
        }

        return area;
    }

    private int countNonPassable(Area area) {
        int retVal = 0;

        // Color with red unreachable elements
        var entities = area.getEntities();
        entities.add(area.getPlayer());

        Map<Position3D, GameBlock> areaBlocks = area.getBlocks();
        Map<Position3D, GameBlock> mapGameBlocks = new HashMap<Position3D, GameBlock>();

        //Filter out entities.
        for (var keyV: areaBlocks.keySet())
        {
            boolean hasSame = false;
            for (var listV : entities) {
                if (keyV == listV.getPosition()) {
                    hasSame = true;
                    break;
                }
            }
            if(hasSame){
                continue;
            }
            mapGameBlocks.put(keyV, areaBlocks.get(keyV));
        }

        for (Position3D tilePos : mapGameBlocks.keySet()){
            GameBlock currentGB = mapGameBlocks.get(tilePos);

            if(area.fetchBlockAt(tilePos).isEmpty()){
                continue;
            }

            if(currentGB.getBaseTile() == Tile.empty()){
                continue;
            }

            if(currentGB.getBaseTile().isEmpty()){
                continue;
            }

            //System.out.println(currentGB.getBaseTile().asCharacterTile());

            var tile = currentGB.getBaseTile().asCharacterTile();
            if(tile.isEmpty()){
                continue;
            }

            if (tile.get().getCharacter() == 'x') {
                //System.out.println(currentGB.getBaseTile().asCharacterTile());
                retVal++;
            }
        }
        System.out.println(retVal + " " + area.getEntities().toArray().length);

        return retVal;
    }

    private Area colorNotPassable(Area area, Position3D position) {
        // Color with red unreachable elements
        var entities = area.getEntities();
        entities.add(area.getPlayer());

        Map<Position3D, GameBlock> areaBlocks = area.getBlocks();
        Map<Position3D, GameBlock> mapGameBlocks = new HashMap<Position3D, GameBlock>();

        //Filter out entities.
        for (var keyV: areaBlocks.keySet())
        {
            boolean hasSame = false;
            for (var listV : entities) {
                if (keyV == listV.getPosition()) {
                    hasSame = true;
                    break;
                }
            }
            if(hasSame){
                continue;
            }
            mapGameBlocks.put(keyV, areaBlocks.get(keyV));
        }

        // Color the unreachable nodes with red?
        for (Position3D tilePos : mapGameBlocks.keySet()){
            GameBlock currentGB = mapGameBlocks.get(tilePos);

            if(area.fetchBlockAt(tilePos).isEmpty()){
                continue;
            }

            if(currentGB.getBaseTile() == Tile.empty()){
                continue;
            }

            //System.out.println(currentGB.getBaseTile().asCharacterTile());

            if(isGameBlockUnreachable(currentGB)){
                setBlock(area, tilePos, new FloorNonTrav());
            }
        }

        return area;
    }

    private boolean isGameBlockUnreachable(GameBlock currentGB) {
        var tile = currentGB.getBaseTile().asCharacterTile();
        if(tile.isEmpty()){
            return false;
        }
        if(!(tile.get().getCharacter() == '*')
            && !(tile.get().getCharacter() == '#')){
            return true;
        }
        return false;
    }

    private Area colorPassable(Area area, Position3D playerPosition3D) {
        var entities = area.getEntities();
        entities.add(area.getPlayer());

        var floodFill = Pathfinding.INSTANCE.floodFill(
                playerPosition3D,
                area,
                Pathfinding.INSTANCE.getEightDirectional(),
                Pathfinding.INSTANCE.getDoorOpening()
        );

        Map<Position3D, Integer> mapFlood = new HashMap<Position3D, Integer>();
        //Filter out entities.
        for (var keyV: floodFill.keySet())
        {
            boolean hasSame = false;
            for (var listV : entities) {
                if (keyV == listV.getPosition()) {
                    hasSame = true;
                    break;
                }
            }
            if(hasSame){
                continue;
            }
            mapFlood.put(keyV, floodFill.get(keyV));
        }

        // Color the tiles, connected to the player position/traversable
        for (Position3D tilePos : mapFlood.keySet()) {
            var block = area.fetchBlockAt(tilePos);
            if(block.isEmpty()) {
                continue;
            }
            var graphic = block.get().getBaseTile().asGraphicTile();
            if(!graphic.isEmpty()) {
                System.out.println(graphic.get().getName());
                if (graphic.get().getName().contains("tairs ")) {
                    //Do nothing ;
                }
            }

            setBlock(area, tilePos, new FloorTrav());
        }
        return area;
    }

    private Area colorDefault(Area area, Position3D playerPosition3D) {
        var entities = area.getEntities();
        entities.add(area.getPlayer());

        var floodFill = Pathfinding.INSTANCE.floodFill(
                playerPosition3D,
                area,
                Pathfinding.INSTANCE.getEightDirectional(),
                Pathfinding.INSTANCE.getDoorOpening()
        );

        Map<Position3D, Integer> mapFlood = new HashMap<Position3D, Integer>();
        //Filter out entities.
        for (var keyV: floodFill.keySet())
        {
            boolean hasSame = false;
            for (var listV : entities) {
                if (keyV == listV.getPosition()) {
                    hasSame = true;
                    break;
                }
            }
            if(hasSame){
                continue;
            }
            mapFlood.put(keyV, floodFill.get(keyV));
        }

        // Color the tiles, connected to the player position/traversable
        for (Position3D tilePos : mapFlood.keySet()) {
            if(area.fetchBlockAt(tilePos).isEmpty()) {
                continue;
            }
            {
                setBlock(area, tilePos, new Floor());
            }
        }
        return area;
    }

    private Area postProcess(Area area, Position3D playerPosition3D) {
        // Post-processing step, 1. - Add connectivity
        // Everything is 'colored' the way I want it already, so I just need to check if the
        // random walk is able to bump into unreachable

        var entities = area.getEntities();
        entities.add(area.getPlayer());

        Set<Position3D> positions = new HashSet<>();
        Map<Position3D, GameBlock> areaBlocks = area.getBlocks();
        Map<Position3D, GameBlock> mapGameBlocks = new HashMap<Position3D, GameBlock>();

        //Filter out entities.
        for (var keyV: areaBlocks.keySet())
        {
            boolean hasSame = false;
            for (var listV : entities) {
                if (keyV == listV.getPosition()) {
                    hasSame = true;
                    break;
                }
            }
            if(hasSame){
                continue;
            }
            mapGameBlocks.put(keyV, areaBlocks.get(keyV));
        }

        // Color the unreachable nodes with red?
        for (Position3D tilePos : mapGameBlocks.keySet()){
            GameBlock currentGB = mapGameBlocks.get(tilePos);

            if(area.fetchBlockAt(tilePos).isEmpty()){
                continue;
            }

            if(currentGB.getBaseTile() == Tile.empty()){
                continue;
            }

            //System.out.println(currentGB.getBaseTile().asCharacterTile());

            if(isGameBlockUnreachable(currentGB)){
                positions.add(tilePos);
            }
        }

        var floodFill = Pathfinding.INSTANCE.floodFill(
                playerPosition3D,
                area,
                Pathfinding.INSTANCE.getEightDirectional(),
                Pathfinding.INSTANCE.getDoorOpening()
        );

        var maxDis = floodFill.values().stream().max((o1, o2) -> o1.compareTo(o2));
        int maxDist = Integer.MAX_VALUE;
        if(maxDis.isPresent()){
            maxDist = maxDis.get();
        }
        var listPos = floodFill.keySet();
        int finalMaxDist = maxDist;
        var filtered = positions.stream().sorted((o1, o2) -> distance(o1, playerPosition3D).compareTo(distance(o2, playerPosition3D))).toArray(); //listPos.stream().filter(position3D -> floodFill.get(position3D) >= finalMaxDist - 10).toArray();
        var startPos = (Position3D) filtered[randomGen.nextInt(filtered.length)];

        var listPosDig = bfsWalk(area, startPos);
        area = digNewTunnel(area, listPosDig, startPos);

        return area;
    }

    private Double distance(Position3D o1, Position3D playerPosition3D) {
        var difY = (o1.getY() - playerPosition3D.getY());
        var difX = (o1.getX() - playerPosition3D.getX());

        return Math.sqrt(difY * difY + difX * difX);
    }

    private Area digNewTunnel(Area area, Map<Position3D, Position3D> listPosDig, Position3D startPos) {
        if(listPosDig == null){
            System.out.println("Null list dig");
            return area;
        }

        Position3D curPos = startPos;
        while (curPos != null)
        {
            if (isGameBlockDiggable(area.getBlocks().get(curPos))) {
                setBlock(area, curPos, new Floor());
            }
            curPos = listPosDig.get(curPos);
        }

        return area;
    }

    private void setBlock(Area area, Position3D curPos, GameBlock floor) {
        area.setBlockAt(curPos, floor);
    }

    private boolean isGameBlockDiggable(GameBlock currentGB) {
        var graphTile = currentGB.getBaseTile().asGraphicTile();
        if(!graphTile.isEmpty()){
//            System.out.println(graphTile.get().getName());
            if(graphTile.get().getName().contains("tairs ")){
                return false;
            }
        }

        var tile = currentGB.getBaseTile().asCharacterTile();
        if(tile.isEmpty()){
            return true;
        }
        if(!(tile.get().getCharacter() == '*')){
            return true;
        }
//        if(!(currentGB instanceof FloorTrav)){
//            return true;
//        }
        return false;
    }

    // Basically, a map of path. - <Current, Next>
    private Map<Position3D, Position3D> bfsWalk(Area area, Position3D startPos) {
        Position3D foundPos = null;
        Map<Position3D, Position3D> retMap = new HashMap<Position3D, Position3D>();
        Map<Position3D, Position3D> parent = new HashMap<Position3D, Position3D>();

        Queue<Position3D> fringe = new ArrayDeque<>();
        int[][] visited = new int[area.getWidth()][area.getHeight()];
        fringe.add(startPos);
        parent.put(startPos, null);

        while (!fringe.isEmpty()){
            var currentPos = fringe.remove();

            // fail check
            if(!positionValid(area, currentPos)){
                break;
            }

            visited[currentPos.getX()][currentPos.getY()] = 1;

            // fail check
            if(area.fetchBlockAt(currentPos).isEmpty()){
                break;
            }

            // Check current position
            if (isGameBlockReachable(area.fetchBlockAt(currentPos).get())) {
                foundPos = currentPos;
                System.out.println(foundPos.toString() + retMap);
                break;
            }

            boolean moved[] = new boolean[4];
            // Move in allowed directions for fringe expansion
            for (int i = 0; i < 4; i++){
                var dir = randomGen.nextInt(4);
                while (true){
                    if(!moved[dir]){
                        break;
                    }
                    dir = randomGen.nextInt(4);
                }

                moved[dir] = true;
                var nextPos = directionMove(currentPos, dir);
                if(!positionValid(area, nextPos)){
                    continue;
                }
                if(visited[nextPos.getX()][nextPos.getY()] > 0){
                    continue;
                }
                parent.put(nextPos, currentPos);
                fringe.add(nextPos);
                visited[nextPos.getX()][nextPos.getY()] = 1;
            }
        }

        if(foundPos == null){
            return retMap;
        }

        // If some field was found, build a clean map
        while(parent.get(foundPos) != null){
            var predecessor = parent.get(foundPos);
            retMap.put(predecessor, foundPos);

            foundPos = predecessor;
        }

        return retMap;
    }

    private boolean positionValid(Area area, Position3D nextPos) {
        if(nextPos.getX() < 0 || nextPos.getX() >= area.getWidth()){
            return false;
        }
        if(nextPos.getY() < 0 || nextPos.getY() >= area.getHeight()){
            return false;
        }

        return true;
    }

    private boolean isGameBlockReachable(GameBlock currentGB) {
        if(currentGB instanceof FloorTrav){
            return true;
        }
        return false;
    }

    private Position3D directionMove(Position3D curPos, int i) {
        Position3D retVal = curPos;

        var x = curPos.getX();
        var y = curPos.getY();

        int rand = i % 4;
        switch (rand){
            case 0:
                retVal = Position3D.create(x + 1, y, 0);
                break;
            case 1:
                retVal = Position3D.create(x, y + 1, 0);
                break;
            case 2:
                retVal = Position3D.create(x - 1, y, 0);
                break;
            case 3:
                retVal = Position3D.create(x, y - 1, 0);
                break;
        }
        return retVal;
    }

    Area buildLevel() {
        Size3D areaSize = Size3D.create(
                GameConfig.WINDOW_WIDTH - GameConfig.SIDEBAR_WIDTH,
                GameConfig.WINDOW_HEIGHT - GameConfig.LOG_AREA_HEIGHT, 1
        );

        // Start with an empty area
        AreaBuilder areaBuilder = (new WFCAreaBuilder(areaSize, areaSize)).create();

        // Place the player at an empty location in the top-left quarter
        areaBuilder.addAtEmptyPosition(
                areaBuilder.getPlayer(),
                Position3D.create(1, 1, 0),
                Size3D.create(areaBuilder.getWidth() / 2 - 2, areaBuilder.getHeight() / 2 - 2, 1)
        );

        // Place the stairs at an empty location in the top-right quarter
        areaBuilder.addAtEmptyPosition(
                new Stairs(),
                Position3D.create(areaBuilder.getWidth() / 2, areaBuilder.getHeight() / 2, 0),
                Size3D.create(areaBuilder.getWidth() / 2 - 2, areaBuilder.getHeight() / 2 - 2, 1)
        );

        // TODO: Add stairs up

        // Add some random rats
        for (int i = 0; i <= currentLevel; ++i) {
            areaBuilder.addAtEmptyPosition(new Rat(), Position3D.defaultPosition(), areaBuilder.getSize());
        }

        // Add some orcs to each level, starting with level 2
        if(currentLevel >= 2) {
            for (int i = 0; i <= currentLevel; ++i) {
                areaBuilder.addAtEmptyPosition(new Orc(), Position3D.defaultPosition(), areaBuilder.getSize());
            }
        }

        // Add some chests to each level
        for (int i = 0; i <= currentLevel + 2; ++i) {
            areaBuilder.addAtEmptyPosition(new Chest(), Position3D.defaultPosition(), areaBuilder.getSize());
        }

        // Build it into a full Area
        var retVal = areaBuilder.build();


        var entities = retVal.getEntities();

        retVal = colorPassable(retVal, retVal.getPlayer().getPosition());
        retVal = colorNotPassable(retVal, retVal.getPlayer().getPosition());

        while (countNonPassable(retVal) > 0)
        {
            retVal = postProcess(retVal, retVal.getPlayer().getPosition());
            retVal = colorPassable(retVal, retVal.getPlayer().getPosition());
            retVal = colorNotPassable(retVal, retVal.getPlayer().getPosition());
        }

        // Fix the colors
        retVal = colorDefault(retVal, retVal.getPlayer().getPosition());
        // Fix the entities
        retVal = fixEntities(retVal, entities);

        // Fix up the tilemap
        retVal.getBlocks().forEach((position3D, gameBlock) ->
        {
            gameBlock.updateTileMap();
        });


        return retVal;
    }

    /**
     * Moving down - goes to a brand new level.
     */
    @Override
    public void moveDown() {
        ++currentLevel;
        (new LoggedEvent(this, "Descended to level " + (currentLevel + 1))).emit();
        if (currentLevel >= getAreas().getSize()) getAreas().add(buildLevel());
        goToArea(getAreas().get(currentLevel));
    }

    /**
     * Moving up would be for revisiting past levels, we do not need that. Check [DungeonWorld] for an implementation.
     */
    @Override
    public void moveUp() {
        // Not implemented
    }
}
