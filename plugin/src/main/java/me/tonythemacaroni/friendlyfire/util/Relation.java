package me.tonythemacaroni.friendlyfire.util;

import com.google.common.base.Preconditions;
import org.jetbrains.annotations.NotNull;

public enum Relation {

    ALLIED,
    FRIENDLY,
    HOSTILE,
    UNFRIENDLY,
    NEUTRAL;

    // TODO: Check if this works properly?
    public static boolean canApply(@NotNull Relation relationship, @NotNull Relation action) {
        Preconditions.checkArgument(relationship != null, "Relationship relation must not be null.");
        Preconditions.checkArgument(action != null, "Action relation must not be null.");

        if (action == ALLIED) return relationship == ALLIED;
        if (relationship == NEUTRAL || action == NEUTRAL) return true;
        if (relationship == ALLIED && action == FRIENDLY) return true;
        if (relationship == HOSTILE && action == UNFRIENDLY) return true;

        return relationship == action;
    }

    // TODO: This needs a better name
    public static Relation combine(@NotNull Relation relation1, @NotNull Relation relation2) {
        Preconditions.checkArgument(relation1 != null, "Relation 1 must not be null.");
        Preconditions.checkArgument(relation2 != null, "Relation 2 must not be null.");

        return relation1.ordinal() <= relation2.ordinal() ? relation1 : relation2;
    }

}
