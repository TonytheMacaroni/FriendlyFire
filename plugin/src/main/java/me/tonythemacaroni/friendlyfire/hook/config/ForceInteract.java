package me.tonythemacaroni.friendlyfire.hook.config;

import me.tonythemacaroni.friendlyfire.util.OrderedPriority;
import me.tonythemacaroni.friendlyfire.util.Priority;
import me.tonythemacaroni.friendlyfire.util.Relation;
import me.tonythemacaroni.friendlyfire.util.Relationship;
import org.spongepowered.configurate.objectmapping.ConfigSerializable;

@ConfigSerializable
public class ForceInteract {

    private boolean enabled;
    private OrderedPriority priorityLevel;

    private transient Relationship relationship;

    public ForceInteract(boolean enabled, OrderedPriority priorityLevel) {
        this.enabled = enabled;
        this.priorityLevel = priorityLevel;
    }

    public ForceInteract() {
        this(true, new OrderedPriority(Priority.HIGHEST));
    }

    public boolean enabled() {
        return enabled;
    }

    public Relationship relationship() {
        if (relationship == null)
            relationship = new Relationship(Relation.NEUTRAL, priorityLevel);

        return relationship;
    }

}
