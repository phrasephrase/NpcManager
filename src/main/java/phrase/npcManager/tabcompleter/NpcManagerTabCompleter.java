package phrase.npcManager.tabcompleter;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.util.List;

public class NpcManagerTabCompleter implements TabCompleter {

    @Override
    public List<String> onTabComplete(CommandSender commandSender, Command command, String s, String[] strings) {
        if (strings.length == 1) {
            return List.of(
                    "create",
                    "remove",
                    "look",
                    "move",
                    "path",
                    "displayname",
                    "add"
            );
        }

        // TODO

        return List.of();
    }
}