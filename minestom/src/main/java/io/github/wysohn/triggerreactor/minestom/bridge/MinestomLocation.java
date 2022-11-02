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
package io.github.wysohn.triggerreactor.minestom.bridge;

import io.github.wysohn.triggerreactor.core.bridge.ILocation;
import io.github.wysohn.triggerreactor.core.bridge.IWorld;
import io.github.wysohn.triggerreactor.core.manager.location.SimpleLocation;
import io.github.wysohn.triggerreactor.minestom.tools.LocationUtil;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.instance.Instance;

public class MinestomLocation implements ILocation {
    private final Instance instance;
    private final Pos location;

    public MinestomLocation(Instance instance, Pos location) {
        this.instance = instance;
        this.location = location;
    }

    @Override
    public <T> T get() {
        return (T) location;
    }

    @Override
    public SimpleLocation toSimpleLocation() {
        return LocationUtil.convertToSimpleLocation(instance, location);
    }

    @Override
    public IWorld getWorld() {
        return new MinestomWorld(instance);
    }
}
