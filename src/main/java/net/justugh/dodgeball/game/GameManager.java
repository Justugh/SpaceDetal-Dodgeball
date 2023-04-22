package net.justugh.dodgeball.game;

import net.justugh.dodgeball.Dodgeball;
import net.justugh.dodgeball.arena.data.Arena;
import net.justugh.dodgeball.util.Format;
import net.justugh.dodgeball.util.PlayerUtil;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.DisplaySlot;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class GameManager {

    private final Dodgeball plugin;
    private final List<DodgeballGame> activeGames = new ArrayList<>();

    public GameManager(Dodgeball plugin) {
        this.plugin = plugin;

        plugin.getServer().getScheduler().runTaskTimer(plugin, () -> {
            if (activeGames.isEmpty()) {
                generateNewGame();
            }
        }, 0, 20);
    }

    public void generateNewGame() {
        Arena arena = plugin.getArenaManager().getRandomArena().clone();

        if (arena == null) {
            return;
        }

        arena.generateArena(plugin.getArenaManager().getNextViableArenaLocation());

        DodgeballGame game = new DodgeballGame(UUID.randomUUID().toString(), arena);
        Bukkit.getServer().getPluginManager().registerEvents(game.getListener(), plugin);
        activeGames.add(game);
    }

    public DodgeballGame getGame(UUID player) {
        return activeGames.stream().filter(g -> g.getPlayer(player) != null).findFirst().orElse(null);
    }

    public DodgeballGame getOpenGame() {
        return activeGames.stream().filter(game -> game.getTimer().getState() == GameState.LOBBY && !game.isFull()).findFirst().orElse(null);
    }

    public void joinGame(DodgeballGame game, Player player) {
        if (activeGames.stream().anyMatch(gp -> game.getPlayer(player.getUniqueId()) != null)) {
            player.sendMessage(Format.format("&cYou're already in a game."));
            return;
        }

        if (game.isFull()) {
            player.sendMessage(Format.format("&cThat game is full!"));
            return;
        }

        if (game.getTimer().getState() != GameState.LOBBY) {
            player.sendMessage(Format.format("&cThe game you tried to join is already in progress."));
            return;
        }

        GamePlayer gamePlayer = new GamePlayer(player.getUniqueId());
        gamePlayer.setTeam(game.getAlivePlayers(GameTeam.RED).size() > game.getAlivePlayers(GameTeam.BLUE).size() ? GameTeam.BLUE : GameTeam.RED);

        game.getPlayers().put(player.getUniqueId(), gamePlayer);
        game.applyLobbyBoard(player);

        // Apply game settings
        player.setGameMode(GameMode.SURVIVAL);
        PlayerUtil.reset(player);

        player.teleport(game.getArena().getLobbySpawn());

        game.sendFormattedDescription(player);
        game.broadcastMessage(Format.format("&a%s &7joined the game. &8(&a%s&8/&a20&8)", player.getName(), game.getPlayers().size()));
    }

    public void quitGame(DodgeballGame game, Player player) {
        if (!game.getPlayers().containsKey(player.getUniqueId())) {
            player.sendMessage(Format.format("&cYou're not in this game."));
            return;
        }

        PlayerUtil.reset(player);
        game.getPlayers().remove(player.getUniqueId());
        player.setGameMode(GameMode.SURVIVAL);
        player.teleport(Bukkit.getWorlds().get(0).getSpawnLocation());
        player.getScoreboard().clearSlot(DisplaySlot.SIDEBAR);

        if (game.getTimer().getState() != GameState.LOBBY) {
            return;
        }

        game.broadcastMessage(Format.format("&a%s &7left the game. &8(&a%s&8/&a20&8)", player.getName(), game.getPlayers().size()));
    }

    public List<DodgeballGame> getActiveGames() {
        return activeGames;
    }
}
