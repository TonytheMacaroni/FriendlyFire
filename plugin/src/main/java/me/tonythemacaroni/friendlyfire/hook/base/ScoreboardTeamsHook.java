package me.tonythemacaroni.friendlyfire.hook.base;

import org.jetbrains.annotations.NotNull;

import org.spongepowered.configurate.objectmapping.ConfigSerializable;

import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.scoreboard.Team;
import org.bukkit.scoreboard.Scoreboard;

import me.tonythemacaroni.friendlyfire.hook.Hook;
import me.tonythemacaroni.friendlyfire.hook.HookInfo;
import me.tonythemacaroni.friendlyfire.util.Priority;
import me.tonythemacaroni.friendlyfire.util.Relation;
import me.tonythemacaroni.friendlyfire.hook.RelationHook;
import me.tonythemacaroni.friendlyfire.util.Relationship;
import me.tonythemacaroni.friendlyfire.util.OrderedPriority;
import me.tonythemacaroni.friendlyfire.util.RelationQueryInfo;

@HookInfo(
    name = "scoreboard-teams",
    description = "Determines interactions between scoreboard teams.",
    namespace = "friendlyfire"
)
public class ScoreboardTeamsHook extends Hook<ScoreboardTeamsHook.Config> implements RelationHook {

    public ScoreboardTeamsHook(Config config) {
        super(config);
    }

    @Override
    public @NotNull Relationship queryRelation(@NotNull Entity actor, @NotNull Entity target, @NotNull RelationQueryInfo info) {
        Scoreboard scoreboard = Bukkit.getScoreboardManager().getMainScoreboard();

        // TODO: Make option for entities in different teams to be enemies. Also make similar option for entities in a
        //  team interacting with entities without a team. Also determine if friendly-fire teams should return a relation.

        Team teamFirst = scoreboard.getEntityTeam(actor);
        if (teamFirst == null) return Relationship.UNKNOWN;

        Team teamSecond = scoreboard.getEntityTeam(target);
        if (!teamFirst.equals(teamSecond) || teamFirst.allowFriendlyFire()) return Relationship.UNKNOWN;

        return new Relationship(Relation.ALLIED, config.priorityLevel);
    }

    @ConfigSerializable
    public static class Config extends Hook.Config {

        public OrderedPriority priorityLevel = new OrderedPriority(Priority.HIGH);

    }

}
