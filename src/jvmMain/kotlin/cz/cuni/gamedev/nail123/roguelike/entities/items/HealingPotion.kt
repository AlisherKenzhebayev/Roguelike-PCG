package cz.cuni.gamedev.nail123.roguelike.entities.items

import cz.cuni.gamedev.nail123.roguelike.entities.Player
import cz.cuni.gamedev.nail123.roguelike.entities.attributes.HasInventory
import cz.cuni.gamedev.nail123.roguelike.tiles.GameTiles

class HealingPotion (val healingPower: Int): Potion(GameTiles.POTION){
    override fun onEquip(character: HasInventory) {
        if (character is Player) {
            character.hitpoints += healingPower
            character.inventory.remove(this);
        }
    }

    override fun onUnequip(character: HasInventory) {
        // Do nothing
    }

    override fun toString(): String {
        if(healingPower > 3){
            return "Healing potion($healingPower)"
        }else{
            return "Healing flask($healingPower)"
        }
    }
}