package io.github.wysohn.triggerreactor.minestom.main;

import io.github.wysohn.triggerreactor.core.config.source.GsonConfigSource;
import io.github.wysohn.triggerreactor.core.main.TriggerReactorCore;
import io.github.wysohn.triggerreactor.core.manager.Manager;
import io.github.wysohn.triggerreactor.minestom.bridge.MinestomCommandSender;
import io.github.wysohn.triggerreactor.minestom.main.serialize.MinestomItemStackSerializer;
import io.github.wysohn.triggerreactor.minestom.main.serialize.MinestomNBTCompoundSerializer;
import io.github.wysohn.triggerreactor.minestom.manager.event.TriggerReactorStartEvent;
import io.github.wysohn.triggerreactor.minestom.manager.event.TriggerReactorStopEvent;
import io.github.wysohn.triggerreactor.tools.ContinuingTasks;
import net.minestom.server.MinecraftServer;
import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.arguments.Argument;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.command.builder.suggestion.SuggestionEntry;
import net.minestom.server.entity.Player;
import net.minestom.server.event.EventDispatcher;
import net.minestom.server.extensions.Extension;
import net.minestom.server.item.ItemStack;
import org.jglrxavpok.hephaistos.nbt.NBTCompound;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.Map;

public class TriggerReactor extends Extension {

    private TriggerReactorCore core;

    private Yaml config;
    private Map data;
    private File file;
    private Thread thread;

    @Override
    public void initialize() {
        core = new MinestomTriggerReactorCore(this);
        DumperOptions options = new DumperOptions();
        options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
        config = new Yaml(options);
        file = new File(getDataDirectory().toFile(), "config.yml");
        loadConfig();
        saveConfig();
        thread = Thread.currentThread();
        MinecraftServer.getCommandManager().register(new Command("triggerreactor:triggerreactor", "triggerreactor", "trg") {
            {
                Argument<String[]> argument = ArgumentType.StringArray("args")
                        .setSuggestionCallback((sender, context, suggestion) -> {
                            String[] args = context.get("args");
                            List<String> list = TriggerReactorCore.onTabComplete(new MinestomCommandSender(sender), args);
                            if (list == null) {
                                list = MinecraftServer.getConnectionManager().getOnlinePlayers().stream().map(Player::getUsername).toList();
                            }
                            for (String s : list) {
                                suggestion.addEntry(new SuggestionEntry(s));
                            }
                        });
                setDefaultExecutor((sender, context) -> {
                    if (sender instanceof Player player) {
                        core.onCommand(MinestomTriggerReactorCore.getWrapper().wrap(player), "triggerreactor", new String[0]);
                    } else {
                        core.onCommand(MinestomTriggerReactorCore.getWrapper().wrap(sender), "triggerreactor", new String[0]);
                    }
                });

                addSyntax((sender, context) -> {
                    String[] args = context.get(argument);
                    if (sender instanceof Player player) {
                        core.onCommand(MinestomTriggerReactorCore.getWrapper().wrap(player), "triggerreactor", args);
                    } else {
                        core.onCommand(MinestomTriggerReactorCore.getWrapper().wrap(sender), "triggerreactor", args);
                    }
                }, argument);
            }
        });

        for (Manager manager : Manager.getManagers()) {
            manager.reload();
        }

        EventDispatcher.call(new TriggerReactorStartEvent());
    }

    @Override
    public void terminate() {
        new ContinuingTasks.Builder()
                .append(() -> EventDispatcher.call(new TriggerReactorStopEvent()))
                .append(() -> core.onCoreDisable())
                .run(Throwable::printStackTrace);
    }

    public void saveConfig() {
        try (FileWriter fileWriter = new FileWriter(file)){
            config.dump(data, fileWriter);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void loadConfig() {
        data = config.load(getClass().getClassLoader().getResourceAsStream("config.yml"));
    }

    public <T> T getConfig(String key, T def) {
        Object data = getConfig(key);
        return data == null ? def : (T) data;
    }

    public Object getConfig(String key) {
        String[] array = key.split("\\.");
        Map<String, Object> data = this.data;
        for (int i = 0; i < array.length - 1; i++) {
            Object d = data.getOrDefault(array[i], null);
            if (d instanceof Map map) {
                data = map;
            }
        }
        return data.getOrDefault(array[array.length-1], null);
    }

    public void setConfig(String key, Object value) {
        String[] array = key.split("\\.");
        Map<String, Object> data = this.data;
        for (int i = 0; i < array.length - 1; i++) {
            Object d = data.getOrDefault(array[i], null);
            if (d instanceof Map map) {
                data = map;
            }
        }
        data.put(array[array.length-1], value);
    }

    public TriggerReactorCore getCore() {
        return core;
    }

    public Thread getThread() {
        return thread;
    }

    static {
        GsonConfigSource.registerSerializer(NBTCompound.class, new MinestomNBTCompoundSerializer());
        GsonConfigSource.registerValidator(obj -> obj instanceof NBTCompound);

        GsonConfigSource.registerSerializer(ItemStack.class, new MinestomItemStackSerializer());
        GsonConfigSource.registerValidator(obj -> obj instanceof ItemStack);
    }
}
