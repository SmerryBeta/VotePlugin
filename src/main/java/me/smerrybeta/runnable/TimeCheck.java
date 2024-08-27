package me.smerrybeta.runnable;

import me.smerrybeta.object.ParentVote;
import me.smerrybeta.saves.VoteDataSave;
import me.smerrybeta.util.Functions;
import org.bukkit.scheduler.BukkitRunnable;

public class TimeCheck extends BukkitRunnable {
    @Override
    public void run() {
        for (ParentVote parentVote : VoteDataSave.getParentVoteList())
            if (Functions.isExpired(parentVote.getDeadline()))
                parentVote.setEnd(true);
    }
}
