package me.tonythemacaroni.friendlyfire.hook;

import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

import org.bukkit.entity.Entity;

public non-sealed interface RelatedHook extends HookCapability {

    void queryRelated(@NotNull Entity entity, @NotNull Consumer<Entity> related);

}
