package io.github.wysohn.triggerreactor.minestom.bridge;

import io.github.wysohn.triggerreactor.core.bridge.*;
import io.github.wysohn.triggerreactor.core.bridge.entity.IEntity;
import io.github.wysohn.triggerreactor.core.bridge.entity.IPlayer;
import io.github.wysohn.triggerreactor.core.bridge.event.IPlayerBlockLocationEvent;
import io.github.wysohn.triggerreactor.minestom.bridge.entity.MinestomEntity;
import io.github.wysohn.triggerreactor.minestom.bridge.entity.MinestomPlayer;
import io.github.wysohn.triggerreactor.minestom.bridge.event.MinestomPlayerBlockLocationEvent;
import io.github.wysohn.triggerreactor.minestom.main.MinestomTriggerReactorCore;
import io.github.wysohn.triggerreactor.minestom.manager.event.PlayerBlockLocationEvent;
import net.minestom.server.command.CommandSender;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.Player;
import net.minestom.server.instance.Instance;
import net.minestom.server.inventory.AbstractInventory;
import net.minestom.server.inventory.Inventory;
import net.minestom.server.item.ItemStack;

/**
 * For future reference: The instance can be found using {@link MinestomTriggerReactorCore#getWrapper()}
 */
public class MinestomWrapper {
    public IEntity wrap(Entity entity) {
        return new MinestomEntity(entity);
    }

    public IPlayer wrap(Player player) {
        return new MinestomPlayer(player);
    }

    public IPlayerBlockLocationEvent wrap(PlayerBlockLocationEvent pble) {
        return new MinestomPlayerBlockLocationEvent(pble);
    }

    public ICommandSender wrap(CommandSender commandSender) {
        return new MinestomCommandSender(commandSender);
    }

    public IInventory wrap(AbstractInventory inventory) {
        return new MinestomInventory(inventory);
    }

    public IItemStack wrap(ItemStack itemStack) {
        return new MinestomItemStack(itemStack);
    }

    public ILocation wrap(Instance instance, Pos location) {
        return new MinestomLocation(instance, location);
    }

    public IWorld wrap(Instance instance) {
        return new MinestomWorld(instance);
    }
}
