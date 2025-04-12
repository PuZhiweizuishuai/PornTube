package com.buguagaoshu.tiktube.model;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.metadata.OrderItem;

import java.util.List;

/**
 * @author Pu Zhiwei {@literal puzhiweipuzhiwei@foxmail.com}
 * create          2022-08-26 9:20
 */
public class CustomPage<T> implements IPage<T> {
    private List<T> records;

    private long total;

    private long size;

    private long current;

    public CustomPage(List<T> records, long total, long size, long current) {
        this.records = records;
        this.total = total;
        this.size = size;
        this.current = current;
    }


    @Override
    public List<OrderItem> orders() {
        return null;
    }

    @Override
    public List<T> getRecords() {
        return records;
    }

    @Override
    public IPage<T> setRecords(List<T> records) {
        return null;
    }

    @Override
    public long getTotal() {
        return total;
    }

    @Override
    public IPage<T> setTotal(long total) {
        return null;
    }

    @Override
    public long getSize() {
        return size;
    }

    @Override
    public IPage<T> setSize(long size) {
        return null;
    }

    @Override
    public long getCurrent() {
        return current;
    }

    @Override
    public IPage<T> setCurrent(long current) {
        return null;
    }
}
