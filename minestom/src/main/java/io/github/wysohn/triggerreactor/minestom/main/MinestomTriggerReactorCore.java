package io.github.wysohn.triggerreactor.minestom.main;

import io.github.wysohn.triggerreactor.core.bridge.ICommandSender;
import io.github.wysohn.triggerreactor.core.bridge.IInventory;
import io.github.wysohn.triggerreactor.core.bridge.IItemStack;
import io.github.wysohn.triggerreactor.core.bridge.IWorld;
import io.github.wysohn.triggerreactor.core.bridge.entity.IPlayer;
import io.github.wysohn.triggerreactor.core.bridge.event.IEvent;
import io.github.wysohn.triggerreactor.core.main.IEventRegistry;
import io.github.wysohn.triggerreactor.core.main.IInventoryHandle;
import io.github.wysohn.triggerreactor.core.main.TriggerReactorCore;
import io.github.wysohn.triggerreactor.core.main.command.ICommandHandler;
import io.github.wysohn.triggerreactor.core.manager.*;
import io.github.wysohn.triggerreactor.core.manager.location.SimpleLocation;
import io.github.wysohn.triggerreactor.core.manager.trigger.Trigger;
import io.github.wysohn.triggerreactor.core.manager.trigger.area.AreaTriggerManager;
import io.github.wysohn.triggerreactor.core.manager.trigger.command.CommandTriggerManager;
import io.github.wysohn.triggerreactor.core.manager.trigger.custom.CustomTriggerManager;
import io.github.wysohn.triggerreactor.core.manager.trigger.inventory.InventoryTrigger;
import io.github.wysohn.triggerreactor.core.manager.trigger.inventory.InventoryTriggerManager;
import io.github.wysohn.triggerreactor.core.manager.trigger.location.*;
import io.github.wysohn.triggerreactor.core.manager.trigger.named.NamedTriggerManager;
import io.github.wysohn.triggerreactor.core.manager.trigger.repeating.RepeatingTriggerManager;
import io.github.wysohn.triggerreactor.core.manager.trigger.share.api.AbstractAPISupport;
import io.github.wysohn.triggerreactor.core.script.interpreter.interrupt.ProcessInterrupter;
import io.github.wysohn.triggerreactor.core.script.wrapper.SelfReference;
import io.github.wysohn.triggerreactor.minestom.bridge.MinestomCommandSender;
import io.github.wysohn.triggerreactor.minestom.bridge.MinestomWrapper;
import io.github.wysohn.triggerreactor.minestom.manager.*;
import io.github.wysohn.triggerreactor.minestom.manager.trigger.AreaTriggerListener;
import io.github.wysohn.triggerreactor.minestom.manager.trigger.ClickTriggerListener;
import io.github.wysohn.triggerreactor.minestom.manager.trigger.InventoryTriggerListener;
import io.github.wysohn.triggerreactor.minestom.manager.trigger.WalkTriggerListener;
import io.github.wysohn.triggerreactor.minestom.manager.trigger.share.MinestomCommonFunctions;
import io.github.wysohn.triggerreactor.minestom.tools.LocationUtil;
import io.github.wysohn.triggerreactor.tools.Lag;
import net.kyori.adventure.text.logger.slf4j.ComponentLogger;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.minestom.server.MinecraftServer;
import net.minestom.server.adventure.provider.MinestomLegacyComponentSerializerProvider;
import net.minestom.server.command.CommandSender;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Player;
import net.minestom.server.event.Event;
import net.minestom.server.event.EventDispatcher;
import net.minestom.server.event.EventNode;
import net.minestom.server.event.inventory.InventoryOpenEvent;
import net.minestom.server.event.inventory.InventoryPreClickEvent;
import net.minestom.server.event.player.PlayerCommandEvent;
import net.minestom.server.event.trait.*;
import net.minestom.server.instance.block.Block;
import net.minestom.server.inventory.AbstractInventory;
import net.minestom.server.item.ItemStack;
import net.minestom.server.network.packet.server.play.BlockChangePacket;
import net.minestom.server.timer.ExecutionType;

