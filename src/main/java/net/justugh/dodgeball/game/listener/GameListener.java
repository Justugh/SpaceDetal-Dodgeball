package net.justugh.dodgeball.game.listener;

import net.justugh.dodgeball.Dodgeball;
import net.justugh.dodgeball.game.DodgeballGame;
import net.justugh.dodgeball.game.GameLineData;
import net.justugh.dodgeball.game.GamePlayer;
import net.justugh.dodgeball.game.GameState;
import net.justugh.dodgeball.util.Format;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Snowball;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.*;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.UUID;

public class GameListener implements Listener {

    private final DodgeballGame game;

    public GameListener(DodgeballGame game) {
        this.game = game;
    }

    @EventHandler
    public void onPvP(EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof Player) || !(event.getDamager() instanceof Player)) {
            return;
        }

        if (!game.isPlaying((Player) event.getEntity()) || !game.isPlaying((Player) event.getDamager())) {
            return;
        }

        event.setCancelled(true);
    }

    @EventHandler
    public void onHungerLoss(FoodLevelChangeEvent event) {
        if (!game.isPlaying((Player) event.getEntity())) {
            return;
        }

        event.setFoodLevel(20);
        event.setCancelled(true);
    }

    @EventHandler
    public void onDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player player)) {
            return;
        }

        if (!game.isPlaying(player)) {
            return;
        }

        if (event.getCause() == EntityDamageEvent.DamageCause.VOID) {
            GamePlayer gamePlayer = game.getPlayer(player.getUniqueId());
            gamePlayer.setAlive(false);
            player.setGameMode(GameMode.SPECTATOR);
            player.teleport(game.getArena().getSpectatorSpawn());

            if (!player.getInventory().contains(Material.SNOWBALL)) {
                return;
            }

            ItemStack ball = new ItemStack(Material.SNOWBALL);
            ItemMeta meta = ball.getItemMeta();
            meta.setDisplayName(Format.format("&cDodge Ball"));
            meta.getPersistentDataContainer().set(new NamespacedKey(Dodgeball.getInstance(), "antistack"), PersistentDataType.STRING, UUID.randomUUID().toString());
            ball.setItemMeta(meta);
            event.getEntity().getWorld().dropItemNaturally(game.getArena().getSpectatorSpawn(), ball);
            return;
        }

        event.setCancelled(true);
    }

    @EventHandler
    public void onPickup(EntityPickupItemEvent event) {
        if (event.getEntity().getType() != EntityType.PLAYER) {
            return;
        }

        if (!game.isPlaying((Player) event.getEntity())) {
            return;
        }

        if (((Player) event.getEntity()).getInventory().contains(Material.SNOWBALL)) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        if (!game.isPlaying(event.getPlayer()) || game.getTimer().getState() != GameState.IN_GAME) {
            return;
        }

        Location to = event.getTo();

        if (to.getBlockY() < game.getArena().getCustomData("MINIMUM_Y").getLocation().getY()) {
            Player player = event.getPlayer();
            GamePlayer gamePlayer = game.getPlayer(player.getUniqueId());
            gamePlayer.setAlive(false);
            player.setGameMode(GameMode.SPECTATOR);
            player.teleport(game.getArena().getSpectatorSpawn());

            if (!player.getInventory().contains(Material.SNOWBALL)) {
                return;
            }

            ItemStack ball = new ItemStack(Material.SNOWBALL);
            ItemMeta meta = ball.getItemMeta();
            meta.setDisplayName(Format.format("&cDodge Ball"));
            meta.getPersistentDataContainer().set(new NamespacedKey(Dodgeball.getInstance(), "antistack"), PersistentDataType.STRING, UUID.randomUUID().toString());
            ball.setItemMeta(meta);
            event.getPlayer().getWorld().dropItemNaturally(game.getArena().getSpectatorSpawn(), ball);
            return;
        }

        GamePlayer gamePlayer = game.getPlayer(event.getPlayer().getUniqueId());
        GameLineData lineData = game.getLineData().get(gamePlayer.getTeam());

        switch (lineData.getDirection()) {
            case POSITIVE:
                if (to.getZ() > lineData.getMaxZ()) {
                    event.setTo(event.getFrom());
                }
                break;
            case NEGATIVE:
                if (to.getZ() < lineData.getMaxZ()) {
                    event.setTo(event.getFrom());
                }
                break;
            default:
                break;
        }
    }

    @EventHandler
    public void onProjectileHit(ProjectileHitEvent event) {
        if (!(event.getEntity() instanceof Snowball)) {
            return;
        }

        if (event.getHitEntity() == null && event.getHitBlock() != null) {
            ItemStack ball = new ItemStack(Material.SNOWBALL);
            ItemMeta meta = ball.getItemMeta();
            meta.setDisplayName(Format.format("&cDodge Ball"));
            meta.getPersistentDataContainer().set(new NamespacedKey(Dodgeball.getInstance(), "antistack"), PersistentDataType.STRING, UUID.randomUUID().toString());
            ball.setItemMeta(meta);
            event.getEntity().getWorld().dropItemNaturally(event.getHitBlock().getLocation().add(0, 1, 0), ball);
            return;
        }

        if (event.getHitEntity() == null || !(event.getHitEntity() instanceof Player player)) {
            return;
        }

        if (!game.isPlaying(player)) {
            return;
        }

        if (event.getEntity().getShooter() instanceof Player shooter) {
            GamePlayer shooterPlayer = game.getPlayer(shooter.getUniqueId());

            if (game.getPlayer(player.getUniqueId()).getTeam() == shooterPlayer.getTeam()) {
                return;
            }

            shooterPlayer.setKills(shooterPlayer.getKills() + 1);
            game.broadcastMessage(Format.format("&a%s &7was knocked out by &a%s", player.getName(), shooter.getName()));
        } else {
            game.broadcastMessage(Format.format("&a%s &7was knocked out", player.getName()));
        }

        GamePlayer gamePlayer = game.getPlayer(player.getUniqueId());
        gamePlayer.setAlive(false);
        player.setGameMode(GameMode.SPECTATOR);
        player.teleport(game.getArena().getSpectatorSpawn());

        ItemStack ball = new ItemStack(Material.SNOWBALL);

        if (player.getInventory().contains(Material.SNOWBALL)) {
            ball.setAmount(ball.getAmount() + 1);
        }

        ItemMeta meta = ball.getItemMeta();
        meta.setDisplayName(Format.format("&cDodge Ball"));
        meta.getPersistentDataContainer().set(new NamespacedKey(Dodgeball.getInstance(), "antistack"), PersistentDataType.STRING, UUID.randomUUID().toString());
        ball.setItemMeta(meta);
        event.getEntity().getWorld().dropItemNaturally(game.getArena().getSpectatorSpawn(), ball);
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        if (!game.isPlaying(event.getPlayer())) {
            return;
        }

        event.setCancelled(true);
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        if (!game.isPlaying(event.getPlayer())) {
            return;
        }

        event.setCancelled(true);
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        if (!game.isPlaying(event.getPlayer())) {
            return;
        }

        if (!event.getAction().equals(Action.RIGHT_CLICK_BLOCK) || !event.getAction().equals(Action.LEFT_CLICK_BLOCK)) {
            return;
        }

        event.setCancelled(true);
    }

    @EventHandler
    public void onDropItem(PlayerDropItemEvent event) {
        if (!game.isPlaying(event.getPlayer())) {
            return;
        }

        event.setCancelled(true);
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        if (!game.isPlaying(event.getPlayer())) {
            return;
        }

        game.getPlayers().remove(event.getPlayer().getUniqueId());
        game.broadcastMessage(Format.format("&c%s logged out.", event.getPlayer().getName()));
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent event) {
        if (!game.isPlaying(event.getEntity())) {
            return;
        }

        event.getDrops().clear();
        event.setDroppedExp(0);
        event.setDeathMessage("");
    }

}
