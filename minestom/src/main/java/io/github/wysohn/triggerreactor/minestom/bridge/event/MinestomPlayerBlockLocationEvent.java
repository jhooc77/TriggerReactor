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
package io.github.wysohn.triggerreactor.minestom.bridge.event;

import io.github.wysohn.triggerreactor.core.bridge.entity.IPlayer;
import io.github.wysohn.triggerreactor.core.bridge.event.IPlayerBlockLocationEvent;
import io.github.wysohn.triggerreactor.core.manager.location.SimpleLocation;
import io.github.wysohn.triggerreactor.minestom.main.MinestomTriggerReactorCore;
import io.github.wysohn.triggerreactor.minestom.manager.event.PlayerBlockLocationEvent;

public class MinestomPlayerBlockLocationEvent implements IPlayerBlockLocationEvent {
    private final PlayerBlockLocationEvent event;

    public MinestomPlayerBlockLocationEvent(PlayerBlockLocationEvent playerBlockLocationEvent) {
        super();
        event = playerBlockLocationEvent;
    }

    @Override
    public IPlayer getIPlayer() {
        return MinestomTriggerReactorCore
                .getWrapper()
                .wrap(event.getPlayer());
    }

    @Override
    public SimpleLocation getFrom() {
        return event.getFrom();
    }

    @Override
    public SimpleLocation getTo() {
        return event.getTo();
    }

    @Override
    public <T> T get() {
        return (T) event;
    }

    @Override
    public boolean isCancelled() {
        return event.isCancelled();
    }

    @Override
    public void setCancelled(boolean b) {
        event.setCancelled(true);
    }

}
