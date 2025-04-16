package me.tonythemacaroni.friendlyfire.hook.base;

import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

import org.spongepowered.configurate.objectmapping.ConfigSerializable;

import org.bukkit.entity.Entity;
import org.bukkit.entity.EvokerFangs;

import me.tonythemacaroni.friendlyfire.hook.Hook;
import me.tonythemacaroni.friendlyfire.util.Priority;
import me.tonythemacaroni.friendlyfire.util.Relation;
import me.tonythemacaroni.friendlyfire.hook.HookInfo;
import me.tonythemacaroni.friendlyfire.hook.RelatedHook;
import me.tonythemacaroni.friendlyfire.hook.RelationHook;
import me.tonythemacaroni.friendlyfire.util.Relationship;
import me.tonythemacaroni.friendlyfire.util.OrderedPriority;
import me.tonythemacaroni.friendlyfire.util.RelationQueryInfo;
import me.tonythemacaroni.friendlyfire.hook.config.ForceInteract;

@HookInfo(
    name = "evoker-fangs-owner",
    description = "Checks for the owner of evoker fangs.",
    namespace = "friendlyfire"
)
public class EvokerFangsOwnerHook extends Hook<EvokerFangsOwnerHook.Config> implements RelationHook, RelatedHook {

    public EvokerFangsOwnerHook(Config config) {
        super(config);
    }

    @Override
    public @NotNull Relationship queryRelation(@NotNull Entity actor, @NotNull Entity target, @NotNull RelationQueryInfo info) {
        if (actor instanceof EvokerFangs fangs && target.equals(fangs.getOwner())) {
            if (config.forceOwnerInteract.enabled() && info.isDirect(actor, target))
                return config.forceOwnerInteract.relationship();

            return new Relationship(Relation.ALLIED, config.priorityLevel);
        }

        if (target instanceof EvokerFangs fangs && actor.equals(fangs.getOwner()))
            return new Relationship(Relation.ALLIED, config.priorityLevel);

        return Relationship.UNKNOWN;
    }

    @Override
    public void queryRelated(@NotNull Entity entity, @NotNull Consumer<Entity> related) {
        if (!(entity instanceof EvokerFangs fangs)) return;

        Entity owner = fangs.getOwner();
        if (owner == null) return;

        related.accept(owner);
    }

    @ConfigSerializable
    public static class Config extends Hook.Config {

        public OrderedPriority priorityLevel = new OrderedPriority(Priority.HIGH);
        public ForceInteract forceOwnerInteract = new ForceInteract();

    }


}
