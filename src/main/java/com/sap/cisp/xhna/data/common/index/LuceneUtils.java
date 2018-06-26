package com.sap.cisp.xhna.data.common.index;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.lang3.SerializationUtils;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.FieldType;
import org.apache.lucene.document.IntField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexNotFoundException;
import org.apache.lucene.index.IndexOptions;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.LogDocMergePolicy;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.index.SerialMergeScheduler;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.NumericRangeQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.store.Lock;
import org.apache.lucene.store.NRTCachingDirectory;
import org.eclipse.jetty.util.ConcurrentHashSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sap.cisp.xhna.data.TaskManager;
import com.sap.cisp.xhna.data.common.serializer.KryoUtils;
import com.sap.cisp.xhna.data.task.worker.JobSubmitClientHelperOfficialVersion;
import com.sap.cisp.xhna.data.task.worker.TaskManagementUtils;
import com.sap.cisp.xhna.data.common.serializer.*;
import com.sap.cisp.xhna.data.config.ConfigInstance;

/**
 * Lucene Utils
 * 
 */
public abstract class LuceneUtils {
    private static final double MAX_MEGER_SIZE_MB = 5.0;
    private static final double MAX_CACHED_SIZE_MB = 2.0;
    private static final int DEFAULT_VALUE = 0;
    private static final String TWITTER_INDEX_PATH = "./index/tweet_index";
    private static final String FACEBOOK_INDEX_PATH = "./index/facebook_index";
    private static final String GOOGLEPLUS_INDEX_PATH = "./index/gplus_index";
    private static final String YOUTUBE_INDEX_PATH = "./index/youtube_index";
    private static final int ID_LIST_CHOPPED_LENGTH = 500;
    private static Logger logger = LoggerFactory.getLogger(LuceneUtils.class);

    /**
     * NRTCachingDirectory
     * 
     * @param indexPath
     * @param maxMergeSizeMB
     * @param maxCachedMB
     * @return Directory
     * @throws IOException
     */
    public static Directory createFSDriectory(String indexPath,
            double maxMergeSizeMB, double maxCachedMB) throws IOException {
        Directory fsDir = FSDirectory.open(Paths.get(indexPath));
        NRTCachingDirectory cachedFSDir = new NRTCachingDirectory(fsDir,
                maxMergeSizeMB, maxCachedMB);
        // Try to acquire lock, if success, will close the lock; return false
        // means no lock for the index directory
        IndexWriter.isLocked(fsDir);
        return cachedFSDir;
    }

    /**
     * NRTCachingDirectory
     * 
     * @param indexPath
     * @return Directory
     * @throws IOException
     */
    public static Directory createFSDriectory(String indexPath)
            throws IOException {
        return createFSDriectory(indexPath, MAX_MEGER_SIZE_MB,
                MAX_CACHED_SIZE_MB);
    }

    public static IndexWriter createFSIndexWriter(String indexPath,
            Analyzer analyzer) throws IOException {
        return buildFSIndexWriter(indexPath, analyzer, OpenMode.CREATE,
                MAX_MEGER_SIZE_MB, MAX_CACHED_SIZE_MB);
    }

    public static IndexWriter openFSIndexWriter(String indexPath,
            Analyzer analyzer) throws IOException {
        return buildFSIndexWriter(indexPath, analyzer,
                OpenMode.CREATE_OR_APPEND, MAX_MEGER_SIZE_MB,
                MAX_CACHED_SIZE_MB);
    }

    public static IndexWriter buildFSIndexWriter(String indexPath,
            Analyzer analyzer, OpenMode openMode, double maxMergeSizeMB,
            double maxCachedMB) throws IOException {
        Directory fsDir = FSDirectory.open(Paths.get(indexPath));
        NRTCachingDirectory cachedFSDir = new NRTCachingDirectory(fsDir,
                maxMergeSizeMB, maxCachedMB);
        IndexWriterConfig conf = new IndexWriterConfig(analyzer);
        LogDocMergePolicy mp = new LogDocMergePolicy();
        mp.setMinMergeDocs(100);
        mp.setMergeFactor(10);
        conf.setMergePolicy(mp);
        conf.setMergeScheduler(new SerialMergeScheduler());
        conf.setOpenMode(openMode);
        conf.setWriteLockTimeout(Lock.LOCK_OBTAIN_WAIT_FOREVER);
        IndexWriter writer = new IndexWriter(cachedFSDir, conf);
        logger.debug("--> Lock Timeout {}", writer.getConfig()
                .getWriteLockTimeout());
        return writer;
    }

