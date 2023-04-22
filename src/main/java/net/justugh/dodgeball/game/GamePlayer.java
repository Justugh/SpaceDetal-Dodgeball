package net.justugh.dodgeball.game;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.UUID;

public class GamePlayer {

    private final UUID uuid;

    private int kills;
    private boolean alive = true;
    private GameTeam team;

    public GamePlayer(UUID uuid) {
        this.uuid = uuid;
    }

    public UUID getUuid() {
        return uuid;
    }

    public Player getPlayer() {
        return Bukkit.getPlayer(uuid);
    }

    public boolean isAlive() {
        return alive;
    }

    public void setAlive(boolean alive) {
        this.alive = alive;
    }

    public int getKills() {
        return kills;
    }

    public void setKills(int kills) {
        this.kills = kills;
    }

    public GameTeam getTeam() {
        return team;
    }

    public void setTeam(GameTeam team) {
        this.team = team;
    }
}
