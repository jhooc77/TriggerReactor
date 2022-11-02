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

import io.github.wysohn.triggerreactor.core.bridge.entity.IPlayer;
import io.github.wysohn.triggerreactor.core.manager.trigger.Trigger;
import io.github.wysohn.triggerreactor.core.manager.trigger.location.LocationBasedTriggerManager;
import io.github.wysohn.triggerreactor.minestom.bridge.MinestomLocation;
import io.github.wysohn.triggerreactor.minestom.bridge.entity.MinestomPlayer;
import io.github.wysohn.triggerreactor.minestom.main.MinestomTriggerReactorCore;
import io.github.wysohn.triggerreactor.minestom.tools.LocationUtil;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Player;
import net.minestom.server.event.Event;
import net.minestom.server.event.EventNode;
import net.minestom.server.event.player.PlayerBlockBreakEvent;
import net.minestom.server.event.player.PlayerBlockInteractEvent;
import net.minestom.server.event.player.PlayerChangeHeldSlotEvent;
import net.minestom.server.instance.block.Block;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;

public abstract class LocationBasedTriggerListener<T extends Trigger, M extends LocationBasedTriggerManager<T>>
        implements MinestomTriggerManager {
    public static final Material INSPECTION_TOOL = Material.BONE;
    public static final Material CUT_TOOL = Material.SHEARS;
    public static final Material COPY_TOOL = Material.PAPER;

    final M manager;

    public LocationBasedTriggerListener(M manager, EventNode<Event> node) {
        this.manager = manager;
        node.addListener(PlayerBlockInteractEvent.class, event -> {
            if (event.getHand() != Player.Hand.MAIN) {
                return;
            }

            Player player = event.getPlayer();
            ItemStack itemStack = event.getPlayer().getItemInHand(event.getHand());
            Block block = event.getBlock();

            MinestomLocation locationWrapped = new MinestomLocation(event.getPlayer().getInstance(), Pos.fromPoint(event.getBlockPosition()));
            MinestomPlayer playerWrapped = new MinestomPlayer(player);

            T trigger = manager.getTriggerForLocation(locationWrapped);
            IPlayer bukkitPlayer = MinestomTriggerReactorCore.getWrapper().wrap(player);

            if (itemStack.material() != Material.AIR && !event.isCancelled() && player.hasPermission("triggerreactor.admin")) {

                if (itemStack.material() == INSPECTION_TOOL) {
                    if (trigger != null) {
                        if (event.getPlayer().isSneaking()) {
                            manager.handleScriptEdit(playerWrapped, trigger);
                            event.setCancelled(true);
                        } else {
                            manager.showTriggerInfo(bukkitPlayer, locationWrapped);
                            event.setCancelled(true);
                        }
                    }
                } else {
                    if (itemStack.material() == CUT_TOOL) {
                        if (trigger != null) {
                            if (manager.cutTrigger(playerWrapped, locationWrapped)) {
                                player.sendMessage(Component.text("Cut Complete!").color(NamedTextColor.GREEN));
                                player.sendMessage(Component.text("Now you can paste it by left click on any block!").color(NamedTextColor.GREEN));
                                event.setCancelled(true);
                            }
                        }
                    } else if (itemStack.material() == COPY_TOOL) {
                        if (trigger != null && manager.copyTrigger(playerWrapped,
                                locationWrapped)) {
                            player.sendMessage(Component.text("Copy Complete!").color(NamedTextColor.GREEN));
                            player.sendMessage(Component.text("Now you can paste it by left click on any block!").color(NamedTextColor.GREEN));
                            event.setCancelled(true);
                        }
                    }
                }
            }

            if (!event.isCancelled() && manager.isLocationSetting(bukkitPlayer)) {
                manager.handleLocationSetting(locationWrapped, playerWrapped);
                event.setCancelled(true);
            }
        }).addListener(PlayerBlockBreakEvent.class, event -> {

            Player player = event.getPlayer();
            ItemStack itemStack = event.getPlayer().getItemInMainHand();
            Block block = event.getBlock();

            MinestomLocation locationWrapped = new MinestomLocation(event.getPlayer().getInstance(), Pos.fromPoint(event.getBlockPosition()));
            MinestomPlayer playerWrapped = new MinestomPlayer(player);

            T trigger = manager.getTriggerForLocation(locationWrapped);
            IPlayer bukkitPlayer = MinestomTriggerReactorCore.getWrapper().wrap(player);

            if (itemStack.material() != Material.AIR && !event.isCancelled() && player.hasPermission("triggerreactor.admin")) {

                if (itemStack.material() == INSPECTION_TOOL) {
                    if (trigger != null) {
                        manager.removeTriggerForLocation(locationWrapped);

                        player.sendMessage(Component.text("A trigger has deleted.").color(NamedTextColor.GREEN));
                        event.setCancelled(true);
                    }
                } else {
                    if (itemStack.material() == CUT_TOOL) {
                        if (manager.pasteTrigger(playerWrapped, locationWrapped)) {
                            player.sendMessage(Component.text("Successfully pasted the trigger!").color(NamedTextColor.GREEN));
                            manager.showTriggerInfo(bukkitPlayer, locationWrapped);
                            event.setCancelled(true);
                        }
                    } else if (itemStack.material() == COPY_TOOL) {
                        if (manager.pasteTrigger(playerWrapped, locationWrapped)) {
                            player.sendMessage(Component.text("Successfully pasted the trigger!").color(NamedTextColor.GREEN));
                            manager.showTriggerInfo(bukkitPlayer, locationWrapped);
                            event.setCancelled(true);
                        }
                    }
                }
            }

            if (!event.isCancelled() && manager.isLocationSetting(bukkitPlayer)) {
                manager.handleLocationSetting(locationWrapped, playerWrapped);
                event.setCancelled(true);
            }
        }).addListener(PlayerBlockBreakEvent.class, event -> {

            T trigger = manager.getTriggerForLocation(LocationUtil.convertToSimpleLocation(event.getInstance(), event.getBlockPosition()));
            if (trigger == null)
                return;

            Player player = event.getPlayer();

            player.sendMessage(Component.text("Cannot break trigger block.").color(NamedTextColor.GRAY));
            player.sendMessage(Component.text("To remove trigger, hold inspection tool " + INSPECTION_TOOL.name()).color(NamedTextColor.GRAY));
            event.setCancelled(true);
        }).addListener(PlayerChangeHeldSlotEvent.class, event -> {
            manager.onItemSwap(new MinestomPlayer(event.getPlayer()));
        });
    }
}
