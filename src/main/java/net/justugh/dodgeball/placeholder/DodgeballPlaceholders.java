package net.justugh.dodgeball.placeholder;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import net.justugh.dodgeball.Dodgeball;
import net.justugh.dodgeball.game.DodgeballGame;
import net.justugh.dodgeball.game.GameTeam;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class DodgeballPlaceholders extends PlaceholderExpansion {

    @Override
    public @NotNull String getIdentifier() {
        return "Dodgeball";
    }

    @Override
    public @NotNull String getAuthor() {
        return "Justugh";
    }

    @Override
    public @NotNull String getVersion() {
        return "2023.4";
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public @Nullable String onPlaceholderRequest(Player player, @NotNull String arg) {
        DodgeballGame game = Dodgeball.getInstance().getGameManager().getGame(player.getUniqueId());

        if (game == null) {
            return "N/A";
        }

        if (arg.equalsIgnoreCase("red_players")) {
            return game.getAlivePlayers(GameTeam.RED) + "";
        }

        if (arg.equalsIgnoreCase("blue_players")) {
            return game.getAlivePlayers(GameTeam.BLUE) + "";
        }

        return "N/A";
    }
}
