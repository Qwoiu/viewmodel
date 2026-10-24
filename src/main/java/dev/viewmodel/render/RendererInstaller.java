package dev.viewmodel.render;

import java.lang.reflect.Field;

import dev.viewmodel.ViewModelMod;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.EntityRenderer;
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraftforge.fml.relauncher.ReflectionHelper;

/** Один раз підміняє ванільний ItemRenderer на ViewModelItemRenderer. */
public final class RendererInstaller {
    private static boolean attempted;
    private static volatile boolean installed;

    private RendererInstaller() {
    }

    public static boolean isInstalled() {
        return installed;
    }

    public static void installOnce(Minecraft mc) {
        if (attempted || mc.entityRenderer == null) {
            return;
        }
        attempted = true;
        try {
            Field field = ReflectionHelper.findField(EntityRenderer.class, "itemRenderer", "field_78516_c");
            Object current = field.get(mc.entityRenderer);
            if (current != null && current.getClass() != ItemRenderer.class) {
                ViewModelMod.LOGGER.warn("ItemRenderer already replaced by {}; View Model will not hook in to avoid conflicts.",
                        current.getClass().getName());
                return;
            }
            ViewModelItemRenderer ours = new ViewModelItemRenderer(mc);
            field.set(mc.entityRenderer, ours);
            installed = true;
            syncMinecraftField(mc, ours);
            ViewModelMod.LOGGER.info("First-person renderer installed.");
        } catch (Throwable t) {
            ViewModelMod.LOGGER.error("Failed to install first-person renderer", t);
        }
    }

    /** Найкращі зусилля: тримаємо Minecraft#itemRenderer на тому ж екземплярі. Помилки ігноруємо. */
    private static void syncMinecraftField(Minecraft mc, ItemRenderer ours) {
        try {
            Field f = ReflectionHelper.findField(Minecraft.class, "itemRenderer", "field_175620_Y");
            if (f.getType() == ItemRenderer.class) {
                f.set(mc, ours);
            }
        } catch (Throwable ignored) {
            // не критично
        }
    }
}