import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.*;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class MinestomTriggerReactorCore extends TriggerReactorCore {

    private static TriggerReactorCore core;

    public static MinestomWrapper wrapper;

    private final TriggerReactor extension;
    private Logger logger;
    private ScriptEngineManager sem;
    private Lag tpsHelper;
    private AbstractExecutorManager executorManager;
    private AbstractPlaceholderManager placeholderManager;
    private AbstractScriptEditManager scriptEditManager;
    private AbstractPlayerLocationManager locationManager;
    private AbstractPermissionManager permissionManager;
    private AbstractAreaSelectionManager selectionManager;
    private AbstractInventoryEditManager invEditManager;
    private ClickTriggerManager clickManager;
    private WalkTriggerManager walkManager;
    private CommandTriggerManager cmdManager;
    private InventoryTriggerManager invManager;
    private AreaTriggerManager areaManager;
    private CustomTriggerManager customManager;
    private RepeatingTriggerManager repeatManager;
    private NamedTriggerManager namedTriggerManager;
    private ICommandHandler commandHandler;
    private IEventRegistry eventRegistry;
    private IInventoryHandle<ItemStack> inventoryHandle;
    private SelfReference selfReference;

    private boolean enable;

    private class SimpleLogger extends Logger {

        final ComponentLogger logger;
        final LegacyComponentSerializer serializer;


        protected SimpleLogger(ComponentLogger logger) {
            super("TriggerReactor", null);
            this.logger = logger;
            serializer = new MinestomLegacyComponentSerializerProvider().legacySection();
        }

        @Override
        public void log(LogRecord record) {
            int level = record.getLevel().intValue();
            if (level > 900) {
                logger.error(record.getMessage());
            } else if (level > 800) {
                logger.warn(record.getMessage());
            } else if (level > 700) {
                logger.info(record.getMessage());
            } else {
                logger.info("[" + record.getLevel().getName() + "] " + record.getMessage());
            }
        }
    }

    protected MinestomTriggerReactorCore(TriggerReactor extension) {

        enable = true;

        this.extension = extension;

        EventNode<Event> node = extension.getEventNode();

        core = this;
        wrapper = new MinestomWrapper();
        logger = new SimpleLogger(extension.getLogger());
        sem = new ScriptEngineManager();

        try {
            ScriptEngineInitializer.initScriptEngine(sem);
            initScriptEngine(sem);
        } catch (ScriptException e) {
            initFailed(e);
            return;
        }

        try {
            executorManager = new ExecutorManager(this, sem);
        } catch (ScriptException | IOException e) {
            initFailed(e);
            return;
        }

        try {
            placeholderManager = new PlaceholderManager(this, sem);
        } catch (ScriptException | IOException e) {
            initFailed(e);
            return;
        }

        try {
            commandHandler = new MinestomCommandHandler();
        } catch (Exception e) {
            initFailed(e);
            return;
        }

        try {
            inventoryHandle = new MinestomInventoryHandle();
        } catch (Exception e) {
            initFailed(e);
            return;
        }

        try {
            eventRegistry = new MinestomEventRegistry(node);
        } catch (Exception e) {
            initFailed(e);
            return;
        }

        // managers
        scriptEditManager = new ScriptEditManager(this);
        locationManager = new PlayerLocationManager(this, node);
        //permissionManager = new PermissionManager(this);
        selectionManager = new AreaSelectionManager(this, node);
        invEditManager = new InventoryEditManager(this, node);

        clickManager = new ClickTriggerManager(this);
        walkManager = new WalkTriggerManager(this);
        cmdManager = new CommandTriggerManager(this, commandHandler);
        invManager = new InventoryTriggerManager<>(this, inventoryHandle);
        areaManager = new AreaTriggerManager(this, this, this);
        customManager = new CustomTriggerManager(this, eventRegistry);
        repeatManager = new RepeatingTriggerManager(this, this);

        namedTriggerManager = new NamedTriggerManager(this);

        // listeners
        new ClickTriggerListener(clickManager, node);
        new WalkTriggerListener(walkManager, node);
        new InventoryTriggerListener(invManager, node);

        new AreaTriggerListener(areaManager, node);

        selfReference = new MinestomCommonFunctions();

        tpsHelper = new Lag();
        new Thread() {
            @Override
            public void run() {
                try {
                    Thread.sleep(50L * 100);

                    while (isAlive() && !isInterrupted()) {
                        submitSync(() -> {
                            tpsHelper.run();
                            return null;
                        }).get();
                        Thread.sleep(50L);
                    }
                } catch (ExecutionException | InterruptedException ex) {
                    getLogger().info("TPS Helper stopped working." + ex);
                }

            }
        }.start();

        onCoreEnable();
    }


    private void initScriptEngine(ScriptEngineManager sem) {
        sem.put("plugin", this);

        for (Map.Entry<String, AbstractAPISupport> entry : this.getSharedVars().entrySet()) {
            sem.put(entry.getKey(), entry.getValue());
        }

        sem.put("get", new Function<String, Object>() {
            @Override
            public Object apply(String t) {
                return getVariableManager().get(t);
            }
        });

        sem.put("put", new BiFunction<String, Object, Void>() {
            @Override
            public Void apply(String a, Object b) {
                if (!GlobalVariableManager.isValidName(a))
                    throw new RuntimeException("[" + a + "] cannot be used as key");

                if (a != null && b == null) {
                    getVariableManager().remove(a);
                } else {
                    try {
                        getVariableManager().put(a, b);
                    } catch (Exception e) {
                        throw new RuntimeException("Executor -- put(" + a + "," + b + ")", e);
                    }
                }

                return null;
            }
        });

        sem.put("has", new Function<String, Boolean>() {
            @Override
            public Boolean apply(String t) {
                return getVariableManager().has(t);
            }
        });


        for (Manager manager : Manager.getManagers()) {
            manager.reload();
        }
    }

    private void initFailed(Exception e) {
        e.printStackTrace();
        getLogger().severe("Initialization failed!");
        getLogger().severe(e.getMessage());
        disablePlugin();
    }

    @Override
    public SelfReference getSelfReference() {
        return selfReference;
    }

    @Override
    public AbstractExecutorManager getExecutorManager() {
        return executorManager;
    }

    @Override
    public AbstractPlaceholderManager getPlaceholderManager() {
        return placeholderManager;
    }

    @Override
    public AbstractScriptEditManager getScriptEditManager() {
        return scriptEditManager;
    }

    @Override
    public AbstractPlayerLocationManager getLocationManager() {
        return locationManager;
    }

    @Override
    public AbstractPermissionManager getPermissionManager() {
        return permissionManager;
    }

    @Override
    public AbstractAreaSelectionManager getSelectionManager() {
        return selectionManager;
    }

    @Override
    public LocationBasedTriggerManager<ClickTrigger> getClickManager() {
        return clickManager;
    }

    @Override
    public LocationBasedTriggerManager<WalkTrigger> getWalkManager() {
        return walkManager;
    }

    @Override
    public CommandTriggerManager getCmdManager() {
        return cmdManager;
    }

    @Override
    public InventoryTriggerManager<? extends IInventory> getInvManager() {
        return invManager;
    }

    @Override
    public AbstractInventoryEditManager getInvEditManager() {
        return invEditManager;
    }

    @Override
    public AreaTriggerManager getAreaManager() {
        return areaManager;
    }

    @Override
    public CustomTriggerManager getCustomManager() {
        return customManager;
    }

    @Override
    public RepeatingTriggerManager getRepeatManager() {
        return repeatManager;
    }

    @Override
    public NamedTriggerManager getNamedTriggerManager() {
        return namedTriggerManager;
    }

    @Override
    public ICommandHandler getCommandHandler() {
        return commandHandler;
    }

    @Override
    public IEventRegistry getEventRegistry() {
        return eventRegistry;
    }

    @Override
    protected boolean removeLore(IItemStack iS, int index) {
        return false;
    }

    @Override
    protected boolean setLore(IItemStack iS, int index, String lore) {
        return false;
    }

    @Override
    protected void addItemLore(IItemStack iS, String lore) {

    }

    @Override
    protected void setItemTitle(IItemStack iS, String title) {

    }

    @Override
    public IPlayer getPlayer(String string) {
        return wrapper.wrap(MinecraftServer.getConnectionManager().getPlayer(string));
    }

    public interface CommandSenderEvent extends Event{
        CommandSender getCommandSender();
    }

    @Override
    public Object createEmptyPlayerEvent(ICommandSender sender) {
        Object unwrapped = sender.get();

        if (unwrapped instanceof Player player) {
            return (PlayerEvent) () -> player;
        } else if (unwrapped instanceof CommandSender commandSender) {
            return (CommandSenderEvent) () -> commandSender;
        } else {
            throw new RuntimeException("Cannot create empty PlayerEvent for " + sender);
        }
    }

    @Override
    public Object createPlayerCommandEvent(ICommandSender sender, String label, String[] args) {
        Object unwrapped = sender.get();

        StringBuilder builder = new StringBuilder("/");
        builder.append(label);
        for (String arg : args) {
            builder.append(' ');
            builder.append(arg);
        }

        if (unwrapped instanceof Player) {
            return new PlayerCommandEvent((Player) unwrapped, builder.toString());
        } else {
            throw new RuntimeException("Cannot create empty PlayerCommandPreprocessEvent for " + sender);
        }
    }

    @Override
    public Iterable<IWorld> getWorlds() {
        return MinecraftServer.getInstanceManager().getInstances().stream()
                .map(wrapper::wrap)
                .collect(Collectors.toList());
    }

    @Override
    public IWorld getWorld(String world) {
        return Optional.ofNullable(world)
                .map(LocationUtil::retrieveInstance)
                .map(wrapper::wrap)
                .orElse(null);
    }

    @Override
    public IInventoryHandle<?> getInventoryHandle() {
        return inventoryHandle;
    }

    @Override
    protected void sendCommandDesc(ICommandSender sender, String command, String desc) {
        sender.sendMessage("&b" + command + " &8- &7" + desc);
    }

    @Override
    protected void sendDetails(ICommandSender sender, String detail) {
        sender.sendMessage("  &7" + detail);
    }

    @Override
    public String getPluginDescription() {
        return extension.getOrigin().getName();
    }

    @Override
    public String getVersion() {
        return extension.getOrigin().getVersion();
    }

    @Override
    public String getAuthor() {
        return Arrays.toString(extension.getOrigin().getAuthors());
    }

    @Override
    protected void showGlowStones(ICommandSender sender, Set<Map.Entry<SimpleLocation, Trigger>> set) {
        for (Map.Entry<SimpleLocation, Trigger> entry : set) {
            SimpleLocation sloc = entry.getKey();
            Player player = sender.get();
            player.sendPacketToViewersAndSelf(new BlockChangePacket(new Pos(sloc.getX(), sloc.getY(), sloc.getZ()), Block.GLOWSTONE));
        }
    }

    @Override
    public File getDataFolder() {
        return extension.getDataDirectory().toFile();
    }

    @Override
    public Logger getLogger() {
        return logger;
    }

    @Override
    public boolean isEnabled() {
        return enable;
    }

    @Override
    public void disablePlugin() {
        enable = false;
        extension.preTerminate();
        extension.terminate();
        extension.postTerminate();
    }

    @Override
    public <T> T getMain() {
        return (T) extension;
    }

    @Override
    public boolean isConfigSet(String key) {
        return extension.getConfig(key) != null;
    }

    @Override
    public void setConfig(String key, Object value) {
        extension.setConfig(key, value);
    }

    @Override
    public Object getConfig(String key) {
        return extension.getConfig(key);
    }

    @Override
    public <T> T getConfig(String key, T def) {
        return extension.getConfig(key, def);
    }

    @Override
    public void saveConfig() {
        extension.saveConfig();
    }

    @Override
    public void reloadConfig() {
        extension.loadConfig();
    }

    @Override
    public void runTask(Runnable runnable) {
        MinecraftServer.getSchedulerManager().scheduleNextProcess(runnable);
    }

    @Override
    public void saveAsynchronously(Manager manager) {
        MinecraftServer.getSchedulerManager().scheduleNextProcess(() -> {
            manager.saveAll();
        }, ExecutionType.ASYNC);
    }

    @Override
    public ICommandSender getConsoleSender() {
        return new MinestomCommandSender(MinecraftServer.getCommandManager().getConsoleSender());
    }

    private ProcessInterrupter.Builder newInterrupterBuilder() {
        return ProcessInterrupter.Builder.begin()
                .perExecutor((context, command, args) -> {
                    if ("CALL".equalsIgnoreCase(command)) {
                        if (args.length < 1)
                            throw new RuntimeException("Need parameter [String] or [String, boolean]");

                        if (args[0] instanceof String) {
                            Trigger trigger = core.getNamedTriggerManager().get((String) args[0]);
                            if (trigger == null)
                                throw new RuntimeException("No trigger found for Named Trigger " + args[0]);

                            boolean sync = true;
                            if (args.length > 1 && args[1] instanceof Boolean) {
                                sync = (boolean) args[1];
                            }

                            if (sync) {
                                trigger.activate(context.getTriggerCause(), context.getVars(), true);
                            } else {//use snapshot to avoid concurrent modification
                                trigger.activate(context.getTriggerCause(), new HashMap<>(context.getVars()), false);
                            }

                            return true;
                        } else {
                            throw new RuntimeException("Parameter type not match; it should be a String."
                                    + " Make sure to put double quotes, if you provided String literal.");
                        }
                    }

                    return false;
                })
                .perExecutor((context, command, args) -> {
                    if ("CANCELEVENT".equalsIgnoreCase(command)) {
                        if(!core.isServerThread())
                            throw new RuntimeException("Trying to cancel event in async trigger.");

                        if (context.getTriggerCause() instanceof CancellableEvent) {
                            ((CancellableEvent) context.getTriggerCause()).setCancelled(true);
                            return true;
                        } else {
                            throw new RuntimeException(context.getTriggerCause() + " is not a Cancellable event!");
                        }
                    }

                    return false;
                });
    }

    @Override
    public ProcessInterrupter createInterrupter(Map<UUID, Long> cooldowns) {
        return appendCooldownInterrupter(newInterrupterBuilder(), cooldowns)
                .build();
    }

    @Override
    public ProcessInterrupter createInterrupterForInv(Map<UUID, Long> cooldowns, Map<IInventory, InventoryTrigger> inventoryMap) {
        return appendCooldownInterrupter(newInterrupterBuilder(), cooldowns)
                .perNode((context, node) -> {
                    //safety feature to stop all trigger immediately if executing on 'open' or 'click'
                    //  is still running after the inventory is closed.
                    if (context.getTriggerCause() instanceof InventoryOpenEvent
                            || context.getTriggerCause() instanceof InventoryPreClickEvent) {
                        AbstractInventory inv = ((InventoryEvent) context.getTriggerCause()).getInventory();

                        //it's not GUI so stop execution
                        return !inventoryMap.containsKey(MinestomTriggerReactorCore.getWrapper().wrap(inv));
                    }

                    return false;
                })
                .build();
    }

    private ProcessInterrupter.Builder appendCooldownInterrupter(ProcessInterrupter.Builder builder, Map<UUID, Long> cooldowns) {
        return builder.perExecutor(((context, command, args) -> {
            if ("COOLDOWN".equalsIgnoreCase(command)) {
                if (!(args[0] instanceof Number))
                    throw new RuntimeException(args[0] + " is not a number!");

                if (context.getTriggerCause() instanceof PlayerEvent) {
                    long mills = (long) (((Number) args[0]).doubleValue() * 1000L);
                    Player player = ((PlayerEvent) context.getTriggerCause()).getPlayer();
                    UUID uuid = player.getUuid();
                    cooldowns.put(uuid, System.currentTimeMillis() + mills);
                }
                return true;
            }

            return false;
        })).perPlaceholder((context, placeholder, args) -> {
//            if ("cooldown".equals(placeholder)) {
//                if (context.getTriggerCause() instanceof PlayerEvent) {
//                    return cooldowns.getOrDefault(((PlayerEvent) context.getTriggerCause()).getPlayer().getUniqueId(), 0L);
//                } else {
//                    return 0;
//                }
//            }
            return null;
        });
    }

    @Override
    public IPlayer extractPlayerFromContext(Object e) {
        if (e instanceof PlayerEvent) {
            Player player = ((PlayerEvent) e).getPlayer();
            return wrapper.wrap(player);
        }

        return null;
    }

    ExecutorService executor = Executors.newSingleThreadExecutor();

    @Override
    public <T> Future<T> callSyncMethod(Callable<T> call) {
        try {
            /*if (isServerThread()) {
                return CompletableFuture.supplyAsync(() -> {
                    try {
                        return call.call();
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                });
            } else {
                AtomicReference<T> ref = new AtomicReference<>();
                Scheduler scheduler = Scheduler.newScheduler();
                Task task = scheduler.buildTask(() -> {
                    try {
                        ref.set(call.call());
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }).delay(TaskSchedule.park()).schedule();
                task.unpark();
                scheduler.process();
                return CompletableFuture.supplyAsync(ref::get);
            }*/
            return executor.submit(call); //minestom is thread safe program

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public void callEvent(IEvent event) {
        EventDispatcher.call(event.get());
    }

    @Override
    public boolean isServerThread() {
        return Thread.currentThread() == extension.getThread();
    }

    @Override
    public Thread newThread(Runnable runnable, String name, int priority) {
        Thread thread = new Thread(runnable, name);
        thread.setPriority(priority);
        return thread;
    }

    @Override
    public Map<String, Object> getCustomVarsForTrigger(Object context) {
        Map<String, Object> variables = new HashMap<String, Object>();

        if (context instanceof PlayerEvent event) {
            variables.put("player", event.getPlayer());
        }
        if (context instanceof BlockEvent event) {
            variables.put("block", event.getBlock());
        }
        if (context instanceof EntityEvent event) {
            variables.put("entity", event.getEntity());
        }
        if (context instanceof InstanceEvent event) {
            variables.put("world", event.getInstance());
        }
        if (context instanceof ItemEvent event) {
            variables.put("item", event.getItemStack());
        }
        if (context instanceof InventoryEvent event) {
            variables.put("inventory", event.getInventory());
        }

        return variables;
    }

    public static TriggerReactorCore getInstance() {
        return core;
    }

    public static MinestomWrapper getWrapper() {
        return wrapper;
    }
}
