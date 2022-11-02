package io.github.wysohn.triggerreactor.minestom.bridge.entity;

import io.github.wysohn.triggerreactor.core.bridge.ILocation;
import io.github.wysohn.triggerreactor.core.bridge.entity.IEntity;
import io.github.wysohn.triggerreactor.minestom.bridge.MinestomLocation;
import net.minestom.server.entity.Entity;

import java.util.UUID;

public class MinestomEntity implements IEntity {
    protected final Entity entity;

    public MinestomEntity(Entity entity) {
        super();
        this.entity = entity;
    }

    @Override
    public <T> T get() {
        return (T) entity;
    }

    @Override
    public UUID getUniqueId() {
        return entity.getUuid();
    }

    @Override
    public boolean isDead() {
        return entity.isRemoved();
    }

    @Override
    public boolean isValid() {
        return entity.isRemoved();
    }

    @Override
    public ILocation getLocation() {
        return new MinestomLocation(entity.getInstance(), entity.getPosition());
    }

}
