package me.tonythemacaroni.friendlyfire.hook.config;

import me.tonythemacaroni.friendlyfire.util.OrderedPriority;
import me.tonythemacaroni.friendlyfire.util.Relation;
import org.jetbrains.annotations.NotNull;

import org.spongepowered.configurate.objectmapping.ConfigSerializable;

@ConfigSerializable
public record RelationPriorityLevels(
    @NotNull OrderedPriority allied,
    @NotNull OrderedPriority friendly,
    @NotNull  OrderedPriority neutral,
    @NotNull OrderedPriority unfriendly,
    @NotNull OrderedPriority hostile
) {

    public OrderedPriority of(@NotNull Relation relation) {
        return switch (relation) {
            case ALLIED -> allied;
            case FRIENDLY -> friendly;
            case NEUTRAL -> neutral;
            case UNFRIENDLY -> unfriendly;
            case HOSTILE -> hostile;
        };
    }

}
