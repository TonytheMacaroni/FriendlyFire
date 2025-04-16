package me.tonythemacaroni.friendlyfire.util;

import com.google.common.base.Preconditions;
import org.jetbrains.annotations.NotNull;

public enum Relation {

    ALLIED,
    HOSTILE,
    FRIENDLY,
    UNFRIENDLY,
    NEUTRAL;

    // TODO: Check if this works properly?
    public boolean canApply(@NotNull Relation action) {
        Preconditions.checkArgument(action != null, "Action relation must not be null.");

        if (this == ALLIED && action == FRIENDLY) return true;
        if (this == HOSTILE && action == UNFRIENDLY) return true;

        return this == action || this == NEUTRAL || action == NEUTRAL;
    }

    // TODO: Check if this works properly?
    @NotNull
    public Relation combine(@NotNull Relation other) {
        Preconditions.checkArgument(other != null, "Other relation must not be null.");

        return this.ordinal() <= other.ordinal() ? this : other;
    }

}
