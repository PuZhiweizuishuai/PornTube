package com.buguagaoshu.tiktube.repository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.connection.ReturnType;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * Redis操作仓库
 * @author Pu Zhiwei {@literal puzhiweipuzhiwei@foxmail.com}
 * @create 2025-05-14
 */
@Repository
public class RedisRepository {

    private RedisTemplate<String, Object> redisTemplate;

    /**
     * 使用@Autowired(required = false)注解
     * 当Redis未启用时，不会报错
     */
    @Autowired(required = false)
    public void setRedisTemplate(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    /**
     * 检查Redis是否可用
     */
    public boolean isAvailable() {
        return redisTemplate != null;
    }

    /**
     * 指定缓存失效时间
     * @param key 键
     * @param time 时间(秒)
     */
    public boolean expire(String key, long time) {
        if (!isAvailable()) return false;
        try {
            if (time > 0) {
                redisTemplate.expire(key, Duration.ofSeconds(time));
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 获取过期时间
     * @param key 键
     * @return 时间(秒) 返回0代表永久有效
     */
    public long getExpire(String key) {
        if (!isAvailable()) return 0;
        return redisTemplate.getExpire(key, TimeUnit.SECONDS);
    }

    /**
     * 判断key是否存在
     * @param key 键
     * @return true 存在 false不存在
     */
    public boolean hasKey(String key) {
        if (!isAvailable()) return false;
        try {
            return Boolean.TRUE.equals(redisTemplate.hasKey(key));
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 删除缓存
     * @param key 可以传一个或多个值
     */
    public void del(String... key) {
        if (!isAvailable()) return;
        if (key != null && key.length > 0) {
            if (key.length == 1) {
                redisTemplate.delete(key[0]);
            } else {
                redisTemplate.delete(List.of(key));
            }
        }
    }

    // ============================String=============================
    /**
     * 普通缓存获取
     * @param key 键
     * @return 值
     */
    public Object get(String key) {
        if (!isAvailable()) return null;
        return key == null ? null : redisTemplate.opsForValue().get(key);
    }

    // =========================计数 Hash（原始字节序列化，与 HINCRBY 写入格式一致）=========================

    /**
     * 原子取走整个 Hash 并删除，多实例并发排空时每个 field 的增量只会被取走一次
     */
    private static final byte[] DRAIN_HASH_SCRIPT =
            ("local v = redis.call('HGETALL', KEYS[1]) " +
                    "redis.call('DEL', KEYS[1]) " +
                    "return v").getBytes(StandardCharsets.UTF_8);

    private static byte[] toBytes(String value) {
        return value.getBytes(StandardCharsets.UTF_8);
    }

    private static String fromBytes(Object raw) {
        return new String((byte[]) raw, StandardCharsets.UTF_8);
    }

    /**
     * 计数自增（HINCRBY），支持负数回退
     */
    public long hIncrBy(String key, String field, long delta) {
        if (!isAvailable()) return 0;
        Long value = redisTemplate.execute((RedisCallback<Long>) connection ->
                connection.hashCommands().hIncrBy(toBytes(key), toBytes(field), delta));
        return value == null ? 0 : value;
    }

    /**
     * 读取单个计数，不存在返回 0
     */
    public long hGetCount(String key, String field) {
        if (!isAvailable()) return 0;
        byte[] raw = redisTemplate.execute((RedisCallback<byte[]>) connection ->
                connection.hashCommands().hGet(toBytes(key), toBytes(field)));
        return raw == null ? 0 : Long.parseLong(fromBytes(raw));
    }

    /**
     * 读取整个计数 Hash
     */
    public Map<String, Long> hGetAllCounts(String key) {
        if (!isAvailable()) return Map.of();
        Map<byte[], byte[]> raw = redisTemplate.execute((RedisCallback<Map<byte[], byte[]>>) connection ->
                connection.hashCommands().hGetAll(toBytes(key)));
        Map<String, Long> result = new HashMap<>();
        if (raw != null) {
            raw.forEach((k, v) -> result.put(fromBytes(k), Long.parseLong(fromBytes(v))));
        }
        return result;
    }

    /**
     * 批量读取指定的若干计数，一次 HMGET 完成，返回值只包含存在的 field
     */
    public Map<String, Long> hMultiGetCounts(String key, Collection<String> fields) {
        if (!isAvailable() || fields == null || fields.isEmpty()) return Map.of();
        List<String> orderedFields = List.copyOf(fields);
        byte[][] rawFields = orderedFields.stream().map(RedisRepository::toBytes).toArray(byte[][]::new);
        List<byte[]> values = redisTemplate.execute((RedisCallback<List<byte[]>>) connection ->
                connection.hashCommands().hMGet(toBytes(key), rawFields));
        Map<String, Long> result = new HashMap<>();
        if (values == null) {
            return result;
        }
        for (int i = 0; i < orderedFields.size() && i < values.size(); i++) {
            byte[] raw = values.get(i);
            if (raw != null) {
                result.put(orderedFields.get(i), Long.parseLong(fromBytes(raw)));
            }
        }
        return result;
    }

    /**
     * Lua 原子地取走整个计数 Hash 并删除，后续写入落在全新 Hash 上，无丢数窗口
     */
    public Map<String, Long> hDrain(String key) {
        if (!isAvailable()) return Map.of();
        List<Object> raw = redisTemplate.execute((RedisCallback<List<Object>>) connection ->
                connection.scriptingCommands().eval(DRAIN_HASH_SCRIPT, ReturnType.MULTI, 1, toBytes(key)));
        Map<String, Long> result = new HashMap<>();
        if (raw != null) {
            for (int i = 0; i + 1 < raw.size(); i += 2) {
                result.put(fromBytes(raw.get(i)), Long.parseLong(fromBytes(raw.get(i + 1))));
            }
        }
        return result;
    }

    /**
     * 普通缓存放入
     * @param key 键
     * @param value 值
     * @return true成功 false失败
     */
    public boolean set(String key, Object value) {
        if (!isAvailable()) return false;
        try {
            redisTemplate.opsForValue().set(key, value);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 普通缓存放入并设置时间
     * @param key 键
     * @param value 值
     * @param time 时间(秒) time要大于0 如果time小于等于0 将设置无限期
     * @return true成功 false 失败
     */
    public boolean set(String key, Object value, long time) {
        if (!isAvailable()) return false;
        try {
            if (time > 0) {
                redisTemplate.opsForValue().set(key, value, Duration.ofSeconds(time));
            } else {
                set(key, value);
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 递增
     * @param key 键
     * @param delta 要增加几(大于0)
     * @return
     */
    public long incr(String key, long delta) {
        if (!isAvailable()) return 0;
        if (delta < 0) {
            throw new RuntimeException("递增因子必须大于0");
        }
        return redisTemplate.opsForValue().increment(key, delta);
    }

    /**
     * 递减
     * @param key 键
     * @param delta 要减少几(小于0)
     * @return
     */
    public long decr(String key, long delta) {
        if (!isAvailable()) return 0;
        if (delta < 0) {
            throw new RuntimeException("递减因子必须大于0");
        }
        return redisTemplate.opsForValue().increment(key, -delta);
    }

    // ================================Map=================================
    /**
     * HashGet
     * @param key 键 不能为null
     * @param item 项 不能为null
     * @return 值
     */
    public Object hget(String key, String item) {
        if (!isAvailable()) return null;
        return redisTemplate.opsForHash().get(key, item);
    }

    /**
     * 获取hashKey对应的所有键值
     * @param key 键
     * @return 对应的多个键值
     */
    public Map<Object, Object> hmget(String key) {
        if (!isAvailable()) return null;
        return redisTemplate.opsForHash().entries(key);
    }

    /**
     * HashSet
     * @param key 键
     * @param map 对应多个键值
     * @return true 成功 false 失败
     */
    public boolean hmset(String key, Map<String, Object> map) {
        if (!isAvailable()) return false;
        try {
            redisTemplate.opsForHash().putAll(key, map);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * HashSet 并设置时间
     * @param key 键
     * @param map 对应多个键值
     * @param time 时间(秒)
     * @return true成功 false失败
     */
    public boolean hmset(String key, Map<String, Object> map, long time) {
        if (!isAvailable()) return false;
        try {
            redisTemplate.opsForHash().putAll(key, map);
            if (time > 0) {
                expire(key, time);
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 向一张hash表中放入数据,如果不存在将创建
     * @param key 键
     * @param item 项
     * @param value 值
     * @return true 成功 false失败
     */
    public boolean hset(String key, String item, Object value) {
        if (!isAvailable()) return false;
        try {
            redisTemplate.opsForHash().put(key, item, value);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 向一张hash表中放入数据,如果不存在将创建
     * @param key 键
     * @param item 项
     * @param value 值
     * @param time 时间(秒) 注意:如果已存在的hash表有时间,这里将会替换原有的时间
     * @return true 成功 false失败
     */
    public boolean hset(String key, String item, Object value, long time) {
        if (!isAvailable()) return false;
        try {
            redisTemplate.opsForHash().put(key, item, value);
            if (time > 0) {
                expire(key, time);
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 删除hash表中的值
     * @param key 键 不能为null
     * @param item 项 可以使多个 不能为null
     */
    public void hdel(String key, Object... item) {
        if (!isAvailable()) return;
        redisTemplate.opsForHash().delete(key, item);
    }

    /**
     * 判断hash表中是否有该项的值
     * @param key 键 不能为null
     * @param item 项 不能为null
     * @return true 存在 false不存在
     */
    public boolean hHasKey(String key, String item) {
        if (!isAvailable()) return false;
        return redisTemplate.opsForHash().hasKey(key, item);
    }

    /**
     * hash递增 如果不存在,就会创建一个 并把新增后的值返回
     * @param key 键
     * @param item 项
     * @param by 要增加几(大于0)
     * @return
     */
    public double hincr(String key, String item, double by) {
        if (!isAvailable()) return 0;
        return redisTemplate.opsForHash().increment(key, item, by);
    }

    /**
     * hash递减
     * @param key 键
     * @param item 项
     * @param by 要减少记(小于0)
     * @return
     */
    public double hdecr(String key, String item, double by) {
        if (!isAvailable()) return 0;
        return redisTemplate.opsForHash().increment(key, item, -by);
    }

    // ============================set=============================
    /**
     * 根据key获取Set中的所有值
     * @param key 键
     * @return
     */
    public Set<Object> sGet(String key) {
        if (!isAvailable()) return null;
        try {
            return redisTemplate.opsForSet().members(key);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 根据value从一个set中查询,是否存在
     * @param key 键
     * @param value 值
     * @return true 存在 false不存在
     */
    public boolean sHasKey(String key, Object value) {
        if (!isAvailable()) return false;
        try {
            return Boolean.TRUE.equals(redisTemplate.opsForSet().isMember(key, value));
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 将数据放入set缓存
     * @param key 键
     * @param values 值 可以是多个
     * @return 成功个数
     */
    public long sSet(String key, Object... values) {
        if (!isAvailable()) return 0;
        try {
            return redisTemplate.opsForSet().add(key, values);
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    /**
     * 将set数据放入缓存
     * @param key 键
     * @param time 时间(秒)
     * @param values 值 可以是多个
     * @return 成功个数
     */
    public long sSetAndTime(String key, long time, Object... values) {
        if (!isAvailable()) return 0;
        try {
            Long count = redisTemplate.opsForSet().add(key, values);
            if (time > 0) {
                expire(key, time);
            }
            return count;
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    /**
     * 获取set缓存的长度
     * @param key 键
     * @return
     */
    public long sGetSetSize(String key) {
        if (!isAvailable()) return 0;
        try {
            return redisTemplate.opsForSet().size(key);
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    /**
     * 移除值为value的
     * @param key 键
     * @param values 值 可以是多个
     * @return 移除的个数
     */
    public long setRemove(String key, Object... values) {
        if (!isAvailable()) return 0;
        try {
            return redisTemplate.opsForSet().remove(key, values);
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    // ===============================list=================================
    /**
     * 获取list缓存的内容
     * @param key 键
     * @param start 开始
     * @param end 结束 0 到 -1代表所有值
     * @return
     */
    public List<Object> lGet(String key, long start, long end) {
        if (!isAvailable()) return null;
        try {
            return redisTemplate.opsForList().range(key, start, end);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 获取list缓存的长度
     * @param key 键
     * @return
     */
    public long lGetListSize(String key) {
        if (!isAvailable()) return 0;
        try {
            return redisTemplate.opsForList().size(key);
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    /**
     * 通过索引 获取list中的值
     * @param key 键
     * @param index 索引 index>=0时， 0 表头，1 第二个元素，依次类推；index<0时，-1，表尾，-2倒数第二个元素，依次类推
     * @return
     */
    public Object lGetIndex(String key, long index) {
        if (!isAvailable()) return null;
        try {
            return redisTemplate.opsForList().index(key, index);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 根据模式获取键
     * @param pattern 模式
     * @return 匹配的键集合
     */
    public Set<String> keys(String pattern) {
        if (!isAvailable()) return java.util.Collections.emptySet();
        try {
            return redisTemplate.keys(pattern);
        } catch (Exception e) {
            e.printStackTrace();
            return java.util.Collections.emptySet();
        }
    }
    
    /**
     * 使用scan命令根据模式获取键，性能更好
     * @param pattern 模式
     * @return 匹配的键集合
     */
    public Set<String> scan(String pattern) {
        if (!isAvailable()) return java.util.Collections.emptySet();
        try {
            Set<String> keys = new java.util.HashSet<>();
            redisTemplate.execute((connection) -> {
                try (org.springframework.data.redis.core.Cursor<byte[]> cursor = 
                        connection.keyCommands().scan(
                            org.springframework.data.redis.core.ScanOptions.scanOptions()
                                .match(pattern)
                                .count(100)
                                .build())) {
                    while (cursor.hasNext()) {
                        keys.add(new String(cursor.next()));
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return null;
            }, true);
            return keys;
        } catch (Exception e) {
            e.printStackTrace();
            return java.util.Collections.emptySet();
        }
    }

    /**
     * 将list放入缓存
     * @param key 键
     * @param value 值
     * @return
     */
    public boolean lSet(String key, Object value) {
        if (!isAvailable()) return false;
        try {
            redisTemplate.opsForList().rightPush(key, value);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 将list放入缓存
     * @param key 键
     * @param value 值
     * @param time 时间(秒)
     * @return
     */
    public boolean lSet(String key, Object value, long time) {
        if (!isAvailable()) return false;
        try {
            redisTemplate.opsForList().rightPush(key, value);
            if (time > 0) {
                expire(key, time);
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 将list放入缓存
     * @param key 键
     * @param value 值
     * @return
     */
    public boolean lSet(String key, List<Object> value) {
        if (!isAvailable()) return false;
        try {
            redisTemplate.opsForList().rightPushAll(key, value);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 将list放入缓存
     * @param key 键
     * @param value 值
     * @param time 时间(秒)
     * @return
     */
    public boolean lSet(String key, List<Object> value, long time) {
        if (!isAvailable()) return false;
        try {
            redisTemplate.opsForList().rightPushAll(key, value);
            if (time > 0) {
                expire(key, time);
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 根据索引修改list中的某条数据
     * @param key 键
     * @param index 索引
     * @param value 值
     * @return
     */
    public boolean lUpdateIndex(String key, long index, Object value) {
        if (!isAvailable()) return false;
        try {
            redisTemplate.opsForList().set(key, index, value);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 移除N个值为value
     * @param key 键
     * @param count 移除多少个
     * @param value 值
     * @return 移除的个数
     */
    public long lRemove(String key, long count, Object value) {
        if (!isAvailable()) return 0;
        try {
            return redisTemplate.opsForList().remove(key, count, value);
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }
}
