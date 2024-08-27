package me.smerrybeta.object;

import me.smerrybeta.VotePlugin;
import me.smerrybeta.util.Functions;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.*;
import java.text.SimpleDateFormat;

public class ParentVote {
    private final int id;
    private String title;
    private String description;
    private int voteLimit;
    private Set<ChildVote> childVotes;
    private Date deadline;
    private boolean end = false;


    // 构造函数
    public ParentVote (int id, String title, String description, int voteLimit, Set<ChildVote> childVotes, Date deadline, boolean yamlLoad) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.voteLimit = voteLimit;
        this.childVotes = childVotes;
        this.deadline = deadline;

        if (yamlLoad) { // 设置 YML 配置
            SimpleDateFormat formatter = new SimpleDateFormat("yyyy/MM/dd/HH:mm");
            String formattedDeadline = formatter.format(deadline);

            VotePlugin.plugin.getResource().getParentVote().set("PV." + this.id + ".Title", title);
            VotePlugin.plugin.getResource().getParentVote().set("PV." + this.id + ".Description", description);
            VotePlugin.plugin.getResource().getParentVote().set("PV." + this.id + ".VoteLimit", voteLimit);
            VotePlugin.plugin.getResource().getParentVote().set("PV." + this.id + ".Deadline", formattedDeadline);
            VotePlugin.plugin.getResource().getParentVote().set("PV." + this.id + ".Over", "false");

            List<Integer> CVIds = new ArrayList<>();

            for (ChildVote childVote : childVotes)
                CVIds.add(childVote.getId());
            VotePlugin.plugin.getResource().getParentVote().set("PV." + this.id + ".CVIds", CVIds);

            VotePlugin.plugin.getResource().getParentVote().save();
        }
    }

    @Override
    public boolean equals (Object o) {
        if (this == o) return true;
        if (! (o instanceof ParentVote that)) return false;

        if (getId() != that.getId()) return false;
        if (getVoteLimit() != that.getVoteLimit()) return false;
        if (isEnd() != that.isEnd()) return false;
        if (getTitle() != null ? ! getTitle().equals(that.getTitle()) : that.getTitle() != null) return false;
        if (getDescription() != null ? ! getDescription().equals(that.getDescription()) : that.getDescription() != null)
            return false;
        if (getChildVotes() != null ? ! getChildVotes().equals(that.getChildVotes()) : that.getChildVotes() != null)
            return false;
        return getDeadline() != null ? getDeadline().equals(that.getDeadline()) : that.getDeadline() == null;
    }

    // set 方法
    public void setTitle (String title) {
        VotePlugin.plugin.getResource().getParentVote().set("PV." + this.id + ".Title", title);
        VotePlugin.plugin.getResource().getParentVote().save();
        this.title = title;
    }

    public void setDescription (String description) {
        VotePlugin.plugin.getResource().getParentVote().set("PV." + this.id + ".Description", description);
        VotePlugin.plugin.getResource().getParentVote().save();
        this.description = description;
    }

    public void setVoteLimit (int voteLimit) {
        VotePlugin.plugin.getResource().getParentVote().set("PV." + this.id + ".VoteLimit", voteLimit);
        VotePlugin.plugin.getResource().getParentVote().save();
        this.voteLimit = voteLimit;
    }

    public void setChildVotes (Set<ChildVote> childVotes) {
        List<Integer> CVIds = new ArrayList<>();

        for (ChildVote childVote : childVotes)
            CVIds.add(childVote.getId());

        VotePlugin.plugin.getResource().getParentVote().set("PV." + this.id + ".CVIds", CVIds);
        VotePlugin.plugin.getResource().getParentVote().save();

        this.childVotes = childVotes;
    }

    public void addChildVotes (ChildVote... childVotes) {
        this.childVotes.addAll(Arrays.asList(childVotes));

        List<Integer> CVIds = new ArrayList<>();

        for (ChildVote childVote : this.childVotes) // 该类本身自带的
            CVIds.add(childVote.getId());
        for (ChildVote childVote : childVotes) // 新来的
            CVIds.add(childVote.getId());

        VotePlugin.plugin.getResource().getParentVote().set("PV." + this.id + ".CVIds", CVIds);
        VotePlugin.plugin.getResource().getParentVote().save();

    }

    public void setDeadline (Date deadline) {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy/MM/dd/HH:mm");
        String formattedDeadline = formatter.format(deadline);

        VotePlugin.plugin.getResource().getParentVote().set("PV." + this.id + ".Deadline", formattedDeadline);
        VotePlugin.plugin.getResource().getParentVote().save();

        this.deadline = deadline;
    }

    public void setEnd (boolean end) {
        VotePlugin.plugin.getResource().getParentVote().set("PV." + this.id + ".End", end);
        if (! VotePlugin.plugin.getResource().getParentVote().contains("PV." + this.id + ".Over"))
            VotePlugin.plugin.getResource().getParentVote().set("PV." + this.id + ".Over", "false");

        this.end = end;

        if (this.end) {
            ChildVote maxChild = null;
            int max = 0;
            if (childVotes.isEmpty()) return;
            for (ChildVote childVote : childVotes) {
                if (max < childVote.getVotedNum())
                    maxChild = childVote;
                max = Math.max(max, childVote.getVotedNum());
            }
            if (maxChild != null) {
                String command = maxChild.getCommand();
                String over = VotePlugin.plugin.getResource().getParentVote().getString("PV." + this.id + ".Over");
                if (command != null && over != null && over.equalsIgnoreCase("false")) {
                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command);
                    Bukkit.getServer().getConsoleSender().sendMessage(Functions.IR() + "§c执行了指令：" + command);
                }
            }
            VotePlugin.plugin.getResource().getParentVote().set("PV." + this.id + ".Over", "true");
        } else VotePlugin.plugin.getResource().getParentVote().set("PV." + this.id + ".Over", "false");

        VotePlugin.plugin.getResource().getParentVote().save();
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

    public int getVoteLimit () {
        return voteLimit;
    }

    public Set<ChildVote> getChildVotes () {
        return childVotes;
    }

    public Date getDeadline () {
        return deadline;
    }

    public boolean isEnd () {
        return end;
    }

    public boolean hasPlayer (Player player) {
        for (ChildVote childVote : childVotes)
            if (childVote.getAgreePlayers().contains(player))
                return true;
        return false;
    }

    public boolean hasGetPrice (Player player) {
        if (hasPlayer(player)) {
            Set<String> uuids = new HashSet<>(VotePlugin.plugin.getResource().getVotePrice().getStringList("VP." + id + ".GottenPlayers"));
            for (String uuid : uuids)
                if (player.getUniqueId().toString().equalsIgnoreCase(uuid))
                    return true;
        }
        return false;
    }

    public void addToGottenList (Player player) {
        Set<String> uuids = new HashSet<>(VotePlugin.plugin.getResource().getVotePrice().getStringList("VP." + id + ".GottenPlayers"));
        uuids.add(player.getUniqueId().toString());
        VotePlugin.plugin.getResource().getVotePrice().set("VP." + id + ".GottenPlayers", new ArrayList<>(uuids));
        VotePlugin.plugin.getResource().getVotePrice().save();
    }

    public boolean hasPrice () {
        HashMap<Integer, ItemStack> kitMap = Functions.getKitMap(this);
        return kitMap != null && ! kitMap.isEmpty();
    }
}
