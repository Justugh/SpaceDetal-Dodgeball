package net.justugh.dodgeball.commands;

import net.justugh.dodgeball.Dodgeball;
import net.justugh.dodgeball.game.DodgeballGame;
import net.justugh.dodgeball.util.Format;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class JoinCommand implements CommandExecutor {

    private final Dodgeball plugin;

    public JoinCommand(Dodgeball plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(Format.format("&cYou must be a player!"));
            return true;
        }

        DodgeballGame game = plugin.getGameManager().getOpenGame();

        if (game == null) {
            sender.sendMessage(Format.format("&cUnable to find an open game!"));
            return true;
        }

        sender.sendMessage(Format.format("&aJoining game..."));
        plugin.getGameManager().joinGame(game, (Player) sender);
        return true;
    }

}
