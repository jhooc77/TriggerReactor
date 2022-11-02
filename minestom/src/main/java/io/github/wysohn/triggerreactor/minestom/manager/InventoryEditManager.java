package io.github.wysohn.triggerreactor.minestom.manager;

import io.github.wysohn.triggerreactor.core.bridge.IInventory;
import io.github.wysohn.triggerreactor.core.bridge.IItemStack;
import io.github.wysohn.triggerreactor.core.bridge.entity.IPlayer;
import io.github.wysohn.triggerreactor.core.main.TriggerReactorCore;
import io.github.wysohn.triggerreactor.core.manager.AbstractInventoryEditManager;
import io.github.wysohn.triggerreactor.core.manager.trigger.inventory.InventoryTrigger;
import io.github.wysohn.triggerreactor.core.script.wrapper.IScriptObject;
import io.github.wysohn.triggerreactor.minestom.bridge.MinestomItemStack;
import io.github.wysohn.triggerreactor.minestom.main.MinestomTriggerReactorCore;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.minestom.server.entity.Player;
import net.minestom.server.event.Event;
import net.minestom.server.event.EventNode;
import net.minestom.server.event.inventory.InventoryCloseEvent;
import net.minestom.server.event.player.PlayerDisconnectEvent;
import net.minestom.server.inventory.Inventory;
import net.minestom.server.inventory.InventoryType;
import net.minestom.server.item.ItemStack;

import java.util.Optional;
import java.util.UUID;

public class InventoryEditManager extends AbstractInventoryEditManager {

    private static final Component message = Component.text(CHECK + " Save")
            .decorate(TextDecoration.BOLD)
            .color(NamedTextColor.GREEN)
            .clickEvent(ClickEvent.runCommand("/trg links inveditsave"))
            .append(Component.newline())
            .append(Component.text(PENCIL + " Continue Editing")
                    .decorate(TextDecoration.BOLD)
                    .color(NamedTextColor.YELLOW)
                    .clickEvent(ClickEvent.runCommand("/trg links inveditcontinue")))
            .append(Component.text(X + " Cancel")
                    .decorate(TextDecoration.BOLD)
                    .color(NamedTextColor.RED)
                    .clickEvent(ClickEvent.runCommand("/trg links inveditdiscard")));

    public InventoryEditManager(TriggerReactorCore plugin, EventNode<Event> node) {
        super(plugin);
        node.addListener(InventoryCloseEvent.class, event -> {
            UUID u = event.getPlayer().getUuid();
            if (!sessions.containsKey(u)) {
                return;
            }
            //filter out already suspended
            if (suspended.containsKey(u)) {
                return;
            }
            Inventory inv = event.getInventory();

            suspended.put(u, MinestomTriggerReactorCore.getWrapper().wrap(inv));
            sendMessage(event.getPlayer());
        }).addListener(PlayerDisconnectEvent.class, event -> {
            stopEdit(MinestomTriggerReactorCore.getWrapper().wrap(event.getPlayer()));
        });
    }

    @Override
    public void reload() {
    }

    @Override
    public void saveAll() {
    }

    @Override
    public void startEdit(IPlayer player, InventoryTrigger trigger) {
        UUID u = player.getUniqueId();
        if (sessions.containsKey(u)) {
            return;
        }
        sessions.put(u, trigger);

        IItemStack[] iitems = trigger.getItems();
        ItemStack[] items = new ItemStack[iitems.length];
        for (int i = 0; i < items.length; i++) {
            items[i] = Optional.ofNullable(iitems[i])
                    .map(IScriptObject::get)
                    .filter(ItemStack.class::isInstance)
                    .map(ItemStack.class::cast)
                    .orElse(ItemStack.AIR);
        }

        Inventory inv = new Inventory(InventoryType.valueOf("CHEST_" + items.length/9 + "_ROW"), trigger.getInfo().getTriggerName());
        inv.copyContents(items);
        player.openInventory(MinestomTriggerReactorCore.getWrapper().wrap(inv));
    }

    @Override
    public void continueEdit(IPlayer player) {
        UUID u = player.getUniqueId();
        if (!suspended.containsKey(u)) {
            return;
        }
        IInventory inv = suspended.remove(u);
        player.openInventory(inv);
    }

    @Override
    public void discardEdit(IPlayer player) {
        if (!sessions.containsKey(player.getUniqueId())) {
            return;
        }
        stopEdit(player);
        player.sendMessage("Discarded edits");
    }

    @Override
    public void saveEdit(IPlayer player) {
        UUID u = player.getUniqueId();
        if (!sessions.containsKey(u)) {
            return;
        }
        Inventory inv = suspended.get(u).get();
        InventoryTrigger trigger = sessions.get(u);

        ItemStack[] items = inv.getItemStacks();
        IItemStack[] iitems = new IItemStack[items.length];
        for (int i = 0; i < items.length; i++) {
            iitems[i] = new MinestomItemStack(items[i]);
        }

        replaceItems(trigger, iitems);
        stopEdit(player);
        player.sendMessage("Saved edits");
    }

    private void sendMessage(Player player) {
        player.sendMessage(message);
    }
}
