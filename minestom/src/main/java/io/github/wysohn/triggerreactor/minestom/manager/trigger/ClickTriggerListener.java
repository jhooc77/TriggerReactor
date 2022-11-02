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

import io.github.wysohn.triggerreactor.core.manager.trigger.location.Activity;
import io.github.wysohn.triggerreactor.core.manager.trigger.location.ClickTrigger;
import io.github.wysohn.triggerreactor.core.manager.trigger.location.LocationBasedTriggerManager;
import io.github.wysohn.triggerreactor.minestom.bridge.MinestomBlock;
import io.github.wysohn.triggerreactor.minestom.bridge.MinestomItemStack;
import io.github.wysohn.triggerreactor.minestom.bridge.entity.MinestomPlayer;
import net.minestom.server.coordinate.Point;
import net.minestom.server.entity.Player;
import net.minestom.server.event.Event;
import net.minestom.server.event.EventNode;
import net.minestom.server.event.player.PlayerBlockBreakEvent;
import net.minestom.server.event.player.PlayerBlockInteractEvent;
import net.minestom.server.event.player.PlayerHandAnimationEvent;
import net.minestom.server.event.player.PlayerUseItemEvent;

public class ClickTriggerListener
        extends LocationBasedTriggerListener<ClickTrigger,
                LocationBasedTriggerManager<ClickTrigger>> {


    public ClickTriggerListener(LocationBasedTriggerManager<ClickTrigger> manager, EventNode<Event> node) {
        super(manager, node);
        node.addListener(PlayerBlockInteractEvent.class, event -> {
            if (event.getHand() != Player.Hand.MAIN) return;
            manager.handleClick(event,
                    new MinestomBlock(event.getBlock(), event.getInstance(), event.getBlockPosition()),
                    new MinestomPlayer(event.getPlayer()),
                    new MinestomItemStack(event.getPlayer().getItemInHand(event.getHand())),
                    Activity.RIGHT_CLICK_BLOCK);
        }).addListener(PlayerUseItemEvent.class, event -> {
            if (event.getHand() != Player.Hand.MAIN) return;
            manager.handleClick(event,
                    null,
                    new MinestomPlayer(event.getPlayer()),
                    new MinestomItemStack(event.getPlayer().getItemInHand(event.getHand())),
                    Activity.RIGHT_CLICK_AIR);
        }).addListener(PlayerBlockBreakEvent.class, event -> {
            manager.handleClick(event,
                    new MinestomBlock(event.getBlock(), event.getInstance(), event.getBlockPosition()),
                    new MinestomPlayer(event.getPlayer()),
                    new MinestomItemStack(event.getPlayer().getItemInMainHand()),
                    Activity.LEFT_CLICK_BLOCK);
        }).addListener(PlayerHandAnimationEvent.class, event -> {
            Point block = event.getPlayer().getTargetBlockPosition(5);
            if (block == null) {
                manager.handleClick(event,
                        null,
                        new MinestomPlayer(event.getPlayer()),
                        new MinestomItemStack(event.getPlayer().getItemInMainHand()),
                        Activity.LEFT_CLICK_AIR);
            }
        });
    }

}
