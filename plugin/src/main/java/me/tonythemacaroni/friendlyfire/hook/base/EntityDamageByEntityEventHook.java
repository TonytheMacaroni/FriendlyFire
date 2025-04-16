package me.tonythemacaroni.friendlyfire.hook.base;

import java.util.List;
import java.util.Collection;

import org.bukkit.entity.Entity;
import org.bukkit.event.Listener;
import org.bukkit.damage.DamageSource;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

import me.tonythemacaroni.friendlyfire.hook.Hook;
import me.tonythemacaroni.friendlyfire.util.Relation;
import me.tonythemacaroni.friendlyfire.hook.HookInfo;
import me.tonythemacaroni.friendlyfire.hook.EventHook;
import me.tonythemacaroni.friendlyfire.util.EventListener;
import me.tonythemacaroni.friendlyfire.hook.config.EventPriorityConfig;

@HookInfo(
    name = "entity-damage",
    description = "Controls damage between two entities.",
    namespace = "friendlyfire"
)
public class EntityDamageByEntityEventHook extends Hook<EventPriorityConfig> implements EventHook {

    public EntityDamageByEntityEventHook(EventPriorityConfig config) {
        super(config);
    }

    @Override
    public Collection<Listener> collectListeners() {
        return List.of(
            EventListener.of(EntityDamageByEntityEvent.class, this)
                .priority(config.eventPriority)
                .actor(this::getDamager)
                .target(EntityDamageByEntityEvent::getEntity, Relation.HOSTILE)
                .build()
        );
    }

    @SuppressWarnings("UnstableApiUsage")
    private Entity getDamager(EntityDamageByEntityEvent event) {
        DamageSource source = event.getDamageSource();

        Entity entity = source.getDirectEntity();
        if (entity != null) return entity;

        return event.getDamager();
    }

}
