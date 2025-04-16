package me.tonythemacaroni.friendlyfire.hook.external.magicspells;

import java.util.List;
import java.util.Collection;
import java.util.WeakHashMap;

import org.spongepowered.configurate.objectmapping.ConfigSerializable;

import org.bukkit.event.Listener;
import org.bukkit.event.EventHandler;

import com.nisovin.magicspells.Spell;
import com.nisovin.magicspells.MagicSpells;
import com.nisovin.magicspells.util.MagicConfig;
import com.nisovin.magicspells.events.SpellTargetEvent;
import com.nisovin.magicspells.events.MagicSpellsLoadedEvent;

import me.tonythemacaroni.friendlyfire.hook.Hook;
import me.tonythemacaroni.friendlyfire.hook.HookInfo;
import me.tonythemacaroni.friendlyfire.util.Relation;
import me.tonythemacaroni.friendlyfire.hook.EventHook;
import me.tonythemacaroni.friendlyfire.util.EventListener;
import me.tonythemacaroni.friendlyfire.hook.config.EventPriorityConfig;

@HookInfo(
    name = "spell-target",
    description = "Controls interactions with spell targeting,",
    namespace = "magicspells",
    depends = "MagicSpells"
)
public class SpellTargetEventHook extends Hook<SpellTargetEventHook.Config> implements EventHook, Listener {

    private final WeakHashMap<Spell, Relation> relations = new WeakHashMap<>();

    public SpellTargetEventHook(Config config) {
        super(config);

        if (MagicSpells.isLoaded()) loadRelations();
    }

    @Override
    public Collection<Listener> collectListeners() {
        return List.of(
            this,
            EventListener.of(SpellTargetEvent.class, this)
                .priority(config.eventPriority)
                .actor(SpellTargetEvent::getCaster)
                .target(SpellTargetEvent::getTarget, event -> {
                    Spell spell = event.getSpell();

                    Relation relation = relations.get(spell);
                    if (relation != null) return relation;

                    return spell.isBeneficial() ? Relation.FRIENDLY : Relation.HOSTILE;
                })
                .build()
        );
    }

    @EventHandler
    public void onMagicSpellsLoaded(MagicSpellsLoadedEvent event) {
        loadRelations();
    }

    private void loadRelations() {
        relations.clear();

        MagicConfig magicConfig = MagicSpells.getInstance().getMagicConfig();

        for (Spell spell : MagicSpells.getSpellsOrdered()) {
            String path = "spells." + spell.getInternalName();

            String relationString = magicConfig.getString(path + ".relation", null);
            if (relationString == null) {
                if (config.defaultToNeutral && !magicConfig.isBoolean(path + ".beneficial"))
                    relations.put(spell, Relation.NEUTRAL);

                continue;
            }

            Relation relation;
            try {
                relation = Relation.valueOf(relationString.toUpperCase());
            } catch (IllegalArgumentException e) {
                continue;
            }

            relations.put(spell, relation);
        }
    }

    @ConfigSerializable
    public static class Config extends EventPriorityConfig {

        public boolean defaultToNeutral = false;

    }

}
