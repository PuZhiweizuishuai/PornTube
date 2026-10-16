package com.buguagaoshu.tiktube.utils;

import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SQLFilterTest {

    @Test
    void onlyPlainIdentifiersAreSafe() {
        assertTrue(SQLFilter.isSafeIdentifier("create_time"));
        assertTrue(SQLFilter.isSafeIdentifier("viewCount2"));
        assertFalse(SQLFilter.isSafeIdentifier("create_time desc"));
        assertFalse(SQLFilter.isSafeIdentifier("(select 1)"));
        assertFalse(SQLFilter.isSafeIdentifier("create_time; drop table user"));
        assertFalse(SQLFilter.isSafeIdentifier("`create_time`"));
        assertFalse(SQLFilter.isSafeIdentifier(null));
    }

    @Test
    void orderFieldMustHitTheWhitelist() {
        Set<String> allowed = Set.of("create_time", "view_count");
        assertEquals("create_time", SQLFilter.safeOrderField("create_time", allowed));
        assertEquals("view_count", SQLFilter.safeOrderField("VIEW_COUNT", allowed));
        // 未登记的列、非法字符、没有白名单时都不接受前端排序
        assertNull(SQLFilter.safeOrderField("password", allowed));
        assertNull(SQLFilter.safeOrderField("create_time desc", allowed));
        assertNull(SQLFilter.safeOrderField("create_time", null));
    }

    @Test
    void countColumnMustBeWhitelisted() {
        assertEquals("fans_count", SQLFilter.checkedCountColumn("fans_count"));
        assertThrows(IllegalArgumentException.class, () -> SQLFilter.checkedCountColumn("password"));
        assertThrows(IllegalArgumentException.class, () -> SQLFilter.checkedCountColumn("view_count = view_count + 1 -- "));
        assertThrows(IllegalArgumentException.class, () -> SQLFilter.checkedCountColumn(null));
    }

    @Test
    void likeWildcardsAreEscaped() {
        assertEquals("", SQLFilter.escapeLike(null));
        assertEquals("普通关键词", SQLFilter.escapeLike("普通关键词"));
        // 用户传一个 % 不应该变成无条件全表匹配
        assertEquals("\\%", SQLFilter.escapeLike("%"));
        assertEquals("\\_", SQLFilter.escapeLike("_"));
        assertEquals("100\\%", SQLFilter.escapeLike("100%"));
    }

    @Test
    void escapeLikeHandlesBackslashBeforeWildcards() {
        // 反斜杠必须最先转义，否则上一步补出来的转义符会被再转义一次
        assertEquals("\\\\", SQLFilter.escapeLike("\\"));
        assertEquals("a\\\\b", SQLFilter.escapeLike("a\\b"));
        assertEquals("a\\\\\\%b", SQLFilter.escapeLike("a\\%b"));
        assertEquals("50\\%\\_off\\\\", SQLFilter.escapeLike("50%_off\\"));
    }
}
