package cz.cuni.gamedev.nail123.roguelike.mechanics;

import cz.cuni.gamedev.nail123.roguelike.blocks.GameBlock;
import cz.cuni.gamedev.nail123.roguelike.entities.enemies.Chest;
import cz.cuni.gamedev.nail123.roguelike.entities.enemies.Enemy;
import cz.cuni.gamedev.nail123.roguelike.entities.enemies.Orc;
import cz.cuni.gamedev.nail123.roguelike.entities.enemies.Rat;
import cz.cuni.gamedev.nail123.roguelike.entities.items.HealingPotion;
import cz.cuni.gamedev.nail123.roguelike.entities.items.Item;
import cz.cuni.gamedev.nail123.roguelike.entities.items.Sword;
import kotlin.Pair;
import kotlin.random.Random;

import java.util.*;
import java.util.function.Supplier;

public class LootSystem {
    // Store rng for convenience
    static Random rng = Random.Default;

    // Sword with power 1-3
    static OneDrop basicSword = new OneDrop(() -> new Sword(rng.nextInt(2) + 1));
    // Sword with power 5-6
    static OneDrop rareSword = new OneDrop(() -> new Sword(rng.nextInt(2) + 5));

    // healing flask with power 1-2
    static OneDrop flaskHealing = new OneDrop(() -> new HealingPotion(rng.nextInt(1) + 1));
    // Potion with power 3-4
    static OneDrop potionHealing = new OneDrop(() -> new HealingPotion(rng.nextInt(1) + 3));

    @SuppressWarnings("rawtypes")
    static Map<Class, ItemDrop> enemyDrops = new HashMap<>();

    static {
        enemyDrops.put(Rat.class, new TreasureClass(1, Arrays.asList(
                new Pair<>(2, NoDrop.getInstance()),
                new Pair<>(1, basicSword)
        )));
        enemyDrops.put(Orc.class, new TreasureClass(1, Arrays.asList(
                new Pair<>(7, NoDrop.getInstance()),
                new Pair<>(4, flaskHealing),
                new Pair<>(2, basicSword),
                new Pair<>(1, rareSword)
        )));
        enemyDrops.put(Chest.class, new TreasureClass(1, Arrays.asList(
                new Pair<>(5, NoDrop.getInstance()),
                new Pair<>(3, flaskHealing),
                new Pair<>(1, potionHealing),
                new Pair<>(1, rareSword)
        )));
    }

    public static void onDeath(Enemy enemy) {
        ItemDrop drop = enemyDrops.get(enemy.getClass());
        if (drop == null) return;
        for (Item item : drop.getDrops()) {
            GameBlock block = enemy.getArea().get(enemy.getPosition());
            if (block != null) {
                block.getEntities().add(item);
            }
        }
    }

    interface ItemDrop {
        List<Item> getDrops();
    }
    static class NoDrop implements ItemDrop {
        // Singleton pattern
        static NoDrop instance = null;
        private NoDrop() {}

        public static NoDrop getInstance() {
            if (instance == null) {
                instance = new NoDrop();
            }
            return instance;
        }

        @Override
        public List<Item> getDrops() { return new ArrayList<>(); }
    }
    static class OneDrop implements ItemDrop {
        Supplier<Item> instantatior;
        public OneDrop(Supplier<Item> instantiator) {
            this.instantatior = instantiator;
        }
        @Override
        public List<Item> getDrops() { return Collections.singletonList(instantatior.get()); }
    }
    static class TreasureClass implements ItemDrop {
        int numDrops;
        List<Pair<Integer, ItemDrop>> possibleDrops;
        int totalProb;

        public TreasureClass(int numDrops, List<Pair<Integer, ItemDrop>> possibleDrops) {
            this.numDrops = numDrops;
            this.possibleDrops = possibleDrops;
            for (Pair<Integer, ItemDrop> drop : possibleDrops) {
                totalProb += drop.getFirst();
            }
        }

        @Override
        public List<Item> getDrops() {
            ArrayList<Item> drops = new ArrayList<Item>();
            for (int i = 0; i < possibleDrops.size(); ++i) {
                drops.addAll(pickDrop().getDrops());
            }
            return drops;
        }

        private ItemDrop pickDrop() {
            int randNumber = Random.Default.nextInt(totalProb);
            for (Pair<Integer, ItemDrop> drop : possibleDrops) {
                randNumber -= drop.getFirst();
                if (randNumber < 0) return drop.getSecond();
            }
            // Never happens, but we need to place something here anyway
            return possibleDrops.get(possibleDrops.size() - 1).getSecond();
        }
    }
}

