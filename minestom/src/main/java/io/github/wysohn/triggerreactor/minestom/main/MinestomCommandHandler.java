package io.github.wysohn.triggerreactor.minestom.main;

import io.github.wysohn.triggerreactor.core.main.command.ICommand;
import io.github.wysohn.triggerreactor.core.main.command.ICommandHandler;
import net.minestom.server.MinecraftServer;
import net.minestom.server.command.builder.Command;
import net.minestom.server.entity.Player;

public class MinestomCommandHandler implements ICommandHandler {

    public MinestomCommandHandler() {
    }

    @Override
    public ICommand register(String name, String[] aliases) {

        if (MinecraftServer.getCommandManager().commandExists(name)) {
            return null;
        }

        MinestomCommand command = new MinestomCommand(name, aliases);

        MinecraftServer.getCommandManager().register(command);

        return command;
    }

    @Override
    public boolean unregister(String name) {
        Command command = MinecraftServer.getCommandManager().getCommand(name);
        if (command == null)
            return false;

        MinecraftServer.getCommandManager().unregister(command);
        return true;
    }

    @Override
    public void sync() {
        for (Player onlinePlayer : MinecraftServer.getConnectionManager().getOnlinePlayers()) {
            onlinePlayer.sendPacket(MinecraftServer.getCommandManager().createDeclareCommandsPacket(onlinePlayer));
        }
    }
}
