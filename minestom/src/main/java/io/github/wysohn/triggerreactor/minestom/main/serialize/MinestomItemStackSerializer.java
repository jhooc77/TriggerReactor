package io.github.wysohn.triggerreactor.minestom.main.serialize;

import io.github.wysohn.gsoncopy.*;
import io.github.wysohn.triggerreactor.core.config.serialize.Serializer;
import net.minestom.server.item.ItemStack;
import org.jglrxavpok.hephaistos.nbt.NBTCompound;

import java.lang.reflect.Type;

public class MinestomItemStackSerializer implements Serializer<ItemStack> {
    @Override
    public ItemStack deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        JsonObject ser = (JsonObject) json;

        try {
            NBTCompound compound = context.deserialize(ser.get(Serializer.SER_VALUE), NBTCompound.class);
            return ItemStack.fromItemNBT(compound);
        } catch (Exception ex) {
            throw new RuntimeException("Cannot deserialize " + json, ex);
        }
    }

    @Override
    public JsonElement serialize(ItemStack src, Type typeOfSrc, JsonSerializationContext context) {
        return Serializer.serialize(ItemStack.class, src.toItemNBT(), context);
    }
}
