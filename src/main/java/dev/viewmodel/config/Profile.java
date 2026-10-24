package dev.viewmodel.config;

/** Набір значень усіх параметрів (одна вкладка меню: «Предмет» або «Порожня рука»). */
public class Profile {
    private final float[] values = new float[Param.values().length];

    public Profile() {
        reset();
    }

    public float get(Param p) {
        return values[p.ordinal()];
    }

    public void set(Param p, float v) {
        values[p.ordinal()] = p.snap(v);
    }

    public void reset() {
        for (Param p : Param.values()) {
            values[p.ordinal()] = p.def;
        }
    }

    public void copyFrom(Profile other) {
        System.arraycopy(other.values, 0, values, 0, values.length);
    }

    /** true, якщо все за замовчуванням — тоді рендер не чіпаємо взагалі. */
    public boolean isDefault() {
        for (Param p : Param.values()) {
            if (values[p.ordinal()] != p.def) {
                return false;
            }
        }
        return true;
    }
}
