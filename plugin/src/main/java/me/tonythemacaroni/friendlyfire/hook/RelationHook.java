package me.tonythemacaroni.friendlyfire.hook;

import me.tonythemacaroni.friendlyfire.util.RelationQueryInfo;
import org.jetbrains.annotations.NotNull;

import org.bukkit.entity.Entity;

import me.tonythemacaroni.friendlyfire.util.Relationship;

public non-sealed interface RelationHook extends HookCapability {

    @NotNull
    Relationship queryRelation(@NotNull Entity actor, @NotNull Entity target, @NotNull RelationQueryInfo info);

}
