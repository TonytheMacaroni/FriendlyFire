package me.tonythemacaroni.friendlyfire.hook.base;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.Collections;

import org.jetbrains.annotations.Nullable;
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
import me.tonythemacaroni.friendlyfire.hook.config.RelationPriorityLevels;

@HookInfo(
    name = "world-pvp",
    description = "Controls PvP interactions within worlds.",
    namespace = "friendlyfire"
)
public class WorldPvPHook extends Hook<WorldPvPHook.Config> implements RelationHook {

    public WorldPvPHook(Config config) {
        super(config);
    }

    @Override
    public @NotNull Relationship queryRelation(@NotNull Entity actor, @NotNull Entity target, @NotNull RelationQueryInfo info) {
        if (!(actor instanceof Player && target instanceof Player))
            return Relationship.UNKNOWN;

        Relation relationActor = getRelation(actor);
        Relation relationTarget = getRelation(target);

        Relation relation;
        if (relationActor == null && relationTarget == null) return Relationship.UNKNOWN;
        else if (relationActor != null && relationTarget != null) relation = Relation.combine(relationActor, relationTarget);
        else relation = relationActor == null ? relationTarget : relationActor;

        return new Relationship(relation, config.priorityLevel.of(relation));
    }

    @Nullable
    private Relation getRelation(@NotNull Entity entity) {
        String world = entity.getWorld().getName();
        return config.worlds.get(world);
    }

    @ConfigSerializable
    public static class Config extends Hook.Config {

        public RelationPriorityLevels priorityLevel = new RelationPriorityLevels(
            new OrderedPriority(Priority.HIGH),
            new OrderedPriority(Priority.HIGH),
            new OrderedPriority(Priority.LOW),
            new OrderedPriority(Priority.LOW),
            new OrderedPriority(Priority.LOW)
        );

        public Map<String, Relation> worlds = Collections.emptyMap();

    }

}
