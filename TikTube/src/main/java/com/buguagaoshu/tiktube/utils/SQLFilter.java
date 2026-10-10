package com.buguagaoshu.tiktube.utils;


import java.util.Collection;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * @author Pu Zhiwei {@literal puzhiweipuzhiwei@foxmail.com}
 * create          2020-05-05 17:25
 * 拼接进 SQL 的标识符（列名、排序列）过滤
 */
public class SQLFilter {

    /**
     * 合法标识符：字母或下划线开头，只含字母、数字、下划线，最长 64
     */
    private static final Pattern IDENTIFIER = Pattern.compile("^[A-Za-z_][A-Za-z0-9_]{0,63}$");

    /**
     * 允许通过 ${col} 拼进计数更新语句的列
     * 新增计数列时必须同步登记，否则服务层校验会直接拒绝
     */
    public static final Set<String> COUNT_COLUMNS = Set.of(
            "view_count", "comment_count", "like_count", "dislike_count",
            "favorite_count", "danmaku_count", "score_count", "submit_count",
            "follow_count", "fans_count", "use_tokens");

    /**
     * 是否是可以直接拼进 SQL 的标识符
     */
    public static boolean isSafeIdentifier(String name) {
        return name != null && IDENTIFIER.matcher(name).matches();
    }

    /**
     * 取前端传来的排序列
     * 排序列最终会被拼到 ORDER BY 后面，只有命中调用方给出的白名单才生效，
     * 其余情况一律忽略前端排序，使用服务端默认排序
     *
     * @param rawOrderField      前端传来的排序列
     * @param allowedOrderFields 允许排序的列，为 null 表示不允许前端指定排序
     * @return 可用的排序列，不可用时返回 null
     */
    public static String safeOrderField(String rawOrderField, Collection<String> allowedOrderFields) {
        if (rawOrderField == null || allowedOrderFields == null) {
            return null;
        }
        String field = rawOrderField.toLowerCase();
        if (!isSafeIdentifier(field)) {
            return null;
        }
        for (String allowed : allowedOrderFields) {
            if (field.equalsIgnoreCase(allowed)) {
                return field;
            }
        }
        return null;
    }

    /**
     * 校验要拼进 ${col} 的列名，未命中白名单直接拒绝
     */
    public static String checkedColumn(String column, Set<String> whitelist) {
        if (column == null || !isSafeIdentifier(column) || !whitelist.contains(column)) {
            throw new IllegalArgumentException("非法的数据库列名！");
        }
        return column;
    }

    /**
     * 计数列校验
     */
    public static String checkedCountColumn(String column) {
        return checkedColumn(column, COUNT_COLUMNS);
    }
}
