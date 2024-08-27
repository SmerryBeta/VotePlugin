package me.smerrybeta.saves;

import me.smerrybeta.object.ChildVote;
import me.smerrybeta.object.ParentVote;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class VoteDataSave {
    private static List<ParentVote> parentVoteList = new ArrayList<>();
    private static List<ChildVote> childVotes = new ArrayList<>();
    public static void setParentVoteList (List<ParentVote> parentVoteList) {
        VoteDataSave.parentVoteList = parentVoteList;
    }
    public static void setChildVotes (List<ChildVote> childVotes) {
        VoteDataSave.childVotes = childVotes;
    }
    public static List<ChildVote> getChildVotes () {
        return childVotes;
    }
    public static List<ParentVote> getParentVoteList () {
        return parentVoteList;
    }
    public static ParentVote getPVFromId (int id) {
        if (parentVoteList != null)
            for (ParentVote parentVote : parentVoteList)
                if (parentVote.getId() == id)
                    return parentVote;

        return null ;
    }
    public static ChildVote getCVFromId (int id) {
        if (childVotes != null)
            for (ChildVote childVote : childVotes)
                if (childVote.getId() == id)
                    return childVote;

        return null ;
    }

    public static void addParentVote(ParentVote parentVote) {
        if (parentVoteList != null &&  parentVote != null) {
            parentVoteList.add(parentVote);
        }
    }

    // 添加 ChildVote 对象到 childVotes
    public static void addChildVote(ChildVote... childVotes) {
        if (childVotes != null)
            VoteDataSave.childVotes.addAll(List.of(childVotes));
    }

    // 删除 ParentVote 对象从 parentVoteList
    public static void removeParentVote(ParentVote parentVote) {
        if (parentVote != null)
            parentVoteList.remove(parentVote);
    }

    // 删除 ChildVote 对象从 childVotes
    public static void removeChildVote(ChildVote childVote) {
        if (childVote != null) {
            childVotes.remove(childVote);
            ParentVote parentVote = childVote.getParentVote();
            Set<ChildVote> childVoteList = parentVote.getChildVotes() != null? new HashSet<>(childVote.getParentVote().getChildVotes()) : new HashSet<>();
            childVoteList.remove(childVote);
            parentVote.setChildVotes(childVoteList);
        }
    }
}
