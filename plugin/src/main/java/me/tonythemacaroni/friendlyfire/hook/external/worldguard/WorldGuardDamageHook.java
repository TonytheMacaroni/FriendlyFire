package me.tonythemacaroni.friendlyfire.hook.external.worldguard;

import org.jetbrains.annotations.NotNull;

import org.spongepowered.configurate.objectmapping.ConfigSerializable;

import org.bukkit.entity.Entity;

import com.sk89q.worldguard.bukkit.ProtectionQuery;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;

import me.tonythemacaroni.friendlyfire.hook.Hook;
import me.tonythemacaroni.friendlyfire.hook.HookInfo;
import me.tonythemacaroni.friendlyfire.util.Priority;
import me.tonythemacaroni.friendlyfire.util.Relation;
import me.tonythemacaroni.friendlyfire.hook.RelationHook;
import me.tonythemacaroni.friendlyfire.util.Relationship;
import me.tonythemacaroni.friendlyfire.util.OrderedPriority;
import me.tonythemacaroni.friendlyfire.util.RelationQueryInfo;

@HookInfo(
    name = "simulate-damage",
    description = "Controls interactions in WorldGuard regions by simulating dealing damage.",
    namespace = "worldguard",
    depends = "WorldGuard"
)
public class WorldGuardDamageHook extends Hook<WorldGuardDamageHook.Config> implements RelationHook {

    private final ProtectionQuery protectionQuery;
    
    public WorldGuardDamageHook(WorldGuardDamageHook.Config config) {
        super(config);

        protectionQuery = WorldGuardPlugin.inst().createProtectionQuery();
    }

    @Override
    public @NotNull Relationship queryRelation(@NotNull Entity actor, @NotNull Entity target, @NotNull RelationQueryInfo info) {
        boolean allowed = protectionQuery.testEntityDamage(actor, target);
        if (!allowed) return new Relationship(Relation.FRIENDLY, config.priorityLevel);

        return Relationship.UNKNOWN;
    }

    @ConfigSerializable
    public static class Config extends Hook.Config {

        public OrderedPriority priorityLevel = new OrderedPriority(Priority.HIGH);

    }

}
