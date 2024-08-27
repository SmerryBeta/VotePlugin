package me.smerrybeta;

import me.smerrybeta.util.Functions;
import org.jetbrains.annotations.NotNull;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static me.smerrybeta.util.Functions.addTime;

//TIP 要<b>运行</b>代码，请按 <shortcut actionId="Run"/> 或
// 点击间距中的 <icon src="AllIcons.Actions.Execute"/> 图标。
public class Main {
    public static String generateVoteCreateCommand(int itemCount) {
        // 基础命令部分
        StringBuilder command = new StringBuilder("/vote create 测试 test 2024/08/26/12:00 2");

        // 根据传入的itemCount生成子选项
        for (int i = 1; i <= itemCount; i++) {
            command.append(" \"").append(i).append("\":\"").append(i).append("\"");
        }

        // 返回完整的命令
        return command.toString();
    }


    public static void main(String[] args) {
//        for (int i = 0; i < 37 ; i ++)
//            System.out.println(generateVoteCreateCommand(i));
        ;
    }
}