package me.tonythemacaroni.friendlyfire.util;

import me.tonythemacaroni.friendlyfire.hook.Hook;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.google.common.base.Preconditions;

import org.bukkit.event.Event;
import org.bukkit.entity.Entity;

public record RelationQueryInfo(@NotNull Entity primaryActor, @NotNull Entity primaryTarget, @Nullable Hook<?> hook, @Nullable Event event) {

    public RelationQueryInfo {
        Preconditions.checkNotNull(primaryActor);
        Preconditions.checkNotNull(primaryTarget);
    }

    public boolean isPrimaryActor(@NotNull Entity actor) {
        return primaryActor.equals(actor);
    }

    public boolean isPrimaryTarget(@NotNull Entity target) {
        return primaryTarget.equals(target);
    }

    public boolean isDirect(@NotNull Entity actor, @NotNull Entity target) {
        return isPrimaryActor(actor) && isPrimaryTarget(target);
    }

}
