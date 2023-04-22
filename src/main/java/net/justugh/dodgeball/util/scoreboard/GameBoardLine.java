package net.justugh.dodgeball.util.scoreboard;

import net.md_5.bungee.api.ChatColor;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

public class GameBoardLine {

    private final String id;
    private final int position;
    private final Scoreboard board;
    private final Team team;
    private final ChatColor color;
    private final String originalText;

    private boolean update;

    public GameBoardLine(String id, int position, Scoreboard board, Team team, ChatColor color, String originalText, boolean update) {
        this.id = id;
        this.position = position;
        this.board = board;
        this.team = team;
        this.color = color;
        this.originalText = originalText;
        this.update = update;
    }

    public String getCurrentText() {
        return team.getPrefix();
    }

    public String getId() {
        return id;
    }

    public int getPosition() {
        return position;
    }

    public Scoreboard getBoard() {
        return board;
    }

    public Team getTeam() {
        return team;
    }

    public ChatColor getColor() {
        return color;
    }

    public String getOriginalText() {
        return originalText;
    }

    public boolean isUpdate() {
        return update;
    }

    public void setUpdate(boolean update) {
        this.update = update;
    }
}
