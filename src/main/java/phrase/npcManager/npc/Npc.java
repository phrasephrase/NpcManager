package phrase.npcManager.npc;

import com.mojang.authlib.GameProfile;
import net.minecraft.network.protocol.game.*;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.craftbukkit.v1_18_R2.CraftServer;
import org.bukkit.craftbukkit.v1_18_R2.CraftWorld;
import org.bukkit.craftbukkit.v1_18_R2.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;
import phrase.npcManager.Plugin;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class Npc extends ServerPlayer {

    private static final Map<Integer, Npc> npcs = new HashMap<>();
    private final ServerPlayer npc;
    private final Location location;
    private boolean look;

    public Npc(int id, GameProfile gameProfile, Location location) {
        super(
                ((CraftServer) Plugin.getInstance().getServer()).getServer(),
                ((CraftWorld) location.getWorld()).getHandle(),
                gameProfile
        );

        this.location = location;

        setPos(this.location.getX(), this.location.getY(), this.location.getZ());

        npcs.put(id, this);
        npc = getBukkitEntity().getHandle();

        getBukkitEntity().setPlayerListName(ChatColor.translateAlternateColorCodes('&', "&8[NpcM] ") + id);

        for (Player player : npc.getBukkitEntity().getServer().getOnlinePlayers()) {

            ServerPlayer serverPlayer = ((CraftPlayer) player).getHandle();
            ServerGamePacketListenerImpl connection = serverPlayer.connection;

            connection.send(new ClientboundPlayerInfoPacket(ClientboundPlayerInfoPacket.Action.ADD_PLAYER, npc));
            connection.send(new ClientboundAddPlayerPacket(npc));
            connection.send(new ClientboundTeleportEntityPacket(npc));

        }

        final Location finalLocation = this.location;
        new BukkitRunnable() {
            @Override
            public void run() {
                File file = new File(Plugin.getInstance().getDataFolder(), "npc.yml");

                if (!file.exists()) {
                    try {
                        file.createNewFile();
                    } catch (IOException e) {
                        Plugin.getInstance().getLogger().severe("Не удалось создать конфигурационный файл npc");
                        cancel();
                    }
                }

                YamlConfiguration config = YamlConfiguration.loadConfiguration(file);


                config.set(id + ".name", gameProfile.getName());
                config.set(id + ".id", gameProfile.getId().toString());
                config.set(id + ".location", finalLocation);

                try {
                    config.save(file);
                    cancel();
                } catch (IOException e) {
                    Plugin.getInstance().getLogger().severe("Не удалось сохранить конфигурационный файл npc");
                    cancel();
                }
            }
        }.runTaskAsynchronously(Plugin.getInstance());

    }

    public static void updateNpc() {

        long cooldownUpdates;
        if (Plugin.getInstance().getConfig().contains("settings.cooldownUpdates")) {
            cooldownUpdates = Plugin.getInstance().getConfig().getLong("settings.cooldownUpdates");
        } else {
            Plugin.getInstance().getLogger().severe("В конфигурационном файле npc в каталоге settings отсутствует параметр cooldownUpdates");
            return;
        }
        
        if (cooldownUpdates < 0) {
            Plugin.getInstance().getLogger().severe("В конфигурационном файле npc в каталоге settings параметр cooldownUpdates должен быть положительным значением");
            return;
        }


        new BukkitRunnable() {
            @Override
            public void run() {
                for (Player player : Bukkit.getServer().getOnlinePlayers()) {

                    ServerPlayer serverPlayer = ((CraftPlayer) player).getHandle();
                    ServerGamePacketListenerImpl connection = serverPlayer.connection;

                    for (Map.Entry<Integer, Npc> entry : getNpcs().entrySet()) {

                        // TODO

                    }

                }
            }
        }.runTaskTimer(Plugin.getInstance(), 0L, cooldownUpdates);

    }

    public static void loadNpc(ServerGamePacketListenerImpl connection) {

        if(getNpcs().isEmpty()) {
            return;
        }

        for (Map.Entry<Integer, Npc> entry : getNpcs().entrySet()) {

            connection.send(new ClientboundPlayerInfoPacket(ClientboundPlayerInfoPacket.Action.ADD_PLAYER, getNpcs().get(entry.getKey())));
            connection.send(new ClientboundAddPlayerPacket(getNpcs().get(entry.getKey())));
            connection.send(new ClientboundTeleportEntityPacket(getNpcs().get(entry.getKey())));
        }

    }

    public static void remove(int id) {

        ServerPlayer npc = Npc.getNpcs().get(id).getNpc();

        Npc.getNpcs().remove(id);

        for(Player player : npc.getBukkitEntity().getServer().getOnlinePlayers()) {

            ServerPlayer serverPlayer = ((CraftPlayer) player).getHandle();
            ServerGamePacketListenerImpl connection = serverPlayer.connection;

            connection.send(new ClientboundPlayerInfoPacket(ClientboundPlayerInfoPacket.Action.REMOVE_PLAYER, npc));
            connection.send(new ClientboundRemoveEntitiesPacket(npc.getId()));

        }

        new BukkitRunnable() {
            @Override
            public void run() {
                File file = new File(Plugin.getInstance().getDataFolder(), "npc.yml");

                YamlConfiguration config = YamlConfiguration.loadConfiguration(file);

                for(String key : config.getKeys(false)) {

                    if(!key.equalsIgnoreCase(String.valueOf(id))) {
                        continue;
                    }

                    config.set(String.valueOf(id), null);

                }

                try {
                    config.save(file);
                    cancel();
                } catch (IOException e) {
                    Plugin.getInstance().getLogger().severe("Не удалось сохранить конфигурационный файл npc.yml");
                    cancel();
                }
            }
        }.runTaskAsynchronously(Plugin.getInstance());

    }

    public static void look(int id, boolean look) {

        ServerPlayer npc = Npc.getNpcs().get(id).getNpc();

        Npc.getNpcs().get(id).setLook(look);

        if(Npc.getNpcs().get(id).isLook()) {

            new BukkitRunnable() {

                @Override
                public void run() {
                    for (Player player : npc.getBukkitEntity().getLocation().getWorld().getPlayers()) {

                        if(!Npc.getNpcs().get(id).isLook()) {
                            cancel();
                        }

                        ServerPlayer serverPlayer = ((CraftPlayer) player).getHandle();
                        ServerGamePacketListenerImpl connection = serverPlayer.connection;

                        Vector lookVec = player.getLocation().toVector().subtract(npc.getBukkitEntity().getLocation().toVector());
                        Location entityLoc = npc.getBukkitEntity().getLocation().setDirection(lookVec);

                        npc.setYRot(entityLoc.getYaw());
                        npc.setXRot(entityLoc.getPitch());


                        connection.send(new ClientboundRotateHeadPacket(npc, (byte) (entityLoc.getYaw() * 256 / 360)));
                        connection.send(new ClientboundMoveEntityPacket.PosRot(npc.getId(), (short) entityLoc.getX(),
                                (short) entityLoc.getY(), (short) entityLoc.getZ(),
                                (byte) (entityLoc.getYaw() * 256 / 360), (byte) (entityLoc.getPitch() * 256 / 360), true));
                        connection.send(new ClientboundTeleportEntityPacket(npc));
                    }
                }
            }.runTaskTimer(Plugin.getInstance(), 0L,1L);

        }

        for (Player player : npc.getBukkitEntity().getLocation().getWorld().getPlayers()) {

            ServerPlayer serverPlayer = ((CraftPlayer) player).getHandle();
            ServerGamePacketListenerImpl connection = serverPlayer.connection;

            Location location = Npc.getNpcs().get(id).getLocation();

            npc.setYRot(location.getYaw());
            npc.setXRot(location.getPitch());

            connection.send(new ClientboundRotateHeadPacket(npc, (byte) (location.getYaw() * 256 / 360)));
            connection.send(new ClientboundMoveEntityPacket.PosRot(npc.getId(), (short) location.getX(),
                    (short) location.getY(), (short) location.getZ(),
                    (byte) (location.getYaw() * 256 / 360), (byte) (location.getPitch() * 256 / 360), true));
            connection.send(new ClientboundTeleportEntityPacket(npc));
        }
    }

    public static void move(int id) {

        ServerPlayer npc = Npc.getNpcs().get(id).getNpc();

        // TODO

    }



    public static Map<Integer, Npc> getNpcs() {
        return npcs;
    }

    public ServerPlayer getNpc() {
        return npc;
    }

    public void setLook(boolean look) {
        this.look = look;
    }

    public boolean isLook() {
        return look;
    }

    public Location getLocation() {
        return location;
    }
}
