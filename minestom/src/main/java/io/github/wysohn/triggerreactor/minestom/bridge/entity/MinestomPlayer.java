package io.github.wysohn.triggerreactor.minestom.bridge.entity;

import io.github.wysohn.triggerreactor.core.bridge.IInventory;
import io.github.wysohn.triggerreactor.core.bridge.IItemStack;
import io.github.wysohn.triggerreactor.core.bridge.entity.IPlayer;
import io.github.wysohn.triggerreactor.core.manager.location.SimpleChunkLocation;
import io.github.wysohn.triggerreactor.minestom.bridge.MinestomInventory;
import io.github.wysohn.triggerreactor.minestom.bridge.MinestomItemStack;
import io.github.wysohn.triggerreactor.minestom.tools.LocationUtil;
import io.github.wysohn.triggerreactor.minestom.tools.TextUtil;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Player;
import net.minestom.server.inventory.Inventory;

public class MinestomPlayer extends MinestomEntity implements IPlayer {
    private final Player player;
    public MinestomPlayer(Player player) {
        super(player);
        this.player = player;
    }

    @Override
    public void sendMessage(String message) {
        player.sendMessage(TextUtil.coloredAmpersand(message));
    }

    @Override
    public boolean hasPermission(String permission) {
        return entity.hasPermission(permission);
    }

    @Override
    public String getName() {
        return player.getUsername();
    }

    @Override
    public IInventory getInventory() {
        return new MinestomInventory(player.getInventory());
    }

    @Override
    public void openInventory(IInventory inventory) {
        player.openInventory((Inventory) inventory.get());
    }

    @Override
    public SimpleChunkLocation getChunk() {
        Pos pos = player.getPosition();
        return new SimpleChunkLocation(LocationUtil.getInstanceNameOrUUID(player.getInstance()),
                                       pos.blockX() >> 4,
                                       pos.blockZ() >> 4);
    }

    @Override
    public IItemStack getItemInMainHand() {
        return new MinestomItemStack(player.getInventory().getItemInMainHand());
    }

    @Override
    public void setItemInMainHand(IItemStack iS) {
        player.getInventory().setItemInMainHand(iS.get());
    }
}
