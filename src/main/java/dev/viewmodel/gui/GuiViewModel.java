package dev.viewmodel.gui;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import dev.viewmodel.ViewModelMod;
import dev.viewmodel.config.Profile;
import dev.viewmodel.config.ViewModelConfig;
import dev.viewmodel.render.RendererInstaller;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

/**
 * Меню налаштувань. Панель зліва, світ (і рука) лишається видимим справа,
 * тож зміни видно наживо.
 */
public class GuiViewModel extends GuiScreen implements SliderButton.Source {
    private static final int PANEL_X = 6;
    private static final int PANEL_Y = 6;
    private static final int PANEL_W = 190;
    private static final int PANEL_H = 202;
    private static final int PAD = 6;

    private static final int ID_TAB_ITEM = 0;
    private static final int ID_TAB_HAND = 1;
    private static final int ID_RESET = 2;
    private static final int ID_COPY = 3;
    private static final int ID_SWING = 4;
    private static final int ID_TOGGLE = 5;
    private static final int ID_DONE = 6;
    private static final int ID_SLIDER_BASE = 100;

    private final List<SliderButton> sliders = new ArrayList<SliderButton>();
    private FlatButton tabItem;
    private FlatButton tabHand;
    private FlatButton copyButton;
    private FlatButton toggleButton;

    /** 0 = «Предмет», 1 = «Порожня рука». */
    private int tab;
    private String status = "";
    private long statusUntil;

    public GuiViewModel() {
        EntityPlayerSP player = Minecraft.getMinecraft().thePlayer;
        // Одразу відкриваємо ту вкладку, яка зараз видна на екрані.
        this.tab = (player != null && player.getHeldItem() == null) ? 1 : 0;
    }

    // ---- SliderButton.Source ----

    @Override
    public Profile current() {
        return tab == 0 ? ViewModelConfig.ITEM : ViewModelConfig.HAND;
    }

    @Override
    public void changed(boolean saveNow) {
        if (saveNow) {
            ViewModelConfig.save();
        }
    }

    // ---- побудова екрана ----

    @Override
    public void initGui() {
        buttonList.clear();
        sliders.clear();

        int x = PANEL_X + PAD;
        int w = PANEL_W - PAD * 2;
        int half = (w - 2) / 2;
        int y = PANEL_Y + 16;

        tabItem = new FlatButton(ID_TAB_ITEM, x, y, half, 14, "Предмет");
        tabHand = new FlatButton(ID_TAB_HAND, x + half + 2, y, w - half - 2, 14, "Порожня рука");
        buttonList.add(tabItem);
        buttonList.add(tabHand);
        y += 18;

        for (dev.viewmodel.config.Param p : dev.viewmodel.config.Param.values()) {
            SliderButton slider = new SliderButton(ID_SLIDER_BASE + p.ordinal(), x, y, w, 14, p, this);
            sliders.add(slider);
            buttonList.add(slider);
            y += 16;
        }
        y += 4;

        buttonList.add(new FlatButton(ID_RESET, x, y, half, 14, "Скинути"));
        copyButton = new FlatButton(ID_COPY, x + half + 2, y, w - half - 2, 14, "");
        buttonList.add(copyButton);
        y += 16;

        buttonList.add(new FlatButton(ID_SWING, x, y, half, 14, "Замах"));
        toggleButton = new FlatButton(ID_TOGGLE, x + half + 2, y, w - half - 2, 14, "");
        buttonList.add(toggleButton);
        y += 16;

        buttonList.add(new FlatButton(ID_DONE, x, y, w, 14, "Готово"));

        refreshLabels();
    }

    private void refreshLabels() {
        tabItem.active = tab == 0;
        tabHand.active = tab == 1;
        copyButton.displayString = tab == 0 ? "Копія → Рука" : "Копія → Предмет";
        toggleButton.displayString = ViewModelConfig.enabled ? "Мод: УВІМК" : "Мод: ВИМК";
        toggleButton.active = ViewModelConfig.enabled;
    }

