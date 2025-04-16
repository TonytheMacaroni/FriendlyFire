package me.tonythemacaroni.friendlyfire.util;

import org.bukkit.entity.Entity;
import org.bukkit.craftbukkit.entity.CraftEntity;

public final class NMSUtil {

    public static boolean isPickable(Entity entity) {
        return ((CraftEntity) entity).getHandle().isPickable();
    }

    public static float getPickRadius(Entity entity) {
        return ((CraftEntity) entity).getHandle().getPickRadius();
    }

}