    public static Analyzer createStandardAnalyzer() {
        return new StandardAnalyzer();
    }

    protected static IndexWriter getIndexWriter(MediaIndexPath path)
            throws IOException {
        Analyzer analyzer = LuceneUtils.createStandardAnalyzer();
        return LuceneUtils.openFSIndexWriter(path.getPath(), analyzer);
    }

    public static List<String> getUnCachedPosts(MediaIndexPath path,
            ConcurrentHashMap<String, String> resultMap, List<String> results)
            throws Exception {
        // Local handling for non worker-only work mode
        if (ConfigInstance.getCurrentWorkMode() != TaskManager.WorkMode.WORKER_ONLY) {
            return getUnCachedPostsFromLocal(path, resultMap, results);
        } else {
            return getUnCachedPostsFromServer(path, resultMap, results, true);
        }
    }

    public static List<String> getUnCachedPostsFromServer(MediaIndexPath path,
            ConcurrentHashMap<String, String> resultMap, List<String> results,
            boolean isSerializationOptimized) throws Exception {
        if (isSerializationOptimized) {
            // submit index request remotely for worker-only work mode
            synchronized (path) {
                ArrayList<String> idList = new ArrayList<String>();
                
                Serializer<PostIdsHolder> serializer = new KryoUtils.CustomSerializer<PostIdsHolder>(
                        KryoUtils.PostIdsContainerTypeHandler);
                for (Map.Entry<String, String> entry : resultMap.entrySet()) {
                    String id = entry.getKey();
                    idList.add(id);
                }
                //After strictly test, there is packet size limitation for gearman
                //Data to go size better to < 5000 bytes
                //For Facebook id, 100 * per id szie ~= 2823 bytes (i.e. 5550296508_10153888099886509)
                //For Twitter id, 100 * per id size ~= 1820 bytes (i.e. 625236407351521280)
                //For g+ id, 100 * per id size ~= 3520 bytes (i.e. z13ls52zom2isjgor04cidmz3svdxpq5v0c)
                //For youtube  1122 bytes(i.e. SR9Af22AVjw)
                //Optimization history: Java built-in serializer -> kryo ->kryo optimizer -> kryo compressed
                //After compressed, id number chooped length from 100 -> 500
                List<List<String>> subLists = chopped(idList, ID_LIST_CHOPPED_LENGTH);
                List<List<String>> resultSubLists = new ArrayList<List<String>> ();
                for (List<String> subList : subLists) {
                    PostIdsHolder content = new PostIdsHolder();
                    content.setIdList(subList);
                    content.setPath(path.getPath());
                    byte[] data =  serializer.serialize(content);
                    logger.debug(
                            "===> Get uncached social post from index server: path {}, candidate id sub-list size {}, data to go size {}",
                            path.getPath(), subList.size(), data.length);
                    byte[] jobReturn = null;
                    try {
                        jobReturn = JobSubmitClientHelperOfficialVersion
                                .getInstance()
                                .init(JobSubmitClientHelperOfficialVersion.ClientTypeEnum.OTHER)
                                .submitJob(
                                        TaskManagementUtils.FunctionEnum.GET_UNCACHED_SOCIAL_POSTS
                                                .toString(), data);
                    } catch (InterruptedException e) {
                        logger.error(
                                "Check social post cached  remotely error.", e);
                        throw new Exception(e);
                    }
                    if (jobReturn != null && jobReturn.length != 0) {
                        content = (PostIdsHolder) serializer
                                .deserialize(jobReturn);
                        resultSubLists.add(content.getIdList());
                    }
                }
                for (List<String> resultSubList : resultSubLists) {
                    for (String id : resultSubList) {
                        results.add(resultMap.get(id));
                    }
                }
                logger.debug("--> To be cached Post id list size {}", results.size());
            }
            return results;
        } else {
            // should not be here for low performance
            return getUnCachedPostsFromServer(path, resultMap, results);
        }
    }

