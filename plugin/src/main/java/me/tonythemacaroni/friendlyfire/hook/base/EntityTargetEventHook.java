package me.tonythemacaroni.friendlyfire.hook.base;

import java.util.List;
import java.util.Collection;

import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityTargetEvent;

import me.tonythemacaroni.friendlyfire.hook.Hook;
import me.tonythemacaroni.friendlyfire.util.Relation;
import me.tonythemacaroni.friendlyfire.hook.HookInfo;
import me.tonythemacaroni.friendlyfire.hook.EventHook;
import me.tonythemacaroni.friendlyfire.util.EventListener;
import me.tonythemacaroni.friendlyfire.hook.config.EventPriorityConfig;

@HookInfo(
    name = "entity-target",
    description = "Controls entity targeting.",
    namespace = "friendlyfire"
)
public class EntityTargetEventHook extends Hook<EventPriorityConfig> implements EventHook {

    public EntityTargetEventHook(EventPriorityConfig config) {
        super(config);
    }

    @Override
    public Collection<Listener> collectListeners() {
        return List.of(
            EventListener.of(EntityTargetEvent.class, this)
                .priority(config.eventPriority)
                .condition(event -> event.getTarget() != null)
                .actor(EntityTargetEvent::getEntity)
                .target(EntityTargetEvent::getTarget, event -> switch (event.getReason()) {
                    case TEMPT -> Relation.ALLIED;
                    case FOLLOW_LEADER -> Relation.FRIENDLY;
                    default -> Relation.HOSTILE;
                })
                .build()
        );
    }

}
