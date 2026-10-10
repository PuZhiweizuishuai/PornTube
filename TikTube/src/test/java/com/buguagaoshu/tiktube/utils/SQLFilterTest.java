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
}
