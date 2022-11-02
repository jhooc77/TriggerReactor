package io.github.wysohn.triggerreactor.minestom.main;

import io.github.wysohn.triggerreactor.core.IEventHook;
import io.github.wysohn.triggerreactor.core.main.IEventRegistry;
import io.github.wysohn.triggerreactor.core.main.TriggerReactorCore;
import io.github.wysohn.triggerreactor.minestom.manager.event.TriggerReactorStartEvent;
import io.github.wysohn.triggerreactor.minestom.manager.event.TriggerReactorStopEvent;
import io.github.wysohn.triggerreactor.tools.ReflectionUtil;
import net.minestom.server.MinecraftServer;
import net.minestom.server.event.Event;
import net.minestom.server.event.EventListener;
import net.minestom.server.event.EventNode;
import net.minestom.server.event.entity.EntityDeathEvent;
import net.minestom.server.event.player.*;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.*;

public class MinestomEventRegistry implements IEventRegistry {
    private final EventNode<Event> eventNode;
    private final Map<IEventHook, EventListener<?>> listenerMap = new HashMap<>();

    public MinestomEventRegistry(EventNode<Event> node) {
        try {
            initEvents();
        } catch (IOException e) {
            e.printStackTrace();
        }
        eventNode = EventNode.all("trg-events");
        node.addChild(eventNode);
    }

    protected void initEvents() throws IOException {
        for (String clazzName : ReflectionUtil.getAllClasses(MinecraftServer.class.getClassLoader(), basePackageName)) {
            Class<?> test = null;
            try {
                test = Class.forName(clazzName);
            } catch (ClassNotFoundException e1) {
                e1.printStackTrace();
            }

            if (!Event.class.isAssignableFrom(test))
                continue;

            Class<? extends Event> clazz = (Class<? extends Event>) test;
            if (clazz.equals(Event.class))
                continue;

            EVENTS.put(clazz.getSimpleName(), clazz);
        }
    }

    @Override
    public boolean eventExist(String eventStr) {
        try {
            return getEvent(eventStr) != null;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    @Override
    public Class<?> getEvent(String eventStr) throws ClassNotFoundException {
        Class<? extends Event> event;
        if (ABBREVIATIONS.containsKey(eventStr)) {
            event = ABBREVIATIONS.get(eventStr);
        } else if (EVENTS.containsKey(eventStr)) {
            event = EVENTS.get(eventStr);
        } else {
            event = (Class<? extends Event>) Class.forName(eventStr);
        }

        return event;
    }

    @Override
    public void registerEvent(TriggerReactorCore plugin, Class<?> clazz, IEventHook eventHook) {
        EventListener<?> listener = new EventListener<>() {
            @Override
            public @NotNull Class eventType() {
                return clazz;
            }

            @Override
            public @NotNull Result run(@NotNull Event event) {
                eventHook.onEvent(event);
                return Result.SUCCESS;
            }
        };
        listenerMap.put(eventHook, listener);
        eventNode.addListener(listener);
    }

    @Override
    public void unregisterEvent(TriggerReactorCore plugin, IEventHook eventHook) {
        EventListener<?> listener = listenerMap.remove(eventHook);
        if (listener != null) {
            eventNode.removeListener(listener);
        }
    }

    @Override
    public void unregisterAll(){
        for (Map.Entry<IEventHook, EventListener<?>> entry : listenerMap.entrySet()) {
            eventNode.removeListener(entry.getValue());
        }
        listenerMap.clear();
    }

    public Collection<String> getAbbreviations() {
        return ABBREVIATIONS.keySet();
    }

    static final Map<String, Class<? extends Event>> EVENTS = new TreeMap<String, Class<? extends Event>>(String.CASE_INSENSITIVE_ORDER);
    static final List<Class<? extends Event>> BASEEVENTS = new ArrayList<Class<? extends Event>>();
    @SuppressWarnings("serial")
    private static final Map<String, Class<? extends Event>> ABBREVIATIONS = new HashMap<String, Class<? extends Event>>() {{
        put("onJoin", PlayerLoginEvent.class);
        put("onQuit", PlayerDisconnectEvent.class);
        put("onPlayerDeath", PlayerDeathEvent.class);
        put("onInteractBlock", PlayerBlockInteractEvent.class);
        put("onInteractEntity", PlayerEntityInteractEvent.class);
        put("onChat", PlayerChatEvent.class);
        put("onCommand", PlayerCommandEvent.class);

        //put("onEntitySpawn", EntitySpawnEvent.class);
        put("onEntityDeath", EntityDeathEvent.class);

        put("onBlockPlace", PlayerBlockPlaceEvent.class);
        put("onBlockBreak", PlayerBlockBreakEvent.class);

        put("onStart", TriggerReactorStartEvent.class);
        put("onStop", TriggerReactorStopEvent.class);
    }};
    private static final String basePackageName = "org.bukkit.event";
}
