package me.tonythemacaroni.friendlyfire.hook.base;

import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Optional;
import java.util.Collection;
import java.util.Comparator;

import org.spongepowered.configurate.objectmapping.ConfigSerializable;

import org.bukkit.World;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.util.Vector;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.util.BoundingBox;
import org.bukkit.FluidCollisionMode;
import org.bukkit.attribute.Attribute;
import org.bukkit.util.RayTraceResult;

import io.papermc.paper.event.player.PrePlayerAttackEntityEvent;

import me.tonythemacaroni.friendlyfire.hook.HookInfo;
import me.tonythemacaroni.friendlyfire.hook.Hook;
import me.tonythemacaroni.friendlyfire.FriendlyFire;
import me.tonythemacaroni.friendlyfire.util.NMSUtil;
import me.tonythemacaroni.friendlyfire.util.Relation;
import me.tonythemacaroni.friendlyfire.hook.EventHook;
import me.tonythemacaroni.friendlyfire.util.EventListener;
import me.tonythemacaroni.friendlyfire.util.SkipCondition;
import me.tonythemacaroni.friendlyfire.hook.config.EventPriorityConfig;

@HookInfo(
    name = "pre-player-attack",
    description = "Controls attacks coming from a player.",
    namespace = "friendlyfire"
)
public class PrePlayerAttackEntityEventHook extends Hook<PrePlayerAttackEntityEventHook.Config> implements EventHook {

    private final SkipCondition<PrePlayerAttackEntityEvent> skipCondition = new SkipCondition<>();

    public PrePlayerAttackEntityEventHook(Config config) {
        super(config);
    }

    @Override
    public Collection<Listener> collectListeners() {
        return List.of(
            EventListener.of(PrePlayerAttackEntityEvent.class, this)
                .priority(config.eventPriority)
                .condition(skipCondition)
                .actor(PrePlayerAttackEntityEvent::getPlayer)
                .target(PrePlayerAttackEntityEvent::getAttacked, Relation.HOSTILE)
                .canceller((event, entity) -> {
                    event.setCancelled(true);

                    Player player = event.getPlayer();
                    if (!player.isRiptiding() && config.retryAttack) {
                        getValidTarget(player, entity).ifPresent(target -> skipCondition.start(
                            e -> player.equals(e.getPlayer()) && target.equals(e.getAttacked()),
                            () -> player.attack(target)
                        ));
                    }
                })
                .build()
        );
    }

    @NotNull
    private Optional<Entity> getValidTarget(@NotNull Player player, @NotNull Entity ignore) {
        double entityRange = player.getAttribute(Attribute.ENTITY_INTERACTION_RANGE).getValue();
        Location start = player.getEyeLocation();
        Vector startVector = start.toVector();
        Vector direction = start.getDirection();
        World world = player.getWorld();

        RayTraceResult result = world.rayTraceBlocks(start, direction, entityRange, FluidCollisionMode.NEVER, false);

        double range, rangeSq;
        if (result == null) {
            range = entityRange;
            rangeSq = range * range;
        } else {
            rangeSq = result.getHitPosition().distanceSquared(startVector);
            range = Math.sqrt(rangeSq);
        }

        BoundingBox box = player.getBoundingBox().expandDirectional(direction.multiply(range)).expand(1, 1, 1);

        record EntityWithDistance(Entity entity, double distanceSq) {

        }

        return player.getWorld()
            .getNearbyEntities(
                box,
                e -> !e.equals(player) && !e.equals(ignore) && NMSUtil.isPickable(e) &&
                    (!(e instanceof Player player1) || player1.getGameMode() != GameMode.SPECTATOR)
            )
            .stream()
            .map(entity -> {
                BoundingBox bb = entity.getBoundingBox().expand(NMSUtil.getPickRadius(entity));
                if (bb.contains(startVector)) return new EntityWithDistance(entity, 0);

                RayTraceResult r = bb.rayTrace(startVector, direction, range);
                if (r == null) return null;

                return new EntityWithDistance(entity, r.getHitPosition().distanceSquared(startVector));
            })
            .filter(entity -> entity != null && entity.distanceSq < rangeSq)
            .sorted(
                Comparator.comparingDouble(EntityWithDistance::distanceSq)
                    .thenComparingDouble(e -> e.entity.getLocation().distanceSquared(start))
            )
            .map(EntityWithDistance::entity)
            .filter(entity -> Relation.canApply(FriendlyFire.queryRelation(entity, player), Relation.HOSTILE))
            .findFirst();
    }

    @ConfigSerializable
    public static class Config extends EventPriorityConfig {

        public boolean retryAttack = true; // TODO: Find a better name

    }

}