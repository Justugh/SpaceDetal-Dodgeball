package net.justugh.dodgeball.util;

import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class PlayerUtil {

    public static void clearPotionEffects(Player player) {
        player.getActivePotionEffects().forEach(potionEffect -> player.removePotionEffect(potionEffect.getType()));
    }

    public static void resetHealth(Player player) {
        player.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(20);
        player.setHealth(20);
    }

    public static void clearArmor(Player player) {
        player.getInventory().setArmorContents(new ItemStack[]{new ItemStack(Material.AIR), new ItemStack(Material.AIR),
                new ItemStack(Material.AIR), new ItemStack(Material.AIR)});
    }

    public static void reset(Player player) {
        clearArmor(player);
        clearPotionEffects(player);
        resetHealth(player);

        player.getInventory().clear();
        player.setFireTicks(0);
        player.setFoodLevel(20);
        player.setSaturation(20);
        player.resetPlayerTime();
        player.resetPlayerWeather();
        player.setTotalExperience(0);
        player.setArrowsInBody(0);
        player.setMaximumNoDamageTicks(20);
        player.setAllowFlight(player.getGameMode() == GameMode.CREATIVE || player.getGameMode() == GameMode.SPECTATOR);
        player.setFlying(player.isFlying() && (player.getGameMode() == GameMode.CREATIVE || player.getGameMode() == GameMode.SPECTATOR));
    }

}
