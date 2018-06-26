package com.sap.cisp.xhna.data.common.cache;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import com.sap.cisp.xhna.data.common.Util;

public class LRUCacheConfig {

    String description = "Concurrent LRU Cache Config";

    @SuppressWarnings("rawtypes")
    private static ConcurrentHashMap<String, ConcurrentLRUCache> cacheMap = new ConcurrentHashMap<String, ConcurrentLRUCache>();
    // contains the statistics objects for all open caches of the same type
    private static List<ConcurrentLRUCache.Stats> statsList;

    static {
        loadConfig();
    }

    public static LRUCacheConfig getInstance() {
        return LRUCacheConfigHolder.instance;
    }

    private static class LRUCacheConfigHolder {
        public static LRUCacheConfig instance = new LRUCacheConfig();
    }

    private LRUCacheConfig() {
    }

    @SuppressWarnings("rawtypes")
    private static void loadConfig() {
        // must be the first time a cache of this type is being created
        // Use a CopyOnWriteArrayList since puts are very rare and iteration
        // may be a frequent operation
        // because it is used in getStatistics()
        statsList = new CopyOnWriteArrayList<>();

        // the first entry will be for cumulative stats of caches that have
        // been closed.
        statsList.add(new ConcurrentLRUCache.Stats());

        List<Map<String, String>> paramsList = Util
                .getLRUCacheConfig("configurations/LRUCacheConfig.xml");

        for (Map<String, String> params : paramsList) {
            params.put("cleanupThread", "false");
            params.put("showItems", String.valueOf(0));
            ConcurrentLRUCache cache = (ConcurrentLRUCache) LRUCacheConfig
                    .newInstance(params.get("cacheName"), params);
            cache.setAlive(true);
        }
    }

    @SuppressWarnings("rawtypes")
    public static Cache init(String cacheName, Map args) {
        String str = (String) args.get("size");
        int limit = str == null ? 1024 : Integer.parseInt(str);
        int minLimit;
        // int showItems = 0;
        str = (String) args.get("minSize");
        if (str == null) {
            minLimit = (int) (limit * 0.9);
        } else {
            minLimit = Integer.parseInt(str);
        }
        if (minLimit == 0)
            minLimit = 1;
        if (limit <= minLimit)
            limit = minLimit + 1;

        int acceptableLimit;
        str = (String) args.get("acceptableSize");
        if (str == null) {
            acceptableLimit = (int) (limit * 0.95);
        } else {
            acceptableLimit = Integer.parseInt(str);
        }
        // acceptable limit should be somewhere between minLimit and limit
        acceptableLimit = Math.max(minLimit, acceptableLimit);

        str = (String) args.get("initialSize");
        final int initialSize = str == null ? limit : Integer.parseInt(str);
        str = (String) args.get("cleanupThread");
        boolean newThread = str == null ? true : Boolean.parseBoolean(str);

        str = (String) args.get("showItems");

        ConcurrentLRUCache<Object, Object> cache = new ConcurrentLRUCache<>(
                limit, minLimit, acceptableLimit, initialSize, newThread,
                false, null);
        cache.setShowItems(str == null ? 0 : Integer.parseInt(str));
        cache.setDescription(generateDescription(limit, initialSize, minLimit,
                acceptableLimit, newThread));
        cache.setAlive(false);
        cacheMap.putIfAbsent(cacheName, cache);
        statsList.add(cache.getStats());
        return cache;
    }

    @SuppressWarnings("rawtypes")
    public Cache getCache(String cacheName) throws Exception {
        Cache cache = cacheMap.get(cacheName);
        if (cache == null) {
            throw new Exception("Try to get invalid cache.");
        }
        return cache;
    }

    /**
     * @return Returns the description of this Cache.
     */
    protected static String generateDescription(int limit, int initialSize,
            int minLimit, int acceptableLimit, boolean newThread) {
        String description = "Concurrent LRU Cache(maxSize=" + limit
                + ", initialSize=" + initialSize + ", minSize=" + minLimit
                + ", acceptableSize=" + acceptableLimit + ", cleanupThread="
                + newThread;
        description += ')';
        return description;
    }

