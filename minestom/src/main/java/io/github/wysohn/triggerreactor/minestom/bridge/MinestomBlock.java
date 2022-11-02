package io.github.wysohn.triggerreactor.minestom.bridge;

import io.github.wysohn.triggerreactor.core.bridge.IBlock;
import io.github.wysohn.triggerreactor.core.bridge.ILocation;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;

public class MinestomBlock implements IBlock {
    private final Block block;
    private final Instance instance;
    private final Point point;

    public MinestomBlock(Block block, Instance instance, Point point) {
        this.instance = instance;
        this.point = point;
        this.block = block;
    }

    @Override
    public String getTypeName() {
        return block.name();
    }

    @Override
    public ILocation getLocation() {
        return new MinestomLocation(instance, Pos.fromPoint(point));
    }

    @Override
    public <T> T get() {
        return (T) block;
    }
}
