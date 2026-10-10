package com.buguagaoshu.tiktube.utils;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.metadata.OrderItem;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class QueryTest {

    private final Set<String> allowed = Set.of("create_time");

    private Map<String, Object> params(String sidx, String order) {
        Map<String, Object> params = new HashMap<>();
        params.put(Constant.ORDER_FIELD, sidx);
        params.put(Constant.ORDER, order);
        return params;
    }

    @Test
    void clientOrderFieldIsIgnoredWithoutWhitelist() {
        IPage<Object> page = new Query<>().getPage(params("create_time", "asc"));

        assertTrue(page.orders().isEmpty());
    }

    @Test
    void onlyWhitelistedOrderFieldReachesOrderBy() {
        // 未登记的列
        assertTrue(new Query<>().getPage(params("password", "asc"), null, false, allowed).orders().isEmpty());
        // 拼带额外内容的排序列
        assertTrue(new Query<>().getPage(params("create_time desc", "asc"), null, false, allowed).orders().isEmpty());

        List<OrderItem> orders = new Query<>().getPage(params("CREATE_TIME", "desc"), null, false, allowed).orders();
        assertEquals(1, orders.size());
        assertEquals("create_time", orders.get(0).getColumn());
        assertFalse(orders.get(0).isAsc());
    }
}
