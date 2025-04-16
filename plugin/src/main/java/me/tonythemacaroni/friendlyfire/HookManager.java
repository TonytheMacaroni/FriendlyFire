package me.tonythemacaroni.friendlyfire;

import org.jetbrains.annotations.NotNull;

import java.util.Set;
import java.util.List;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.lang.reflect.Type;
import java.util.LinkedHashSet;
import java.util.function.Consumer;

import io.leangen.geantyref.GenericTypeReflector;

import org.jetbrains.annotations.Nullable;
import org.spongepowered.configurate.yaml.NodeStyle;
import org.spongepowered.configurate.loader.HeaderMode;
import org.spongepowered.configurate.util.MapFactories;
import org.spongepowered.configurate.CommentedConfigurationNode;
import org.spongepowered.configurate.yaml.YamlConfigurationLoader;
import org.spongepowered.configurate.serialize.TypeSerializerCollection;

import net.kyori.adventure.key.Key;

import org.bukkit.Bukkit;
import org.bukkit.event.Listener;
import org.bukkit.event.HandlerList;

import me.tonythemacaroni.friendlyfire.hook.Hook;
import me.tonythemacaroni.friendlyfire.hook.base.*;
import me.tonythemacaroni.friendlyfire.hook.HookInfo;
import me.tonythemacaroni.friendlyfire.hook.EventHook;
import me.tonythemacaroni.friendlyfire.hook.RelatedHook;
import me.tonythemacaroni.friendlyfire.hook.RelationHook;
import me.tonythemacaroni.friendlyfire.util.EventListener;
import me.tonythemacaroni.friendlyfire.hook.HookCapability;
import me.tonythemacaroni.friendlyfire.event.HookLoadingEvent;
import me.tonythemacaroni.friendlyfire.hook.external.mythicmobs.*;
import me.tonythemacaroni.friendlyfire.hook.external.worldguard.*;
import me.tonythemacaroni.friendlyfire.hook.external.parties.PartiesHook;
import me.tonythemacaroni.friendlyfire.hook.external.mcmmo.McMMOPartyHook;
import me.tonythemacaroni.friendlyfire.hook.external.towny.GenericTownyHook;
import me.tonythemacaroni.friendlyfire.hook.external.battlearena.BattleArenaHook;
import me.tonythemacaroni.friendlyfire.util.configuration.OrderedPrioritySerializer;
import me.tonythemacaroni.friendlyfire.hook.external.magicspells.SpellTargetEventHook;

public final class HookManager {

    private static final Consumer<TypeSerializerCollection.Builder> SERIALIZERS = builder -> builder
        .register(new OrderedPrioritySerializer());

    private final Set<RegisteredHook> registeredHooks;

    private final List<RelationHook> relationHooks;
    private final List<RelatedHook> relatedHooks;

    private boolean registering;

    HookManager() {
        registeredHooks = new LinkedHashSet<>();

        relationHooks = new ArrayList<>();
        relatedHooks = new ArrayList<>();
    }

    void registerHooks() {
        try {
            registering = true;

            // Base
            registerHook(AreaEffectCloudOwnerHook.class);
            registerHook(EntityDamageByEntityEventHook.class);
            registerHook(EntityMountEventHook.class);
            registerHook(EntityTargetEventHook.class);
            registerHook(EvokerFangsOwnerHook.class);
            registerHook(PotionEffectApplyHook.class);
            registerHook(PrePlayerAttackEntityEventHook.class);
            registerHook(ProjectileHitEventHook.class);
            registerHook(ProjectileOwnerHook.class);
            registerHook(ScoreboardTeamsHook.class);
            registerHook(SelfHook.class);
            registerHook(TameableHook.class);
            registerHook(VehicleEnterEventHook.class);
            registerHook(VehicleHook.class);
            registerHook(WaterBottleSplashEventHook.class);
            registerHook(WorldPvPHook.class);

            // BattleArena
            registerHook(BattleArenaHook.class);

            // MagicSpells
            registerHook(SpellTargetEventHook.class);

            // mcMMO
            registerHook(McMMOPartyHook.class);

            // MythicMobs
            registerHook(MythicDamageEventHook.class);
            registerHook(MythicHealMechanicEventHook.class);
            registerHook(MythicMobsOwnerHook.class);
            registerHook(MythicMobsParentHook.class);

            // Parties
            registerHook(PartiesHook.class);

            // Towny
            registerHook(GenericTownyHook.class);

            // WorldGuard
            registerHook(WorldGuardDamageHook.class);
            registerHook(WorldGuardPvPHook.class);

            new HookLoadingEvent(this).callEvent();
        } finally {
            registering = false;
        }
    }

