package net.justugh.dodgeball.game;

import net.justugh.dodgeball.Dodgeball;
import net.justugh.dodgeball.arena.data.Arena;
import net.justugh.dodgeball.arena.data.ArenaSignData;
import net.justugh.dodgeball.game.listener.GameListener;
import net.justugh.dodgeball.game.timer.GameTimer;
import net.justugh.dodgeball.util.Format;
import net.justugh.dodgeball.util.scoreboard.GameBoard;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

public class DodgeballGame {

    private Arena arena;

    private final String ID;

    private final GameTimer timer = new GameTimer(this);
    private final GameListener listener = new GameListener(this);

    private HashMap<UUID, GamePlayer> players = new HashMap<>();
    private GameTeam winningTeam;

    private HashMap<GameTeam, List<Location>> teamSpawns = new HashMap<>();
    private HashMap<GameTeam, GameLineData> lineData = new HashMap<>();

    public DodgeballGame(String ID, Arena arena) {
        this.ID = ID;
        this.arena = arena;

        ArenaSignData redData = arena.getCustomData("RED_MAX_Z");
        GameLineData.MovementDirection redDirection = GameLineData.MovementDirection.valueOf(redData.getLine(1));
        lineData.put(GameTeam.RED, new GameLineData(redData.getLocation().getBlockZ(), redDirection));

        List<Location> redSpawns = new ArrayList<>();
        arena.getSpawns().stream().filter(sp -> redDirection == GameLineData.MovementDirection.POSITIVE ? sp.getBlockZ() < redData.getLocation().getBlockZ()
                : sp.getBlockZ() > redData.getLocation().getBlockZ()).forEach(redSpawns::add);

        teamSpawns.put(GameTeam.RED, redSpawns);

        ArenaSignData blueData = arena.getCustomData("BLUE_MAX_Z");
        GameLineData.MovementDirection blueDirection = GameLineData.MovementDirection.valueOf(blueData.getLine(1));
        lineData.put(GameTeam.BLUE, new GameLineData(blueData.getLocation().getBlockZ(), blueDirection));

        List<Location> blueSpawns = new ArrayList<>();
        arena.getSpawns().stream().filter(sp -> blueDirection == GameLineData.MovementDirection.POSITIVE ? sp.getBlockZ() < blueData.getLocation().getBlockZ()
                : sp.getBlockZ() > blueData.getLocation().getBlockZ()).forEach(blueSpawns::add);

        teamSpawns.put(GameTeam.BLUE, blueSpawns);

        timer.runTaskTimer(Dodgeball.getInstance(), 0, 1);
    }

    public Location getRandomTeamSpawn(GameTeam team) {
        return teamSpawns.get(team).get(ThreadLocalRandom.current().nextInt(teamSpawns.get(team).size() - 1));
    }

    public void unregister() {
        HandlerList.unregisterAll(listener);
    }

    public void applyLobbyBoard(Player player) {
        player.setScoreboard(new GameBoard(
                Format.format("        &c&lDODGEBALL        "))
                .addBlankLine()
                .addLine("players", Format.format("&8» &a&lPlayers"))
                .addLine("alive", getPlayers().keySet().size() + "/20")
                .addBlankLine()
                .addLine("arena-label", Format.format("&8» &a&lArena"))
                .addLine("arena", arena.getName())
                .addBlankLine()
                .addLine("ad", "&7play.dodgeball.net").getScoreboard());
    }

    public void applyGameBoard(Player player) {
        player.setScoreboard(new GameBoard(
                Format.format("        &c&lDODGEBALL        "))
                .addBlankLine()
                .addLine("players", Format.format("&8» &a&lAlive Players"))
                .addLine("alive", Format.format("&c" + getAlivePlayers(GameTeam.RED).size() + " &8| &9" + getAlivePlayers(GameTeam.BLUE).size()))
                .addBlankLine()
                .addLine("kills-label", Format.format("&8» &a&lKills"))
                .addLine("kills", "0")
                .addBlankLine()
                .addLine("ad", "&7play.dodgeball.net").getScoreboard());
    }

