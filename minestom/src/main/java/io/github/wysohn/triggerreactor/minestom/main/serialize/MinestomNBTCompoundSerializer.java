package io.github.wysohn.triggerreactor.minestom.main.serialize;

import io.github.wysohn.gsoncopy.*;
import io.github.wysohn.triggerreactor.core.config.serialize.Serializer;
import io.github.wysohn.triggerreactor.core.config.source.GsonConfigSource;
import org.jglrxavpok.hephaistos.nbt.NBT;
import org.jglrxavpok.hephaistos.nbt.NBTCompound;
import org.jglrxavpok.hephaistos.nbt.NBTException;
import org.jglrxavpok.hephaistos.parser.SNBTParser;

import java.io.StringReader;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class MinestomNBTCompoundSerializer implements Serializer<NBTCompound> {

    @Override
    public NBTCompound deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        JsonObject ser = (JsonObject) json;

        Map<String, NBT> map = new LinkedHashMap<>();

        try {
            Map<String, NBT> subs = new HashMap<>();
            ser.entrySet().forEach(entry -> {
                String key = entry.getKey();
                JsonElement value = entry.getValue();

                if (value instanceof JsonObject) {
                    NBTCompound sub = deserialize(value, typeOfT, context);
                    if (sub == null) { // just a Map if JsonObject and is not serialized value
                        subs.put(key, context.deserialize(value, Map.class));
                    } else {
                        subs.put(key, sub);
                    }
                } else {
                    try {
                        subs.put(key, new SNBTParser(new StringReader(context.deserialize(value, String.class))).parse());
                    } catch (NBTException e) {
                        throw new RuntimeException(e);
                    }
                }
            });
            map.putAll(subs);

            return new NBTCompound(map);
        } catch (Exception ex) {
            throw new RuntimeException("Cannot deserialize " + json, ex);
        }
    }

    private Map<String, Object> flatCompound(NBTCompound compound) {
        Map<String, Object> map = new HashMap<>();
        compound.forEach((key, value) -> {
            if (value instanceof NBTCompound c) {
                map.put(key, flatCompound(c));
            } else {
                map.put(key, value.toSNBT());
            }
        });
        return map;
    }

    @Override
    public JsonElement serialize(NBTCompound src, Type typeOfSrc, JsonSerializationContext context) {
        return Serializer.serialize(NBTCompound.class, flatCompound(src), context);
    }
}
