package me.tonythemacaroni.friendlyfire.hook.base;

import org.jetbrains.annotations.NotNull;

import java.util.UUID;
import java.util.function.Consumer;

import org.spongepowered.configurate.objectmapping.ConfigSerializable;

import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Projectile;

import me.tonythemacaroni.friendlyfire.hook.Hook;
import me.tonythemacaroni.friendlyfire.hook.HookInfo;
import me.tonythemacaroni.friendlyfire.util.Relation;
import me.tonythemacaroni.friendlyfire.util.Priority;
import me.tonythemacaroni.friendlyfire.hook.RelatedHook;
import me.tonythemacaroni.friendlyfire.hook.RelationHook;
import me.tonythemacaroni.friendlyfire.util.Relationship;
import me.tonythemacaroni.friendlyfire.util.OrderedPriority;
import me.tonythemacaroni.friendlyfire.util.RelationQueryInfo;
import me.tonythemacaroni.friendlyfire.hook.config.ForceInteract;

@HookInfo(
    name = "projectile-owner",
    description = "Checks for the owner of projectiles.",
    namespace = "friendlyfire"
)
public class ProjectileOwnerHook extends Hook<ProjectileOwnerHook.Config> implements RelationHook, RelatedHook {

    public ProjectileOwnerHook(Config config) {
        super(config);
    }

    @Override
    public @NotNull Relationship queryRelation(@NotNull Entity actor, @NotNull Entity target, @NotNull RelationQueryInfo info) {
        if (actor instanceof Projectile projectile && target.getUniqueId().equals(projectile.getOwnerUniqueId())) {
            if (config.forceOwnerInteract.enabled() && info.isDirect(actor, target))
                return config.forceOwnerInteract.relationship();

            return new Relationship(Relation.ALLIED, config.priorityLevel);
        }

        if (target instanceof Projectile projectile && actor.getUniqueId().equals(projectile.getOwnerUniqueId()))
            return new Relationship(Relation.ALLIED, config.priorityLevel);

        return Relationship.UNKNOWN;
    }

    @Override
    public void queryRelated(@NotNull Entity entity, @NotNull Consumer<Entity> related) {
        if (!(entity instanceof Projectile projectile)) return;

        UUID uuid = projectile.getOwnerUniqueId();
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
