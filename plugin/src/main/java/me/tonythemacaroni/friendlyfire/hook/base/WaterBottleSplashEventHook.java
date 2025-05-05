package me.tonythemacaroni.friendlyfire.hook.base;

import java.util.Map;
import java.util.List;
import java.util.HashMap;
import java.util.Collection;

import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.ThrownPotion;

import org.bukkit.event.Listener;

import io.papermc.paper.event.entity.WaterBottleSplashEvent;

import me.tonythemacaroni.friendlyfire.hook.Hook;
import me.tonythemacaroni.friendlyfire.hook.HookInfo;
import me.tonythemacaroni.friendlyfire.util.Relation;
import me.tonythemacaroni.friendlyfire.hook.EventHook;
import me.tonythemacaroni.friendlyfire.util.EventListener;
import me.tonythemacaroni.friendlyfire.hook.config.EventPriorityConfig;

@HookInfo(
    name = "water-bottle-splash",
    description = "Controls interactions with water bottle splash potions.",
    namespace = "friendlyfire"
)
public class WaterBottleSplashEventHook extends Hook<EventPriorityConfig> implements EventHook {

    public WaterBottleSplashEventHook(EventPriorityConfig config) {
        super(config);
    }

    @Override
    public Collection<Listener> collectListeners() {
        return List.of(
            EventListener.<WaterBottleSplashEvent, ThrownPotion, LivingEntity, WaterBottleSplashEventHook>
                    of(WaterBottleSplashEvent.class, this)
                .priority(config.eventPriority)
                .actor(WaterBottleSplashEvent::getEntity)
                .targets(event -> {
                    Map<LivingEntity, Relation> targets = new HashMap<>();

                    for (LivingEntity entity : event.getToExtinguish()) targets.put(entity, Relation.FRIENDLY);
                    for (LivingEntity entity : event.getToRehydrate()) targets.put(entity, Relation.FRIENDLY);

                    return targets;
                })
                .canceller((event, entity) -> {
                    event.getToExtinguish().remove(entity);
                    event.getToRehydrate().remove(entity);
                })
                .build(),
            // TODO: Add options to separate out
            EventListener.<WaterBottleSplashEvent, ThrownPotion, LivingEntity, WaterBottleSplashEventHook>
                    of(WaterBottleSplashEvent.class, this)
                .priority(config.eventPriority)
                .actor(WaterBottleSplashEvent::getEntity)
                .targets(WaterBottleSplashEvent::getToDamage, Relation.HOSTILE)
                .canceller(WaterBottleSplashEvent::doNotDamageAsWaterSensitive)
                .build()
        );
    }

}
