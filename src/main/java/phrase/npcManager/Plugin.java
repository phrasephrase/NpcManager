package phrase.npcManager;

import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.mojang.authlib.GameProfile;
import org.bukkit.Location;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import phrase.npcManager.commands.NpcManagerCMD;
import phrase.npcManager.listeners.Join;
import phrase.npcManager.npc.Npc;

import java.io.File;
import java.util.UUID;

public final class Plugin extends JavaPlugin {

    private static Plugin instance;
    private static ProtocolManager protocolManager;

    @Override
    public void onEnable() {
        instance = this;
        protocolManager = ProtocolLibrary.getProtocolManager();

        getCommand("npcmanager").setExecutor(new NpcManagerCMD());
        getServer().getPluginManager().registerEvents(new Join(), this);
        saveDefaultConfig();
        loadNpc();
        // Npc.updateNpc();

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
            UUID id = UUID.fromString(config.getString(key + ".id"));
            Location location = config.getLocation(key + ".location");

            new Npc(Integer.parseInt(key),
                    new GameProfile(id, name),
                    location
                    ) {
            };

        }

    }

    public static Plugin getInstance() {
        return instance;
    }

    public static ProtocolManager getProtocolManager() {
        return protocolManager;
    }
}
