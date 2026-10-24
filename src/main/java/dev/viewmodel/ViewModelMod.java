package dev.viewmodel;

import dev.viewmodel.config.ViewModelConfig;
import dev.viewmodel.gui.GuiViewModel;
import dev.viewmodel.render.RendererInstaller;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraftforge.client.ClientCommandHandler;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.input.Keyboard;

@Mod(modid = ViewModelMod.MODID,
        name = ViewModelMod.NAME,
        version = ViewModelMod.VERSION,
        clientSideOnly = true,
        acceptableRemoteVersions = "*",
        acceptedMinecraftVersions = "[1.8.9]")
public class ViewModelMod {
    public static final String MODID = "viewmodel";
    public static final String NAME = "View Model";
    public static final String VERSION = "1.0.0";
    public static final Logger LOGGER = LogManager.getLogger(NAME);

    /** Клавіша відкриття меню (змінюється в Керуванні). */
    public static KeyBinding openKey;

    /** Скільки тіків ігнорувати клавішу після закриття меню нею ж (щоб не відкрилось знову). */
    public static volatile int ignoreKeyTicks;

    private static volatile boolean pendingOpen;

    public static void requestOpenGui() {
        pendingOpen = true;
    }

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        ViewModelConfig.load(event.getSuggestedConfigurationFile());

        openKey = new KeyBinding("key.viewmodel.open", Keyboard.KEY_RSHIFT, "key.categories.viewmodel");
        ClientRegistry.registerKeyBinding(openKey);
        ClientCommandHandler.instance.registerCommand(new ViewModelCommand());

        FMLCommonHandler.instance().bus().register(this);
    }

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }
        Minecraft mc = Minecraft.getMinecraft();
        RendererInstaller.installOnce(mc);

        // Завжди «спустошуємо» натискання, щоб вони не накопичувались, поки відкрито інші екрани.
        boolean pressed = false;
        while (openKey.isPressed()) {
            pressed = true;
        }
        if (ignoreKeyTicks > 0) {
            ignoreKeyTicks--;
            pressed = false;
        }
        if (mc.currentScreen != null || mc.thePlayer == null) {
            return;
        }
        if (pressed || pendingOpen) {
            pendingOpen = false;
            mc.displayGuiScreen(new GuiViewModel());
        }
    }
}
