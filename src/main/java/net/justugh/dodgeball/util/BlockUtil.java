package net.justugh.dodgeball.util;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;

import java.util.HashSet;
import java.util.Set;

public class BlockUtil {

    public static Set<Block> blocksFromTwoPoints(Location cornerA, Location cornerB) {
        World world = cornerA.getWorld() == null ? cornerB.getWorld() : cornerA.getWorld();

        if (world == null) {
            return null;
        }

        Set<Block> blocks = new HashSet<>();

        int topBlockX = Math.max(cornerA.getBlockX(), cornerB.getBlockX());
        int topBlockY = Math.max(cornerA.getBlockY(), cornerB.getBlockY());
        int topBlockZ = Math.max(cornerA.getBlockZ(), cornerB.getBlockZ());

        int bottomBlockX = Math.min(cornerA.getBlockX(), cornerB.getBlockX());
        int bottomBlockY = Math.min(cornerA.getBlockY(), cornerB.getBlockY());
        int bottomBlockZ = Math.min(cornerA.getBlockZ(), cornerB.getBlockZ());

        for (int x = bottomBlockX; x <= topBlockX; x++) {
            for (int z = bottomBlockZ; z <= topBlockZ; z++) {
                for (int y = bottomBlockY; y <= topBlockY; y++) {
                    Block block = world.getBlockAt(x, y, z);

                    blocks.add(block);
                }
            }
        }

        return blocks;
    }

}
