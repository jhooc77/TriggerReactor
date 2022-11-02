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
package io.github.wysohn.triggerreactor.minestom.manager;

import io.github.wysohn.triggerreactor.core.main.TriggerReactorCore;
import io.github.wysohn.triggerreactor.core.manager.AbstractAreaSelectionManager;
import io.github.wysohn.triggerreactor.core.manager.location.SimpleLocation;
import io.github.wysohn.triggerreactor.minestom.tools.LocationUtil;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.entity.Player;
import net.minestom.server.event.Event;
import net.minestom.server.event.EventNode;
import net.minestom.server.event.player.PlayerBlockBreakEvent;
import net.minestom.server.event.player.PlayerBlockInteractEvent;
import net.minestom.server.event.player.PlayerDisconnectEvent;

import java.util.UUID;

public class AreaSelectionManager extends AbstractAreaSelectionManager {
    public AreaSelectionManager(TriggerReactorCore plugin, EventNode<Event> node) {
        super(plugin);
        node.addListener(PlayerDisconnectEvent.class, event -> {
            resetSelections(event.getPlayer().getUuid());
        }).addListener(PlayerBlockInteractEvent.class, event -> {
            Player player = event.getPlayer();
            UUID uuid = player.getUuid();

            if (!selecting.contains(uuid))
                return;

            event.setCancelled(true);

            if (event.getHand() != Player.Hand.MAIN)
                return;

            SimpleLocation sloc = LocationUtil.convertToSimpleLocation(event.getInstance(), event.getBlockPosition());

            ClickResult result = onClick(ClickAction.RIGHT_CLICK_BLOCK, uuid, sloc);

            if (result != null) {
                switch (result) {
                    case DIFFERENTWORLD:
                        player.sendMessage(Component.text("Positions have different world name.").color(NamedTextColor.RED));
                        break;
                    case COMPLETE:
                        SimpleLocation left = leftPosition.get(uuid);
                        SimpleLocation right = rightPosition.get(uuid);

                        SimpleLocation smallest = getSmallest(left, right);
                        SimpleLocation largest = getLargest(left, right);

                        player.sendMessage(Component.text("Smallest: " + smallest + " , Largest: " + largest).color(NamedTextColor.LIGHT_PURPLE));
                        break;
                    case LEFTSET:
                        player.sendMessage(Component.text("Left ready").color(NamedTextColor.GREEN));
                        break;
                    case RIGHTSET:
                        player.sendMessage(Component.text("Right ready").color(NamedTextColor.GREEN));
                        break;
                }
            }
        }).addListener(PlayerBlockBreakEvent.class, event -> {
            Player player = event.getPlayer();
            UUID uuid = player.getUuid();

            if (!selecting.contains(uuid))
                return;

            event.setCancelled(true);

            SimpleLocation sloc = LocationUtil.convertToSimpleLocation(event.getInstance(), event.getBlockPosition());

            ClickResult result = onClick(ClickAction.LEFT_CLICK_BLOCK, uuid, sloc);

            if (result != null) {
                switch (result) {
                    case DIFFERENTWORLD:
                        player.sendMessage(Component.text("Positions have different world name.").color(NamedTextColor.RED));
                        break;
                    case COMPLETE:
                        SimpleLocation left = leftPosition.get(uuid);
                        SimpleLocation right = rightPosition.get(uuid);

                        SimpleLocation smallest = getSmallest(left, right);
                        SimpleLocation largest = getLargest(left, right);

                        player.sendMessage(Component.text("Smallest: " + smallest + " , Largest: " + largest).color(NamedTextColor.LIGHT_PURPLE));
                        break;
                    case LEFTSET:
                        player.sendMessage(Component.text("Left ready").color(NamedTextColor.GREEN));
                        break;
                    case RIGHTSET:
                        player.sendMessage(Component.text("Right ready").color(NamedTextColor.GREEN));
                        break;
                }
            }
        });
    }

    @Override
    public void reload() {
        // TODO Auto-generated method stub

    }

    @Override
    public void saveAll() {
        // TODO Auto-generated method stub

    }
}