package dev.viewmodel.config;

import java.io.File;

import net.minecraftforge.common.config.Configuration;

/** Збереження/завантаження налаштувань у config/viewmodel.cfg. */
public final class ViewModelConfig {
    private static final String GENERAL = "general";
    private static final String CAT_ITEM = "item";
    private static final String CAT_HAND = "hand";

    public static volatile boolean enabled = true;
    /** Налаштування, коли в руці є предмет. */
    public static final Profile ITEM = new Profile();
    /** Налаштування для порожньої руки. */
    public static final Profile HAND = new Profile();

    private static Configuration config;

    private ViewModelConfig() {
    }

    public static void load(File file) {
        config = new Configuration(file);
        enabled = config.getBoolean("enabled", GENERAL, true, "Вмикає/вимикає мод повністю.");
        read(ITEM, CAT_ITEM);
        read(HAND, CAT_HAND);
        if (config.hasChanged()) {
            config.save();
        }
    }

    public static synchronized void save() {
        if (config == null) {
            return;
        }
        config.get(GENERAL, "enabled", true).set(enabled);
        write(ITEM, CAT_ITEM);
        write(HAND, CAT_HAND);
        config.save();
    }

    private static void read(Profile profile, String category) {
        for (Param p : Param.values()) {
            float v = config.getFloat(p.key, category, p.def, p.min, p.max, p.label);
            profile.set(p, v);
        }
    }

    private static void write(Profile profile, String category) {
        for (Param p : Param.values()) {
            config.get(category, p.key, (double) p.def).set((double) profile.get(p));
        }
    }
}
