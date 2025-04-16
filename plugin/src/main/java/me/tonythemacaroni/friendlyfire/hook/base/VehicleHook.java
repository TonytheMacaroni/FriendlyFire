package me.tonythemacaroni.friendlyfire.hook.base;

import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.function.Consumer;

import org.spongepowered.configurate.objectmapping.ConfigSerializable;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Vehicle;

import me.tonythemacaroni.friendlyfire.hook.Hook;
import me.tonythemacaroni.friendlyfire.hook.HookInfo;
import me.tonythemacaroni.friendlyfire.util.Priority;
import me.tonythemacaroni.friendlyfire.util.Relation;
import me.tonythemacaroni.friendlyfire.hook.RelatedHook;
import me.tonythemacaroni.friendlyfire.hook.RelationHook;
import me.tonythemacaroni.friendlyfire.util.Relationship;
import me.tonythemacaroni.friendlyfire.util.OrderedPriority;
import me.tonythemacaroni.friendlyfire.util.RelationQueryInfo;

@HookInfo(
    name = "vehicle",
    description = "Determines interactions with vehicles and their riders.",
    namespace = "friendlyfire"
)
public class VehicleHook extends Hook<VehicleHook.Config> implements RelationHook, RelatedHook {

    public VehicleHook(Config config) {
        super(config);
    }

    @Override
    public @NotNull Relationship queryRelation(@NotNull Entity actor, @NotNull Entity target, @NotNull RelationQueryInfo info) {
        Vehicle vehicleActor = actor.getVehicle() instanceof Vehicle vehicle ? vehicle : null;
        if (target.equals(vehicleActor))
            return new Relationship(Relation.ALLIED, config.priorityLevel);

        Vehicle vehicleTarget = target.getVehicle() instanceof Vehicle vehicle ? vehicle : null;
        if (actor.equals(vehicleTarget))
            return new Relationship(Relation.ALLIED, config.priorityLevel);

        return Relationship.UNKNOWN;
    }

    @Override
    public void queryRelated(@NotNull Entity entity, @NotNull Consumer<Entity> related) {
        if (!(entity instanceof Vehicle)) return;

        List<Entity> passengers = entity.getPassengers();
        if (passengers.isEmpty()) return;

        related.accept(passengers.getFirst());
    }

    @ConfigSerializable
    public static class Config extends Hook.Config {

        public OrderedPriority priorityLevel = new OrderedPriority(Priority.HIGH);

    }

}
