package dev.viewmodel.render;

import dev.viewmodel.config.Param;
import dev.viewmodel.config.Profile;
import dev.viewmodel.config.ViewModelConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.ItemRenderer;

/**
 * Обгортає ванільний рендер руки від першої особи: перед викликом оригіналу
 * застосовує зсув/поворот/масштаб навколо звичайного положення руки.
 * Вся ванільна логіка (замах, їжа, лук, блок, мапа) залишається недоторканою.
 */
public class ViewModelItemRenderer extends ItemRenderer {
    /** Опорна точка руки в просторі камери (як у ванільному transformFirstPersonItem). */
    private static final float PIVOT_X = 0.56F;
    private static final float PIVOT_Y = -0.52F;
    private static final float PIVOT_Z = -0.72F;

    private final Minecraft minecraft;

    public ViewModelItemRenderer(Minecraft mc) {
        super(mc);
        this.minecraft = mc;
    }

    @Override
    public void renderItemInFirstPerson(float partialTicks) {
        Profile profile = activeProfile();
        if (profile == null) {
            super.renderItemInFirstPerson(partialTicks);
            return;
        }

        GlStateManager.pushMatrix();
        try {
            // Z: додатне значення = далі від камери (в просторі камери вперед це -Z).
            GlStateManager.translate(
                    PIVOT_X + profile.get(Param.POS_X),
                    PIVOT_Y + profile.get(Param.POS_Y),
                    PIVOT_Z - profile.get(Param.POS_Z));

            float rx = profile.get(Param.ROT_X);
            float ry = profile.get(Param.ROT_Y);
            float rz = profile.get(Param.ROT_Z);
            if (rx != 0f) {
                GlStateManager.rotate(rx, 1f, 0f, 0f);
            }
            if (ry != 0f) {
                GlStateManager.rotate(ry, 0f, 1f, 0f);
            }
            if (rz != 0f) {
                GlStateManager.rotate(rz, 0f, 0f, 1f);
            }

            float s = profile.get(Param.SCALE);
            if (s != 1f) {
                GlStateManager.scale(s, s, s);
            }

            GlStateManager.translate(-PIVOT_X, -PIVOT_Y, -PIVOT_Z);
            super.renderItemInFirstPerson(partialTicks);
        } finally {
            GlStateManager.popMatrix();
        }
    }

    /** null = нічого міняти (мод вимкнено або профіль за замовчуванням). */
    private Profile activeProfile() {
        if (!ViewModelConfig.enabled) {
            return null;
        }
        EntityPlayerSP player = minecraft.thePlayer;
        boolean holdsItem = player != null && player.getHeldItem() != null;
        Profile p = holdsItem ? ViewModelConfig.ITEM : ViewModelConfig.HAND;
        return p.isDefault() ? null : p;
    }
}
