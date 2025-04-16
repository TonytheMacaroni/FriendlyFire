package me.tonythemacaroni.friendlyfire.util;

import org.jetbrains.annotations.NotNull;

import com.google.common.base.Preconditions;

public record OrderedPriority(@NotNull Priority priority, int ordinal) implements Comparable<OrderedPriority> {

    public OrderedPriority {
        Preconditions.checkArgument(priority != null, "Priority cannot be null.");
    }

    public OrderedPriority(@NotNull Priority priority) {
        this(priority, 0);
    }

    @Override
    public int compareTo(@NotNull OrderedPriority o) {
        int compare = priority.compareTo(o.priority);
        return compare != 0 ? compare : Integer.compare(ordinal, o.ordinal);
    }

}
