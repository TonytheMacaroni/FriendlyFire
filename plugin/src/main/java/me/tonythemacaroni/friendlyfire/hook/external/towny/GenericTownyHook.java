package me.tonythemacaroni.friendlyfire.hook.external.towny;

import org.jetbrains.annotations.NotNull;

import org.spongepowered.configurate.objectmapping.ConfigSerializable;

import org.bukkit.entity.Entity;
import org.bukkit.event.entity.EntityDamageEvent;

import com.palmergames.bukkit.towny.utils.CombatUtil;

import me.tonythemacaroni.friendlyfire.hook.Hook;
import me.tonythemacaroni.friendlyfire.hook.HookInfo;
import me.tonythemacaroni.friendlyfire.util.Priority;
import me.tonythemacaroni.friendlyfire.util.Relation;
import me.tonythemacaroni.friendlyfire.hook.RelationHook;
import me.tonythemacaroni.friendlyfire.util.Relationship;
import me.tonythemacaroni.friendlyfire.util.OrderedPriority;
import me.tonythemacaroni.friendlyfire.util.RelationQueryInfo;

@HookInfo(
    name = "generic",
    description = "Controls interactions within Towny regions.",
    namespace = "towny",
    depends = "Towny"
)
public class GenericTownyHook extends Hook<GenericTownyHook.Config> implements RelationHook {

    public GenericTownyHook(Config config) {
        super(config);
    }

    @Override
    public @NotNull Relationship queryRelation(@NotNull Entity actor, @NotNull Entity target, @NotNull RelationQueryInfo info) {
        EntityDamageEvent.DamageCause cause = EntityDamageEvent.DamageCause.CUSTOM;
        if (info.event() instanceof EntityDamageEvent event) cause = event.getCause();

        if (!CombatUtil.preventDamageCall(actor, target, cause))
            return Relationship.UNKNOWN;

        return new Relationship(Relation.FRIENDLY, config.priorityLevel);
    }

    @ConfigSerializable
    public static class Config extends Hook.Config {

        public OrderedPriority priorityLevel = new OrderedPriority(Priority.HIGH);

    }

}
