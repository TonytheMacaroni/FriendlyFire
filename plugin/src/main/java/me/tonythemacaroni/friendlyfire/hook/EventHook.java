package me.tonythemacaroni.friendlyfire.hook;

import java.util.Collection;

import org.bukkit.event.Listener;

public non-sealed interface EventHook extends HookCapability {

    Collection<Listener> collectListeners();

}
