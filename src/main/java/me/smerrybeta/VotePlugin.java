package me.smerrybeta;

import me.smerrybeta.command.Command;
import me.smerrybeta.command.TabCommand;
import me.smerrybeta.perferm.VotingListener;
import me.smerrybeta.perferm.VotePriceListener;
import me.smerrybeta.runnable.TimeCheck;
import me.smerrybeta.util.CreateFileConfig;
import me.smerrybeta.util.Functions;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Objects;

public class VotePlugin extends JavaPlugin {
    public static VotePlugin plugin;
    private CreateFileConfig createFileConfig;
    @Override
    public void onEnable () {
        plugin = this;
        this.createFileConfig = new CreateFileConfig(this);
        Bukkit.getServer().getConsoleSender().sendMessage("§bVote-Plugin >> §aPLUGIN has been Started §d(Coded By SmerryBeta)");

        Objects.requireNonNull(getCommand("Vote")).setExecutor(new Command());
        Objects.requireNonNull(getCommand("Vote")).setTabCompleter(new TabCommand());

        Bukkit.getPluginManager().registerEvents(new VotingListener(), this);
        Bukkit.getPluginManager().registerEvents(new VotePriceListener(), this);

        new TimeCheck().runTaskTimer(this, 0, 60 * 20L);

        Functions.reloadVote();
    }
    @Override
    public void onDisable () {
        Bukkit.getServer().getConsoleSender().sendMessage("§bVote-Plugin >> §aPLUGIN has been Closed §d(Coded By SmerryBeta)");
    }

    public CreateFileConfig getResource () {
        return createFileConfig;
    }
}