    /**
     * This method should be obsolete, since there is no optimization
     * 
     * @param path
     * @param resultMap
     * @param results
     * @return
     * @throws Exception
     */
    public static List<String> getUnCachedPostsFromServer(MediaIndexPath path,
            ConcurrentHashMap<String, String> resultMap, List<String> results)
            throws Exception {
        // submit index request remotely for worker-only work mode
        synchronized (path) {
            HashMap<String, String> params = new HashMap<String, String>();
            params.putIfAbsent("path", path.getPath());
            for (Map.Entry<String, String> entry : resultMap.entrySet()) {
                String id = entry.getKey();
                params.putIfAbsent("id", id);
                logger.debug(
                        "===> Check if social post cached from index server: path {}, id {}",
                        path.getPath(), id);
                byte[] jobReturn = null;
                try {
                    jobReturn = JobSubmitClientHelperOfficialVersion
                            .getInstance()
                            .init(JobSubmitClientHelperOfficialVersion.ClientTypeEnum.OTHER)
                            .submitJob(
                                    TaskManagementUtils.FunctionEnum.CHECK_IF_UNCACHED_SOCIAL_POSTS
                                            .toString(),
                                    SerializationUtils.serialize(params));
                } catch (InterruptedException e) {
                    logger.error("Check social post cached  remotely error.", e);
                    throw new Exception(e);
                }
                if (jobReturn != null && jobReturn.length != 0) {
                    boolean isUnCached = ((Boolean) SerializationUtils
                            .deserialize(jobReturn)).booleanValue();
                    logger.debug(
                            "Check if social post cached remotely -  result : {}.",
                            isUnCached ? "Not Cached" : "Cached");
                    if (isUnCached) {
                        logger.debug("Add uncached post to result list: id {}",
                                id);
                        results.add(entry.getValue());
                    }
                    // remove processed id
                    params.remove("id");
                }
            }
        }
        return results;
    }
    
    /**
     * This method is invoken on server side for work mode: singlton, server-worker, server-only
     * @param path
     * @param resultMap
     * @param results
     * @return
     * @throws Exception
     */
    public static List<String> getUnCachedPostsFromLocal(MediaIndexPath path,
            ConcurrentHashMap<String, String> resultMap, List<String> results)
            throws Exception {
        IndexReader reader = null;
        long len = 0;
        ArrayList<String> toBeCachedIDList = new ArrayList<String>();
        IndexWriter writer = null;
        // different media obtain different lock, same media share same lock
        synchronized (path) {
            try {
                try {
                    reader = createReader(path);
                    logger.debug("Access Reader directory locally ----> {}",
                            reader);
                } catch (Exception e) {
                    logger.error(
                            "Cannot create reader. Maybe index has never been created yet.",
                            e);
                }
                if (reader != null) {
                    len = reader.maxDoc();
                    logger.debug("Index Reader: Maxdoc {}", len);
                    IndexSearcher searcher = new IndexSearcher(reader);

                    for (Map.Entry<String, String> entry : resultMap.entrySet()) {
                        boolean isCached = false;
                        String id = entry.getKey();
                        org.apache.lucene.search.Query query = NumericRangeQuery
                                .newIntRange(id, 1, DEFAULT_VALUE,
                                        DEFAULT_VALUE, true, true);
                        TopDocs hits = searcher.search(query, 1);

                        if (hits.totalHits > 0) {
                            logger.debug(
                                    ">>> Post {} has been Cached; Hits {}.",
                                    id, hits.totalHits);
                            isCached = true;
                            continue;
                        }
                        if (!isCached) {
                            toBeCachedIDList.add(id);
                        }
                    }
                } else {
                    for (Map.Entry<String, String> entry : resultMap.entrySet()) {
                        String id = entry.getKey();
                        toBeCachedIDList.add(id);
                    }
                }

                // For non-cached doc, add it to index
                writer = getIndexWriter(path);
                for (String id : toBeCachedIDList) {
                    Document doc = new Document();
                    doc.add(new IntField(
                            id,
                            DEFAULT_VALUE,
                            LuceneUtils.CustomFieldType.INT_TYPE_NOT_STORED_NO_TIRE));
                    logger.info("******** Index new doc with id {} ", id);
                    writer.addDocument(doc);
                    results.add(resultMap.get(id));
                }
                writer.commit();
            } catch (Exception e) {
                e.printStackTrace();
                logger.error("Exception in check tweet caching.", e);
                throw e;
            } finally {
                if (reader != null) {
                    try {
                        reader.close();
                        reader = null;
                    } catch (IOException e) {
                        // TODO Auto-generated catch block
                        logger.error("IOException", e);
                        throw e;
                    }
                }
                if (writer != null) {
                    try {
                        writer.close();
                        writer = null;
                    } catch (IOException e) {
                        // TODO Auto-generated catch block
                        logger.error("IOException", e);
                        throw e;
                    }
                }
            }
        }
        return results;
    }

