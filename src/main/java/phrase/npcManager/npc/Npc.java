package phrase.npcManager.npc;

import com.mojang.authlib.GameProfile;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.*;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.phys.Vec3;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.craftbukkit.v1_18_R2.CraftServer;
import org.bukkit.craftbukkit.v1_18_R2.CraftWorld;
import org.bukkit.craftbukkit.v1_18_R2.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;
import phrase.npcManager.Plugin;
import phrase.npcManager.utils.ChatUtil;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class Npc extends ServerPlayer {

    private static final Map<Integer, Npc> npcs = new HashMap<>();
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

        getBukkitEntity().setDisplayName(gameProfile.getName());
        getBukkitEntity().setPlayerListName(ChatColor.translateAlternateColorCodes('&', "&8[NpcM] ") + id);

        for (Player player : this.getBukkitEntity().getServer().getOnlinePlayers()) {

            ServerPlayer serverPlayer = ((CraftPlayer) player).getHandle();
            ServerGamePacketListenerImpl connection = serverPlayer.connection;

            connection.send(new ClientboundPlayerInfoPacket(ClientboundPlayerInfoPacket.Action.ADD_PLAYER, this));
            connection.send(new ClientboundAddPlayerPacket(this));
            connection.send(new ClientboundTeleportEntityPacket(this));

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
                        return;
                    }
                }

                YamlConfiguration config = YamlConfiguration.loadConfiguration(file);


                config.set(id + ".name", gameProfile.getName());
                config.set(id + ".displayname", gameProfile.getName());
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
                for (Player player : Plugin.getInstance().getServer().getOnlinePlayers()) {

                    ServerPlayer serverPlayer = ((CraftPlayer) player).getHandle();
                    ServerGamePacketListenerImpl connection = serverPlayer.connection;

                    for (Map.Entry<Integer, Npc> entry : getNpcs().entrySet()) {

                        connection.send(new ClientboundPlayerInfoPacket(ClientboundPlayerInfoPacket.Action.UPDATE_DISPLAY_NAME, entry.getValue()));

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

        Npc npc = Npc.getNpcs().get(id);

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

        Npc npc = Npc.getNpcs().get(id);

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

    public static void changeDisplayName(int id, String displayName) {

        Npc npc = Npc.getNpcs().get(id);
        npc.setCustomName(Component.nullToEmpty(displayName));
        npc.setCustomNameVisible(true);

        for (Player player : Plugin.getInstance().getServer().getOnlinePlayers()) {

            ServerPlayer serverPlayer = ((CraftPlayer) player).getHandle();
            ServerGamePacketListenerImpl connection = serverPlayer.connection;

            connection.send(new ClientboundPlayerInfoPacket(ClientboundPlayerInfoPacket.Action.UPDATE_DISPLAY_NAME, npc));

        }

        new BukkitRunnable() {
            @Override
            public void run() {

                File file = new File(Plugin.getInstance().getDataFolder(), "npc.yml");

                if(!file.exists()) {
                    return;
                }

                YamlConfiguration config = YamlConfiguration.loadConfiguration(file);

                for(String key : config.getKeys(false)) {

                    if(!key.equalsIgnoreCase(String.valueOf(id))) {
                        return;
                    }

                    config.set(key + ".displayname", displayName);

                }

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

    public static void move(int idNpc, int idPath) {

        Npc npc = Npc.getNpcs().get(idNpc);
        List<Location> locations = Path.getPaths().get(idPath);

        new BukkitRunnable() {
            private int currentIndex = 0;

            @Override
            public void run() {
                    if (currentIndex >= locations.size()) {
                         cancel();
                         return;
                    }

                    Location startPosition = npc.getBukkitEntity().getLocation();
                    Location finishPosition = locations.get(currentIndex);

                    double toMoveX = 0;
                    double toMoveY = 0;
                    double toMoveZ = 0;

                    if (startPosition.distance(finishPosition) < 1) {
                        currentIndex++;
                        return;
                    }

                    if (Math.abs(startPosition.getX() - finishPosition.getX()) > 0.2) {
                        if (startPosition.getX() > finishPosition.getX()) {
                            toMoveX = -0.2;
                        } else {
                            toMoveX = 0.2;
                        }
                    }

                    if(Math.abs(startPosition.getY() - finishPosition.getY()) >= 1.0) {
                        if(startPosition.getY() > finishPosition.getY()) {
                            toMoveY = -1.0;
                        } else {
                            toMoveY = 1.0;
                        }
                    }

                    if (Math.abs(startPosition.getZ() - finishPosition.getZ()) > 0.2) {
                        if (startPosition.getZ() > finishPosition.getZ()) {
                            toMoveZ = -0.2;
                        } else {
                            toMoveZ = 0.2;
                        }
                    }

                    Vector lookVec = finishPosition.toVector().subtract(npc.getBukkitEntity().getLocation().toVector());
                    Location entityLoc = npc.getBukkitEntity().getLocation().setDirection(lookVec);

                    npc.move(MoverType.SELF, new Vec3(toMoveX, toMoveY, toMoveZ));

                    for (Player player : finishPosition.getWorld().getPlayers()) {
                        ServerGamePacketListenerImpl serverGamePacketListener = ((CraftPlayer) player).getHandle().connection;

                        serverGamePacketListener.send(new ClientboundTeleportEntityPacket(npc));
                        serverGamePacketListener.send(new ClientboundRotateHeadPacket(npc, (byte) (entityLoc.getYaw() * 256 / 360)));
                    }
            }
        }.runTaskTimer(Plugin.getInstance(), 0L, 1L);

    }

    public static class Path {

        private static final HashMap<UUID, Integer> pathEditors = new HashMap<>();
        private static final Map<Integer, List<Location>> paths = new HashMap<>();
        private static int countPaths = 0;

        public static void savePath(UUID player) {
            int idPath = getPathEditors().get(player);
            getPathEditors().remove(player);

            List<Location> locations = getPaths().get(idPath);
            int finalIdPath = idPath++;
            new BukkitRunnable() {
                @Override
                public void run() {

                    File file = new File(Plugin.getInstance().getDataFolder(), "path.yml");

                    if (!file.exists()) {
                        try {
                            file.createNewFile();
                        } catch (IOException e) {
                            Plugin.getInstance().getLogger().severe("Не удалось создать конфигурационный файл path");
                            cancel();
                        }
                    }

                    YamlConfiguration config = YamlConfiguration.loadConfiguration(file);

                    config.set(finalIdPath + ".locations", locations);

                    try {
                        config.save(file);
                        cancel();
                    } catch (IOException e) {
                        Plugin.getInstance().getLogger().severe("Не удалось сохранить конфигурационный файл path");
                        cancel();
                    }

                }
            }.runTaskAsynchronously(Plugin.getInstance());

        }

        public static void editPath(UUID player) {
            int idPath = getCountPaths() + 1;

            getPathEditors().put(player, idPath);

            List<Location> locations = new ArrayList<>();

            getPaths().put(idPath, locations);
        }

        public static void setPoint(Player player, Block block) {

            if(!Npc.Path.getPathEditors().containsKey(player.getUniqueId())) {
                return;
            }

            int idPath = Npc.Path.getPathEditors().get(player.getUniqueId());

            List<Location> locations = Npc.Path.getPaths().get(idPath);

            locations.add(block.getLocation());

            block.getLocation().getWorld().spawnParticle(
                    Particle.REDSTONE, block.getLocation(), 1, new Particle.DustOptions(Color.RED, 20));

            Npc.Path.getPaths().replace(idPath, locations);
            ChatUtil.sendMessage(player, Plugin.getInstance().getConfig().getString("message.setPoint"));

        }

        public static Map<UUID, Integer> getPathEditors() {
            return pathEditors;
        }

        public static Map<Integer, List<Location>> getPaths() {
            return paths;
        }

        public static int getCountPaths() {
            return countPaths;
        }

    }

    public static Map<Integer, Npc> getNpcs() {
        return npcs;
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
