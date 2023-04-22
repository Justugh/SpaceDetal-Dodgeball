package net.justugh.dodgeball.arena.data;

import org.bukkit.Location;

import java.util.List;

public class ArenaSignData {

    private final List<String> data;
    private final Location location;

    public ArenaSignData(List<String> data, Location location) {
        this.data = data;
        this.location = location;
    }

    public String getLine(int line) {
        return data.size() > line ? data.get(line) : null;
    }

    public List<String> getData() {
        return data;
    }

    public Location getLocation() {
        return location;
    }
}
