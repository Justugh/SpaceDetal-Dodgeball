package net.justugh.dodgeball.game.timer;

import net.justugh.dodgeball.Dodgeball;
import net.justugh.dodgeball.game.DodgeballGame;
import net.justugh.dodgeball.game.GameState;
import net.justugh.dodgeball.game.GameTeam;
import net.justugh.dodgeball.util.Format;
import net.justugh.dodgeball.util.PlayerUtil;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.DisplaySlot;

import java.util.UUID;

public class GameTimer extends BukkitRunnable {

    private final DodgeballGame game;
    private final GameTimerSettings settings = new GameTimerSettings();

    private GameState state = GameState.LOBBY;
    private long lastTick = System.currentTimeMillis() + 1000;

    public GameTimer(DodgeballGame game) {
        this.game = game;
    }

    @Override
    public void run() {
        switch (state) {
            case LOBBY:
                for (UUID uuid : game.getPlayers().keySet()) {
                    Player player = Bukkit.getPlayer(uuid);

                    if (player == null) {
                        continue;
                    }

                    player.getScoreboard().getTeam("alive").setPrefix(game.getPlayers().keySet().size() + "/20");
                }
                break;
            case IN_GAME:
                for (UUID uuid : game.getPlayers().keySet()) {
                    Player player = Bukkit.getPlayer(uuid);

                    if (player == null) {
                        continue;
                    }

                    player.getScoreboard().getTeam("kills").setPrefix(game.getPlayer(player.getUniqueId()).getKills() + "");
                    player.getScoreboard().getTeam("alive").setPrefix(Format.format("&c"
                            + game.getAlivePlayers(GameTeam.RED).size()
                            + " &8| &9"
                            + game.getAlivePlayers(GameTeam.BLUE).size()));
                }
                break;
            default:
                break;
        }

        if (settings.isPaused()) {
            if (game.getPlayers().isEmpty()) {
                return;
            }

            if (game.getPlayers().keySet().size() >= 2) {
                settings.setPaused(false);
                game.broadcastMessage(Format.format("&aThe game is starting!"));
            }
        }

        if (settings.isPaused()) {
            return;
        }

        if (state == GameState.LOBBY) {
            if (game.getPlayers().keySet().size() < 2) {
                settings.setPaused(true);
                settings.setLobbyTime(60);
                game.broadcastMessage(Format.format("&cWaiting for more players..."));
                return;
            }
        }

        // Ensure a second has passed.
        if (lastTick - System.currentTimeMillis() > 0) {
            return;
        }

        lastTick = System.currentTimeMillis() + 1000;

        switch (state) {
            case LOBBY:
                for (UUID uuid : game.getPlayers().keySet()) {
                    Player player = Bukkit.getPlayer(uuid);

                    if (player == null) {
                        continue;
                    }

                    player.getScoreboard().getTeam("alive").setPrefix(game.getPlayers().keySet().size() + "/20");
                }

                if (settings.getLobbyTime() <= 0) {
                    game.broadcastSound(Sound.ENTITY_PLAYER_LEVELUP);

                    for (UUID uuid : game.getPlayers().keySet()) {
                        Player player = Bukkit.getPlayer(uuid);

                        if (player == null) {
                            continue;
                        }

                        PlayerUtil.reset(player);
                        player.setGameMode(GameMode.SURVIVAL);
                        game.sendFormattedDescription(player);
                        game.applyGameBoard(player);

                        player.teleport(game.getRandomTeamSpawn(game.getPlayer(player.getUniqueId()).getTeam()));
                        player.getInventory().setHeldItemSlot(4);
                    }

                    state = GameState.IN_GAME;


                    ItemStack ball = new ItemStack(Material.SNOWBALL);
                    ItemMeta meta = ball.getItemMeta();
                    meta.setDisplayName(Format.format("&cDodge Ball"));
                    meta.getPersistentDataContainer().set(new NamespacedKey(Dodgeball.getInstance(), "antistack"), PersistentDataType.STRING, UUID.randomUUID().toString());
                    ball.setItemMeta(meta);

                    ItemStack ballBlue = new ItemStack(Material.SNOWBALL);
                    ItemMeta blueMeta = ball.getItemMeta();
                    blueMeta.setDisplayName(Format.format("&cDodge Ball"));
                    blueMeta.getPersistentDataContainer().set(new NamespacedKey(Dodgeball.getInstance(), "antistack"), PersistentDataType.STRING, UUID.randomUUID().toString());
                    ballBlue.setItemMeta(blueMeta);

                    game.getAlivePlayers(GameTeam.RED).get(0).getPlayer().getInventory().setItem(4, ball);
                    game.getAlivePlayers(GameTeam.BLUE).get(0).getPlayer().getInventory().setItem(4, ballBlue);
                    return;
                }

                if (settings.getLobbyTime() > 0) {
                    game.broadcastActionBar(Format.format("Starting In&8: &c" + settings.getLobbyTime()));
                    settings.setLobbyTime(settings.getLobbyTime() - 1);

                    if (settings.getLobbyTime() <= 10) {
                        game.broadcastSound(Sound.BLOCK_STONE_BUTTON_CLICK_ON);
                    }
                }

                break;
            case IN_GAME:
                if (game.getAliveAmount() == 0) {
                    Dodgeball.getInstance().getConfig().getStringList("Victory-Commands").forEach(cmd -> Bukkit.dispatchCommand(Bukkit.getConsoleSender(), cmd.replace("%team%", "TIE")));
                    game.broadcastSound(Sound.ENTITY_FIREWORK_ROCKET_BLAST);
                    game.broadcastTitle("&a&lGAME OVER", "&f&lTIE!", 5);
                    state = GameState.END;
                    game.getSpigotPlayers().forEach(game::sendFormattedEnd);
                    game.getSpigotPlayers().forEach(game::applyEndBoard);
                }

                if (game.getAliveAmount() == 1) {
                    GameTeam winningTeam = game.getAlivePlayers().get(0).getTeam();
                    Dodgeball.getInstance().getConfig().getStringList("Victory-Commands").forEach(cmd -> Bukkit.dispatchCommand(Bukkit.getConsoleSender(), cmd.replace("%team%", winningTeam.name())));

                    game.getPlayers().values().stream().filter(gp -> gp.getTeam() == winningTeam).forEach(p -> {
                        Dodgeball.getInstance().getConfig().getStringList("Victory-Player-Commands")
                                .forEach(cmd -> Bukkit.dispatchCommand(Bukkit.getConsoleSender(),
                                        cmd.replace("%team%", winningTeam.name()).replace("%player%", p.getPlayer().getName())));
                    });

                    game.setWinningTeam(winningTeam);
                    game.broadcastSound(Sound.ENTITY_FIREWORK_ROCKET_BLAST);
                    game.broadcastTitle("&a&lGAME OVER", winningTeam.getColor() + winningTeam.name() + " &aWON!", 5);
                    state = GameState.END;
                    game.getSpigotPlayers().forEach(game::sendFormattedEnd);
                    game.getSpigotPlayers().forEach(game::applyEndBoard);
                }

                if (settings.getGameTime() > 0) {
                    settings.setGameTime(settings.getGameTime() - 1);
                } else {
                    state = GameState.END;
                    game.getSpigotPlayers().forEach(game::sendFormattedEnd);
                    game.getSpigotPlayers().forEach(game::applyEndBoard);
                }

                break;
            case END:
                if (settings.getEndTime() > 0) {
                    game.broadcastActionBar(Format.format("Ending In&8: &a%s", settings.getEndTime()));

                    if (settings.getEndTime() <= 5) {
                        game.broadcastSound(Sound.BLOCK_STONE_BUTTON_CLICK_ON);
                    }

                    settings.setEndTime(settings.getEndTime() - 1);
                    return;
                }

                for (UUID uuid : game.getPlayers().keySet()) {
                    Player player = Bukkit.getPlayer(uuid);

                    if (player == null) {
                        continue;
                    }

                    player.setGameMode(GameMode.SURVIVAL);
                    PlayerUtil.reset(player);
                    player.getScoreboard().clearSlot(DisplaySlot.SIDEBAR);
                    player.teleport(Bukkit.getWorlds().get(0).getSpawnLocation());
                }

                game.unregister();
                Dodgeball.getInstance().getGameManager().getActiveGames().remove(game);
                cancel();
                break;
            default:
                break;
        }
    }

    public GameState getState() {
        return state;
    }
}