    /**
     * This method is invoken from gearman server to return toBeCachedPostIds to worker side
     * @param path
     * @param idList
     * @return
     * @throws Exception
     */
    public static List<String> getUnCachedPostIdsFromLocal(MediaIndexPath path,
            List<String> idList) throws Exception {
        IndexReader reader = null;
        long len = 0;
        Set<String> toBeCachedIDSet = new ConcurrentHashSet<String>();
        ArrayList<String> toBeCachedIDList = new ArrayList<String>();
        IndexWriter writer = null;
        // different media obtain different lock, same media share same lock
        synchronized (path) {
            try {
                try {
                    reader = createReader(path);
                    logger.debug("Access Reader directory locally for uncached Post IDs ----> {}",
                            reader);
                } catch (Exception e) {
                    logger.error(
                            "Cannot create reader. Maybe index has never been created yet.",
                            e);
                }
                if (reader != null) {
                    len = reader.maxDoc();
                    logger.debug("Index Reader from server : Maxdoc {}", len);
                    IndexSearcher searcher = new IndexSearcher(reader);

                    for (String id : idList) {
                        boolean isCached = false;
                        org.apache.lucene.search.Query query = NumericRangeQuery
                                .newIntRange(id, 1, DEFAULT_VALUE,
                                        DEFAULT_VALUE, true, true);
                        TopDocs hits = searcher.search(query, 1);

                        if (hits.totalHits > 0) {
                            logger.debug(
                                    ">>> Post {} has been Cached; Hits {}.",
                                    id, hits.totalHits);
                            isCached = true;
                            continue;
                        }
                        if (!isCached) {
                            toBeCachedIDSet.add(id);
                        }
                    }
                } else {
                    // no index yet, all to be cached.
                    toBeCachedIDSet.addAll(idList);
                }

                // For non-cached doc, add it to index
                writer = getIndexWriter(path);
                for (String id : toBeCachedIDSet) {
                    Document doc = new Document();
                    doc.add(new IntField(
                            id,
                            DEFAULT_VALUE,
                            LuceneUtils.CustomFieldType.INT_TYPE_NOT_STORED_NO_TIRE));
                    logger.info("******** Index new doc with id {} ", id);
                    writer.addDocument(doc);
                }
                writer.commit();
            } catch (Exception e) {
                e.printStackTrace();
                logger.error("Exception in check tweet caching.", e);
                throw e;
            } finally {
                if (reader != null) {
                    try {
                        reader.close();
                        reader = null;
                    } catch (IOException e) {
                        // TODO Auto-generated catch block
                        logger.error("IOException", e);
                        throw e;
                    }
                }
                if (writer != null) {
                    try {
                        writer.close();
                        writer = null;
                    } catch (IOException e) {
                        // TODO Auto-generated catch block
                        logger.error("IOException", e);
                        throw e;
                    }
                }
            }
        }
        toBeCachedIDList.addAll(toBeCachedIDSet);
        return toBeCachedIDList;
    }

    public static boolean checkIfUnCachedPostFromLocal(MediaIndexPath path,
            String id) throws Exception {
        if (ConfigInstance.getCurrentWorkMode() != TaskManager.WorkMode.WORKER_ONLY) {
            IndexReader reader = null;
            long len = 0;
            IndexWriter writer = null;
            // different media obtain different lock, same media share same lock
            synchronized (path) {
                try {
                    try {
                        reader = createReader(path);
                        logger.debug(
                                "Check Reader directory on server ----> {}",
                                reader);
                    } catch (Exception e) {
                        logger.error(
                                "Cannot create reader. Maybe index has never been created yet.",
                                e);
                    }
                    if (reader != null) {
                        len = reader.maxDoc();
                        logger.debug("Index Reader on server: Maxdoc {}", len);
                        IndexSearcher searcher = new IndexSearcher(reader);
                        org.apache.lucene.search.Query query = NumericRangeQuery
                                .newIntRange(id, 1, DEFAULT_VALUE,
                                        DEFAULT_VALUE, true, true);
                        TopDocs hits = searcher.search(query, 1);

                        if (hits.totalHits > 0) {
                            logger.debug(
                                    ">>> Post {} has been Cached; Hits {}.",
                                    id, hits.totalHits);
                            return false;
                        }
                    }
                    // For non-cached doc, add it to index
                    writer = getIndexWriter(path);
                    Document doc = new Document();
                    doc.add(new IntField(
                            id,
                            DEFAULT_VALUE,
                            LuceneUtils.CustomFieldType.INT_TYPE_NOT_STORED_NO_TIRE));
                    logger.info("******** Index new doc on server with id {} ",
                            id);
                    writer.addDocument(doc);
                    writer.commit();

                } catch (Exception e) {
                    e.printStackTrace();
                    logger.error("Exception in check post caching.", e);
                    throw e;
                } finally {
                    if (reader != null) {
                        try {
                            reader.close();
                            reader = null;
                        } catch (IOException e) {
                            // TODO Auto-generated catch block
                            logger.error("IOException", e);
                            throw e;
                        }
                    }
                    if (writer != null) {
                        try {
                            writer.close();
                            writer = null;
                        } catch (IOException e) {
                            // TODO Auto-generated catch block
                            logger.error("IOException", e);
                            throw e;
                        }
                    }
                }
            }
        }
        return true;
    }

