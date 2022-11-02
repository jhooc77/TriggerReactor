package io.github.wysohn.triggerreactor.minestom.tools;

import io.github.wysohn.triggerreactor.core.manager.location.SimpleLocation;
import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.instance.AnvilLoader;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.InstanceContainer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Field;
import java.nio.file.Path;
import java.util.UUID;

public class LocationUtil {

    private static Field INSTANCE_PATH_FIELD = null;

    static {
        try {
            INSTANCE_PATH_FIELD = AnvilLoader.class.getDeclaredField("path");
            INSTANCE_PATH_FIELD.setAccessible(true);
        } catch (NoSuchFieldException | ClassCastException ignored) {}
    }

    @Nullable
    public static String getInstanceName(Instance instance) {
        if (instance instanceof InstanceContainer instanceContainer && instanceContainer.getChunkLoader() instanceof AnvilLoader anvilLoader) {
            try {
                return ((Path) INSTANCE_PATH_FIELD.get(anvilLoader)).getFileName().toString();
            } catch (IllegalAccessException ignored) {}
        }
        return null;
    }

    @NotNull
    public static String getInstanceNameOrUUID(Instance instance) {
        String name = getInstanceName(instance);
        if (name == null) {
            return instance.getUniqueId().toString();
        } else {
            return name;
        }
    }

    public static SimpleLocation convertToSimpleLocation(Instance instance, Pos pos) {
        String world = getInstanceName(instance);
        if (world == null) {
            world = instance.getUniqueId().toString();
        }
        return new SimpleLocation(world, pos.blockX(), pos.blockY(), pos.blockZ(), pos.pitch(), pos.yaw());
    }
    public static SimpleLocation convertToSimpleLocation(Instance instance, Point pos) {
        String world = getInstanceName(instance);
        if (world == null) {
            world = instance.getUniqueId().toString();
        }
        return new SimpleLocation(world, pos.blockX(), pos.blockY(), pos.blockZ());
    }

    public static Instance retrieveInstance(SimpleLocation sloc) {
        String name = sloc.getWorld();
        return retrieveInstance(name);
    }

    public static Instance retrieveInstance(String name) {
        if (name.matches("^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$")) {
            UUID uuid = UUID.fromString(name);
            return MinecraftServer.getInstanceManager().getInstance(uuid);
        } else {
            return MinecraftServer.getInstanceManager().getInstances().stream().filter(i -> name.equals(getInstanceName(i))).findAny().orElse(null);
        }
    }
}
