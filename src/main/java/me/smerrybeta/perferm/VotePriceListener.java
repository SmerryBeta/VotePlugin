package me.smerrybeta.perferm;

import me.smerrybeta.object.ParentVote;
import me.smerrybeta.saves.VoteDataSave;
import me.smerrybeta.util.Functions;
import org.bukkit.entity.HumanEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;

public class VotePriceListener implements Listener {
    @EventHandler
    public void onPlayerSetOverKitInv (InventoryCloseEvent event) {
        Inventory inventory = event.getInventory();
        String title = event.getView().getTitle();
        if (title.startsWith("正在投票")) return;
        HumanEntity humanEntity = event.getPlayer();
        ParentVote parentVote = VoteDataSave.getPVFromId(Functions.extractIdFromString(title));
        if (parentVote == null) return;
        Functions.saveKitInvToYml(parentVote, inventory);
        humanEntity.sendMessage(Functions.IR() + "§c" + parentVote.getTitle() + " §a的奖励箱修改成功！投票id:§r" + parentVote.getId());
    }
}