    protected static IndexReader createReader(MediaIndexPath path)
            throws IOException {
        Directory d = createFSDriectory(path.getPath());
        IndexReader reader = null;
        try {
            reader = DirectoryReader.open(d);
        } catch (IndexNotFoundException ie) {
            logger.error("Create reader error.", ie);
            throw ie;
        }
        IndexReader newReader = null;
        // Real time check for dynamic index change
        try {
            newReader = DirectoryReader.openIfChanged((DirectoryReader) reader);
            // return null if no change
            if (newReader != null) {
                reader.close();
                reader = newReader;
                logger.info("Return new reader {}", reader);
            }
        } catch (Exception e) {
            logger.error("Create reader error.", e);
            throw e;
        }
        return reader;
    }

    /**
     * // chops a list into non-view sublists of length L
     * @param list
     * @param L
     * @return
     */
    public static <T> List<List<T>> chopped(List<T> list, final int L) {
        List<List<T>> parts = new ArrayList<List<T>>();
        final int N = list.size();
        for (int i = 0; i < N; i += L) {
            parts.add(new ArrayList<T>(
                list.subList(i, Math.min(N, i + L)))
            );
        }
        return parts;
    }
    
    /**
     * // chops a map into non-view sub entry lists of length L
     * @param Map
     * @param L
     * @return sublists of map entry
     */
    public static  <T,K> List<List<Map.Entry<T,K>>> chopped(Map<T,K> map, final int L) {
        List<List<Map.Entry<T,K>>> parts = new ArrayList<List<Map.Entry<T,K>>>();
        List<Map.Entry<T,K>> entryList = new ArrayList<Map.Entry<T,K>>();
        for (Map.Entry<T, K> entry : map.entrySet()) {
           entryList.add(entry);
        }
        parts = LuceneUtils.chopped(entryList, L);
        return parts;
    }

    /**
     * Optimize for id index storage to reduce index size
     *
     */
    public final static class CustomFieldType {
        public static final FieldType INT_TYPE_NOT_STORED_NO_TIRE = new FieldType();
        static {
            INT_TYPE_NOT_STORED_NO_TIRE.setStored(true);
            INT_TYPE_NOT_STORED_NO_TIRE.setTokenized(true);
            INT_TYPE_NOT_STORED_NO_TIRE.setOmitNorms(true);
            INT_TYPE_NOT_STORED_NO_TIRE.setIndexOptions(IndexOptions.DOCS);
            INT_TYPE_NOT_STORED_NO_TIRE
                    .setNumericType(FieldType.NumericType.INT);
            INT_TYPE_NOT_STORED_NO_TIRE
                    .setNumericPrecisionStep(Integer.MAX_VALUE);
            INT_TYPE_NOT_STORED_NO_TIRE.freeze();
        }
    }

    public static enum MediaIndexPath {
        Twitter(TWITTER_INDEX_PATH), Facebook(FACEBOOK_INDEX_PATH), GooglePlus(
                GOOGLEPLUS_INDEX_PATH), YouTube(YOUTUBE_INDEX_PATH), Test(
                "Test");

        private String path;

        public String getPath() {
            return path;
        }

        private MediaIndexPath(String path) {
            this.path = path;
        }
    }

    public static MediaIndexPath getMediaIndexPathByName(String path) {
        switch (path) {
        case TWITTER_INDEX_PATH:
            return MediaIndexPath.Twitter;
        case FACEBOOK_INDEX_PATH:
            return MediaIndexPath.Facebook;
        case GOOGLEPLUS_INDEX_PATH:
            return MediaIndexPath.GooglePlus;
        case YOUTUBE_INDEX_PATH:
            return MediaIndexPath.YouTube;
        case "Test":
            return MediaIndexPath.Test;
        default:
            return null;
        }
    }
}