    /**
     * Returns a "Hit Ratio" (ie: max of 1.00, not a percentage) suitable for
     * display purposes.
     */
    protected static float calcHitRatio(long lookups, long hits) {
        return (lookups == 0) ? 0.0f : BigDecimal
                .valueOf((double) hits / (double) lookups)
                .setScale(2, RoundingMode.HALF_EVEN).floatValue();
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    public String getStatistics(String cacheName,
            boolean isOverallStatisticsNeeded) {
        ConcurrentHashMap<String, String> lst = new ConcurrentHashMap<String, String>();
        ConcurrentLRUCache cache = cacheMap.get(cacheName);
        if (cache == null)
            return "";
        ConcurrentLRUCache.Stats stats = ((ConcurrentLRUCache) cache)
                .getStats();
        long lookups = stats.getCumulativeLookups();
        long hits = stats.getCumulativeHits();
        long inserts = stats.getCumulativePuts();
        long evictions = stats.getCumulativeEvictions();
        long size = stats.getCurrentSize();
        long clookups = 0;
        long chits = 0;
        long cinserts = 0;
        long cevictions = 0;

        lst.putIfAbsent("lookups", lookups + "");
        lst.putIfAbsent("hits", hits + "");
        lst.putIfAbsent("hitratio", calcHitRatio(lookups, hits) + "");
        lst.putIfAbsent("inserts", inserts + "");
        lst.putIfAbsent("evictions", evictions + "");
        lst.putIfAbsent("size", size + "");

        if (isOverallStatisticsNeeded) {
            // NOTE: It is safe to iterate on a CopyOnWriteArrayList
            for (ConcurrentLRUCache.Stats statistiscs : statsList) {
                clookups += statistiscs.getCumulativeLookups();
                chits += statistiscs.getCumulativeHits();
                cinserts += statistiscs.getCumulativePuts();
                cevictions += statistiscs.getCumulativeEvictions();
            }

            lst.putIfAbsent("cumulative_lookups", clookups + "");
            lst.putIfAbsent("cumulative_hits", chits + "");
            lst.putIfAbsent("cumulative_hitratio",
                    calcHitRatio(clookups, chits) + "");
            lst.putIfAbsent("cumulative_inserts", cinserts + "");
            lst.putIfAbsent("cumulative_evictions", cevictions + "");
        }

        if (cache.getShowItems() != 0) {
            Map items = cache
                    .getLatestAccessedItems(cache.getShowItems() == -1 ? Integer.MAX_VALUE
                            : cache.getShowItems());
            for (Map.Entry e : (Set<Map.Entry>) items.entrySet()) {
                Object k = e.getKey();
                Object v = e.getValue();

                String ks = "item_" + k;
                String vs = v.toString();
                lst.putIfAbsent(ks, vs);
            }
        }
        return printMap((Map<String, String>) lst);
    }

    @SuppressWarnings("rawtypes")
    public String getStatistics() {
        ConcurrentHashMap<String, String> lst = new ConcurrentHashMap<String, String>();
        if (cacheMap.isEmpty())
            return "";
        StringBuilder sb = new StringBuilder(description + ":\n");
        for (Map.Entry<String, ConcurrentLRUCache> entry : cacheMap.entrySet()) {
            String cacheName = entry.getKey();
            ConcurrentLRUCache cache = entry.getValue();
            if (cache == null)
                continue;
            sb.append("Get statistics for Concurrent LRU Cache " + cacheName
                    + " [");
            sb.append(cache.getDescription() + "] :\n");
            sb.append(getStatistics(cacheName, false));
        }

        long clookups = 0;
        long chits = 0;
        long cinserts = 0;
        long cevictions = 0;

        // NOTE: It is safe to iterate on a CopyOnWriteArrayList
        for (ConcurrentLRUCache.Stats statistiscs : statsList) {
            clookups += statistiscs.getCumulativeLookups();
            chits += statistiscs.getCumulativeHits();
            cinserts += statistiscs.getCumulativePuts();
            cevictions += statistiscs.getCumulativeEvictions();
        }
        sb.append("\nThe overall statistics for opening caches: \n");
        lst.putIfAbsent("cumulative_lookups", clookups + "");
        lst.putIfAbsent("cumulative_hits", chits + "");
        lst.putIfAbsent("cumulative_hitratio", calcHitRatio(clookups, chits)
                + "");
        lst.putIfAbsent("cumulative_inserts", cinserts + "");
        lst.putIfAbsent("cumulative_evictions", cevictions + "");
        sb.append(printMap((Map<String, String>) lst));
        return sb.toString();
    }

    private String printMap(Map<String, String> map) {
        StringBuilder sb = new StringBuilder("");
        for (Map.Entry<String, String> entry : map.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            sb.append(key + "=" + value + " ;");
        }
        sb.append("\n");
        return sb.toString();
    }

    @Override
    public String toString() {
        return getStatistics();
    }

    @SuppressWarnings("rawtypes")
    public static Cache newInstance(String cacheName, Map args) {
        try {
            return init(cacheName, args);
        } catch (Exception e) {

            return null;
        }
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    public Map<String, Object> toMap(Map args) {
        Map result = Collections.unmodifiableMap(args);
        return result;
    }

    public static enum CacheType {
        traditionalUrlCache("traditionalUrlCache"), twitterQueryCache(
                "twitterQueryCache"), facebookQueryCache("facebookQueryCache"), googlePlusQueryCache(
                "googlePlusQueryCache"), YouTubeQueryCache("YouTubeQueryCache");

        private String name;

        public String getName() {
            return name;
        }

        @Override
        public String toString() {
            return name;
        }

        private CacheType(String name) {
            this.name = name;
        }
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    public static void main(String... args) {
        LRUCacheConfig lru = LRUCacheConfig.getInstance();
        Map<String, String> params = new HashMap<>();
        params.put("size", String.valueOf(100));
        params.put("initialSize", "10");
        params.put("cleanupThread", "true");
        params.put("showItems", String.valueOf(2));
        ConcurrentLRUCache testCache1 = (ConcurrentLRUCache) LRUCacheConfig
                .newInstance("testCache1", params);
        testCache1.setAlive(true);

        params.put("size", String.valueOf(5000));
        ConcurrentLRUCache testCache2 = (ConcurrentLRUCache) LRUCacheConfig
                .newInstance("testCache2", params);
        testCache2.setAlive(true);
        for (int i = 0; i < 1001; i++) {
            testCache1.put(i + 1, "" + (i + 1));
            testCache2.put(i + 1, "" + (i + 1));
        }
        testCache1.get(25);
        testCache2.get(99);
        testCache1.get(110);
        testCache2.get(66);
        testCache1.get(88);
        System.out.println(lru);
        System.exit(0);
    }
}
