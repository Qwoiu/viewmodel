package dev.viewmodel.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;

/** Проста плоска кнопка в стилі меню мода. */
public class FlatButton extends GuiButton {
    public boolean active;

    public FlatButton(int id, int x, int y, int w, int h, String text) {
        super(id, x, y, w, h, text);
    }

    @Override
    public void drawButton(Minecraft mc, int mouseX, int mouseY) {
        if (!visible) {
            return;
        }
        boolean over = enabled && mouseX >= xPosition && mouseY >= yPosition
                && mouseX < xPosition + width && mouseY < yPosition + height;
        int fill = active ? 0xFF2F6FB5 : (over ? 0xFF4A4A4A : 0xFF2A2A2A);
        int border = over ? 0xFF7A7A7A : 0xFF3F3F3F;
        drawRect(xPosition, yPosition, xPosition + width, yPosition + height, border);
        drawRect(xPosition + 1, yPosition + 1, xPosition + width - 1, yPosition + height - 1, fill);
        drawCenteredString(mc.fontRendererObj, displayString, xPosition + width / 2,
                yPosition + (height - 8) / 2, enabled ? 0xFFFFFF : 0x777777);
    }
}
