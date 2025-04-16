package me.tonythemacaroni.friendlyfire.hook.base;

import java.util.List;
import java.util.Collection;


import org.bukkit.event.Listener;
import org.bukkit.event.EventPriority;
import org.bukkit.event.vehicle.VehicleEnterEvent;

import me.tonythemacaroni.friendlyfire.hook.Hook;
import me.tonythemacaroni.friendlyfire.hook.HookInfo;
import me.tonythemacaroni.friendlyfire.util.Relation;
import me.tonythemacaroni.friendlyfire.hook.EventHook;
import me.tonythemacaroni.friendlyfire.util.EventListener;
import me.tonythemacaroni.friendlyfire.hook.config.EventPriorityConfig;

@HookInfo(
    name = "vehicle-enter",
    description = "Controls entering vehicles.",
    namespace = "friendlyfire"
)
public class VehicleEnterEventHook extends Hook<EventPriorityConfig> implements EventHook {

    public VehicleEnterEventHook(EventPriorityConfig config) {
        super(config);
    }

    @Override
    public Collection<Listener> collectListeners() {
        return List.of(
            EventListener.of(VehicleEnterEvent.class, this)
                .priority(config.eventPriority)
                .actor(VehicleEnterEvent::getEntered)
                .target(VehicleEnterEvent::getVehicle, Relation.ALLIED)
                .build()
        );
    }

}
