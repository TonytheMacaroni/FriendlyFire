package me.tonythemacaroni.friendlyfire.hook.base;

import org.jetbrains.annotations.NotNull;

import java.util.UUID;
import java.util.function.Consumer;

import org.spongepowered.configurate.objectmapping.ConfigSerializable;

import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.AreaEffectCloud;

import me.tonythemacaroni.friendlyfire.hook.Hook;
import me.tonythemacaroni.friendlyfire.hook.HookInfo;
import me.tonythemacaroni.friendlyfire.util.Priority;
import me.tonythemacaroni.friendlyfire.util.Relation;
import me.tonythemacaroni.friendlyfire.hook.RelatedHook;
import me.tonythemacaroni.friendlyfire.hook.RelationHook;
import me.tonythemacaroni.friendlyfire.util.Relationship;
import me.tonythemacaroni.friendlyfire.util.OrderedPriority;
import me.tonythemacaroni.friendlyfire.util.RelationQueryInfo;
import me.tonythemacaroni.friendlyfire.hook.config.ForceInteract;

@HookInfo(
    name = "area-effect-cloud-owner",
    description = "Checks for the owner of an area effect cloud.",
    namespace = "friendlyfire"
)
public class AreaEffectCloudOwnerHook extends Hook<AreaEffectCloudOwnerHook.Config> implements RelationHook, RelatedHook {

    public AreaEffectCloudOwnerHook(Config config) {
        super(config);
    }

    @Override
    public @NotNull Relationship queryRelation(@NotNull Entity actor, @NotNull Entity target, @NotNull RelationQueryInfo info) {
        if (actor instanceof AreaEffectCloud cloud && target.getUniqueId().equals(cloud.getOwnerUniqueId())) {
            if (config.forceOwnerInteract.enabled() && info.isDirect(actor, target))
                return config.forceOwnerInteract.relationship();

            return new Relationship(Relation.ALLIED, config.priorityLevel);
        }

        if (target instanceof AreaEffectCloud cloud && actor.getUniqueId().equals(cloud.getOwnerUniqueId()))
            return new Relationship(Relation.ALLIED, config.priorityLevel);

        return Relationship.UNKNOWN;
    }

    @Override
    public void queryRelated(@NotNull Entity entity, @NotNull Consumer<Entity> related) {
        if (!(entity instanceof AreaEffectCloud cloud)) return;

        UUID uuid = cloud.getOwnerUniqueId();
        if (uuid == null) return;

        Entity owner = Bukkit.getEntity(uuid);
        if (owner == null) return;

        related.accept(owner);
    }

    @ConfigSerializable
    public static class Config extends Hook.Config {

        public OrderedPriority priorityLevel = new OrderedPriority(Priority.HIGH);
        public ForceInteract forceOwnerInteract = new ForceInteract();

    }

}
