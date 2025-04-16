package me.tonythemacaroni.friendlyfire.hook.external.mythicmobs;

import org.jetbrains.annotations.NotNull;

import java.util.UUID;
import java.util.function.Consumer;

import org.spongepowered.configurate.objectmapping.ConfigSerializable;

import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;

import io.lumine.mythic.bukkit.MythicBukkit;
import io.lumine.mythic.core.mobs.ActiveMob;
import io.lumine.mythic.bukkit.BukkitAPIHelper;
import io.lumine.mythic.bukkit.utils.serialize.Optl;

import me.tonythemacaroni.friendlyfire.hook.Hook;
import me.tonythemacaroni.friendlyfire.hook.HookInfo;
import me.tonythemacaroni.friendlyfire.util.Priority;
import me.tonythemacaroni.friendlyfire.util.Relation;
import me.tonythemacaroni.friendlyfire.hook.RelatedHook;
import me.tonythemacaroni.friendlyfire.hook.RelationHook;
import me.tonythemacaroni.friendlyfire.util.Relationship;
import me.tonythemacaroni.friendlyfire.util.OrderedPriority;
import me.tonythemacaroni.friendlyfire.util.RelationQueryInfo;

@HookInfo(
    name = "owner",
    description = "Controls interactions between a mythic mob and its owner.",
    namespace = "mythicmobs",
    depends = "MythicMobs"
)
public class MythicMobsOwnerHook extends Hook<MythicMobsOwnerHook.Config> implements RelationHook, RelatedHook {

    public MythicMobsOwnerHook(Config config) {
        super(config);
    }

    @NotNull
    @Override
    public Relationship queryRelation(@NotNull Entity actor, @NotNull Entity target, @NotNull RelationQueryInfo info) {
        if (isOwner(actor, target) || isOwner(target, actor))
            return new Relationship(Relation.ALLIED, config.priorityLevel);

        return Relationship.UNKNOWN;
    }

    private boolean isOwner(@NotNull Entity first, @NotNull Entity second) {
        BukkitAPIHelper helper = MythicBukkit.inst().getAPIHelper();

        ActiveMob mob = helper.getMythicMobInstance(first);
        if (mob == null) return false;

        Optl<UUID> optl = mob.getOwner();
        return optl.isPresent() && second.getUniqueId().equals(optl.get());
    }

    @Override
    public void queryRelated(@NotNull Entity entity, @NotNull Consumer<Entity> related) {
        BukkitAPIHelper helper = MythicBukkit.inst().getAPIHelper();

        ActiveMob mob = helper.getMythicMobInstance(entity);
        if (mob == null) return;

        Optl<UUID> optl = mob.getOwner();
        if (!optl.isPresent()) return;

        Entity owner = Bukkit.getEntity(optl.get());
        if (owner == null) return;

        related.accept(owner);
    }

    @ConfigSerializable
    public static class Config extends Hook.Config {

        public OrderedPriority priorityLevel = new OrderedPriority(Priority.HIGH);

    }

}
