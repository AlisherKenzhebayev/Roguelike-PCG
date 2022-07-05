package cz.cuni.gamedev.nail123.roguelike.entities.enemies;

import cz.cuni.gamedev.nail123.roguelike.entities.attributes.HasVision;
import cz.cuni.gamedev.nail123.roguelike.mechanics.LootSystem;
import cz.cuni.gamedev.nail123.roguelike.mechanics.NavigationKt;
import cz.cuni.gamedev.nail123.roguelike.mechanics.Vision;
import cz.cuni.gamedev.nail123.roguelike.tiles.GameTiles;
import org.hexworks.zircon.api.data.Position3D;

import java.util.List;

public class Chest extends Enemy implements HasVision {
    protected int hitpoints = getMaxHitpoints();
    protected int attack = 0;
    protected int defense = 1;
    protected boolean hasSeenPlayer = false;
    protected boolean skipFrame = false;

    public Chest() {
        super(GameTiles.INSTANCE.getCHEST());
    }

    @Override public boolean getBlocksVision() { return false; }
    @Override public int getMaxHitpoints() { return 1; }
    @Override public int getHitpoints() { return this.hitpoints; }
    @Override public void setHitpoints(int hitpoints) { this.hitpoints = hitpoints; }
    @Override public int getAttack() { return attack; }
    @Override public void setAttack(int attack) { this.attack = attack; }
    @Override public int getDefense() { return this.defense; }
    @Override public void setDefense(int defense) { this.defense = defense; }
    @Override public int getVisionRadius() { return 15; }

    @Override
    public void update() {
        // Get the player position
        Position3D playerPosition = getArea().getPlayer().getPosition();

        List<Position3D> visible = Vision.INSTANCE.getVisiblePositionsFrom(getArea(), getPosition(), getVisionRadius());
        boolean canSeePlayer = visible.contains(playerPosition);

        if (canSeePlayer) hasSeenPlayer = true;

        if (hasSeenPlayer) {

            if (skipFrame) {
                // Do nothing
            } else {
                NavigationKt.goBlindlyFrom(this, playerPosition);
            }
        }

        skipFrame = !skipFrame;
    }

    @Override
    public void die() {
        super.die();
        LootSystem.onDeath(this);
    }
}

