package me.tonythemacaroni.friendlyfire.hook.external.mythicmobs;

import java.util.List;
import java.util.Collection;

import org.bukkit.event.Listener;

import io.lumine.mythic.bukkit.events.MythicDamageEvent;

import me.tonythemacaroni.friendlyfire.hook.Hook;
import me.tonythemacaroni.friendlyfire.hook.HookInfo;
import me.tonythemacaroni.friendlyfire.util.Relation;
import me.tonythemacaroni.friendlyfire.hook.EventHook;
import me.tonythemacaroni.friendlyfire.util.EventListener;
import me.tonythemacaroni.friendlyfire.hook.config.EventPriorityConfig;

@HookInfo(
    name = "damage",
    description = "Controls damage.",
    namespace = "mythicmobs",
    depends = "MythicMobs"
)
public class MythicDamageEventHook extends Hook<EventPriorityConfig> implements EventHook {

    public MythicDamageEventHook(EventPriorityConfig config) {
        super(config);
    }

    @Override
    public Collection<Listener> collectListeners() {
        return List.of(
            EventListener.of(MythicDamageEvent.class, this)
                .priority(config.eventPriority)
                .actor(event -> event.getCaster().getEntity().getBukkitEntity())
                .target(event -> event.getTarget().getBukkitEntity(), Relation.HOSTILE)
                .build()
        );
    }

}
