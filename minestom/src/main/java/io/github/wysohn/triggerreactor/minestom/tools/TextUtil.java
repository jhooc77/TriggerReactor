package io.github.wysohn.triggerreactor.minestom.tools;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import net.minestom.server.adventure.provider.MinestomLegacyComponentSerializerProvider;
import net.minestom.server.adventure.provider.MinestomPlainTextComponentSerializerProvider;

public class TextUtil {

    private static LegacyComponentSerializer serializer = new MinestomLegacyComponentSerializerProvider().legacyAmpersand();
    private static PlainTextComponentSerializer plainTextComponentSerializer = new MinestomPlainTextComponentSerializerProvider().plainTextSimple();

    public static Component coloredAmpersand(String text) {
        return serializer.deserialize(text);
    }

    public static String serializeComponent(Component component) {
        return plainTextComponentSerializer.serialize(component);
    }


}
