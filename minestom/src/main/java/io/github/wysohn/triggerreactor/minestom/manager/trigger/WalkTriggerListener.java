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

import io.github.wysohn.triggerreactor.core.manager.location.SimpleLocation;
import io.github.wysohn.triggerreactor.core.manager.trigger.location.LocationBasedTriggerManager;
import io.github.wysohn.triggerreactor.core.manager.trigger.location.WalkTrigger;
import io.github.wysohn.triggerreactor.minestom.bridge.MinestomBlock;
import io.github.wysohn.triggerreactor.minestom.bridge.entity.MinestomPlayer;
import io.github.wysohn.triggerreactor.minestom.manager.event.PlayerBlockLocationEvent;
import io.github.wysohn.triggerreactor.minestom.tools.LocationUtil;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.event.Event;
import net.minestom.server.event.EventNode;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;

public class WalkTriggerListener
        extends LocationBasedTriggerListener<WalkTrigger,
        LocationBasedTriggerManager<WalkTrigger>> {


    public WalkTriggerListener(LocationBasedTriggerManager<WalkTrigger> manager, EventNode<Event> node) {
        super(manager, node);
        node.addListener(PlayerBlockLocationEvent.class, event -> {
            SimpleLocation bottomLoc = event.getTo().add(0, -1, 0);

            Instance instance = LocationUtil.retrieveInstance(bottomLoc);
            Point point = new Pos(bottomLoc.getX(), bottomLoc.getY(), bottomLoc.getZ());
            Block block = instance.getBlock(point);

            manager.handleWalk(event,
                    new MinestomPlayer(event.getPlayer()),
                    event.getFrom(),
                    event.getTo(),
                    new MinestomBlock(block, instance, point));
        });
    }
}
