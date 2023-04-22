package net.justugh.dodgeball.game;

import org.bukkit.ChatColor;

public enum GameTeam {

    RED(ChatColor.RED),
    BLUE(ChatColor.BLUE);

    ChatColor color;

    GameTeam(ChatColor color) {
        this.color = color;
    }

    public ChatColor getColor() {
        return color;
    }
}
