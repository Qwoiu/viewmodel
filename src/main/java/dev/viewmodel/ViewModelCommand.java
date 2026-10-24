package dev.viewmodel;

import java.util.Collections;
import java.util.List;

import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;

/** Клієнтська команда /viewmodel (або /vm) — відкриває меню. */
public class ViewModelCommand extends CommandBase {

    @Override
    public String getCommandName() {
        return "viewmodel";
    }

    @Override
    public List<String> getCommandAliases() {
        return Collections.singletonList("vm");
    }

    @Override
    public String getCommandUsage(ICommandSender sender) {
        return "/viewmodel";
    }

    @Override
    public void processCommand(ICommandSender sender, String[] args) throws CommandException {
        // Чат закривається одразу після команди, тому меню відкриваємо на наступному тіку.
        ViewModelMod.requestOpenGui();
    }

    @Override
    public boolean canCommandSenderUseCommand(ICommandSender sender) {
        return true;
    }

    @Override
    public int getRequiredPermissionLevel() {
        return 0;
    }
}
