package phrase.npcManager.utils;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import phrase.npcManager.Plugin;

public class ChatUtil {

    private static final String prefix = Plugin.getInstance().getConfig().getString("prefix");

    private ChatUtil() {
    }

    public static void sendMessage(CommandSender commandSender, String message) {

        commandSender.sendMessage(ChatColor.translateAlternateColorCodes('&', prefix + message));

    }

    public static void sendMessage(Player player, String message) {

        player.sendMessage(ChatColor.translateAlternateColorCodes('&', prefix + message));

    }

}
