package dev.viewmodel.config;

import java.util.Locale;

/**
 * Один налаштовуваний параметр положення руки/предмета.
 * Описує діапазон, значення за замовчуванням, крок та формат показу.
 */
public enum Param {
    POS_X("pos_x", "Позиція X", -1.5f, 1.5f, 0f, 0.01f, "%.2f"),
    POS_Y("pos_y", "Позиція Y", -1.5f, 1.5f, 0f, 0.01f, "%.2f"),
    /** Додатне значення = далі від камери. Від'ємне обмежене, щоб рука не йшла за near-plane. */
    POS_Z("pos_z", "Глибина Z", -0.5f, 1.5f, 0f, 0.01f, "%.2f"),
    ROT_X("rot_x", "Обертання X", -180f, 180f, 0f, 1f, "%.0f°"),
    ROT_Y("rot_y", "Обертання Y", -180f, 180f, 0f, 1f, "%.0f°"),
    ROT_Z("rot_z", "Обертання Z", -180f, 180f, 0f, 1f, "%.0f°"),
    SCALE("scale", "Розмір", 0.1f, 2.5f, 1f, 0.01f, "%.2f");

    public final String key;
    public final String label;
    public final float min;
    public final float max;
    public final float def;
    public final float step;
    private final String format;

    Param(String key, String label, float min, float max, float def, float step, String format) {
        this.key = key;
        this.label = label;
        this.min = min;
        this.max = max;
        this.def = def;
        this.step = step;
        this.format = format;
    }

    /** Обмежує значення діапазоном і округлює до кроку (без "-0"). */
    public float snap(float v) {
        if (Float.isNaN(v)) {
            return def;
        }
        v = Math.max(min, Math.min(max, v));
        float r = Math.round(v / step) * step;
        r = Math.round(r * 1000f) / 1000f;
        r = Math.max(min, Math.min(max, r));
        return r == 0f ? 0f : r;
    }

    public String display(float v) {
        return String.format(Locale.ROOT, format, v == 0f ? 0f : v);
    }
}
