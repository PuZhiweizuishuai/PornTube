package com.buguagaoshu.porntube.utils;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.metadata.OrderItem;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import java.util.Map;

/**
 * @author Pu Zhiwei {@literal puzhiweipuzhiwei@foxmail.com}
 * create          2020-05-05 17:22
 */
public class Query<T> {
    public IPage<T> getPage(Map<String, Object> params) {
        return this.getPage(params, null, false);
    }

    public IPage<T> getPage(Map<String, Object> params, String defaultOrderField, boolean isAsc) {
        // 分页参数
        long curPage = 1;
        long limit = 10;

        /**
         * 此处如果不捕获处理异常
         *  由 CurriculumServiceImpl 类的 selectUserCurriculumList
         *  方法查询的分页参数会有奇怪的bug
         *  目前原因未知
         *  class com.baomidou.mybatisplus.extension.plugins.pagination.Page cannot be cast to class java.lang.String (com.baomidou.mybatisplus.extension.plugins.pagination.Page is in unnamed module of loader 'app'; java.lang.String is in module java.base of loader '
         * */
        if (params.get(Constant.PAGE) != null) {
            try {
                curPage = Long.parseLong((String) params.get(Constant.PAGE));
            } catch (Exception ignored) {
            }
        }
        if (params.get(Constant.LIMIT) != null) {
            try {
                limit = Long.parseLong((String) params.get(Constant.LIMIT));
            } catch (Exception ignored) {
            }

        }

        // 分页对象
        Page<T> page = new Page<>(curPage, limit);

        // 分页参数
        params.put(Constant.PAGE, page);

        // 排序字段
        // 防止SQL注入（因为sidx、order是通过拼接SQL实现排序的，会有SQL注入风险）
        String orderField = SQLFilter.sqlInject((String) params.get(Constant.ORDER_FIELD));
        String order = (String) params.get(Constant.ORDER);


        // 前端字段排序
        if (StringUtils.isNotBlank(orderField) && StringUtils.isNotBlank(order)) {
            if (Constant.ASC.equalsIgnoreCase(order)) {
                return page.addOrder(OrderItem.asc(orderField));
            } else {
                return page.addOrder(OrderItem.desc(orderField));
            }
        }

        // 没有排序字段，则不排序
        if (StringUtils.isBlank(defaultOrderField)) {
            return page;
        }

        // 默认排序
        if (isAsc) {
            page.addOrder(OrderItem.asc(defaultOrderField));
        } else {
            page.addOrder(OrderItem.desc(defaultOrderField));
        }

        return page;
    }
}
