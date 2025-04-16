package me.tonythemacaroni.friendlyfire.util.configuration;

import org.jetbrains.annotations.NotNull;

import java.util.function.Predicate;
import java.lang.reflect.AnnotatedType;

import org.spongepowered.configurate.serialize.ScalarSerializer;
import org.spongepowered.configurate.serialize.SerializationException;
import org.spongepowered.configurate.serialize.CoercionFailedException;

import me.tonythemacaroni.friendlyfire.util.Priority;
import me.tonythemacaroni.friendlyfire.util.OrderedPriority;

public class OrderedPrioritySerializer extends ScalarSerializer.Annotated<OrderedPriority> {

    public OrderedPrioritySerializer() {
        super(OrderedPriority.class);
    }

    @Override
    public OrderedPriority deserialize(@NotNull AnnotatedType type, @NotNull Object obj) throws SerializationException {
        if (!(obj instanceof String value))
            throw new CoercionFailedException(type, obj, "string");

        String[] data = value.split("~");
        if (data.length > 2)
            throw new SerializationException("Invalid ordered priority:" + value);

        Priority priority;
        try {
            priority = Priority.valueOf(data[0].toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new SerializationException("Invalid priority: "  + data[0]);
        }

        int ordinal = 0;
        if (data.length > 1) {
            try {
                ordinal = Integer.parseInt(data[1]);
            } catch (NumberFormatException e) {
                throw new SerializationException("Invalid ordinal: " + data[1]);
            }
        }

        return new OrderedPriority(priority, ordinal);
    }

    @Override
    @NotNull
    protected Object serialize(OrderedPriority item, @NotNull Predicate<Class<?>> typeSupported) {
        String value = item.priority().name().toLowerCase();
        if (item.ordinal() != 0) value = value + "~" + item.ordinal();
        return value;
    }

}
