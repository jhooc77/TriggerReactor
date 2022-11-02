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
import io.github.wysohn.triggerreactor.core.manager.trigger.area.AreaTriggerManager;
import io.github.wysohn.triggerreactor.minestom.bridge.entity.MinestomEntity;
import io.github.wysohn.triggerreactor.minestom.bridge.entity.MinestomPlayer;
import io.github.wysohn.triggerreactor.minestom.manager.event.PlayerBlockLocationEvent;
import io.github.wysohn.triggerreactor.minestom.tools.LocationUtil;
import net.minestom.server.event.Event;
import net.minestom.server.event.EventNode;
import net.minestom.server.event.entity.EntityDeathEvent;
import net.minestom.server.event.entity.EntitySpawnEvent;
import net.minestom.server.event.player.PlayerDeathEvent;
import net.minestom.server.event.player.PlayerSpawnEvent;

public class AreaTriggerListener implements MinestomTriggerManager {
    private final AreaTriggerManager manager;

    public AreaTriggerListener(AreaTriggerManager manager, EventNode<Event> node) {
        this.manager = manager;
        node.addListener(PlayerSpawnEvent.class, event -> {
            if (event.isFirstSpawn())
                manager.onJoin(LocationUtil.convertToSimpleLocation(event.getSpawnInstance(), event.getPlayer().getPosition()), new MinestomPlayer(event.getPlayer()));
        }).addListener(PlayerBlockLocationEvent.class, event -> {
            manager.onLocationChange(event, event.getFrom(), event.getTo(), new MinestomPlayer(event.getPlayer()));
        }).addListener(EntitySpawnEvent.class, event -> {
            SimpleLocation sloc = LocationUtil.convertToSimpleLocation(event.getSpawnInstance(), event.getEntity().getPosition());

            manager.onSpawn(new MinestomEntity(event.getEntity()), sloc);
        }).addListener(EntityDeathEvent.class, event -> {
            SimpleLocation sloc = LocationUtil.convertToSimpleLocation(event.getInstance(), event.getEntity().getPosition());

            manager.onDeath(event.getEntity().getUuid(), sloc);
        }).addListener(PlayerDeathEvent.class, event -> {
            SimpleLocation sloc = LocationUtil.convertToSimpleLocation(event.getInstance(), event.getPlayer().getPosition());

            manager.onDeath(event.getPlayer().getUuid(), sloc);
        });
    }
}
