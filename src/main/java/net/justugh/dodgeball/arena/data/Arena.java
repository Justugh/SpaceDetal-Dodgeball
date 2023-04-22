package net.justugh.dodgeball.arena.data;

import net.justugh.dodgeball.Dodgeball;
import net.justugh.dodgeball.util.BlockUtil;
import net.justugh.dodgeball.util.SchematicAPI;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;

public class Arena implements Cloneable {

    private final String name;
    private final String author;
    private final File schematic;

    private Location spectatorSpawn;
    private Location lobbySpawn;
    private final List<Location> spawns = new ArrayList<>();
    private final List<ArenaSignData> customData = new ArrayList<>();

    private Location cornerA;
    private Location cornerB;

    public Arena(String name, String author, File schematic) {
        this.name = name;
        this.author = author;
        this.schematic = schematic;
    }

    public void generateArena(Location location) {
        List<Location> locations = SchematicAPI.paste(schematic, location);

        if (locations == null || locations.size() < 2) {
            Dodgeball.getInstance().getLogger().log(Level.WARNING, "Unable to generate Arena '{0}', corner locations are null.", name);
            return;
        }

        this.cornerA = locations.get(0);
        this.cornerB = locations.get(1);

        Set<Block> blocks = BlockUtil.blocksFromTwoPoints(cornerA, cornerB);

        if (blocks == null) {
            Dodgeball.getInstance().getLogger().log(Level.WARNING, "Unable to scan blocks for Arena '{0}'.", name);
            return;
        }

        for (Block block : blocks) {
            if (block.getType().equals(Material.SPONGE)) {
                spawns.add(block.getLocation().add(0.5, 0, 0.5));
                block.setType(Material.AIR);
            }

            if (block.getType().equals(Material.COMMAND_BLOCK)) {
                spectatorSpawn = block.getLocation().add(0.5, 0, 0.5);
                block.setType(Material.AIR);
            }

            if (block.getType().equals(Material.DAYLIGHT_DETECTOR)) {
                lobbySpawn = block.getLocation().add(0.5, 0, 0.5);
                block.setType(Material.AIR);
            }

            if (block.getType().name().contains("SIGN")) {
                Sign sign = (Sign) block.getState();

                if (sign.getLines().length == 0 || !sign.getLine(0).equalsIgnoreCase("ARENA_DATA")) {
                    continue;
                }

                List<String> lines = new ArrayList<>();
                lines.addAll(List.of(sign.getLines()));
                lines.remove(0);

                customData.add(new ArenaSignData(lines, block.getLocation()));
                block.setType(Material.AIR);
            }
        }
    }

    @Override
    public Arena clone() {
        return new Arena(name, author, schematic);
    }

    public Location getSpectatorSpawn() {
        return spectatorSpawn;
    }

    public Location getLobbySpawn() {
        return lobbySpawn;
    }

    public List<Location> getSpawns() {
        return spawns;
    }

    public List<ArenaSignData> getCustomData() {
        return customData;
    }

    public ArenaSignData getCustomData(String id) {
        return customData.stream().filter(cd -> cd.getData().contains(id)).findFirst().orElse(null);
    }

    public String getName() {
        return name;
    }

    public String getAuthor() {
        return author;
    }
}
