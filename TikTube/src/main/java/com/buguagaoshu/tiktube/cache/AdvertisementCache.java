package com.buguagaoshu.tiktube.cache;

import com.buguagaoshu.tiktube.entity.AdvertisementEntity;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Pu Zhiwei {@literal puzhiweipuzhiwei@foxmail.com}
 * @create 2025-05-03
 */
public class AdvertisementCache {
    public static volatile Map<Integer, List<AdvertisementEntity>> advertisementCache = new HashMap<Integer, List<AdvertisementEntity>>();


}
