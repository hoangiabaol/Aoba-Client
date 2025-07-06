/*
 * Aoba Hacked Client
 * Copyright (C) 2019-2024 coltonk9043
 *
 * Licensed under the GNU General Public License, Version 3 or later.
 * See <http://www.gnu.org/licenses/>.
 */

package net.aoba.command.commands;

import net.aoba.Aoba;
import net.aoba.command.Command;
import net.aoba.command.InvalidSyntaxException;
import net.aoba.managers.CommandManager;
import net.aoba.module.modules.render.AntiTrap;

public class CmdAntiTrap extends Command {

    public CmdAntiTrap() {
        super("antitrap", "Toggles AntiTrap module (removes ArmorStands and Signs).", "[toggle] [on/off]");
    }

    @Override
    public void runCommand(String[] parameters) throws InvalidSyntaxException {
        if (parameters.length != 2)
            throw new InvalidSyntaxException(this);

        AntiTrap module = (AntiTrap) Aoba.getInstance().moduleManager.getModuleByName("AntiTrap");
        String action = parameters[0].toLowerCase();
        String value = parameters[1].toLowerCase();

        if (action.equals("toggle")) {
            switch (value) {
                case "on" -> {
                    module.state.setValue(true);
                    CommandManager.sendChatMessage("AntiTrap toggled ON");
                }
                case "off" -> {
                    module.state.setValue(false);
                    CommandManager.sendChatMessage("AntiTrap toggled OFF");
                }
                default -> CommandManager.sendChatMessage("Invalid toggle value. Use: on/off");
            }
        } else {
            throw new InvalidSyntaxException(this);
        }
    }

    @Override
    public String[] getAutocorrect(String previousParameter) {
        return switch (previousParameter.toLowerCase()) {
            case "toggle" -> new String[] { "on", "off" };
            default -> new String[] { "toggle" };
        };
    }
}