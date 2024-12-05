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
        if(strings[0].equalsIgnoreCase("displayname")) {

            if (strings.length < 3) {
                ChatUtil.sendMessage(player, Plugin.getInstance().getConfig().getString("message.usageDisplayName"));
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

            String displayName = strings[2];

            Npc.changeDisplayName(id, displayName);
            ChatUtil.sendMessage(player, Plugin.getInstance().getConfig().getString("message.displayName"));
            return true;

        }

        if(strings[0].equalsIgnoreCase("path")) {

            if(strings.length < 2) {
                ChatUtil.sendMessage(player, Plugin.getInstance().getConfig().getString("message.usagePath"));
                return true;
            }

            if(strings[1].equalsIgnoreCase("edit")) {

                if(Npc.Path.getPathEditors().containsKey(player.getUniqueId())) {
                    ChatUtil.sendMessage(player, Plugin.getInstance().getConfig().getString("message.alreadyEditing"));
                    return true;
                }

                Npc.Path.editPath(player.getUniqueId());
                ChatUtil.sendMessage(player, Plugin.getInstance().getConfig().getString("message.editPath"));
                return true;
            }
            if(strings[1].equalsIgnoreCase("save")) {

                if(!Npc.Path.getPathEditors().containsKey(player.getUniqueId())) {
                    ChatUtil.sendMessage(player, Plugin.getInstance().getConfig().getString("message.alreadyEditing"));
                    return true;
                }

                Npc.Path.savePath(player.getUniqueId());
                ChatUtil.sendMessage(player, Plugin.getInstance().getConfig().getString("message.savePath"));
            }

            return true;
        }

        if(strings[0].equalsIgnoreCase("move")) {

            if (strings.length < 3) {
                ChatUtil.sendMessage(player, Plugin.getInstance().getConfig().getString("message.usageMove"));
                return true;
            }

            int idNpc;
            try {
                idNpc = Integer.parseInt(strings[1]);
            } catch (NumberFormatException e) {
                ChatUtil.sendMessage(player, Plugin.getInstance().getConfig().getString("id"));
                return true;
            }

            if(!Npc.getNpcs().containsKey(idNpc)) {
                ChatUtil.sendMessage(player, Plugin.getInstance().getConfig().getString("message.doesNoExists"));
                return true;
            }

            int idPath;
            try {
                idPath = Integer.parseInt(strings[2]);
            } catch (NumberFormatException e) {
                ChatUtil.sendMessage(player, Plugin.getInstance().getConfig().getString("id"));
                return true;
            }

            if(!Npc.Path.getPaths().containsKey(idPath)) {
                ChatUtil.sendMessage(player, Plugin.getInstance().getConfig().getString("message.doesNoPath"));
                return true;
            }

            Npc.move(idNpc, idPath);

            return true;
        }



        return true;
    }
}
