package me.smerrybeta.object;

import java.util.ArrayList;
import java.util.List;

public class ChildBuilder {
    private int id;
    private String title;
    private String description;

    // 构造函数
    public ChildBuilder(int id, String title, String description) {
        this.id = id;
        this.title = title;
        this.description = description;
    }

    // Getters
    public int getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    // 工具方法：解析格式字符串并返回 ChildBuilder 对象
    public static ChildBuilder parseString(String input, int id) {
        String[] parts = input.split("\":\"");
        if (parts.length == 2) {
            String title = parts[0].substring(1); // 去掉开头的引号
            String description = parts[1].substring(0, parts[1].length() - 1); // 去掉结尾的引号
            return new ChildBuilder(id, title, description);
        }
        return null;
    }

    // 工具方法：处理 args 数组并返回 ChildBuilder 对象列表
    public static List<ChildBuilder> parseArgs(String[] args) {
        List<ChildBuilder> builders = new ArrayList<>();
        int idCounter = 1; // 用于生成唯一的 id
        for (int i = 5; i < args.length; i++) {
            ChildBuilder builder = parseString(args[i], idCounter++);
            if (builder != null)
                builders.add(builder);
        }
        return builders;
    }

    // 示例方法，显示 ChildBuilder 对象的信息
    public void display() {
        System.out.println("ID: " + id + ", Title: " + title + ", Description: " + description);
    }
}