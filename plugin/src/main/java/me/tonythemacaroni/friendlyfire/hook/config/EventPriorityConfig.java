package me.tonythemacaroni.friendlyfire.hook.config;

import org.spongepowered.configurate.objectmapping.ConfigSerializable;

import org.bukkit.event.EventPriority;

import me.tonythemacaroni.friendlyfire.hook.Hook;

@ConfigSerializable
public class EventPriorityConfig extends Hook.Config {

    public EventPriority eventPriority = EventPriority.HIGHEST;

}
