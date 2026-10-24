package dev.viewmodel.gui;

import dev.viewmodel.config.Param;
import dev.viewmodel.config.Profile;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.SoundHandler;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiButton;

/**
 * Слайдер одного параметра.
 * ЛКМ/перетягування — змінити, ПКМ — скинути, колесо — точно (Shift = ×10).
 * Біля значення за замовчуванням слайдер «прилипає», тож нуль легко влучити.
 */
public class SliderButton extends GuiButton {

    /** Те, з чим працює слайдер (поточна вкладка меню). */
    public interface Source {
        Profile current();

        /** Значення змінилось. saveNow = true — записати конфіг одразу. */
        void changed(boolean saveNow);
    }

    private final Param param;
    private final Source source;
    private boolean dragging;

    public SliderButton(int id, int x, int y, int w, int h, Param param, Source source) {
        super(id, x, y, w, h, param.label);
        this.param = param;
        this.source = source;
    }

    public boolean isInside(int mouseX, int mouseY) {
        return visible && mouseX >= xPosition && mouseY >= yPosition
                && mouseX < xPosition + width && mouseY < yPosition + height;
    }

    private int pixelFor(float value) {
        float t = (value - param.min) / (param.max - param.min);
        t = Math.max(0f, Math.min(1f, t));
        return xPosition + 1 + (int) (t * (width - 3));
    }

    private void setFromMouse(int mouseX) {
        float t = (mouseX - (xPosition + 1)) / (float) (width - 3);
        t = Math.max(0f, Math.min(1f, t));
        float v = param.min + t * (param.max - param.min);
        if (Math.abs(v - param.def) <= (param.max - param.min) * 0.012f) {
            v = param.def;
        }
        source.current().set(param, v);
        source.changed(false);
    }

    public void nudge(int direction, boolean coarse) {
        float delta = param.step * (coarse ? 10f : 1f) * direction;
        Profile p = source.current();
        p.set(param, p.get(param) + delta);
        source.changed(false);
    }

    public void reset() {
        source.current().set(param, param.def);
        source.changed(true);
    }

    @Override
    public boolean mousePressed(Minecraft mc, int mouseX, int mouseY) {
        if (enabled && isInside(mouseX, mouseY)) {
            dragging = true;
            setFromMouse(mouseX);
            return true;
        }
        return false;
    }

    @Override
    public void mouseReleased(int mouseX, int mouseY) {
        if (dragging) {
            dragging = false;
            source.changed(true);
        }
    }

    /** Без клацання на кожне перетягування. */
    @Override
    public void playPressSound(SoundHandler soundHandlerIn) {
    }

    @Override
    public void drawButton(Minecraft mc, int mouseX, int mouseY) {
        if (!visible) {
            return;
        }
        if (dragging) {
            setFromMouse(mouseX);
        }
        boolean over = isInside(mouseX, mouseY) || dragging;
        float value = source.current().get(param);

        int x1 = xPosition;
        int y1 = yPosition;
        int x2 = xPosition + width;
        int y2 = yPosition + height;

        drawRect(x1, y1, x2, y2, over ? 0xFF6A6A6A : 0xFF3F3F3F);
        drawRect(x1 + 1, y1 + 1, x2 - 1, y2 - 1, 0xFF1C1C1C);

        int handle = pixelFor(value);
        int defX = pixelFor(param.def);

        // заливка від значення за замовчуванням до поточного
        int fillLo = Math.min(defX, handle);
        int fillHi = Math.max(defX, handle);
        if (fillHi > fillLo) {
            drawRect(fillLo, y1 + 1, fillHi, y2 - 1, 0xFF2F6FB5);
        }
        // позначка значення за замовчуванням
        drawRect(defX, y2 - 3, defX + 1, y2 - 1, 0x99FFFFFF);
        // сам повзунок
        drawRect(Math.max(x1 + 1, handle - 1), y1 + 1, Math.min(x2 - 1, handle + 1), y2 - 1, 0xFFFFFFFF);

        FontRenderer fr = mc.fontRendererObj;
        int textY = y1 + (height - 8) / 2;
        fr.drawStringWithShadow(param.label, x1 + 4, textY, 0xFFFFFF);
        String text = param.display(value);
        fr.drawStringWithShadow(text, x2 - 4 - fr.getStringWidth(text), textY, 0xFFFFFF);
    }
}
