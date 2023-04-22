package net.justugh.dodgeball;

import net.justugh.dodgeball.arena.ArenaManager;
import net.justugh.dodgeball.commands.JoinCommand;
import net.justugh.dodgeball.game.GameManager;
import net.justugh.dodgeball.placeholder.DodgeballPlaceholders;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;

public class Dodgeball extends JavaPlugin {

    private static Dodgeball instance;

    private File arenasFile;
    private YamlConfiguration arenaConfig;

    private ArenaManager arenaManager;
    private GameManager gameManager;

    @Override
    public void onEnable() {
        instance = this;

        // Save configuration files/schematics
        saveDefaultConfig();

        arenasFile = new File(getDataFolder(), "arenas.yml");
        arenaConfig = YamlConfiguration.loadConfiguration(arenasFile);

        arenaManager = new ArenaManager(this);
        gameManager = new GameManager(this);

        new DodgeballPlaceholders().register();

        getCommand("join").setExecutor(new JoinCommand(this));
    }

    @Override
    public void onDisable() {

    }

    public static Dodgeball getInstance() {
        return instance;
    }

    public YamlConfiguration getArenaConfig() {
        return arenaConfig;
    }

    public GameManager getGameManager() {
        return gameManager;
    }

    public ArenaManager getArenaManager() {
        return arenaManager;
    }
}
