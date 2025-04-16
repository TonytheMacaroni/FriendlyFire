package me.tonythemacaroni.friendlyfire.hook.external.worldguard;

import org.jetbrains.annotations.NotNull;

import org.spongepowered.configurate.objectmapping.ConfigSerializable;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.LocalPlayer;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.flags.EnumFlag;
import com.sk89q.worldguard.protection.regions.RegionQuery;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import com.sk89q.worldguard.protection.flags.registry.FlagRegistry;
import com.sk89q.worldguard.protection.flags.registry.FlagConflictException;

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
    name = "region-pvp",
    description = "Controls PvP interactions in WorldGuard regions via a 'pvp-relation' flag.",
    namespace = "worldguard",
    depends = "WorldGuard"
)
public class WorldGuardPvPHook extends Hook<WorldGuardPvPHook.Config> implements RelationHook {

    private final EnumFlag<Relation> relationPvPFlag;

    public WorldGuardPvPHook(WorldGuardPvPHook.Config config) {
        super(config);

        EnumFlag<Relation> flag;

        FlagRegistry registry = WorldGuard.getInstance().getFlagRegistry();
        try {
            flag = new EnumFlag<>("pvp-relation", Relation.class);
            registry.register(flag);
        } catch (FlagConflictException | IllegalStateException e) {
            if (!(registry.get("pvp-relation") instanceof EnumFlag<?> existing) || existing.getEnumClass() != Relation.class)
                throw new RuntimeException("Could not register 'pvp-relation' flag", e);

            //noinspection unchecked
            flag = (EnumFlag<Relation>) existing;
        }

        relationPvPFlag = flag;
    }

    @Override
    public @NotNull Relationship queryRelation(@NotNull Entity actor, @NotNull Entity target, @NotNull RelationQueryInfo info) {
        if (!(actor instanceof Player playerActor && target instanceof Player playerTarget))
            return Relationship.UNKNOWN;

        RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
        RegionQuery query = container.createQuery();

        WorldGuardPlugin worldGuard = WorldGuardPlugin.inst();
        LocalPlayer localActor = worldGuard.wrapPlayer(playerActor);
        LocalPlayer localTarget = worldGuard.wrapPlayer(playerTarget);

        Relation relationActor = query.queryValue(localActor.getLocation(), localActor, relationPvPFlag);
        Relation relationTarget = query.queryValue(localTarget.getLocation(), localTarget, relationPvPFlag);

        Relation relation;
        if (relationActor == null && relationTarget == null) return Relationship.UNKNOWN;
        else if (relationActor != null && relationTarget != null) relation = relationActor.combine(relationTarget);
        else relation = relationActor == null ? relationTarget : relationActor;

        return new Relationship(relation, config.priorityLevel.of(relation));
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

    }

}
