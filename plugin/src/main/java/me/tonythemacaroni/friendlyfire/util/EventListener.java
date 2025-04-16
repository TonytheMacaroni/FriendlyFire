package me.tonythemacaroni.friendlyfire.util;

import me.tonythemacaroni.friendlyfire.hook.EventHook;
import me.tonythemacaroni.friendlyfire.hook.Hook;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.google.common.base.Preconditions;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.BiConsumer;

import org.bukkit.Bukkit;
import org.bukkit.event.Event;
import org.bukkit.entity.Entity;
import org.bukkit.event.Listener;
import org.bukkit.event.Cancellable;
import org.bukkit.event.EventPriority;

import me.tonythemacaroni.friendlyfire.FriendlyFire;

public record EventListener<E extends Event, A extends Entity, T extends Entity, H extends Hook<?> & EventHook>(
    @NotNull H hook,
    @NotNull Class<E> eventClass,
    @NotNull EventPriority eventPriority,
    boolean ignoreCancelled,
    @Nullable Predicate<E> condition,
    @NotNull Function<E, ? extends A> actor,
    @NotNull Function<E, Map<? extends T, Relation>> targets,
    @NotNull BiConsumer<E, T> canceller
) implements Listener {

    public EventListener {
        Preconditions.checkArgument(hook != null, "Event hook cannot be null");
        Preconditions.checkArgument(eventClass != null, "Event class cannot be null");
        Preconditions.checkArgument(eventPriority != null, "Event priority cannot be null");
        Preconditions.checkArgument(actor != null, "Actor function cannot be null");
        Preconditions.checkArgument(targets != null, "Targets function cannot be null");
        Preconditions.checkArgument(canceller != null, "Event canceller cannot be null");
    }

    public void onEvent(E event) {
        if (condition != null && !condition.test(event)) return;

        Entity actor = this.actor.apply(event);
        if (actor == null) return;

        Map<? extends T, Relation> targets = this.targets.apply(event);
        if (targets.isEmpty()) return;

        Set<Entity> actorRelated = null;

        for (Map.Entry<? extends T, Relation> entry : targets.entrySet()) {
            Relation eventRelation = entry.getValue();
            if (eventRelation == Relation.NEUTRAL) continue;

            if (actorRelated == null)
                actorRelated = FriendlyFire.queryRelated(actor);

            T target = entry.getKey();
            RelationQueryInfo info = new RelationQueryInfo(actor, target, hook, event);
            Set<Entity> targetRelated = FriendlyFire.queryRelated(target);

            Relation relation = FriendlyFire.queryRelation(actorRelated, targetRelated, info);
            if (!relation.canApply(eventRelation)) canceller.accept(event, target);
        }
    }

    @SuppressWarnings("unchecked")
    public void register() {
        Bukkit.getPluginManager().registerEvent(
            eventClass,
            this,
            eventPriority,
            (listener, event) -> {
                if (!eventClass.isAssignableFrom(event.getClass())) return;

                onEvent((E) event);
            },
            FriendlyFire.getInstance(),
            ignoreCancelled
        );
    }

    public static <E extends Event, A extends Entity, T extends Entity, H extends Hook<?> & EventHook> Builder<E, A, T, H> of(Class<E> eventClass, H hook) {
        return new Builder<>(eventClass, hook);
    }

    public static class Builder<E extends Event, A extends Entity, T extends Entity, H extends Hook<?> & EventHook> {

        private final Class<E> eventClass;
        private final H hook;

        private EventPriority priority = EventPriority.NORMAL;
        private boolean ignoreCancelled = true;
        private Predicate<E> condition;
        private Function<E, ? extends A> actor;
        private Function<E, Map<? extends T, Relation>> targets;
        private BiConsumer<E, T> canceller;

        public Builder(Class<E> eventClass, H hook) {
            Preconditions.checkArgument(eventClass != null, "Event class cannot be null.");
            Preconditions.checkArgument(hook != null, "Event hook cannot be null.");

            this.eventClass = eventClass;
            this.hook = hook;

            if (Cancellable.class.isAssignableFrom(eventClass))
                canceller = (event, second) -> ((Cancellable) event).setCancelled(true);
        }

        public Builder<E, A, T, H> priority(EventPriority priority) {
            Preconditions.checkArgument(priority != null, "Event priority cannot be null.");
            this.priority = priority;
            return this;
        }

        public Builder<E, A, T, H> ignoreCancelled(boolean ignoreCancelled) {
            this.ignoreCancelled = ignoreCancelled;
            return this;
        }

        public Builder<E, A, T, H> condition(Predicate<E> condition) {
            this.condition = condition;
            return this;
        }

        public Builder<E, A, T, H> actor(Function<E, ? extends A> actor) {
            Preconditions.checkArgument(actor != null, "Actor function cannot be null.");
            this.actor = actor;
            return this;
        }

        public Builder<E, A, T, H> target(Function<E, ? extends T> target, Relation relation) {
            Preconditions.checkArgument(target != null, "Target function cannot be null.");
            Preconditions.checkArgument(relation != null, "Relation cannot be null.");
            this.targets = event -> Map.of(target.apply(event), relation);
            return this;
        }

        public Builder<E, A, T, H> target(Function<E, ? extends T> target, Function<E, Relation> relation) {
            Preconditions.checkArgument(target != null, "Target function cannot be null.");
            Preconditions.checkArgument(relation != null, "Relation function cannot be null.");
            this.targets = event -> Map.of(target.apply(event), relation.apply(event));
            return this;
        }

        public Builder<E, A, T, H> targets(Function<E, Collection<? extends T>> targets, Relation relation) {
            Preconditions.checkArgument(targets != null, "Targets function cannot be null.");
            Preconditions.checkArgument(relation != null, "Relation cannot be null.");
            this.targets = event -> {
                Collection<? extends T> entities = targets.apply(event);

                Map<T, Relation> map = new HashMap<>(entities.size());
                for (T entity : entities) map.put(entity, relation);

                return map;
            };

            return this;
        }

        public Builder<E, A, T, H> targets(Function<E, Collection<? extends T>> targets, Function<E, Relation> relation) {
            Preconditions.checkArgument(targets != null, "Targets function cannot be null.");
            Preconditions.checkArgument(relation != null, "Relation function cannot be null.");
            this.targets = event -> {
                Collection<? extends T> entities = targets.apply(event);
                Relation rel = relation.apply(event);

                Map<T, Relation> map = new HashMap<>(entities.size());
                for (T entity : entities) map.put(entity, rel);

                return map;
            };

            return this;
        }

        public Builder<E, A, T, H> targets(Function<E, Map<? extends T, Relation>> targets) {
            Preconditions.checkArgument(targets != null, "Targets function cannot be null.");
            this.targets = targets;
            return this;
        }

        public Builder<E, A, T, H> canceller(Consumer<E> canceller) {
            Preconditions.checkArgument(canceller != null, "Event canceller cannot be null.");
            this.canceller = (event, entity) -> canceller.accept(event);
            return this;
        }

        public Builder<E, A, T, H> canceller(BiConsumer<E, T> canceller) {
            Preconditions.checkArgument(canceller != null, "Event canceller cannot be null.");
            this.canceller = canceller;
            return this;
        }

        public EventListener<E, A, T, H> build() {
            return new EventListener<>(hook, eventClass, priority, ignoreCancelled, condition, actor, targets, canceller);
        }

    }

}
