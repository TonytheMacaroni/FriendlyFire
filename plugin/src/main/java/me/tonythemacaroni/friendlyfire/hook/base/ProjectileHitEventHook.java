package me.tonythemacaroni.friendlyfire.hook.base;

import java.util.List;
import java.util.Collection;

import org.bukkit.event.Listener;
import org.bukkit.event.entity.ProjectileHitEvent;

import me.tonythemacaroni.friendlyfire.hook.Hook;
import me.tonythemacaroni.friendlyfire.util.Relation;
import me.tonythemacaroni.friendlyfire.hook.HookInfo;
import me.tonythemacaroni.friendlyfire.hook.EventHook;
import me.tonythemacaroni.friendlyfire.util.EventListener;
import me.tonythemacaroni.friendlyfire.hook.config.EventPriorityConfig;

@HookInfo(
    name = "projectile-hit",
    description = "Controls interactions with projectiles on hit.",
    namespace = "friendlyfire"
)
public class ProjectileHitEventHook extends Hook<EventPriorityConfig> implements EventHook {

    public ProjectileHitEventHook(EventPriorityConfig config) {
        super(config);
    }

    // TODO: Check if this causes issues with fireworks, since projectile hit says cancel will still cause explosions?
    @Override
    public Collection<Listener> collectListeners() {
        return List.of(
            EventListener.of(ProjectileHitEvent.class, this)
                .priority(config.eventPriority)
                .condition(event -> event.getHitEntity() != null)
                .actor(ProjectileHitEvent::getEntity)
                .target(ProjectileHitEvent::getHitEntity, Relation.HOSTILE)
                .build()
        );
    }

}
