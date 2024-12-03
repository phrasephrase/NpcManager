package phrase.npcManager.commands;

import com.mojang.authlib.GameProfile;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import phrase.npcManager.Plugin;
import phrase.npcManager.npc.Npc;
import phrase.npcManager.utils.ChatUtil;

import java.util.UUID;

public class NpcManagerCMD implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender commandSender, Command command,
                             String s, String[] strings) {

        if(!(commandSender instanceof Player)) {
            ChatUtil.sendMessage(commandSender, Plugin.getInstance().getConfig().getString("isAPlayer"));
            return true;
        }

        Player player = (Player) commandSender;

        if(strings.length < 1) {
            ChatUtil.sendMessage(player, Plugin.getInstance().getConfig().getString("usage"));
            return true;
        }

        if(strings[0].equalsIgnoreCase("create")) {

            if(strings.length < 3) {
                ChatUtil.sendMessage(player, Plugin.getInstance().getConfig().getString("message.usageCreate"));
                return true;
            }

            int id;
            try {
                id = Integer.parseInt(strings[1]);
            } catch (NumberFormatException e) {
                ChatUtil.sendMessage(player, Plugin.getInstance().getConfig().getString("id"));
                return true;
            }

            if(Npc.getNpcs().containsKey(id)) {
                ChatUtil.sendMessage(player, Plugin.getInstance().getConfig().getString("message.alreadyExists"));
                return true;
            }

            new Npc(
                    id,
                    new GameProfile(UUID.randomUUID(), strings[2]),
                    player.getLocation()
            );

            ChatUtil.sendMessage(player, Plugin.getInstance().getConfig().getString("message.create"));

            return true;
        }
        if(strings[0].equalsIgnoreCase("remove")) {

            if(strings.length < 2) {
                ChatUtil.sendMessage(player, Plugin.getInstance().getConfig().getString("message.usageRemove"));
                return true;
            }

            int id;
            try {
                id = Integer.parseInt(strings[1]);
            } catch (NumberFormatException e) {
                ChatUtil.sendMessage(player, Plugin.getInstance().getConfig().getString("id"));
                return true;
            }

            if(!Npc.getNpcs().containsKey(id)) {
                ChatUtil.sendMessage(player, Plugin.getInstance().getConfig().getString("message.doesNoExists"));
                return true;
            }

            Npc.remove(id);
            ChatUtil.sendMessage(player, Plugin.getInstance().getConfig().getString("message.remove"));

            return true;
        }
        if(strings[0].equalsIgnoreCase("look")) {

            if (strings.length < 3) {
                ChatUtil.sendMessage(player, Plugin.getInstance().getConfig().getString("message.usageLook"));
                return true;
            }

            int id;
            try {
                id = Integer.parseInt(strings[1]);
            } catch (NumberFormatException e) {
                ChatUtil.sendMessage(player, Plugin.getInstance().getConfig().getString("id"));
                return true;
            }

            if(!Npc.getNpcs().containsKey(id)) {
                ChatUtil.sendMessage(player, Plugin.getInstance().getConfig().getString("message.doesNoExists"));
                return true;
            }

            boolean look;
            try {
                if ("true".equalsIgnoreCase(strings[2])) {
                    look = Boolean.parseBoolean(strings[2]);
                } else if ("false".equalsIgnoreCase(strings[2])) {
                    look = Boolean.parseBoolean(strings[2]);
                } else {
                    throw new IllegalArgumentException();
                }
            } catch (IllegalArgumentException e) {
                ChatUtil.sendMessage(player, Plugin.getInstance().getConfig().getString("boolean"));
                return true;
            }

            Npc.look(id, look);

            return true;
        }
        if(strings[0].equalsIgnoreCase("move")) {

            if (strings.length < 2) {
                ChatUtil.sendMessage(player, Plugin.getInstance().getConfig().getString("message.usageMove"));
                return true;
            }

            int id;
            try {
                id = Integer.parseInt(strings[1]);
            } catch (NumberFormatException e) {
                ChatUtil.sendMessage(player, Plugin.getInstance().getConfig().getString("id"));
                return true;
            }

            if(!Npc.getNpcs().containsKey(id)) {
                ChatUtil.sendMessage(player, Plugin.getInstance().getConfig().getString("message.doesNoExists"));
                return true;
            }

            // TODO

            return true;
        }



        return true;
    }
}
