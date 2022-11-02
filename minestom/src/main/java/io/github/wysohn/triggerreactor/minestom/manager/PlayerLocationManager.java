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
import io.github.wysohn.triggerreactor.core.manager.AbstractPlayerLocationManager;
import io.github.wysohn.triggerreactor.core.manager.location.SimpleLocation;
import io.github.wysohn.triggerreactor.minestom.bridge.event.MinestomPlayerBlockLocationEvent;
import io.github.wysohn.triggerreactor.minestom.manager.event.PlayerBlockLocationEvent;
import io.github.wysohn.triggerreactor.minestom.tools.LocationUtil;
import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Player;
import net.minestom.server.event.Event;
import net.minestom.server.event.EventNode;
import net.minestom.server.event.player.PlayerDisconnectEvent;
import net.minestom.server.event.player.PlayerMoveEvent;
import net.minestom.server.event.player.PlayerRespawnEvent;
import net.minestom.server.event.player.PlayerSpawnEvent;

public class PlayerLocationManager extends AbstractPlayerLocationManager {


    public PlayerLocationManager(TriggerReactorCore plugin, EventNode<Event> node) {
        super(plugin);
        node.addListener(PlayerRespawnEvent.class, event -> {
            Player player = event.getPlayer();
            SimpleLocation sloc = LocationUtil.convertToSimpleLocation(player.getInstance(), player.getPosition());
            setCurrentBlockLocation(player.getUuid(), sloc);
        }).addListener(PlayerMoveEvent.class, event -> {
            Player player = event.getPlayer();

            if (event.getPlayer().getDistance(event.getNewPosition()) < Vec.EPSILON)
                return;

            SimpleLocation from = getCurrentBlockLocation(player.getUuid());
            SimpleLocation to = LocationUtil.convertToSimpleLocation(event.getInstance(), event.getNewPosition());

            PlayerBlockLocationEvent pble = new PlayerBlockLocationEvent(player, from, to);
            onMove(new MinestomPlayerBlockLocationEvent(pble));
            if (pble.isCancelled()) {
                Pos pos = new Pos(from.getX(), from.getY(), from.getY(), player.getPosition().yaw(), player.getPosition().pitch());
                event.setNewPosition(pos);
            }
        }).addListener(PlayerDisconnectEvent.class, event -> {
            Player player = event.getPlayer();
            removeCurrentBlockLocation(player.getUuid());
        }).addListener(PlayerSpawnEvent.class, event -> {

            if (!event.isFirstSpawn()) return;
            Player player = event.getPlayer();
            SimpleLocation sloc = LocationUtil.convertToSimpleLocation(event.getSpawnInstance(), player.getPosition());
            setCurrentBlockLocation(player.getUuid(), sloc);
        });
    }

    @Override
    public void reload() {
        for (Player player : MinecraftServer.getConnectionManager().getOnlinePlayers()) {
            SimpleLocation sloc = LocationUtil.convertToSimpleLocation(player.getInstance(), player.getPosition());
            setCurrentBlockLocation(player.getUuid(), sloc);
        }
    }

    @Override
    public void saveAll() {
        // TODO Auto-generated method stub

    }

}
