package io.github.wysohn.triggerreactor.minestom.bridge;

import io.github.wysohn.triggerreactor.core.bridge.IBlock;
import io.github.wysohn.triggerreactor.core.bridge.ILocation;
import io.github.wysohn.triggerreactor.core.bridge.IWorld;
import io.github.wysohn.triggerreactor.core.bridge.entity.IEntity;
import io.github.wysohn.triggerreactor.core.manager.location.SimpleLocation;
import io.github.wysohn.triggerreactor.minestom.bridge.entity.MinestomEntity;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.instance.Instance;

import java.util.stream.Collectors;

public class MinestomWorld implements IWorld {
    private final Instance world;

    public MinestomWorld(Instance instance) {
        this.world = instance;
    }

    @Override
    public Iterable<IEntity> getEntities() {
        return world.getEntities().stream()
                .map(MinestomEntity::new)
                .collect(Collectors.toList());
    }

    @Override
    public IBlock getBlock(SimpleLocation clicked) {
        Point point = new Pos(clicked.getX(), clicked.getY(), clicked.getZ());
        return new MinestomBlock(world.getBlock(point), world, point);
    }

    @Override
    public IBlock getBlock(ILocation location) {
        return getBlock(location.toSimpleLocation());
    }
}
