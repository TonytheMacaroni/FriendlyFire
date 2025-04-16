package me.tonythemacaroni.friendlyfire.hook;

import java.lang.annotation.Target;
import java.lang.annotation.Retention;
import java.lang.annotation.ElementType;
import java.lang.annotation.RetentionPolicy;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface HookInfo {

    String name();

    String description();

    String namespace();

    String[] depends() default {};

}