    void reloadHooks() {
        relationHooks.clear();
        relatedHooks.clear();

        for (RegisteredHook hook : registeredHooks)
            hook.listeners.forEach(HandlerList::unregisterAll);

        registeredHooks.clear();

        registerHooks();
    }

    public <T extends Hook<C> & HookCapability, C extends Hook.Config> void registerHook(@NotNull Class<T> type) {
        registerHook(type, null);
    }

    public <T extends Hook<C> & HookCapability, C extends Hook.Config> void registerHook(@NotNull Class<T> type, @Nullable Consumer<TypeSerializerCollection.Builder> serializers) {
        if (!registering)
            throw new IllegalStateException("Attempted to register hook '" + type.getCanonicalName() + "' outside of HookLoadingEvent");

        HookInfo info = type.getAnnotation(HookInfo.class);
        if (info == null)
            throw new IllegalArgumentException("Cannot register hook '" + type.getCanonicalName() + "' - registered hooks must be annotated with @HookInfo");

        if (!Key.parseableNamespace(info.namespace()))
            throw new IllegalArgumentException("Invalid hook namespace '" + info.namespace() + "' for hook with name'" + info.name() + "'");

        if (!Key.parseableValue(info.name()))
            throw new IllegalArgumentException("Invalid hook name '" + info.name() + "' for hook with namespace '" + info.namespace() + "'");

        for (String dependency : info.depends())
            if (Bukkit.getPluginManager().getPlugin(dependency) == null)
                return;

        Type configType = GenericTypeReflector.getTypeParameter(type, Hook.class.getTypeParameters()[0]);

        try {
            YamlConfigurationLoader loader = YamlConfigurationLoader.builder()
                .path(FriendlyFire.getInstance().getDataPath().resolve(Path.of(info.namespace(), info.name() + ".yml")))
                .indent(4)
                .nodeStyle(NodeStyle.BLOCK)
                .headerMode(HeaderMode.PRESERVE)
                .defaultOptions(options -> options
                    .mapFactory(MapFactories.sortedNatural())
                    .header(info.description())
                    .serializers(serializers == null ? SERIALIZERS : SERIALIZERS.andThen(serializers))
                )
                .build();

            CommentedConfigurationNode node = loader.load();
            Object config = node.require(configType);

            loader.save(node);

            Hook<?> hook = type.getConstructor((Class<?>) configType).newInstance(config);
            if (!hook.config().enabled) return;

            Collection<Listener> listeners = Collections.emptyList();
            if (hook instanceof EventHook eventHook) {
                listeners = eventHook.collectListeners();

                for (Listener listener : listeners) {
                    if (listener instanceof EventListener<?, ?, ?, ?> eventListener) {
                        eventListener.register();
                        continue;
                    }

                    Bukkit.getPluginManager().registerEvents(listener, FriendlyFire.getInstance());
                }
            }

            if (hook instanceof RelatedHook relatedHook) relatedHooks.add(relatedHook);
            if (hook instanceof RelationHook relationHook) relationHooks.add(relationHook);

            registeredHooks.add(new RegisteredHook(hook, info, listeners));
        } catch (Exception e) {
            throw new RuntimeException("Failed to register hook '" + info.namespace() + ":" + info.name() + "'", e);
        }
    }

    public List<? extends RelationHook> getRelationHooks() {
        return relationHooks;
    }

    public List<? extends RelatedHook> getRelatedHooks() {
        return relatedHooks;
    }

    public int getHookCount() {
        return registeredHooks.size();
    }

    public record RegisteredHook(Hook<?> hook, HookInfo info, Collection<Listener> listeners) {

    }

}
