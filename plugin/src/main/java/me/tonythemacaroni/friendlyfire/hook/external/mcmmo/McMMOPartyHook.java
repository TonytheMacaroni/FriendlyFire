package me.tonythemacaroni.friendlyfire.hook.external.mcmmo;

import org.jetbrains.annotations.NotNull;

import org.spongepowered.configurate.objectmapping.ConfigSerializable;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import com.gmail.nossr50.mcMMO;
import com.gmail.nossr50.api.PartyAPI;
import com.gmail.nossr50.util.Permissions;
import com.gmail.nossr50.party.PartyManager;

import me.tonythemacaroni.friendlyfire.hook.Hook;
import me.tonythemacaroni.friendlyfire.hook.HookInfo;
import me.tonythemacaroni.friendlyfire.util.Relation;
import me.tonythemacaroni.friendlyfire.util.Priority;
import me.tonythemacaroni.friendlyfire.hook.RelationHook;
import me.tonythemacaroni.friendlyfire.util.Relationship;
import me.tonythemacaroni.friendlyfire.util.OrderedPriority;
import me.tonythemacaroni.friendlyfire.util.RelationQueryInfo;

@HookInfo(
    name = "party",
    description = "Controls interactions between mcMMO party members.",
    namespace = "mcmmo",
    depends = "mcMMO"
)
public class McMMOPartyHook extends Hook<McMMOPartyHook.Config> implements RelationHook {

    public McMMOPartyHook(Config config) {
        super(config);
    }

    @Override
    public @NotNull Relationship queryRelation(@NotNull Entity actor, @NotNull Entity target, @NotNull RelationQueryInfo info) {
        if (!(actor instanceof Player playerActor) || !(target instanceof Player playerTarget) || !PartyAPI.isPartySystemEnabled())
            return Relationship.UNKNOWN;

        PartyManager manager = mcMMO.p.getPartyManager();
        if (!manager.inSameParty(playerActor, playerTarget) && !manager.areAllies(playerActor, playerTarget))
            return Relationship.UNKNOWN;

        if (Permissions.friendlyFire(playerActor) && Permissions.friendlyFire(playerTarget))
            return Relationship.UNKNOWN;

        return new Relationship(Relation.ALLIED, config.priorityLevel);
    }

    @ConfigSerializable
    public static class Config extends Hook.Config {

        public OrderedPriority priorityLevel = new OrderedPriority(Priority.HIGH);

    }

}
