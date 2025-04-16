package me.tonythemacaroni.friendlyfire.hook.base;

import com.google.common.collect.Iterables;
import org.bukkit.Tag;
import org.bukkit.entity.AreaEffectCloud;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.potion.PotionEffectType.Category;
import org.bukkit.potion.PotionType;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Collection;
import java.util.Map;

import org.spongepowered.configurate.objectmapping.ConfigSerializable;

import org.bukkit.entity.Entity;
import org.bukkit.event.Listener;
import org.bukkit.event.EventPriority;
import org.bukkit.entity.LivingEntity;
import org.bukkit.potion.PotionEffect;
import org.bukkit.event.entity.PotionSplashEvent;
import org.bukkit.event.entity.AreaEffectCloudApplyEvent;
import io.papermc.paper.event.entity.WaterBottleSplashEvent;

import me.tonythemacaroni.friendlyfire.hook.Hook;
import me.tonythemacaroni.friendlyfire.util.Relation;
import me.tonythemacaroni.friendlyfire.hook.HookInfo;
import me.tonythemacaroni.friendlyfire.hook.EventHook;
import me.tonythemacaroni.friendlyfire.util.EventListener;

@HookInfo(
    name = "potion-effect-apply",
    description = "Controls interactions with potion effect application, such as with area effect clouds and splash potions.",
    namespace = "friendlyfire"
)
public class PotionEffectApplyHook extends Hook<PotionEffectApplyHook.Config> implements EventHook {

    public PotionEffectApplyHook(Config config) {
        super(config);
    }

    @Override
    public Collection<Listener> collectListeners() {
        return List.of(
            EventListener.<AreaEffectCloudApplyEvent, Entity, LivingEntity, PotionEffectApplyHook>
                    of(AreaEffectCloudApplyEvent.class, this)
                .priority(config.eventPriority.areaEffectCloud)
                .actor(AreaEffectCloudApplyEvent::getEntity)
                .targets(event -> {
                    Collection<PotionEffect> effects = getEffects(event.getEntity());
                    List<LivingEntity> entities = event.getAffectedEntities();

                    Map<LivingEntity, Relation> relations = new HashMap<>();
                    for (LivingEntity target : entities)
                        relations.put(target, config.strategy.getRelation(target, effects));

                    return relations;
                })
                .canceller((event, entity) -> event.getAffectedEntities().remove(entity))
                .build(),
            EventListener.<PotionSplashEvent, Entity, LivingEntity, PotionEffectApplyHook>
                    of(PotionSplashEvent.class, this)
                .priority(config.eventPriority.splashPotion)
                .condition(event -> !(event instanceof WaterBottleSplashEvent))
                .actor(PotionSplashEvent::getEntity)
                .targets(event -> {
                    Collection<PotionEffect> effects = event.getEntity().getEffects();
                    Collection<LivingEntity> entities = event.getAffectedEntities();

                    Map<LivingEntity, Relation> relations = new HashMap<>();
                    for (LivingEntity target : entities)
                        relations.put(target, config.strategy.getRelation(target, effects));

                    return relations;
                })
                .canceller((event, entity) -> event.setIntensity(entity, 0))
                .build()
        );
    }

    private Collection<PotionEffect> getEffects(AreaEffectCloud cloud) {
        PotionType type = cloud.getBasePotionType();
        boolean hasCustom = cloud.hasCustomEffects();
        if (type == null && !hasCustom) return Collections.emptyList();

        if (!hasCustom) return type.getPotionEffects();
        if (type == null) return cloud.getCustomEffects();

        List<PotionEffect> baseEffects = type.getPotionEffects();
        List<PotionEffect> customEffects = cloud.getCustomEffects();
        List<PotionEffect> effects = new ArrayList<>(baseEffects.size() + customEffects.size());

        effects.addAll(baseEffects);
        effects.addAll(customEffects);

        return effects;
    }

    private static Category getCategory(LivingEntity target, PotionEffect effect) {
        PotionEffectType type = effect.getType();
        if (type == PotionEffectType.INSTANT_DAMAGE || type == PotionEffectType.INSTANT_HEALTH) {
            boolean inverted = Tag.ENTITY_TYPES_INVERTED_HEALING_AND_HARM.isTagged(target.getType());
            if (inverted) return type == PotionEffectType.INSTANT_DAMAGE ? Category.BENEFICIAL : Category.HARMFUL;
        }

        return type.getEffectCategory();
    }

    public enum PotionEffectRelationStrategy {

        PRIORITIZE_BENEFICIAL {
            @NotNull
            @Override
            public Relation getRelation(@NotNull LivingEntity target, @NotNull Collection<PotionEffect> effects) {
                if (effects.isEmpty()) return Relation.NEUTRAL;

                boolean harmful = false;
                for (PotionEffect effect : effects) {
                    switch (getCategory(target, effect)) {
                        case BENEFICIAL -> {
                            return Relation.FRIENDLY;
                        }
                        case HARMFUL -> harmful = true;
                    }
                }

                return harmful ? Relation.HOSTILE : Relation.NEUTRAL;
            }

        },
        COUNT {
            @NotNull
            @Override
            public Relation getRelation(@NotNull LivingEntity target, @NotNull Collection<PotionEffect> effects) {
                if (effects.isEmpty()) return Relation.NEUTRAL;

                boolean hasBeneficial = false;
                int beneficial = 0;

                for (PotionEffect effect : effects) {
                    switch (getCategory(target, effect)) {
                        case BENEFICIAL -> {
                            beneficial++;
                            hasBeneficial = true;
                        }
                        case HARMFUL -> beneficial--;
                    }
                }

                if (beneficial == 0) return hasBeneficial ? Relation.FRIENDLY : Relation.NEUTRAL;

                return beneficial < 0 ? Relation.HOSTILE : Relation.FRIENDLY;
            }

        };

        @NotNull
        public abstract Relation getRelation(@NotNull LivingEntity target, @NotNull Collection<PotionEffect> effects);

    }

    @ConfigSerializable
    public static class Config extends Hook.Config {

        public EventPriorities eventPriority = new EventPriorities(EventPriority.HIGHEST, EventPriority.HIGHEST);
        public PotionEffectRelationStrategy strategy = PotionEffectRelationStrategy.PRIORITIZE_BENEFICIAL;

    }

    @ConfigSerializable
    public record EventPriorities(EventPriority areaEffectCloud, EventPriority splashPotion) {

    }

}
