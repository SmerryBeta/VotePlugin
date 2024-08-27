package me.smerrybeta.perferm;

import me.smerrybeta.object.ChildVote;
import me.smerrybeta.object.ParentVote;
import me.smerrybeta.saves.VoteDataSave;
import me.smerrybeta.util.Functions;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.Sound;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public class VotingListener implements Listener {
    @EventHandler
    public void onPlayerVote (InventoryClickEvent event) {
        Inventory clicked = event.getClickedInventory();
        Inventory inventory = event.getInventory();
        InventoryView view = event.getView();
        ParentVote parentVote = VoteDataSave.getPVFromId(Functions.extractIdFromString(view.getTitle()));
        int clickedSlot = event.getSlot();
        // 设置的界面则退出
        if (view.getTitle().startsWith("正在设置")) return;
        // 如果没获取到这个id
        if (parentVote == null) return;
        // 超范围点击
        if (inventory.getSize() != 54 || event.getSlot() > 54 || event.getSlot() < 0) return;
        // 如果是玩家的物品栏则退出                      // 非顶上的物品栏则退出
        if (inventory == view.getBottomInventory() || clicked != view.getTopInventory()) return;
        else event.setCancelled(true);
        // 非玩家则退出
        HumanEntity humanEntity = event.getWhoClicked();
        if (! (humanEntity instanceof Player player)) return;
        // 处理点击的选票
        int chooseNum = Functions.getChooseNum(player, parentVote);
        ItemStack itemStack = event.getCurrentItem();
//        System.out.println("检测点1"); // todo
        if (itemStack != null && clickedSlot > 8 && clickedSlot < 45 && ! itemStack.getType().equals(Material.AIR)) {
//            System.out.println("检测点2"); // todo
            ChildVote childVote = VoteDataSave.getCVFromId(Functions.extractIdFromString(Objects.requireNonNull(itemStack.getItemMeta()).getDisplayName()));
            if (childVote != null) {
//                System.out.println("检测点3"); // todo
                Set<OfflinePlayer> players = childVote.getAgreePlayers() != null ? childVote.getAgreePlayers() : new HashSet<>();
                if (itemStack.getEnchantments().get(Enchantment.DURABILITY) == null) {
//                    System.out.println("检测点4"); // todo
                    if (parentVote.getVoteLimit() > chooseNum) {
                        players.add(player);
                        childVote.setAgreePlayers(players);
                        Functions.setChoose(itemStack);
                        inventory.setItem(clickedSlot, itemStack);
                        player.playSound(player, Sound.ENTITY_VILLAGER_YES, 1, 1);
                        player.sendMessage(Functions.IR() + "§a成功选择 §r" + childVote.getTitle() + " §a！还可以选 §r" + (parentVote.getVoteLimit() - chooseNum - 1) + " §a个选项！");
                    } else {
                        player.sendMessage(Functions.IR() + "§c你已经选了 §r" + chooseNum + " §c个了，不能再选了！");
                        player.playSound(player, Sound.ENTITY_VILLAGER_NO, 1, 1);
                    }
                } else {
                    players.remove(player);
                    childVote.setAgreePlayers(players);
                    Functions.cancelChoose(itemStack);
                    inventory.setItem(clickedSlot, itemStack);
                    player.playSound(player, Sound.ENTITY_VILLAGER_NO, 1, 1);
                    player.sendMessage(Functions.IR() + "§c取消选择 §r" + childVote.getTitle() + "§c ！还可以选 §r" + (parentVote.getVoteLimit() - chooseNum + 1) + " §c个选项！");
                }
            }
        }
    }

    @EventHandler
    public void onPlayerDragItem (InventoryDragEvent event) {
        Inventory inventory = event.getInventory();
        InventoryView view = event.getView();
        ParentVote parentVote = VoteDataSave.getPVFromId(Functions.extractIdFromString(view.getTitle()));
        // 设置的界面则退出
        if (view.getTitle().startsWith("正在设置")) return;
        // 如果是玩家的物品栏
        if (inventory == view.getBottomInventory()) return;
        // 如果没获取到这个id
        if (parentVote == null) return;
        // 拖东西拖到选票系统里
        for (int slot : event.getRawSlots()) {
            if (slot < view.getTopInventory().getSize()) {
                event.setCancelled(true);
                return;
            }
        }
    }
}
