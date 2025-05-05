package me.tonythemacaroni.friendlyfire.hook.external.battlearena;

import org.jetbrains.annotations.NotNull;

import org.spongepowered.configurate.objectmapping.ConfigSerializable;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import org.battleplugins.arena.ArenaPlayer;
import org.battleplugins.arena.team.ArenaTeam;
import org.battleplugins.arena.options.DamageOption;
import org.battleplugins.arena.competition.Competition;
import org.battleplugins.arena.options.ArenaOptionType;
import org.battleplugins.arena.competition.LiveCompetition;
import org.battleplugins.arena.options.types.EnumArenaOption;

import me.tonythemacaroni.friendlyfire.hook.Hook;
import me.tonythemacaroni.friendlyfire.hook.HookInfo;
import me.tonythemacaroni.friendlyfire.util.Priority;
import me.tonythemacaroni.friendlyfire.util.Relation;
import me.tonythemacaroni.friendlyfire.hook.RelationHook;
import me.tonythemacaroni.friendlyfire.util.Relationship;
import me.tonythemacaroni.friendlyfire.util.OrderedPriority;
import me.tonythemacaroni.friendlyfire.util.RelationQueryInfo;

@HookInfo(
    name = "competitions",
    description = "Controls interactions during BattleArena competitions.",
    namespace = "battlearena",
    depends = "BattleArena"
)
public class BattleArenaHook extends Hook<BattleArenaHook.Config> implements RelationHook {

    public BattleArenaHook(Config config) {
        super(config);
    }

    @Override
    public @NotNull Relationship queryRelation(@NotNull Entity actor, @NotNull Entity target, @NotNull RelationQueryInfo info) {
        // TODO: Reference: https://github.com/BattlePlugins/BattleArena/blob/910f4be9243642a97759b21e8c8bec22558aaf53/plugin/src/main/java/org/battleplugins/arena/competition/OptionsListener.java#L85
        //  Check the usages of hostile vs unknown.

        if (!(actor instanceof Player playerActor)) return Relationship.UNKNOWN;

        ArenaPlayer arenaActor = ArenaPlayer.getArenaPlayer(playerActor);
        if (arenaActor == null) return Relationship.UNKNOWN;

        LiveCompetition<? extends Competition<?>> competition = arenaActor.getCompetition();

        if (!(target instanceof Player playerTarget)) {
            DamageOption option = competition.option(ArenaOptionType.DAMAGE_ENTITIES)
                .map(EnumArenaOption::getOption)
                .orElse(DamageOption.ALWAYS);

            if (option == DamageOption.NEVER)
                return new Relationship(Relation.ALLIED, config.priorityLevel.allied);

            return Relationship.UNKNOWN;
        }

        DamageOption option = competition.option(ArenaOptionType.DAMAGE_PLAYERS)
            .map(EnumArenaOption::getOption)
            .orElse(DamageOption.ALWAYS);

        ArenaPlayer arenaTarget = ArenaPlayer.getArenaPlayer(playerTarget);
        if (arenaTarget == null || !competition.equals(arenaTarget.getCompetition()))
            return Relationship.UNKNOWN;

        return switch (option) {
            case ALWAYS -> new Relationship(Relation.HOSTILE, config.priorityLevel.hostile);
            case NEVER -> new Relationship(Relation.ALLIED, config.priorityLevel.allied);
            case OTHER_TEAM -> {
                ArenaTeam teamActor = arenaActor.getTeam(), teamTarget = arenaTarget.getTeam();
                if (teamActor == null || teamTarget == null) yield Relationship.UNKNOWN;

                if (teamActor.isHostileTo(teamTarget))
                    yield new Relationship(Relation.HOSTILE, config.priorityLevel.hostile);

                yield new Relationship(Relation.ALLIED, config.priorityLevel.allied);
            }
        };
    }

    @ConfigSerializable
    public static class Config extends Hook.Config {

        public PriorityLevels priorityLevel = new PriorityLevels(new OrderedPriority(Priority.HIGH), new OrderedPriority(Priority.LOW));

    }

    @ConfigSerializable
    public record PriorityLevels(OrderedPriority allied, OrderedPriority hostile) {

    }

}
