package me.tonythemacaroni.friendlyfire.util;

import org.jetbrains.annotations.NotNull;

import com.google.common.base.Preconditions;

public record Relationship(@NotNull Relation relation, @NotNull OrderedPriority priority) implements Comparable<Relationship> {

    public static final Relationship UNKNOWN = new Relationship(Relation.NEUTRAL, new OrderedPriority(Priority.LOWEST, Integer.MIN_VALUE));

    public Relationship {
        Preconditions.checkArgument(relation != null, "Relation cannot be null.");
        Preconditions.checkArgument(priority != null, "Priority cannot be null.");
    }

    @Override
    public int compareTo(@NotNull Relationship o) {
        int compare = priority.compareTo(o.priority);
        return compare != 0 ? compare : o.relation.compareTo(relation);
    }

}
