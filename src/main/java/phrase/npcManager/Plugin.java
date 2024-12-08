package phrase.npcManager;

import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.mojang.authlib.GameProfile;
import net.minecraft.network.chat.Component;
import org.bukkit.Location;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import phrase.npcManager.commands.NpcManagerCMD;
import phrase.npcManager.enums.ActionType;
import phrase.npcManager.listeners.Click;
import phrase.npcManager.listeners.Join;
import phrase.npcManager.npc.Npc;
import phrase.npcManager.tabcompleter.NpcManagerTabCompleter;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public final class Plugin extends JavaPlugin {

    private static Plugin instance;
    private ProtocolManager protocolManager;

    @Override
    public void onEnable() {
        instance = this;



        protocolManager = ProtocolLibrary.getProtocolManager();
        getCommand("npcmanager").setExecutor(new NpcManagerCMD());
        getCommand("npcmanager").setTabCompleter(new NpcManagerTabCompleter());
        getServer().getPluginManager().registerEvents(new Join(), this);
        getServer().getPluginManager().registerEvents(new Click(), this);
        saveDefaultConfig();
        loadNpc();
        loadPath();
        Npc.updateNpc();

    }

    @Override
    public void onDisable() {
        saveConfig();
    }

    private void loadNpc() {

        File file = new File(getDataFolder(), "npc.yml");

        if(!file.exists()) {
            return;
        }

        YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
        for(String key : config.getKeys(false)) {

            String name = config.getString(key + ".name");
            String displayName = config.getString(key + ".displayname");
            ActionType actionType = ActionType.valueOf(config.getString(key + ".actionType"));
            String execute = config.getString(key + ".execute");
            UUID id = UUID.fromString(config.getString(key + ".id"));
            Location location = config.getLocation(key + ".location");

            Npc npc = new Npc(Integer.parseInt(key),
                    new GameProfile(id, name),
                    location);

            if(actionType != null || execute != null) {
                Npc.add(Integer.parseInt(key), actionType, execute);
            }

            npc.setCustomName(Component.nullToEmpty(displayName));
            npc.setCustomNameVisible(true);

        }

    }

    private void loadPath() {

        File file = new File(getDataFolder(), "path.yml");

        if(!file.exists()) {
            return;
        }

        YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
        for(String key : config.getKeys(false)) {

            List<Location> locations = (List<Location>) config.getList(key + ".locations", new ArrayList<>());
            Npc.Path.getPaths().put(Integer.parseInt(key), locations);

        }

    }

    public static Plugin getInstance() {
        return instance;
    }

    public ProtocolManager getProtocolManager() {
        return protocolManager;
    }
}
