package me.tonythemacaroni.friendlyfire.hook.base;

import org.jetbrains.annotations.NotNull;

import org.spongepowered.configurate.objectmapping.ConfigSerializable;

import org.bukkit.entity.Entity;

import me.tonythemacaroni.friendlyfire.hook.Hook;
import me.tonythemacaroni.friendlyfire.hook.HookInfo;
import me.tonythemacaroni.friendlyfire.util.Priority;
import me.tonythemacaroni.friendlyfire.util.Relation;
import me.tonythemacaroni.friendlyfire.hook.RelationHook;
import me.tonythemacaroni.friendlyfire.util.Relationship;
import me.tonythemacaroni.friendlyfire.util.OrderedPriority;
import me.tonythemacaroni.friendlyfire.util.RelationQueryInfo;
import me.tonythemacaroni.friendlyfire.hook.config.ForceInteract;

@HookInfo(
    name = "self",
    description = "Controls self interactions.",
    namespace = "friendlyfire"
)
public class SelfHook extends Hook<SelfHook.Config> implements RelationHook {

    public SelfHook(Config config) {
        super(config);
    }

    @Override
    public @NotNull Relationship queryRelation(@NotNull Entity actor, @NotNull Entity target, @NotNull RelationQueryInfo info) {
        if (!actor.equals(target)) return Relationship.UNKNOWN;

        if (config.forceSelfInteract.enabled() && info.isDirect(actor, target))
            return config.forceSelfInteract.relationship();

        return new Relationship(Relation.ALLIED, config.priorityLevel);
    }

    @ConfigSerializable
    public static class Config extends Hook.Config {

        public OrderedPriority priorityLevel = new OrderedPriority(Priority.VERY_HIGH);
        public ForceInteract forceSelfInteract = new ForceInteract();

    }

}
