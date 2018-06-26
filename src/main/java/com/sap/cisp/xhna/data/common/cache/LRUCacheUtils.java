package com.sap.cisp.xhna.data.common.cache;

import org.apache.commons.lang3.SerializationUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sap.cisp.xhna.data.TaskManager;
import com.sap.cisp.xhna.data.config.ConfigInstance;
import com.sap.cisp.xhna.data.task.worker.JobSubmitClientHelperOfficialVersion;
import com.sap.cisp.xhna.data.task.worker.TaskManagementUtils;

@SuppressWarnings("rawtypes")
public class LRUCacheUtils {
    private static Logger logger = LoggerFactory.getLogger(LRUCacheUtils.class);
    protected static ConcurrentLRUCache traditionalCache;
    static {
        try {
            traditionalCache = (ConcurrentLRUCache) LRUCacheConfig.getInstance().getCache(
                    LRUCacheConfig.CacheType.traditionalUrlCache.getName());
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("Cannot get traditional LRU cache", e);
        }
    }
    
    @SuppressWarnings("unchecked")
    public static boolean checkIfTraditionalUrlCached(String url) {
        if(ConfigInstance.getCurrentWorkMode() != TaskManager.WorkMode.WORKER_ONLY) {
            logger.debug("===> Check traditional post url {} cached result locally : {}", url, (traditionalCache.get(url) != null)?"Cached":"Not Cached");
            return (traditionalCache.get(url) != null);
        } else {
            boolean result = false;
            byte[] jobReturn;
            try {
             // Here too frequent requests may cause gearman packet lost
                byte[] data = SerializationUtils.serialize(url);
                logger.debug("==> Request to check traditional post cached result remotely. url: {}); data size: {}", url, data.length);
                jobReturn = JobSubmitClientHelperOfficialVersion
                        .getInstance()
                        .init(JobSubmitClientHelperOfficialVersion.ClientTypeEnum.GET).submitJob(
                        TaskManagementUtils.FunctionEnum.CHECK_TRADITIONAL_POST_CACHED.toString(),
                        data);
                result = ((Boolean)SerializationUtils.deserialize(jobReturn)).booleanValue();
                logger.debug("===> Check traditional post cached result remotely:  {}", result?"Cached":"Not Cached");
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                logger.error("Error in checking traditional post cached", e);
            }
            return result;
        }
    }
    
    @SuppressWarnings("unchecked")
    public static void addTraditionalUrlToCache(String url) {
        if(ConfigInstance.getCurrentWorkMode() != TaskManager.WorkMode.WORKER_ONLY) {
            logger.debug("===> Add traditional post to cache locally. url {}", url);
            traditionalCache.put(url, 0);
        } else {
            boolean result = false;
            byte[] jobReturn;
            try {
                // Here too frequent requests may cause gearman packet lost
                byte[] data = SerializationUtils.serialize(url);
                logger.debug("==> Request to add traditional url to cache. url: {}); data size: {}", url, data.length);
                jobReturn = JobSubmitClientHelperOfficialVersion
                        .getInstance()
                        .init(JobSubmitClientHelperOfficialVersion.ClientTypeEnum.OTHER).submitJob(
                        TaskManagementUtils.FunctionEnum.CHECK_TRADITIONAL_POST_CACHED.toString(),data
                        );
                result = ((Boolean)SerializationUtils.deserialize(jobReturn)).booleanValue();
                logger.debug("===> Add traditional post to cache remotely:  {}", result?"Successful":"Failed");
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                logger.error("Error in adding traditional post to cache.", e);
            }
        }
    }
    
}
