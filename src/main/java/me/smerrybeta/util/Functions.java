package me.smerrybeta.util;

import me.smerrybeta.VotePlugin;
import me.smerrybeta.object.ChildVote;
import me.smerrybeta.object.ParentVote;
import me.smerrybeta.saves.VoteDataSave;
import org.bukkit.*;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class Functions {
    public static String IR () {
        return "§bVote >> ";
    }

    /**
     * 本方法不支持带数字的 String：
     *
     * @param nums 是一个 Set<String> 类型的表。
     * @return 这个Set表中的最大 int 类型数字。
     */
    public static int getMaxNumber (Set<String> nums) {
        int maxNumber = -1;
        for (String item : nums) {
            int number = Functions.strToInteger(item);
            if (number > maxNumber) maxNumber = number;
        }
        return maxNumber;
    }


    public static int strToInteger (String input) {
        int num = - 1;
        try {
            num = Integer.parseInt(input);
        } catch (NumberFormatException ignored) {
        }
        return num;
    }

    public static int getIndex (String input) {
        // 定义一个正则表达式来匹配整数
        String regex = "(^\\d+)";
        // 创建 Pattern 对象
        Pattern pattern = Pattern.compile(regex);
        // 创建 Matcher 对象
        Matcher matcher = pattern.matcher(input);
        // 查找匹配
        if (matcher.find()) return Integer.parseInt(matcher.group()); // 返回匹配到的整数值
        // 如果未找到整数，则返回 -1 表示无效
        return - 1;
    }


    public static String formatTime (int seconds) {
        int hours = seconds / 3600;
        int minutes = (seconds % 3600) / 60;
        int secs = seconds % 60;

        StringBuilder timeString = new StringBuilder();

        if (hours > 0) timeString.append("§e").append(hours).append("小时");
        if (minutes > 0) timeString.append("§e").append(minutes).append("分钟");
        if (secs > 0) timeString.append("§e").append(secs).append("秒");
        if (timeString.isEmpty()) timeString.append("§e0秒");

        return timeString.toString();
    }

    public static String getCurrentDateTime() {
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy/MM/dd/HH:mm");
        return now.format(formatter);
    }

    public static String addTime(int years, int months, int days, int hours, int minutes) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime newDateTime = now.plusYears(years)
                .plusMonths(months)
                .plusDays(days)
                .plusHours(hours)
                .plusMinutes(minutes);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy/MM/dd/HH:mm");
        return newDateTime.format(formatter);
    }

    public static String parseAndAddTime(String input) {
        // 正则表达式匹配时间单位，无视大小写
        Pattern pattern = Pattern.compile("(?i)(\\d+)y|(\\d+)min|(\\d+)m|(\\d+)d|(\\d+)h");
        Matcher matcher = pattern.matcher(input);

        int years = 0;
        int months = 0;
        int days = 0;
        int hours = 0;
        int minutes = 0;

        // 匹配并提取值
        while (matcher.find()) {
            if (matcher.group(1) != null) {
                years = Integer.parseInt(matcher.group(1));
            }
            if (matcher.group(2) != null) {
                minutes = Integer.parseInt(matcher.group(2));
            }
            if (matcher.group(3) != null) {
                months = Integer.parseInt(matcher.group(3));
            }
            if (matcher.group(4) != null) {
                days = Integer.parseInt(matcher.group(4));
            }
            if (matcher.group(5) != null) {
                hours = Integer.parseInt(matcher.group(5));
            }
        }

        // 调用addTime方法并传递解析后的时间值
        return addTime(years, months, days, hours, minutes);
    }


    /**
     * 获取Data格式
     */
    public static Date stringToDate (String dateString) {
        if (dateString.length() > 3) {
            // TODO substring 取到的数 index不需要 -1！
            String beginStr = dateString.substring(0,3);
            if (beginStr.equalsIgnoreCase("add"))
                return Functions.stringToDate(dateString);
        }
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy/MM/dd/HH:mm");
        try {
            return formatter.parse(dateString);
        } catch (ParseException e) {
//            VotePlugin.plugin.getServer().getLogger().log(Level.SEVERE, e.getMessage(),e);
            return null; // 如果解析失败，返回null
        }
    }

    /**
     * 检查是否超时
     */
    public static boolean isExpired (Date date) {
        Date currentDate = new Date();
        return date.before(currentDate);
    }

    public static void reloadVote () { // 开服即加载
        Set<String> PVStrings = Objects.requireNonNull(VotePlugin.plugin.getResource().getParentVote().getConfigurationSection("PV")).getKeys(false);
        Set<String> CVStrings = Objects.requireNonNull(VotePlugin.plugin.getResource().getChildVote().getConfigurationSection("CV")).getKeys(false);

        for (String pv : PVStrings)
            VoteDataSave.addParentVote(loadParentVoteFromYML(Functions.strToInteger(pv)));
        for (String cv : CVStrings)
            VoteDataSave.addChildVote(loadChildVoteFromYML(Functions.strToInteger(cv)));

        for (ParentVote parentVote : VoteDataSave.getParentVoteList())
            for (ChildVote childVote : VoteDataSave.getChildVotes())
                if (childVote.getParentVote().equals(parentVote))
                    parentVote.addChildVotes(childVote);

        Iterator<ParentVote> parentVoteList = VoteDataSave.getParentVoteList().iterator();
        while (parentVoteList.hasNext()) {
            ParentVote parentVote = parentVoteList.next();
            if (isExpired(parentVote.getDeadline()))
                parentVote.setEnd(true);
            if (parentVote.getChildVotes().isEmpty())
                parentVoteList.remove();
        }
    }

    /**
     * @param args 传入指令如/ir rename 厕所 总部
     * @param startLength 传入开始截取的长度，如填入 1 时从第三个部分开始截取。
     * @return 返回“厕所 总部”
     * */
    public static String returnArgsToStr (String[] args, int startLength) {
        StringBuilder s = new StringBuilder();
        for (;startLength < args.length ;startLength++) s.append(args[startLength]).append(" ");
        return s.substring(0,s.length() - 1);
    }

    public static ParentVote loadParentVoteFromYML (int id) {
        ParentVote parentVote = VoteDataSave.getPVFromId(id);

        if (parentVote != null)
            return parentVote;

        // 获取 YML 配置
        FileConfiguration config = VotePlugin.plugin.getResource().getParentVote();

        // 从 YML 文件中获取数据
        String title = config.getString("PV." + id + ".Title");
        String description = config.getString("PV." + id + ".Description");
        int voteLimit = config.getInt("PV." + id + ".VoteLimit");

        // 解析保存的 Deadline 日期
        String deadlineString = config.getString("PV." + id + ".Deadline");
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy/MM/dd/HH:mm");
        Date deadline = null;
        try {
            deadline = formatter.parse(deadlineString);
        } catch (ParseException e) {
            VotePlugin.plugin.getServer().getLogger().log(Level.SEVERE, e.getMessage(), e);
            // 可以根据需要处理解析错误
        }

        // 使用构造方法初始化 ParentVote 对象
        return new ParentVote(id, title, description, voteLimit, new HashSet<>(), deadline, false);
    }

    public static ChildVote loadChildVoteFromYML (int id) {
        ChildVote childVote = VoteDataSave.getCVFromId(id);

        if (childVote != null)
            return childVote;

        // 获取 YML 配置
        FileConfiguration config = VotePlugin.plugin.getResource().getChildVote();

        // 从 YML 文件中获取数据
        String title = config.getString("CV." + id + ".Title");
        String description = config.getString("CV." + id + ".Description");

        // 获取保存的玩家UUIDs
        Set<String> UIDs = new HashSet<>(config.getStringList("CV." + id + ".AgreePlayers"));
        Set<OfflinePlayer> agreePlayers = new HashSet<>();
        for (String uid : UIDs) {
            OfflinePlayer player = Bukkit.getOfflinePlayer(UUID.fromString(uid));
            agreePlayers.add(player);
        }

        // 获取 NameTag
        ItemStack itemStack = config.getItemStack("CV." + id + ".NameTag");

        // 获取结束指令
        String command = config.getString("CV." + id + ".Command");

        // 获取 ParentVote ID 并加载 ParentVote 对象
        int parentVoteId = config.getInt("CV." + id + ".PVId");
        ParentVote parentVote = loadParentVoteFromYML(parentVoteId);



        // 使用构造方法初始化 ChildVote 对象
        return new ChildVote(id, title, description, agreePlayers, parentVote, itemStack, command, false);
    }
    /**
     * 计数器，记录调用次数。
     */
    private static int times = 0;

    /**
     * 调试方法，用于记录并输出被调用的次数。
     * <p>
     * 每次调用该方法，计数器 {@code times} 将自增1，并在控制台输出当前调用次数。
     * </p>
     *
     * <pre>
     * 示例输出：
     * 检测第1次
     * 检测第2次
     * ...
     * </pre>
     */
    public static <T> void debugger(T e) {
        times++;
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        System.out.println("检测第" + times + "次，时间：" + now.format(formatter) + (e == null? "" : "，元素：" + e));
    }

    public static Inventory getVoteGui (ParentVote parentVote, Player player) {
        Inventory inventory = Bukkit.createInventory(null, 54,  "正在投票的是:" + parentVote.getTitle() + " , id:" + parentVote.getId());
        ItemStack edge = new ItemStack(Material.STRIPPED_SPRUCE_WOOD);
        ItemStack titleItem = new ItemStack(Material.RED_BANNER);
        ItemStack choose = new ItemStack(Material.PAPER);

        setItemName(edge, " ");
        setItemName(titleItem,  "§c(id:§r" + parentVote.getId() + "§c) §6§l" + parentVote.getTitle());

        addItemLore(titleItem, "描述：");
        addItemLore(titleItem, splitStringByCharacterCount(parentVote.getDescription()));


        List<Integer> slots = getCenteredSlots(parentVote.getChildVotes().size());
        List<ChildVote> childVotes = new ArrayList<>(parentVote.getChildVotes());

        childVotes = sortChildVotesById(childVotes);

        // 选项布置
        int getter = 0;
        for (int index : slots) {
            ItemStack newChoose = choose.clone();
            ChildVote childVote = childVotes.get(getter ++);
            if (childVote.getAgreePlayers().contains(player))
                setChoose(newChoose);
            ItemStack nameTag = childVote.getNameTag();
            boolean nullStack = nameTag == null;
            ItemStack show = nullStack ? newChoose : nameTag.clone(); // 空用默认 有用有
            setItemName(show, "§c(id:" + childVote.getId() + ")§a§l" + childVote.getTitle());
            addItemLore(show, "描述：");
            addItemLore(show, splitStringByCharacterCount(childVote.getDescription()));
            show.setAmount(index);
            inventory.setItem(index, show);
        }
        // 边框布置
        for (int i = 0; i <= 54; i++) {
            if ((i < 9 && i != 4) || (44 < i && i < 54))
                inventory.setItem(i, edge);
            if (i == 4)
                inventory.setItem(i, titleItem);
        }

        return inventory;
    }

    public static HashMap<Integer,ItemStack> getKitMap (ParentVote parentVote) {
        HashMap<Integer, ItemStack> kitMap = new HashMap<>();
        Set<String> indexes;
        try {
            indexes = Objects.requireNonNull(VotePlugin.plugin.getResource().getVotePrice().getConfigurationSection("VP." + parentVote.getId())).getKeys(false);
//            Functions.debugger(indexes);
        } catch (NullPointerException e) {
            return null;
        }
        for (String index : indexes)
            try {
                kitMap.put(Integer.valueOf(index), VotePlugin.plugin.getResource().getVotePrice().getItemStack("VP." + parentVote.getId() + "." + index));
            } catch (NumberFormatException ignored) {
            }
        parentVote.setPriceMap(kitMap);
        return kitMap;
    }

    public static boolean pricePlayer (ParentVote parentVote, Player player) {
        HashMap<Integer, ItemStack> kitMap = getKitMap(parentVote);
        if (kitMap != null && !kitMap.isEmpty()) {
            int delay = 1;
            for (int i : kitMap.keySet())
                dropItemLate(delay++, kitMap.get(i), player.getLocation());
            return true;
        }
        return false;
    }

    public static void dropItemLate (int delay, ItemStack itemStack, Location location) {
        if (location != null) {
            Bukkit.getScheduler().runTaskLater(VotePlugin.plugin, () -> {
                Item item = Objects.requireNonNull(location.getWorld()).dropItemNaturally(location, itemStack.clone());
                item.setGlowing(true);
                item.setVelocity(new Vector(0, 0.5, 0));
                Bukkit.getScheduler().runTaskLater(VotePlugin.plugin, () -> item.setGlowing(false), 20L * 3);
            }, 2L * delay);
        }
    }

    public static Inventory getKitSetterInv (ParentVote parentVote) {
        Inventory inventory = Bukkit.createInventory(null,54,"正在设置" + parentVote.getTitle() + "的奖励箱，id:" + parentVote.getId());
        HashMap<Integer,ItemStack> kits = getKitMap(parentVote);
//        debugger(kits);
        if (kits != null)
            for (int key : kits.keySet())
                inventory.setItem(key, kits.get(key));
        return inventory;
    }

    public static void saveKitInvToYml (ParentVote parentVote, Inventory inventory) {
        for (int i = 0; i < 54; i ++) {
            ItemStack itemStack = inventory.getItem(i);
            VotePlugin.plugin.getResource().getVotePrice().set("VP." + parentVote.getId() + "." + i, itemStack);
        }
        VotePlugin.plugin.getResource().getVotePrice().save();
    }

    public static List<ChildVote> sortChildVotesById(List<ChildVote> childVotes) {
        // 创建一个新的列表以避免修改原始列表
        List<ChildVote> sortedList = new ArrayList<>(childVotes);

        // 使用 Comparator 对列表进行排序
        sortedList.sort(Comparator.comparingInt(ChildVote :: getId));

        return sortedList;
    }

    public static List<String> splitStringByCharacterCount(String input) {
        List<String> lines = new ArrayList<>();
        StringBuilder currentLine = new StringBuilder();
        int currentCount = 0;

        for (char c : input.toCharArray()) {
            int charCount = isChinese(c) ? 3 : 1;

            // 如果当前行字符数超出30，保存当前行，开始新的一行
            if (currentCount + charCount > 30) {
                // 保存数据
                lines.add(currentLine.toString());
                // 重置数据
                currentLine = new StringBuilder();
                currentCount = 0;
            }
            currentLine.append(c);
            currentCount += charCount;
        }
        // 添加最后一行
        if (! currentLine.isEmpty())
            lines.add(currentLine.toString());

        return lines;
    }

    private static boolean isChinese(char c) {
        return Character.UnicodeScript.of(c) == Character.UnicodeScript.HAN;
    }


    public static void setChoose (ItemStack item) {
        ItemMeta itemMeta = item.getItemMeta();
        if (itemMeta != null)
            itemMeta.addEnchant(Enchantment.DURABILITY, 1, true);
        item.setItemMeta(itemMeta);
    }
    public static void cancelChoose (ItemStack item) {
        ItemMeta itemMeta = item.getItemMeta();
        if (itemMeta != null)
            itemMeta.removeEnchant(Enchantment.DURABILITY);
        item.setItemMeta(itemMeta);
    }

    public static void setItemName (ItemStack item, String name) {
        ItemMeta itemMeta = item.getItemMeta();
        if (itemMeta != null)
            itemMeta.setDisplayName(name);
        item.setItemMeta(itemMeta);
    }

    public static void addItemLore (ItemStack item, String... newLores) {
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            List<String> lores = meta.getLore() != null? meta.getLore() : new ArrayList<>();
            lores.addAll(List.of(newLores));
            meta.setLore(lores);
        }
        item.setItemMeta(meta);
    }

    public static void addItemLore (ItemStack item, List<String> newLores) {
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            List<String> lores = meta.getLore() != null? meta.getLore() : new ArrayList<>();
            lores.addAll(newLores);
            meta.setLore(lores);
        }
        item.setItemMeta(meta);
    }
    /**
     * 物品栏对齐算法
     *
     * @param itemCount 传入item的数量
     * @return 给出可以放置的物品栏位置，index从9~35。
     */
    public static List<Integer> getCenteredSlots (int itemCount) { // 最多支持36个
        List<Integer> slots = new ArrayList<>();

        if (itemCount <= 0 || itemCount > 36)
            return slots; // 如果数量小于0或大于27，则返回空列表

        int[] allBase = {0,1,2,3,4,5,6,7,8};
        int[] baseIndices = getBaseIndices(itemCount);

        // 第一个优先级: 传入小于等于9的数量时，基础索引 + 18
        if (itemCount <= 9) {
            for (int baseIndex : baseIndices)
                slots.add(baseIndex + 18);
            return slots;
        }
        // 第二个优先级: 传入小于等于18的数量时，基础索引 + 27
        if (itemCount <= 18) {
            for (int base : allBase)
                slots.add(base + 18);
            for (int baseIndex : baseIndices)
                slots.add(baseIndex + 27);
            return slots;
        }
        // 第三个优先级: 传入小于等于27的数量时，基础索引 + 9
        if (itemCount <= 27) {
            for (int baseIndex : baseIndices)
                slots.add(baseIndex + 9);
            for (int base : allBase)
                slots.add(base + 18);
            for (int base : allBase)
                slots.add(base + 27);
            return slots;
        }
        // 第四个优先级: 传入小于等于36的数量时，基础索引 + 36
        for (int base : allBase)
            slots.add(base + 9);
        for (int base : allBase)
            slots.add(base + 18);
        for (int base : allBase)
            slots.add(base + 27);
        for (int baseIndex : baseIndices)
            slots.add(baseIndex + 36);
        return slots;
    }

    private static int @NotNull [] getBaseIndices (int itemCount) {
        int[] baseIndices = {}; // 从中间向两边扩展的基础索引

        switch (itemCount % 9){
            case 1 -> baseIndices = new int[] {4};
            case 2 -> baseIndices = new int[] {3, 4};
            case 3 -> baseIndices = new int[] {3, 4, 5};
            case 4 -> baseIndices = new int[] {2, 3, 4, 5};
            case 5 -> baseIndices = new int[] {2, 3, 4, 5, 6};
            case 6 -> baseIndices = new int[] {1, 2, 3, 4, 5, 6};
            case 7 -> baseIndices = new int[] {1, 2, 3, 4, 5, 6, 7};
            case 8 -> baseIndices = new int[] {0, 1, 2, 3, 4, 5, 6, 7};
            case 0 -> baseIndices = new int[] {0, 1, 2, 3, 4, 5, 6, 7, 8};
        }
        return baseIndices;
    }

    public static int getChooseNum (Player player, ParentVote parentVote) {
        int count = 0;
        for (ChildVote childVote : parentVote.getChildVotes())
            if (childVote.getAgreePlayers().contains(player))
                count ++;

        return count;
    }

    public static LinkedHashMap<String, Integer> sortByValueDescending(LinkedHashMap<String, Integer> map) {
        // 使用stream对map.entrySet()进行排序
        return map.entrySet()
                .stream()
                .sorted((entry1, entry2) -> entry2.getValue().compareTo(entry1.getValue())) // 由大到小排序
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (oldValue, newValue) -> oldValue, // 如果有重复的key，保留旧的value
                        LinkedHashMap::new // 使用LinkedHashMap来收集结果，保持顺序
                ));
    }

    public static int extractIdFromString(String input) {
        // 使用正则表达式来匹配并提取 id 值
        String idPattern = "id:(\\d+)";
        Pattern pattern = Pattern.compile(idPattern);
        Matcher matcher = pattern.matcher(input);

        if (matcher.find())
            return Integer.parseInt(matcher.group(1)); // 提取并返回匹配到的 id

        return -1;
    }

    /**
     * 计算分子和分母的百分比，并将结果保留一位小数。
     *
     * @param numerator 分子，表示部分的值
     * @param denominator 分母，表示整体的值
     * @return 百分比的字符串表示形式，保留一位小数，并附带百分号（例如："45.0%"）
     * @throws IllegalArgumentException 当分母为 0 时抛出异常，因为无法进行除零运算
     */
    public static String getPercent(int numerator, int denominator) {
        if (denominator == 0)
            throw new IllegalArgumentException("分母不能为0");
        // 计算百分比，并保留一位小数
        double percentage = ((double) numerator / denominator) * 100;
        return String.format("%.1f%%", percentage);
    }

    public static void removeAllEnchantments(ItemStack item) {
        if (item != null)
            item.getEnchantments().forEach((enchantment, level) -> item.removeEnchantment(enchantment));
    }

    public static void launchFirework (Location location) {
        Firework firework = Objects.requireNonNull(location.getWorld()).spawn(location.add(0.5, 0, 0.5).clone(), Firework.class);

        FireworkMeta meta = firework.getFireworkMeta();
        FireworkEffect effect = FireworkEffect.builder()
                .withColor(Color.fromRGB(91, 117, 238), Color.WHITE, Color.fromRGB(173, 186, 247))
                .withFade(Color.ORANGE)
                .with(FireworkEffect.Type.BALL_LARGE)
                .withTrail()
                .withFlicker()
                .build();
        meta.addEffect(effect);
        meta.setPower(0);
        firework.setFireworkMeta(meta);
        Bukkit.getScheduler().runTaskLater(VotePlugin.plugin, firework :: detonate, 0);
    }
}
