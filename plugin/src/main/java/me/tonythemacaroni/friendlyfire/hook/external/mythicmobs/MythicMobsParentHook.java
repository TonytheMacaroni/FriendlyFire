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
    name = "parent",
    description = "Controls interactions between a mythic mob and its parent.",
    namespace = "mythicmobs",
    depends = "MythicMobs"
)
public class MythicMobsParentHook extends Hook<MythicMobsParentHook.Config> implements RelationHook, RelatedHook {

    public MythicMobsParentHook(Config config) {
        super(config);
    }

    @NotNull
    @Override
    public Relationship queryRelation(@NotNull Entity actor, @NotNull Entity target, @NotNull RelationQueryInfo info) {
        if (isParent(actor, target) || isParent(target, actor))
            return new Relationship(Relation.ALLIED, config.priorityLevel);

        return Relationship.UNKNOWN;
    }

    private boolean isParent(@NotNull Entity first, @NotNull Entity second) {
        BukkitAPIHelper helper = MythicBukkit.inst().getAPIHelper();

        ActiveMob mob = helper.getMythicMobInstance(first);
        if (mob == null) return false;

        Optl<UUID> optl = mob.getParentUUID();
        return optl.isPresent() && second.getUniqueId().equals(optl.get());
    }

    @Override
    public void queryRelated(@NotNull Entity entity, @NotNull Consumer<Entity> related) {
        BukkitAPIHelper helper = MythicBukkit.inst().getAPIHelper();

        ActiveMob mob = helper.getMythicMobInstance(entity);
        if (mob == null) return;

        Optl<UUID> optl = mob.getParentUUID();
        if (!optl.isPresent()) return;

        Entity parent = Bukkit.getEntity(optl.get());
        if (parent == null) return;

        related.accept(parent);
    }

    @ConfigSerializable
    public static class Config extends Hook.Config {

        public OrderedPriority priorityLevel = new OrderedPriority(Priority.HIGH);

    }

}
