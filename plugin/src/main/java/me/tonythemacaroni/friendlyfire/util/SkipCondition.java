package me.tonythemacaroni.friendlyfire.util;

import org.jetbrains.annotations.NotNull;

import java.util.function.Predicate;

public class SkipCondition<T> implements Predicate<T> {

    private Predicate<T> predicate;

    public void start(@NotNull Predicate<T> predicate, @NotNull Runnable runnable) {
        Predicate<T> previous = this.predicate;
        this.predicate = predicate;

        try {
            runnable.run();
        } finally {
            this.predicate = previous;
        }
    }

    @Override
    public boolean test(T t) {
        return predicate == null || !predicate.test(t);
    }

}
