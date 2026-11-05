package com.example.dzl.utils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.*;

@Component
public class RedisCache
{

    @Autowired
    public StringRedisTemplate stringRedisTemplate;

    /**
     * 缓存字符串（基于 StringRedisTemplate，UTF-8 序列化，无乱码）
     * 适用于 Token、JSON 字符串、普通文本等场景
     *
     * @param key 缓存键
     * @param value 字符串值
     */
    public void setCacheString(final String key, final String value)
    {
        stringRedisTemplate.opsForValue().set(key, value);
    }

    /**
     * 缓存字符串（带过期时间，基于 StringRedisTemplate）
     *
     * @param key 缓存键
     * @param value 字符串值
     * @param timeout 过期时间
     */
    public void setCacheString(final String key, final String value, final long timeout)
    {
        stringRedisTemplate.opsForValue().set(key, value, Duration.ofSeconds( timeout));
    }

    /**
     * 获取缓存的字符串（基于 StringRedisTemplate，纯字符串返回，无反序列化）
     *
     * @param key 缓存键
     * @return 字符串值（null表示无缓存）
     */
    public String getCacheString(final String key)
    {
        return stringRedisTemplate.opsForValue().get(key);
    }

    /**
     * 设置字符串缓存的过期时间（基于 StringRedisTemplate）
     * @param key 缓存键
     * @param timeout 过期时间
     */
    public void expireString(final String key, final long timeout) {

        stringRedisTemplate.expire(key, Duration.ofSeconds(timeout));
    }

    /**
     * 删除字符串缓存（基于 StringRedisTemplate）
     * @param key 缓存键
     */
    public void deleteString(final String key)
    {
        stringRedisTemplate.delete(key);
    }

    /**
     * 缓存字符串List（基于 StringRedisTemplate，纯字符串列表）
     *
     * @param key 缓存键
     * @param dataList 字符串列表
     * @return 缓存的元素个数
     */
    public long setCacheStringList(final String key, final List<String> dataList)
    {
        Long count = stringRedisTemplate.opsForList().rightPushAll(key, dataList);
        return count == null ? 0 : count;
    }

    /**
     * 获取字符串List缓存（基于 StringRedisTemplate）
     *
     * @param key 缓存键
     * @return 字符串列表
     */
    public List<String> getCacheStringList(final String key)
    {
        return stringRedisTemplate.opsForList().range(key, 0, -1);
    }

    /**
     * 缓存字符串Hash（基于 StringRedisTemplate，Key/Value 均为字符串）
     *
     * @param key 缓存键
     * @param dataMap 字符串Hash表
     */
    public void setCacheStringHash(final String key, final Map<String, String> dataMap)
    {
        if (dataMap != null && !dataMap.isEmpty()) {
            stringRedisTemplate.opsForHash().putAll(key, dataMap);
        }
    }

    /**
     * 往字符串Hash中存入单个值（基于 StringRedisTemplate）
     *
     * @param key 缓存键
     * @param hKey Hash键
     * @param value 字符串值
     */
    public void setCacheStringHashValue(final String key, final String hKey, final String value)
    {
        stringRedisTemplate.opsForHash().put(key, hKey, value);
    }

    /**
     * 获取字符串Hash中的单个值（基于 StringRedisTemplate）
     *
     * @param key 缓存键
     * @param hKey Hash键
     * @return 字符串值
     */
    public String getCacheStringHashValue(final String key, final String hKey)
    {
        Object value = stringRedisTemplate.opsForHash().get(key, hKey);
        return value == null ? null : value.toString();
    }

    /**
     * 获取完整的字符串Hash（基于 StringRedisTemplate）
     *
     * @param key 缓存键
     * @return 字符串Hash表
     */
    public Map<Object, Object> getCacheStringHash(final String key)
    {
        return stringRedisTemplate.opsForHash().entries(key);
    }

    /**
     * 获取字符串类型的所有匹配键（基于 StringRedisTemplate）
     *
     * @param pattern 键前缀/通配符（如 "refresh:*"）
     * @return 匹配的键列表
     */
    public Set<String> keysString(final String pattern)
    {
        return stringRedisTemplate.keys(pattern);
    }
}