    private void setStatus(String text) {
        status = text;
        statusUntil = System.currentTimeMillis() + 2000L;
    }

    // ---- події ----

    @Override
    protected void actionPerformed(GuiButton button) throws IOException {
        switch (button.id) {
            case ID_TAB_ITEM:
                tab = 0;
                break;
            case ID_TAB_HAND:
                tab = 1;
                break;
            case ID_RESET:
                current().reset();
                ViewModelConfig.save();
                setStatus("Вкладку скинуто");
                break;
            case ID_COPY: {
                Profile from = current();
                Profile to = tab == 0 ? ViewModelConfig.HAND : ViewModelConfig.ITEM;
                to.copyFrom(from);
                ViewModelConfig.save();
                setStatus("Скопійовано");
                break;
            }
            case ID_SWING:
                if (mc.thePlayer != null) {
                    mc.thePlayer.swingItem();
                }
                break;
            case ID_TOGGLE:
                ViewModelConfig.enabled = !ViewModelConfig.enabled;
                ViewModelConfig.save();
                break;
            case ID_DONE:
                mc.displayGuiScreen(null);
                return;
            default:
                break;
        }
        refreshLabels();
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        if (mouseButton == 1) {
            for (SliderButton slider : sliders) {
                if (slider.isInside(mouseX, mouseY)) {
                    slider.reset();
                    return;
                }
            }
        }
        super.mouseClicked(mouseX, mouseY, mouseButton);
    }

    @Override
    public void handleMouseInput() throws IOException {
        super.handleMouseInput();
        int wheel = Mouse.getEventDWheel();
        if (wheel != 0) {
            int mx = Mouse.getEventX() * width / mc.displayWidth;
            int my = height - Mouse.getEventY() * height / mc.displayHeight - 1;
            for (SliderButton slider : sliders) {
                if (slider.isInside(mx, my)) {
                    slider.nudge(wheel > 0 ? 1 : -1, isShiftKeyDown());
                    break;
                }
            }
        }
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        int openKey = ViewModelMod.openKey.getKeyCode();
        if (openKey != Keyboard.KEY_NONE && keyCode == openKey) {
            ViewModelMod.ignoreKeyTicks = 2;
            mc.displayGuiScreen(null);
            return;
        }
        super.keyTyped(typedChar, keyCode);
    }

    @Override
    public void onGuiClosed() {
        ViewModelConfig.save();
    }

    /** Світ не зупиняємо — щоб було видно анімацію руки. */
    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }

    // ---- малювання ----

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        // Без затемнення фону — рука має бути добре видна.
        drawRect(PANEL_X, PANEL_Y, PANEL_X + PANEL_W, PANEL_Y + PANEL_H, 0xC0101010);
        drawRect(PANEL_X, PANEL_Y, PANEL_X + PANEL_W, PANEL_Y + 1, 0xFF2F6FB5);
        drawString(fontRendererObj, "View Model", PANEL_X + PAD, PANEL_Y + 5, 0xFFFFFF);

        super.drawScreen(mouseX, mouseY, partialTicks);

        int textX = PANEL_X + 2;
        int textY = PANEL_Y + PANEL_H + 4;
        if (!RendererInstaller.isInstalled()) {
            drawString(fontRendererObj, "§cРендер руки не підключено (див. latest.log)", textX, textY, 0xFFFFFF);
        } else if (System.currentTimeMillis() < statusUntil) {
            drawString(fontRendererObj, "§a" + status, textX, textY, 0xFFFFFF);
        } else {
            drawString(fontRendererObj, "ПКМ — скинути слайдер", textX, textY, 0xAAAAAA);
        }
        drawString(fontRendererObj, "Колесо — точно (Shift = x10)", textX, textY + 10, 0xAAAAAA);
    }
}
