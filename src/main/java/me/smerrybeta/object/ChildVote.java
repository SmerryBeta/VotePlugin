package me.smerrybeta.object;

import me.smerrybeta.VotePlugin;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ChildVote {
    private final int id;
    private String title;
    private String description;
    private Set<OfflinePlayer> agreePlayers = new HashSet<>();
    private ParentVote parentVote;
    private ItemStack nameTag;

    private String command;

    // 构造函数1：带有 agreePlayers 参数
    public ChildVote (int id, String title, String description, Set<OfflinePlayer> agreePlayers, ParentVote parentVote, ItemStack nameTag, String command, boolean YmlReload) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.agreePlayers = agreePlayers;
        this.parentVote = parentVote;
        this.nameTag = nameTag;
        this.command = command;

        parentVote.addChildVotes(this);

        if (YmlReload) {// 设置 YML 配置
            VotePlugin.plugin.getResource().getChildVote().set("CV." + this.id + ".Title", title);
            VotePlugin.plugin.getResource().getChildVote().set("CV." + this.id + ".Description", description);
            List<String> UIDs = new ArrayList<>();
            for (OfflinePlayer player : agreePlayers)
                UIDs.add(String.valueOf(player.getUniqueId()));
            VotePlugin.plugin.getResource().getChildVote().set("CV." + this.id + ".AgreePlayers", UIDs);
            VotePlugin.plugin.getResource().getChildVote().set("CV." + this.id + ".PVId", parentVote.getId());
            VotePlugin.plugin.getResource().getChildVote().set("CV." + this.id + ".NameTag", nameTag);
            VotePlugin.plugin.getResource().getChildVote().set("CV." + this.id + ".Command", command);

            VotePlugin.plugin.getResource().getChildVote().save();
        }
    }

    // 构造函数2：不带 agreePlayers 参数
    public ChildVote (int id, String title, String description, ParentVote parentVote) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.parentVote = parentVote;

        // 设置 YML 配置
        VotePlugin.plugin.getResource().getChildVote().set("CV." + this.id + ".Title", title);
        VotePlugin.plugin.getResource().getChildVote().set("CV." + this.id + ".Description", description);
        VotePlugin.plugin.getResource().getChildVote().set("CV." + this.id + ".PVId", parentVote.getId());
        VotePlugin.plugin.getResource().getChildVote().set("CV." + this.id + ".AgreePlayers", new ArrayList<>());

        VotePlugin.plugin.getResource().getChildVote().save();
    }

    @Override
    public boolean equals (Object o) {
        if (this == o) return true;
        if (! (o instanceof ChildVote childVote)) return false;

        if (getId() != childVote.getId()) return false;
        if (getTitle() != null ? ! getTitle().equals(childVote.getTitle()) : childVote.getTitle() != null) return false;
        if (getDescription() != null ? ! getDescription().equals(childVote.getDescription()) : childVote.getDescription() != null)
            return false;
        if (getAgreePlayers() != null ? ! getAgreePlayers().equals(childVote.getAgreePlayers()) : childVote.getAgreePlayers() != null)
            return false;
        return getParentVote() != null ? getParentVote().equals(childVote.getParentVote()) : childVote.getParentVote() == null;
    }

    // set 方法
    public void setTitle (String title) {
        VotePlugin.plugin.getResource().getChildVote().set("CV." + this.id + ".Title", title);
        VotePlugin.plugin.getResource().getChildVote().save();
        this.title = title;
    }

    public void setDescription (String description) {
        VotePlugin.plugin.getResource().getChildVote().set("CV." + this.id + ".Description", description);
        VotePlugin.plugin.getResource().getChildVote().save();
        this.description = description;
    }

    public void setAgreePlayers (Set<OfflinePlayer> agreePlayers) {
        List<String> UIDs = new ArrayList<>();
        for (OfflinePlayer player : agreePlayers)
            UIDs.add(String.valueOf(player.getUniqueId()));
        VotePlugin.plugin.getResource().getChildVote().set("CV." + this.id + ".AgreePlayers", UIDs);
        VotePlugin.plugin.getResource().getChildVote().save();
        this.agreePlayers = agreePlayers;
    }

    public void setParentVote (ParentVote parentVote) {
        VotePlugin.plugin.getResource().getChildVote().set("CV." + this.id + ".PVId", parentVote.getId());
        VotePlugin.plugin.getResource().getChildVote().save();
        this.parentVote = parentVote;
    }

    public void setNameTag (ItemStack nameTag) {
        VotePlugin.plugin.getResource().getChildVote().set("CV." + this.id + ".NameTag", nameTag);
        VotePlugin.plugin.getResource().getChildVote().save();
        this.nameTag = nameTag;
    }

    public void setCommand (String command) {
        VotePlugin.plugin.getResource().getChildVote().set("CV." + this.id + ".Command", command);
        VotePlugin.plugin.getResource().getChildVote().save();
        this.command = command;
    }

    public String getCommand () {
        return command;
    }

    // get 方法
    public int getId () {
        return id;
    }

    public String getTitle () {
        return title;
    }

    public String getDescription () {
        return description;
    }

    public Set<OfflinePlayer> getAgreePlayers () {
        return agreePlayers;
    }

    public int getVotedNum () {
        return this.agreePlayers.size();
    }

    public ParentVote getParentVote () {
        return parentVote;
    }

    public ItemStack getNameTag () {
        return nameTag;
    }
}
