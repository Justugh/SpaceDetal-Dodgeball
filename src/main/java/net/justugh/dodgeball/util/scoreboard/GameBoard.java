package net.justugh.dodgeball.util.scoreboard;

import net.justugh.dodgeball.util.Format;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class GameBoard {

    private final Scoreboard scoreboard;
    private final Objective objective;
    private int currentLine = 15;

    private List<GameBoardLine> lines = new ArrayList<>();

    private BukkitTask updateTask;

    public GameBoard(String name) {
        scoreboard = Bukkit.getScoreboardManager().getNewScoreboard();
        objective = scoreboard.registerNewObjective(UUID.randomUUID().toString(), "dummy", Format.format(name));
        objective.setDisplaySlot(DisplaySlot.SIDEBAR);
    }

    public GameBoard addLine(String id, String text) {
        return addLine(id, text, false);
    }

    public GameBoard addLine(String id, String text, boolean update) {
        Team newLabel = scoreboard.registerNewTeam(id);
        ChatColor color = getAvailableColor();
        newLabel.addEntry(color.toString());
        newLabel.setPrefix(Format.format(text));
        int line = currentLine--;
        objective.getScore(color.toString()).setScore(line);
        lines.add(new GameBoardLine(id, line, scoreboard, newLabel, color, text, update));
        return this;
    }

    public GameBoard addBlankLine() {
        addLine(UUID.randomUUID().toString(), "");
        return this;
    }

    public GameBoardLine getLine(int line) {
        return lines.stream().filter(l -> l.getPosition() == line).findFirst().orElse(null);
    }

    public GameBoardLine getLine(String id) {
        return lines.stream().filter(l -> l.getId().equalsIgnoreCase(id)).findFirst().orElse(null);
    }

    public GameBoard removeLine(int line) {
        GameBoardLine gameBoardLine = getLine(line);
        gameBoardLine.getTeam().unregister();
        scoreboard.resetScores(gameBoardLine.getColor().toString());
        currentLine++;
        return this;
    }

    private ChatColor getAvailableColor() {
        for (ChatColor color : ChatColor.values()) {
            if (lines.stream().anyMatch(line -> line.getColor() == color)) {
                continue;
            }

            return color;
        }

        int R = (int) (Math.random() * 256);
        int G = (int) (Math.random() * 256);
        int B = (int) (Math.random() * 256);

        return ChatColor.of(new Color(R, G, B));
    }

    public Scoreboard getScoreboard() {
        return scoreboard;
    }
}
