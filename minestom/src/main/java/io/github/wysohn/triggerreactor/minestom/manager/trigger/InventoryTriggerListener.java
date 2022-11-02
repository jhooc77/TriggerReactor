/*******************************************************************************
 *     Copyright (C) 2018 wysohn
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *******************************************************************************/
package io.github.wysohn.triggerreactor.minestom.manager.trigger;

import io.github.wysohn.triggerreactor.core.bridge.IInventory;
import io.github.wysohn.triggerreactor.core.bridge.IItemStack;
import io.github.wysohn.triggerreactor.core.bridge.entity.IPlayer;
import io.github.wysohn.triggerreactor.core.manager.trigger.inventory.InventoryTriggerManager;
import io.github.wysohn.triggerreactor.minestom.main.MinestomTriggerReactorCore;
import net.minestom.server.event.Event;
import net.minestom.server.event.EventNode;
import net.minestom.server.event.inventory.InventoryCloseEvent;
import net.minestom.server.event.inventory.InventoryOpenEvent;
import net.minestom.server.event.inventory.InventoryPreClickEvent;
import net.minestom.server.inventory.Inventory;
import net.minestom.server.inventory.click.ClickType;
import net.minestom.server.item.ItemStack;

public class InventoryTriggerListener
        implements MinestomTriggerManager {

    private final InventoryTriggerManager<?> manager;

    public InventoryTriggerListener(InventoryTriggerManager<?> manager, EventNode<Event> node) {
        this.manager = manager;
        node.addListener(InventoryOpenEvent.class, event -> {
            Inventory inventory = event.getInventory();

            IInventory wrappedInventory = MinestomTriggerReactorCore.getWrapper().wrap(inventory);
            IPlayer wrappedPlayer = MinestomTriggerReactorCore.getWrapper().wrap(event.getPlayer());

            manager.onOpen(event, wrappedInventory, wrappedPlayer);
        }).addListener(InventoryPreClickEvent.class, event -> {
            if (event.getSlot() < 0)
                return;

            ItemStack clickedItem = event.getClickedItem();
            IItemStack wrappedItem = MinestomTriggerReactorCore.getWrapper().wrap(clickedItem);

            Inventory inventory = event.getInventory();
            IInventory wrappedInventory = MinestomTriggerReactorCore.getWrapper().wrap(inventory);

            if (event.getClickType() == ClickType.END_LEFT_DRAGGING
                    || event.getClickType() == ClickType.LEFT_DRAGGING
                    || event.getClickType() == ClickType.RIGHT_DRAGGING
                    || event.getClickType() == ClickType.START_LEFT_DRAGGING
                    || event.getClickType() == ClickType.START_RIGHT_DRAGGING
                    || event.getClickType() == ClickType.END_RIGHT_DRAGGING) {

                if (manager.hasInventoryOpen(MinestomTriggerReactorCore.getWrapper().wrap(inventory))) {
                    event.setCancelled(true);
                }
            }

            manager.onClick(event,
                    wrappedInventory,
                    wrappedItem,
                    event.getSlot(),
                    event.getClickType().name(),
                    event.getClickType() == ClickType.CHANGE_HELD ? event.getSlot() : -1,
                    event::setCancelled);
        }).addListener(InventoryCloseEvent.class, event -> {
            IPlayer minestomPlayer = MinestomTriggerReactorCore.getWrapper().wrap(event.getPlayer());
            manager.onInventoryClose(event, minestomPlayer, MinestomTriggerReactorCore.getWrapper().wrap(event.getInventory()));
        });
    }


}
