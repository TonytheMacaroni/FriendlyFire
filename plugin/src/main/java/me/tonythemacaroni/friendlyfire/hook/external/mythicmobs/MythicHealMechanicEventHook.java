package me.tonythemacaroni.friendlyfire.hook.external.mythicmobs;

import java.util.List;
import java.util.Collection;

import org.bukkit.event.Listener;

import io.lumine.mythic.bukkit.events.MythicHealMechanicEvent;

import me.tonythemacaroni.friendlyfire.hook.Hook;
import me.tonythemacaroni.friendlyfire.hook.HookInfo;
import me.tonythemacaroni.friendlyfire.util.Relation;
import me.tonythemacaroni.friendlyfire.hook.EventHook;
import me.tonythemacaroni.friendlyfire.util.EventListener;
import me.tonythemacaroni.friendlyfire.hook.config.EventPriorityConfig;

@HookInfo(
    name = "heal-mechanic",
    description = "Controls heals from healing mechanics.",
    namespace = "mythicmobs",
    depends = "MythicMobs"
)
public class MythicHealMechanicEventHook extends Hook<EventPriorityConfig> implements EventHook {

    public MythicHealMechanicEventHook(EventPriorityConfig config) {
        super(config);
    }

    @Override
    public Collection<Listener> collectListeners() {
        return List.of(
            EventListener.of(MythicHealMechanicEvent.class, this)
                .priority(config.eventPriority)
                .actor(MythicHealMechanicEvent::getEntity)
                .target(MythicHealMechanicEvent::getTarget, Relation.FRIENDLY)
                .build()
        );
    }

}