    public void applyEndBoard(Player player) {
        player.setScoreboard(new GameBoard(
                Format.format("        &c&lDODGEBALL        "))
                .addBlankLine()
                .addLine("winner-label", Format.format("&8» &a&lWINNER"))
                .addLine("winner", winningTeam == null ? "&f&lTIE" : winningTeam.getColor().toString() + winningTeam.name())
                .addBlankLine()
                .addLine("ad", "&7play.dodgeball.net").getScoreboard());
    }

    public GamePlayer getPlayer(UUID uuid) {
        return players.get(uuid);
    }

    public boolean isPlaying(Player p) {
        return players.containsKey(p.getUniqueId());
    }

    public void broadcastMessage(String message) {
        for (UUID uuid : getPlayers().keySet()) {
            Player player = Bukkit.getPlayer(uuid);

            if (player == null) {
                continue;
            }

            player.sendMessage(Format.format(message));
        }
    }

    public void broadcastTitle(String title, String subtitle, int seen) {
        for (UUID uuid : getPlayers().keySet()) {
            Player player = Bukkit.getPlayer(uuid);

            if (player == null) {
                continue;
            }

            player.sendTitle(Format.format(title), Format.format(subtitle), 20, seen, 20);
        }
    }

    public void broadcastActionBar(String message) {
        for (UUID uuid : getPlayers().keySet()) {
            Player player = Bukkit.getPlayer(uuid);

            if (player == null) {
                continue;
            }

            player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(Format.format(message)));
        }
    }

    public void broadcastSound(Sound sound) {
        for (UUID uuid : getPlayers().keySet()) {
            Player player = Bukkit.getPlayer(uuid);

            if (player == null) {
                continue;
            }

            player.playSound(player.getLocation(), sound, 1, 1);
        }
    }

    public void sendFormattedDescription(Player player) {
        player.sendMessage(
                Format.format("&8&l&m------------------------------"),
                Format.format("&c&lDodgeball"),
                " ",
                Format.format("&fDodgeball is a simple game of dodging!"),
                Format.format("&fThe goal is to eliminate the enemy team"),
                Format.format("&fby hitting them with the balls!"),
                " ",
                Format.format("&a&lArena &8• &f%s", arena.getName()),
                Format.format("&a&lArena Author(s) &8• &f%s", arena.getAuthor()),
                Format.format("&8&l&m------------------------------")
        );
    }

    public void sendFormattedEnd(Player player) {
        player.sendMessage(
                Format.format("&8&l&m------------------------------"),
                Format.format("&a&lWINNER &8- " + (winningTeam == null ? "&f&lTIE" : winningTeam.getColor().toString() + winningTeam.name())),
                Format.format("&8&l&m------------------------------")
        );
    }

    public boolean isFull() {
        return players.size() >= 20;
    }

    public int getAliveAmount() {
        return (int) getPlayers().values().stream().filter(GamePlayer::isAlive).count();
    }

    public List<GamePlayer> getAlivePlayers() {
        return players.values().stream().filter(GamePlayer::isAlive).collect(Collectors.toList());
    }

    public List<GamePlayer> getAlivePlayers(GameTeam team) {
        return players.values().stream().filter(gp -> gp.isAlive() && gp.getTeam() == team).collect(Collectors.toList());
    }

    public List<Player> getSpigotPlayers() {
        List<Player> players = new ArrayList<>();
        this.players.values().stream().filter(gamePlayer -> Bukkit.getPlayer(gamePlayer.getUuid()) != null).forEach(gamePlayer -> players.add(Bukkit.getPlayer(gamePlayer.getUuid())));
        return players;
    }

    public HashMap<UUID, GamePlayer> getPlayers() {
        return players;
    }

    public GameTeam getWinningTeam() {
        return winningTeam;
    }

    public void setWinningTeam(GameTeam winningTeam) {
        this.winningTeam = winningTeam;
    }

    public GameTimer getTimer() {
        return timer;
    }

    public Arena getArena() {
        return arena;
    }

    public GameListener getListener() {
        return listener;
    }

    public HashMap<GameTeam, GameLineData> getLineData() {
        return lineData;
    }
}
