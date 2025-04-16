package me.tonythemacaroni.friendlyfire;

import org.jetbrains.annotations.NotNull;

import java.util.*;

import com.google.common.collect.Sets;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

import io.papermc.paper.command.brigadier.CommandSourceStack;

import org.incendo.cloud.paper.PaperCommandManager;
import org.incendo.cloud.execution.ExecutionCoordinator;

import me.tonythemacaroni.friendlyfire.util.Relation;
import me.tonythemacaroni.friendlyfire.hook.RelatedHook;
import me.tonythemacaroni.friendlyfire.hook.RelationHook;
import me.tonythemacaroni.friendlyfire.util.RelationQueryInfo;
import me.tonythemacaroni.friendlyfire.util.Relationship;

public final class FriendlyFire extends JavaPlugin {

    private static FriendlyFire instance;

    private PaperCommandManager<CommandSourceStack> commandManager;
    private HookManager hookManager;

    @Override
    public void onEnable() {
        instance = this;

        commandManager = PaperCommandManager.builder()
            .executionCoordinator(ExecutionCoordinator.simpleCoordinator())
            .buildOnEnable(this);

        commandManager.command(commandManager.commandBuilder("friendlyfire")
            .literal("reload")
            .handler(context -> {
                CommandSourceStack stack = context.sender();
                CommandSender sender = Objects.requireNonNullElse(stack.getExecutor(), stack.getSender());

                sender.sendMessage(Component.text("Reloading FriendlyFire hooks...", NamedTextColor.GREEN));

                hookManager.reloadHooks();

                sender.sendMessage(Component.text("Hooks reloaded. (" + hookManager.getHookCount() + " hooks loaded)", NamedTextColor.GREEN));
            })
        );

        hookManager = new HookManager();
        hookManager.registerHooks();
    }

    @Override
    public void onDisable() {
        instance = null;

        commandManager = null;
        hookManager = null;

        HandlerList.unregisterAll(this);
    }

    public static Relation queryRelation(@NotNull Entity actor, @NotNull Entity target) {
        return queryRelation(queryRelated(actor), queryRelated(target), new RelationQueryInfo(actor, target, null, null));
    }

    public static Relation queryRelation(@NotNull Set<Entity> actors, @NotNull Set<Entity> targets, @NotNull RelationQueryInfo info) {
        List<? extends RelationHook> relationHooks = instance.hookManager.getRelationHooks();

        return Sets.cartesianProduct(actors, targets).stream()
            .flatMap(pair -> relationHooks.stream().map(hook -> hook.queryRelation(pair.getFirst(), pair.getLast(), info)))
            .max(Comparator.naturalOrder())
            .map(Relationship::relation)
            .orElse(Relation.NEUTRAL);
    }

    public static Set<Entity> queryRelated(@NotNull Entity entity) {
        if (entity instanceof Player) return Collections.singleton(entity);

        List<? extends RelatedHook> relatedHooks = instance.hookManager.getRelatedHooks();
        Set<Entity> related = new LinkedHashSet<>();

        ArrayDeque<Entity> fresh = new ArrayDeque<>();
        fresh.add(entity);

        while (!fresh.isEmpty()) {
            Entity freshEntity = fresh.removeFirst();
            if (!related.add(freshEntity)) continue;

            for (RelatedHook hook : relatedHooks)
                hook.queryRelated(freshEntity, fresh::add);
        }

        return related;
    }

    public static FriendlyFire getInstance() {
        return instance;
    }

}
