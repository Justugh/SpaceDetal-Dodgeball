package net.justugh.dodgeball.util;

import com.fastasyncworldedit.core.FaweAPI;
import com.google.common.collect.Lists;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormat;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormats;
import com.sk89q.worldedit.function.operation.Operation;
import com.sk89q.worldedit.function.operation.Operations;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.session.ClipboardHolder;
import org.bukkit.Bukkit;
import org.bukkit.Location;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.logging.Level;

public class SchematicAPI {

    public static List<Location> paste(File schematic, Location pasteLoc) {
        if (!schematic.exists()) {
            return null;
        }

        ClipboardFormat clipboardFormat = ClipboardFormats.findByFile(schematic);

        long startTime = System.currentTimeMillis();

        if (clipboardFormat == null) {
            return null;
        }

        try (Clipboard clipboard = clipboardFormat.load(schematic)) {
            EditSession editSession = WorldEdit.getInstance().newEditSessionBuilder().world(FaweAPI.getWorld(pasteLoc.getWorld().getName())).build();
            Operation operation = new ClipboardHolder(clipboard).createPaste(editSession).to(BlockVector3.at(pasteLoc.getX(), pasteLoc.getY(), pasteLoc.getZ()))
                    .ignoreAirBlocks(true).copyEntities(false).build();

            try {
                Operations.complete(operation);
                editSession.close();
            } catch (WorldEditException worldEditException) {
                worldEditException.printStackTrace();
            }

            BlockVector3 origin = clipboard.getOrigin();
            BlockVector3 minimumPoint = clipboard.getMinimumPoint();
            BlockVector3 maximumPoint = clipboard.getMaximumPoint();

            return Lists.newArrayList(
                    new Location(pasteLoc.getWorld(), pasteLoc.getX() + minimumPoint.getX() - origin.getX(),
                            pasteLoc.getY() + minimumPoint.getY() - origin.getY(),
                            pasteLoc.getZ() + minimumPoint.getZ() - origin.getZ()),
                    new Location(pasteLoc.getWorld(), pasteLoc.getX() + maximumPoint.getX() - origin.getX(),
                            pasteLoc.getY() + maximumPoint.getY() - origin.getY(),
                            pasteLoc.getZ() + maximumPoint.getZ() - origin.getZ()));
        } catch (IOException exception) {
            exception.printStackTrace();
            return null;
        }
    }

}