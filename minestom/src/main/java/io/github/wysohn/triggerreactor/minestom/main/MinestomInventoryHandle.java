package io.github.wysohn.triggerreactor.minestom.main;

import io.github.wysohn.triggerreactor.core.bridge.IInventory;
import io.github.wysohn.triggerreactor.core.bridge.IItemStack;
import io.github.wysohn.triggerreactor.core.main.IInventoryHandle;
import io.github.wysohn.triggerreactor.core.manager.trigger.inventory.InventoryTrigger;
import io.github.wysohn.triggerreactor.minestom.bridge.MinestomInventory;
import io.github.wysohn.triggerreactor.minestom.bridge.MinestomItemStack;
import net.minestom.server.inventory.Inventory;
import net.minestom.server.inventory.InventoryType;
import net.minestom.server.item.ItemStack;

public class MinestomInventoryHandle implements IInventoryHandle<ItemStack> {

    @Override
    public void fillInventory(InventoryTrigger trigger, int size, IInventory inventory) {
        Inventory inv = inventory.get();
        for (int i = 0; i < size; i++) {
            IItemStack item = trigger.getItems()[i];
            if (item != null) {
                inv.setItemStack(i, item.get());
            }
        }
    }

    @Override
    public Class<ItemStack> getItemClass() {
        return ItemStack.class;
    }

    @Override
    public IItemStack wrapItemStack(ItemStack item) {
        return new MinestomItemStack(item);
    }

    @Override
    public IInventory createInventory(int size, String name) {
        return new MinestomInventory(new Inventory(InventoryType.valueOf("CHEST_" + size/9 + "_ROW"), name));
    }
}
