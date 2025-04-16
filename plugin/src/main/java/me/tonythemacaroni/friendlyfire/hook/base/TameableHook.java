package me.tonythemacaroni.friendlyfire.hook.base;

import org.jetbrains.annotations.NotNull;

import java.util.UUID;
import java.util.function.Consumer;

import org.spongepowered.configurate.objectmapping.ConfigSerializable;

import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Tameable;

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
    name = "tameable",
    description = "Determines interactions between tamed mobs and their owners.",
    namespace = "friendlyfire"
)
public class TameableHook extends Hook<TameableHook.Config> implements RelationHook, RelatedHook {

    public TameableHook(Config config) {
        super(config);
    }

    @Override
    public @NotNull Relationship queryRelation(@NotNull Entity actor, @NotNull Entity target, @NotNull RelationQueryInfo info) {
        if (actor instanceof Tameable tameable && target.getUniqueId().equals(tameable.getOwnerUniqueId()))
            return new Relationship(Relation.ALLIED, config.priorityLevel);

        if (target instanceof Tameable tameable && actor.getUniqueId().equals(tameable.getOwnerUniqueId()))
            return new Relationship(Relation.ALLIED, config.priorityLevel);

        return Relationship.UNKNOWN;
    }

    @Override
    public void queryRelated(@NotNull Entity entity, @NotNull Consumer<Entity> related) {
        if (!(entity instanceof Tameable tameable)) return;

        UUID uuid = tameable.getOwnerUniqueId();
        if (uuid == null) return;

        Entity owner = Bukkit.getEntity(uuid);
        if (owner == null) return;

        related.accept(owner);
    }

    @ConfigSerializable
    public static class Config extends Hook.Config {

        public OrderedPriority priorityLevel = new OrderedPriority(Priority.HIGH);

    }

}
