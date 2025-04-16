package me.tonythemacaroni.friendlyfire.hook;

import org.spongepowered.configurate.objectmapping.ConfigSerializable;

public abstract class Hook<T extends Hook.Config> {

    protected final T config;

    public Hook(T config) {
        this.config = config;
    }

    public void onDisable() {

    }

    public T config() {
        return config;
    }

    @ConfigSerializable
    public static class Config {

        // TODO: Flip to false
        public boolean enabled = true;

    }

}
