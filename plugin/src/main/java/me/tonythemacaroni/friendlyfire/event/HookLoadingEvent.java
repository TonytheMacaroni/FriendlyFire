package me.tonythemacaroni.friendlyfire.event;

import org.jetbrains.annotations.NotNull;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import me.tonythemacaroni.friendlyfire.HookManager;

public class HookLoadingEvent extends Event {

    private static final HandlerList HANDLERS = new HandlerList();

    private final HookManager hookManager;

    public HookLoadingEvent(@NotNull HookManager hookManager) {
        this.hookManager = hookManager;
    }

    public @NotNull HookManager getHookManager() {
        return hookManager;
    }

    public static @NotNull HandlerList getHandlerList() {
        return HANDLERS;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return HANDLERS;
    }

}
