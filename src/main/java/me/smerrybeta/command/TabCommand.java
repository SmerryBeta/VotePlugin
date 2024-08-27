package me.smerrybeta.command;

import me.smerrybeta.object.ChildVote;
import me.smerrybeta.object.ParentVote;
import me.smerrybeta.saves.VoteDataSave;
import me.smerrybeta.util.Functions;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class TabCommand implements TabCompleter {
    @Override
    public List<String> onTabComplete (@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        if (command.getName().equalsIgnoreCase("Vote") && args.length == 1)
            return Arrays.asList("create","remove","vote","help","editchoose","addchoose","removechoose","show","setlimit","editvote","setend","deadline","showitem","share","kit","setkit","rekit");
        if (args[0].equalsIgnoreCase("create")) {
            if (args.length == 2)
                return Collections.singletonList("<标题>");
            if (args.length == 3)
                return new ArrayList<>(List.of("<描述>", "点击同意这个选项"));
            if (args.length == 4)
                return new ArrayList<>(List.of("<年年年年/月月/日日/时时:分分>", "add<年>y<月>m,<日>d,<时>h,<分>min", Functions.getCurrentDateTime(), Functions.addTime(0,0,1,0,0)));
            if (args.length == 5)
                return Collections.singletonList("<最大可选数>");
            if (args.length == 6)
                return new ArrayList<>(List.of("\"<子选标题>\":\"<子选描述>\"", "1是否预设", "2五级评价预设"));
            if (args.length > 6)
                return Collections.singletonList("\"<子选标题>\":\"<子选描述>\"");
        }
        if (tabIfArgs2NeedPVId(args[0])) {
            if (args.length == 2)
                return getVotes();
        }
        if (args[0].equalsIgnoreCase("editChoose")) {
            if (args.length == 3)
                return getChoose(Functions.getIndex(args[1]));
            ChildVote childVote = getChildVote(args);
            if (args.length == 4)
                return new ArrayList<>(List.of("<新的标题>", (childVote != null ? childVote.getTitle() : "")));
            if (args.length == 5)
                return new ArrayList<>(List.of("<新的描述>", "点击同意这个选项", (childVote != null ? childVote.getDescription() : "")));
            if (args.length == 6)
                return new ArrayList<>(List.of("<最高选票时结束运行指令(支持空格)>", (childVote != null ? (childVote.getCommand() != null ? childVote.getCommand() : "") : "")));
        }
        if (args[0].equalsIgnoreCase("addChoose")) {
            if (args.length == 3)
                return Collections.singletonList("<新的标题>");
            if (args.length == 4)
                return new ArrayList<>(List.of("<新的描述>", "点击同意这个选项"));
            if (args.length == 5)
                return Collections.singletonList("<最高选票时结束运行指令(支持空格)>");
        }
        if (args[0].equalsIgnoreCase("removeChoose")) {
            if (args.length == 3)
                return getChoose(Functions.getIndex(args[1]));
        }
        if (args[0].equalsIgnoreCase("showItem")) {
            if (args.length == 3)
                return getChoose(Functions.getIndex(args[1]));
        }
        if (args[0].equalsIgnoreCase("setLimit")) {
            if (args.length == 3)
                return Collections.singletonList("<新的限选数量>");
        }
        if (args[0].equalsIgnoreCase("editVote")) {
            ParentVote parentVote = getParentVote(args);
            if (args.length == 3)
                return new ArrayList<>(List.of("<新的名称>", (parentVote != null ? parentVote.getTitle() : "")));
            if (args.length == 4)
                return new ArrayList<>(List.of("<新的描述>", (parentVote != null ? parentVote.getDescription() : "")));
        }
        if (args[0].equalsIgnoreCase("setEnd")) {
            if (args.length == 3)
                return new ArrayList<>(List.of("true","false"));
        }
        if (args[0].equalsIgnoreCase("deadline")) { // TODO 重点来做这边的 tab
            if (args.length == 3)
                return new ArrayList<>(List.of("<年年年年/月月/日日/时时:分分>", Functions.getCurrentDateTime(), Functions.addTime(0,0,1,0,0)));
        }
        return null;
    }

    private boolean tabIfArgs2NeedPVId (String inputCommand) { // 需要在 args[2] tab PVId 的部分
        List<String> tabs = new ArrayList<>(List.of("vote","remove","editChoose","removeChoose","addChoose","show","setLimit","editVote","setEnd","deadline","showItem","share","kit","setKit","reKit"));
        for (String s : tabs)
            if (inputCommand.equalsIgnoreCase(s))
                return true;
        return false;
    }

    public static List<String> getVotes () {
        List<String> votes = new ArrayList<>();
        for (ParentVote parentVote : VoteDataSave.getParentVoteList())
            votes.add(parentVote.getId() + ":" + parentVote.getTitle());
        return votes;
    }

    public static List<String> getChoose (int PVId) {
        List<String> votes = new ArrayList<>();
        ParentVote parentVote =  VoteDataSave.getPVFromId(PVId);
        if (parentVote == null) return votes;
        for (ChildVote childVote : parentVote.getChildVotes())
            votes.add(childVote.getId() + ":" + childVote.getTitle());
        return votes;
    }

    private ParentVote getParentVote (String[] args) {
        if (args.length >= 2) return VoteDataSave.getPVFromId(Functions.getIndex(args[1]));
        return null;
    }
    private ChildVote getChildVote (String[] args) {
        if (args.length >= 3) return VoteDataSave.getCVFromId(Functions.getIndex(args[2]));
        return null;
    }
}