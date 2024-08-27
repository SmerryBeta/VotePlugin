package me.smerrybeta.command;

import me.smerrybeta.VotePlugin;
import me.smerrybeta.object.ChildBuilder;
import me.smerrybeta.object.ChildVote;
import me.smerrybeta.object.ParentVote;
import me.smerrybeta.saves.VoteDataSave;
import me.smerrybeta.util.Functions;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.hover.content.Text;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class Command implements CommandExecutor {
    @Override
    public boolean onCommand (@NotNull CommandSender sender, @NotNull org.bukkit.command.Command command, @NotNull String alias, String[] args) {
        if (args[0].equalsIgnoreCase("help")) {
            sender.sendMessage("""
                    §f6====== 2024/8/26 ===== By SmerryBeta |
                      §b创建投票：§c-create
                      §b删除投票：§c-remove
                      §b对<id>投票：§c-vote
                      §b修改投票：§c-editVote
                      §b修改投票截止时间：§c-deadline
                      §b修改选项§c(可以结束执行的命令)§b：§c-editChoose
                      §b添加选项§c(可以结束执行的命令)§b：§c-addChoose
                      §b删除选项：§c-removeChoose
                      §b投票情况：§c-show
                      §b设置可选次数：§c-setLimit
                      §b设置投票结束：§c-setEnd
                      §b设置选项展示物品：§c-showItem
                      §b分享投票：§c-share
                      §b设置结束奖励：§c-setKit
                      §b获取结束奖励：§c-kit
                      §b重置结束奖励获取状态：§c-reKit
                    §c§l调试指令：§f-p -c
                    §6=======================  Vote System  |
                    """);
            return true;
        } else if (args[0].equalsIgnoreCase("create")) {
            if (hasNoMainPermission(sender)) return true;
            if (args.length >= 4 && args.length <= 41) { // Handle both cases: with or without limit
                Date date = Functions.stringToDate(args[3]);
                if (date == null) {
                    sender.sendMessage(Functions.IR() + "§cDate格式错误，请输入正确的格式！/vote create <title> <description> <deadline(如2024/8/23/20:16)> <limit> \"<子选标题>\":\"<子选描述>\"....");
                    return true;
                }

                int limit = 1; // Default limit if not provided
                if (args.length == 5) {
                    limit = Functions.strToInteger(args[4]);
                    if (limit < 1) {
                        sender.sendMessage(Functions.IR() + "§cLimit数格式错误或小于1，请输入正确的格式！/vote create <title> <description> <deadline(如2024/8/23/20:16)> <limit> \"<子选标题>\":\"<子选描述>\"....");
                        return true;
                    }
                }
                List<ChildBuilder> builders = new ArrayList<>();

                if (args.length == 6) {
                    builders = ChildBuilder.parseArgs(args);
                    if (args[5].equalsIgnoreCase("1是否预设")) {
                        ChildBuilder first = new ChildBuilder(1, "不同意", "点击则同意这个观点");
                        ChildBuilder second = new ChildBuilder(2, "同意", "点击则同意这个观点");
                        builders.addAll(List.of(first, second));
                    } else if (args[5].equalsIgnoreCase("2五级评价预设")) {
                        ChildBuilder first = new ChildBuilder(1, "好", "点击则同意这个观点");
                        ChildBuilder second = new ChildBuilder(2, "非常好", "点击则同意这个观点");
                        ChildBuilder third = new ChildBuilder(3, "一般", "点击则同意这个观点");
                        ChildBuilder forth = new ChildBuilder(4, "不是很好", "点击则不同意这个观点");
                        ChildBuilder fifth = new ChildBuilder(5, "非常不好", "点击则同意这个观点");
                        builders.addAll(List.of(first, second, third, forth, fifth));
                    }

                }

                if (args.length > 6) builders = ChildBuilder.parseArgs(args);

                createVote(sender, args[1], args[2], date, limit, builders);

            } else if (args.length > 41) {
                sender.sendMessage(Functions.IR() + "§c你这也太长了吧！最多支持 §r36 §c个好叭！");
            } else
                sender.sendMessage(Functions.IR() + "§c/vote create <title> <description> <deadline(如2024/8/23/20:16)> <limit（可选）>");
            return true;
        } else if (args[0].equalsIgnoreCase("remove")) {
            if (hasNoMainPermission(sender)) return true;
            if (args.length == 2) {
                int id = Functions.getIndex(args[1]);
                String PVId = VotePlugin.plugin.getResource().getParentVote().getString("PV." + id);
                if (PVId == null) {
                    sender.sendMessage(Functions.IR() + "§c不存在id为 " + args[1] + " 的投票");
                    return true;
                }
                sender.sendMessage(Functions.IR() + "§c输入/vote remove " + args[1] + " confirm 来确认删除。");
                return true;
            } else if (args.length == 3) {
                if (args[2].equalsIgnoreCase("Confirm")) {
                    int id = Functions.getIndex(args[1]);
                    ParentVote parentVote = VoteDataSave.getPVFromId(id);
                    if (parentVote == null) {
                        sender.sendMessage(Functions.IR() + "§c不存在id为 " + args[1] + " 的投票");
                        return true;
                    }
                    // 这里删除了 Yml 里面的部分 + 内存
                    VotePlugin.plugin.getResource().getParentVote().set("PV." + parentVote.getId() + ".CVIds", new ArrayList<>());
                    // TODO 删不干净永远都留个壳在那 然后重启就报错 妈的！！！！生气！
//                    VotePlugin.plugin.getResource().getParentVote().set("PV." + parentVote.getId(), null);

                    VotePlugin.plugin.getResource().getParentVote().save();

                    VoteDataSave.removeParentVote(parentVote);
                    Set<ChildVote> childVoteList = parentVote.getChildVotes();
                    List<ChildVote> cache = new ArrayList<>();
                    for (ChildVote childVote : childVoteList) {
                        VotePlugin.plugin.getResource().getChildVote().set("CV." + childVote.getId(), null);
                        VotePlugin.plugin.getResource().getChildVote().save();
                        cache.add(childVote);
                    }
                    for (ChildVote c : cache)
                        VoteDataSave.removeChildVote(c);
                    sender.sendMessage(Functions.IR() + "§a删除成功！id:§r " + id);
                    return true;
                }
            }
        } else if (args[0].equalsIgnoreCase("vote")) {
            if (args.length == 2) {
                int id = Functions.getIndex(args[1]);
                ParentVote parentVote = VoteDataSave.getPVFromId(id);
                if (parentVote == null) {
                    sender.sendMessage(Functions.IR() + "§c不存在id为 " + args[1] + " 的投票");
                    return true;
                }

                if (parentVote.isEnd()) {
                    sender.sendMessage(Functions.IR() + "§c不好意思噢~ id为§r " + id + " §c的投票已经结束了呢..");
                    return true;
                }

                if (! (sender instanceof Player player)) {
                    sender.sendMessage(Functions.IR() + "§c仅玩家可以投票！");
                    return true;
                }
                sender.sendMessage(Functions.IR() + "§a正在为你打开投票窗口！id:§r" + id);
                player.openInventory(Functions.getVoteGui(parentVote, player));
                return true;
            }
        } else if (args[0].equalsIgnoreCase("editChoose")) { //vote 0editChoose 1<ParentVoteId> 2<ChildVoteId> 3<NewTitle> 4<NewDescription(可选)>
            if (hasNoMainPermission(sender)) return true;
            if (args.length >= 4) {
                ParentVote parentVote = getParentVote(sender, args);
                if (parentVote == null) return true;

                ChildVote childVote = getChildVote(sender, parentVote, args);
                if (childVote == null) return true;

                childVote.setTitle(args[3]);
                if (args.length == 5)
                    childVote.setDescription(args[4]);

                if (args.length >= 6) {
                    String endCommand = Functions.returnArgsToStr(args, 5);
                    if (endCommand.startsWith("/"))
                        endCommand = endCommand.substring(1);
                    childVote.setCommand(endCommand);
                    sender.sendMessage("§a已更改最高选票结束时执行指令为 §r" + endCommand);
                }
                sender.sendMessage(Functions.IR() + "§a成功将id为 §r" + childVote.getId() + " §a的标题更改为 §r" + childVote.getTitle());

                VotePlugin.plugin.getResource().getChildVote().save();
            } else
                sender.sendMessage("§c/vote editChoose <父投票id> <子选项id> <新的标题> <新的描述> <最高选票结束指令>");
            return true;
        } else if (args[0].equalsIgnoreCase("addChoose")) { //vote addChoose <ParentVoteId> <NewTitle> <NewDescription> <Command>
            if (hasNoMainPermission(sender)) return true;
            if (args.length >= 4) {
                int id = Functions.getIndex(args[1]);
                ParentVote parentVote = VoteDataSave.getPVFromId(id);
                if (parentVote == null) {
                    sender.sendMessage(Functions.IR() + "§c不存在id为 " + args[1] + " 的投票");
                    return true;
                }
                int hasNum = parentVote.getChildVotes().size();
                if (hasNum >= 36) {
                    sender.sendMessage(Functions.IR() + "§c你创建的数量太多了！它已经有 §r" + hasNum + " §c个子选项了！");
                    return true;
                }
                String endCommand = "";
                if (args.length >= 5) {
                    endCommand = Functions.returnArgsToStr(args, 4);
                    if (endCommand.startsWith("/"))
                        endCommand = endCommand.substring(1);
                    sender.sendMessage("§a已更改最高选票结束时执行指令为 §r" + endCommand);
                }
                int newId1 = Functions.getMaxNumber(Objects.requireNonNull(VotePlugin.plugin.getResource().getChildVote().getConfigurationSection("CV")).getKeys(false)) + 1;
                ChildVote childVote = new ChildVote(newId1, args[2], args[3], new HashSet<>(), parentVote, null, endCommand, true);

                VoteDataSave.addChildVote(childVote);

                sender.sendMessage(Functions.IR() + "§a标题为 §r" + args[2] + " §a的子选项创建成功！");
            } else sender.sendMessage("§c/vote addChoose <父投票id> <新子选项标题> <对子选项的描述>");
            return true;
        } else if (args[0].equalsIgnoreCase("removeChoose")) { //vote removeChoose <ParentVoteId> <ChildVoteId>
            if (hasNoMainPermission(sender)) return true;
            if (args.length == 3) {
                ParentVote parentVote = getParentVote(sender, args);

                if (parentVote == null) return true;

                if (parentVote.getChildVotes().size() <= 2) {
                    sender.sendMessage(Functions.IR() + "§c子选项不能少于 §r2 §c个！");
                    return true;
                }
                ChildVote childVote = getChildVote(sender, parentVote, args);
                if (childVote == null) return true;

                VoteDataSave.removeChildVote(childVote);

                VotePlugin.plugin.getResource().getChildVote().set("CV." + childVote.getId(), null);
                VotePlugin.plugin.getResource().getChildVote().save();
                sender.sendMessage(Functions.IR() + "§a删除成功！id:§r " + childVote.getId());
            } else sender.sendMessage("§c/vote removeChoose <父投票id> <子选项id>");
            return true;
        } else if (args[0].equalsIgnoreCase("show")) { //vote show <ParentVoteId>
            if (args.length == 1) {
                List<ParentVote> parentVotes = VoteDataSave.getParentVoteList();
                sender.sendMessage(" =========== §a现有投票如下 §r=========== ");
                for (ParentVote parentVote : parentVotes) {
                    ComponentBuilder toThisPlayer = new ComponentBuilder();
                    boolean end = parentVote.isEnd();
                    boolean priceAvailable;
                    if (sender instanceof Player player) {
                        priceAvailable = ! parentVote.hasGetPrice(player) && parentVote.hasPrice();
                    } else priceAvailable = false;

                    toThisPlayer.append(" §cid:§r" + parentVote.getId())
                            .append(" §b标题：").append("§r" + parentVote.getTitle())
                            .append(" §b状态：").append(end ? " §c结束" : " §a进行")
                            .append("§e[§r" + (end ? "查看结果" : "点击投票") + "§e]")
                            .event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text(end ? "查看结果" : "点击投票")))
                            .event(new ClickEvent(ClickEvent.Action.RUN_COMMAND, (end ? "/vote show " : "/vote vote ") + parentVote.getId()));
//                            .append(priceAvailable ? " \n§a你在本次投票有一份奖励可以获取->" : "")
//                            .event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text("")));
                    if (priceAvailable) toThisPlayer.append(" §e[§r领取奖励👆§e]")
                            .event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text("点击获取")))
                            .event(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/vote kit " + parentVote.getId()))
                            ;
                    // 发送消息给本玩家
                    sender.spigot().sendMessage(toThisPlayer.create());
                }
                sender.sendMessage(" ============ §aVote System ============ ");
            } else if (args.length == 2) {
                ParentVote parentVote = getParentVote(sender, args);
                if (parentVote == null) return true;

                if (! parentVote.isEnd() && ! sender.hasPermission("vote.main")) {
                    sender.sendMessage("§c投票还没结束呢！");
                    return true;
                }

                LinkedHashMap<String, Integer> map = new LinkedHashMap<>();
                Set<OfflinePlayer> players = new HashSet<>();

                int sum = 0;
                for (ChildVote childVote : parentVote.getChildVotes()) {
                    players.addAll(childVote.getAgreePlayers());
                    int agree = childVote.getVotedNum();
                    sum += agree;
                    map.put("(id:" + childVote.getId() + ")" + childVote.getTitle(), agree);
                }

                map = Functions.sortByValueDescending(map);
                boolean first = true;
                sender.sendMessage(" ========== §a投票§r " + parentVote.getTitle() + "§a 的情况如下 §r========== ");

                for (String title : map.keySet()) {
                    StringBuilder stringBuilder = new StringBuilder("选择此选项的玩家有：");
                    ChildVote childVote = VoteDataSave.getCVFromId(Functions.extractIdFromString(title));
                    Set<OfflinePlayer> choosePlayers = new HashSet<>();

                    if (childVote != null)
                        choosePlayers.addAll(childVote.getAgreePlayers());

                    for (OfflinePlayer player : choosePlayers) {
                        String playerName = player.getName();
                        stringBuilder.append(playerName).append("、");
                    }
                    ComponentBuilder toThisPlayer = new ComponentBuilder();
                    int peopleNum = map.get(title);
                    toThisPlayer.append("  §b有 ")
                            .append("§r" + peopleNum)
                            .event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text(
                                    stringBuilder.substring(0, (! stringBuilder.isEmpty() ? stringBuilder.length() - 1 : stringBuilder.length())))))
                            .append(" §b位选择了 §c" + title + "§b 占比：§c" + (sum == 0 ? "0.0%" : Functions.getPercent(peopleNum, sum)) + (first ? " §a(✔)" : ""));
                    // 发送消息给本玩家
                    sender.spigot().sendMessage(toThisPlayer.create());
                    first = false;
                }
                sender.sendMessage(" ======== §a总共票数：§c" + sum + " §r| §a参与人数: §c" + players.size() + "§r ========= ");
            } else sender.sendMessage("§c/vote show <父投票id>");
            return true;
        } else if (args[0].equalsIgnoreCase("setLimit")) {
            if (hasNoMainPermission(sender)) return true;
            if (args.length == 3) {
                ParentVote parentVote = getParentVote(sender, args);
                if (parentVote != null) {
                    int limit = Functions.getIndex(args[2]);
                    if (limit >= 1) {
                        parentVote.setVoteLimit(limit);
                        sender.sendMessage(Functions.IR() + "§a已调整 §r" + parentVote.getTitle() + " §a的限制选择数量为 §r" + parentVote.getVoteLimit());
                        return true;
                    }
                }
                sender.sendMessage(Functions.IR() + "§c错误的数量！");
            } else sender.sendMessage("§c/vote setLimit <父投票id> <限选数量>");
            return true;
        } else if (args[0].equalsIgnoreCase("editVote")) {
            if (hasNoMainPermission(sender)) return true;
            if (args.length == 3 || args.length == 4) {
                ParentVote parentVote = getParentVote(sender, args);
                if (parentVote == null) return true;
                parentVote.setTitle(args[2]);
                boolean adjust = false;
                if (args.length == 4) {
                    parentVote.setDescription(args[3]);
                    adjust = true;
                }
                sender.sendMessage(Functions.IR() + "§a已调整id为 §r" + parentVote.getId() + " §a的新标题为 §r"
                        + parentVote.getTitle() + (adjust ? " §a新的描述：§r" + parentVote.getDescription() : ""));
            } else sender.sendMessage("§c/vote editVote <父投票id> <新的名称> <新的描述>");
            return true;
        } else if (args[0].equalsIgnoreCase("p")) { // Debug 指令
            if (hasNoMainPermission(sender)) return true;
            System.out.println(VoteDataSave.getParentVoteList());
            return true;
        } else if (args[0].equalsIgnoreCase("c")) { // Debug 指令
            if (hasNoMainPermission(sender)) return true;
            System.out.println(VoteDataSave.getChildVotes());
            return true;
        } else if (args[0].equalsIgnoreCase("setEnd")) { // vote setEnd <PVId> <true/false>
            if (hasNoMainPermission(sender)) return true;
            if (args.length == 3) {
                ParentVote parentVote = getParentVote(sender, args);
                if (parentVote == null) return true;
                boolean end = Boolean.parseBoolean(args[2]);
                parentVote.setEnd(end);
                sender.sendMessage(Functions.IR() + "§a已调整 §r" + parentVote.getTitle() + " §a为 §r" + (! end ? "继续" : "结束"));
            } else sender.sendMessage("§c/vote setEnd <父投票id> <true/false>");
            return true;
        } else if (args[0].equalsIgnoreCase("deadline")) { // vote deadline <PVId> <Date>
            if (hasNoMainPermission(sender)) return true;
            if (args.length == 3) {
                Date date = Functions.stringToDate(args[2]);
                if (date == null) {
                    sender.sendMessage(Functions.IR() + "§cDate格式错误，请输入正确的格式！");
                    return true;
                }
                ParentVote parentVote = getParentVote(sender, args);

                if (parentVote == null) return true;

                parentVote.setDeadline(date);
                sender.sendMessage(Functions.IR() + "§a已调整 §r" + parentVote.getTitle() + " §a的Deadline为 §r" + date);

                parentVote.setEnd(Functions.isExpired(date));
                sender.sendMessage("§a投票将 §r" + (! parentVote.isEnd() ? "继续！" : "结束！"));
            } else sender.sendMessage("§c/vote deadline <父投票id> <deadline(如2024/8/23/20:16)>");
            return true;
        } else if (args[0].equalsIgnoreCase("showItem")) { // vote showItem <PVId> <CVId>
            if (hasNoMainPermission(sender)) return true;
            if (args.length == 3) {
                if (! (sender instanceof Player player)) return true;
                ParentVote parentVote = getParentVote(sender, args);
                if (parentVote == null) return true;
                ChildVote childVote = getChildVote(sender, parentVote, args);
                if (childVote == null) return true;
                ItemStack hand = player.getInventory().getItemInMainHand().clone();
                if (hand.getType().isAir()) {
                    sender.sendMessage("§c你手上是空气！");
                    return true;
                }
                // 去除所有附魔
                Functions.removeAllEnchantments(hand);
                // 设置为 NameTag
                childVote.setNameTag(hand);
                sender.sendMessage(Functions.IR() + "§a已调整 §r" + childVote.getTitle() + " §a的Deadline为 §r" + childVote.getNameTag().getType());
            } else sender.sendMessage("§c/vote showItem <父投票id> <子选项id>(以手上物品作为默认传入)");
            return true;
        } else if (args[0].equalsIgnoreCase("share")) { // vote share PVId sb
            if (args.length == 3) {
                ParentVote parentVote = getParentVote(sender, args);
                if (parentVote == null) return true;
                Player player = Bukkit.getPlayer(args[2]);
                if (player == null) {
                    sender.sendMessage(Functions.IR() + "§c目标玩家不在线！");
                    return true;
                }
                ComponentBuilder toThisPlayer = new ComponentBuilder();
                toThisPlayer.append(Functions.IR() + "§c" + sender.getName() + " §f向你分享了一个投票->")
                        .append(" §e[§c" + parentVote.getTitle() + "§e] ")
                        .event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text("点击查看投票：" + parentVote.getTitle())))
                        .event(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/vote vote " + parentVote.getId()))
                        .append("§f<-点击参与投票吧！");
                // 发送消息给本玩家
                player.spigot().sendMessage(toThisPlayer.create());
            } else sender.sendMessage("§c/vote showItem <父投票id> <玩家ID>");
            return true;
        } else if (args[0].equalsIgnoreCase("kit")) { // vote kit <PVId>
            if (args.length >= 2) {
                ParentVote parentVote = getParentVote(sender, args);
                if (parentVote == null) return true;
                if (! (sender instanceof Player player)) {
                    sender.sendMessage(Functions.IR() + "§c此指令由于是物品栏操作所以仅限玩家！");
                    return true;
                }
                if (! parentVote.isEnd()) {
                    sender.sendMessage(Functions.IR() + "§c这个投票还没结束呢！");
                    return true;
                }
                if (! parentVote.hasPlayer(player)) {
                    sender.sendMessage(Functions.IR() + "§c你从来都没参与过这个投票！");
                    return true;
                }
                if (parentVote.hasGetPrice(player)) {
                    sender.sendMessage(Functions.IR() + "§c你已经拿到奖励了！");
                    return true;
                }
                if (Functions.pricePlayer(parentVote, player)) {
                    parentVote.addToGottenList(player);
                    Functions.launchFirework(player.getLocation());
                    sender.sendMessage(Functions.IR() + "§a感谢你参与 §r" + parentVote.getTitle() + "§a！这些是你的奖励！");
                } else sender.sendMessage(Functions.IR() + "§c这个问卷似乎没有设置问卷奖励呢..");
            } else sender.sendMessage("§c/vote setKit <父投票id>");
            return true;
        } else if (args[0].equalsIgnoreCase("setKit")) { // vote setKit <PVId>
            if (hasNoMainPermission(sender)) return true;
            if (args.length >= 2) {
                ParentVote parentVote = getParentVote(sender, args);
                if (parentVote == null) return true;
                if (! (sender instanceof Player player)) {
                    sender.sendMessage(Functions.IR() + "§c此指令由于是物品栏操作所以仅限玩家！");
                    return true;
                }
                Inventory setterInv = Functions.getKitSetterInv(parentVote);
                player.openInventory(setterInv);
                sender.sendMessage(Functions.IR() + "§a正在为你打开物品栏！");
            } else sender.sendMessage("§c/vote setKit <父投票id>");
            return true;
        } else if (args[0].equalsIgnoreCase("reKit")) { // vote reKit <PVId>
            if (hasNoMainPermission(sender)) return true;
            if (args.length >= 2) {
                ParentVote parentVote = getParentVote(sender, args);
                if (parentVote == null) return true;
                VotePlugin.plugin.getResource().getVotePrice().set("VP." + parentVote.getId() + ".GottenPlayers", null);
                VotePlugin.plugin.getResource().getVotePrice().save();
                sender.sendMessage(Functions.IR() + "§a清空完成！");
            } else sender.sendMessage("§c/vote reKit <父投票id>");
            return true;
        }

        sender.sendMessage(Functions.IR() + "§6Vote-Plugin (by SmerryBeta)");
        return false;
    }

    private ParentVote getParentVote (CommandSender sender, String[] args) {
        int id = Functions.getIndex(args[1]);
        ParentVote parentVote = VoteDataSave.getPVFromId(id);
        if (parentVote == null)
            sender.sendMessage(Functions.IR() + "§c不存在id为 " + args[1] + " 的投票");
        return parentVote;
    }

    private ChildVote getChildVote (CommandSender sender, ParentVote parentVote, String[] args) {
        int id = Functions.getIndex(args[2]);
        Set<ChildVote> childVoteList = parentVote.getChildVotes();
        for (ChildVote vote : childVoteList) {
            if (vote.getId() == id) {
                return vote;
            }
        }
        sender.sendMessage(Functions.IR() + "§c投票 §r" + parentVote.getTitle() + " §c不存在id为 §r" + args[2] + " 的子选项");
        return null;
    }

    private void createVote (CommandSender sender, String title, String description, Date deadline, int limit, List<ChildBuilder> childBuilders) {
        int newParentVoteId = Functions.getMaxNumber(Objects.requireNonNull(VotePlugin.plugin.getResource().getParentVote().getConfigurationSection("PV")).getKeys(false)) + 1;
        int newChildVoteId1 = Functions.getMaxNumber(Objects.requireNonNull(VotePlugin.plugin.getResource().getChildVote().getConfigurationSection("CV")).getKeys(false)) + 1;
        int newChildVoteId2 = newChildVoteId1 + 1;

        ParentVote parentVote = new ParentVote(newParentVoteId, title, description, limit, new HashSet<>(), deadline, true);

        ChildBuilder child1 = ! childBuilders.isEmpty() ? childBuilders.get(0) : null;
        ChildBuilder child2 = childBuilders.size() >= 2 ? childBuilders.get(1) : null;

        ChildVote childVote1 = new ChildVote(newChildVoteId2, (child1 != null ? child1.getTitle() : "选项1"), (child1 != null ? child1.getDescription() : "点击同意这个选项"), parentVote);
        ChildVote childVote2 = new ChildVote(newChildVoteId1, (child2 != null ? child2.getTitle() : "选项2"), (child2 != null ? child2.getDescription() : "点击同意这个选项"), parentVote);

        List<ChildVote> newChildList = new ArrayList<>(List.of(childVote2, childVote1));

        int createNum = 0;
        if (childBuilders.size() > 2) {
            for (ChildBuilder childBuilder : childBuilders) {
                createNum++; // 跳过前俩个
                if (createNum <= 2) continue;
                newChildVoteId2++;
                ChildVote childVote = new ChildVote(newChildVoteId2, (childBuilder != null ? childBuilder.getTitle() : "选项" + createNum), (childBuilder != null ? childBuilder.getDescription() : "点击同意这个选项"), parentVote);
                newChildList.add(childVote);
            }
        }
        sender.sendMessage("§a已创建新的子选项 §r" + (createNum == 0 ? 2 : createNum) + " §a个");

        VoteDataSave.addParentVote(parentVote);

        for (ChildVote childVote : newChildList) {
            VoteDataSave.addChildVote(childVote); // 这边忘了把新来的加进去了
            parentVote.addChildVotes(childVote);
        }

        VotePlugin.plugin.getResource().getParentVote().set("PV." + newParentVoteId + ".CreateBy", sender.getName());
        VotePlugin.plugin.getResource().getParentVote().save();
        VotePlugin.plugin.getResource().getChildVote().save();

        sender.sendMessage(Functions.IR() + "§a新的投票创建成功！id为: " + newParentVoteId);
    }

    private boolean hasNoMainPermission (CommandSender sender) {
        if (! sender.hasPermission("vote.main")) {
            sender.sendMessage(Functions.IR() + "§c你妹滴权限Q x Q");
            return true;
        }
        return false;
    }
}