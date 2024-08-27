package me.smerrybeta.util;

import me.smerrybeta.VotePlugin;

public class CreateFileConfig {
    private final Configuration config;
    private final Configuration parentVote;
    private final Configuration childVote;
    private final Configuration votePrice;
    private CreateFileConfig createFileConfig;

    public CreateFileConfig (VotePlugin plugin) {
        this.config = new Configuration(plugin, "customConfig.yml"); // customer 的 yml
        this.parentVote = new Configuration(plugin, "parentVote.yml");
        this.childVote = new Configuration(plugin, "childVote.yml");
        this.votePrice = new Configuration(plugin, "votePrice.yml");
        load();
    }

    public Configuration getConfig () {
        return config;
    }
    public Configuration getVotePrice () {
        return votePrice;
    }
    public Configuration getParentVote () {
        return parentVote;
    }

    public Configuration getChildVote () {
        return childVote;
    }

    public void load () {
        this.config.reloadCustomConfig();
        this.parentVote.reloadCustomConfig();
        this.childVote.reloadCustomConfig();
        this.votePrice.reloadCustomConfig();

        votePrice.copyDefaults();
        config.copyDefaults();
        parentVote.copyDefaults();
        childVote.copyDefaults();
    }

    public void reload () {
        load();
    }
}
