package com.sap.cisp.xhna.data.common;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import junit.framework.TestCase;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sap.cisp.xhna.data.common.index.PostIdsHolder;
import com.sap.cisp.xhna.data.common.serializer.KryoUtils;
import com.sap.cisp.xhna.data.common.serializer.Serializer;

public class KryoSerializerTest extends TestCase {
    public static Logger logger = LoggerFactory
            .getLogger(KryoSerializerTest.class);

    public KryoSerializerTest(String name) {
        super(name);
    }

    protected void setUp() throws Exception {
        super.setUp();
    }

    public void testDummy() {
        // just to suppress warning
    }

    protected void tearDown() throws Exception {
        super.tearDown();
    }

    public void testPostIdsContainerSerialization() throws Exception {
        ExecutorService executor = Executors.newFixedThreadPool(100);
        final Serializer<PostIdsHolder> serializer = new KryoUtils.CustomSerializer<PostIdsHolder>(
                KryoUtils.PostIdsContainerTypeHandler);
   
        for (int i = 0; i < 1000; i++) {
            executor.execute(new Runnable() {

                @Override
                public void run() {
                    List<String> idList = new ArrayList<String>();
                    for (int i = 0; i < 3000; i++) {
                        String id = "625236407351521280qweqwewqeqee" + i;
                        idList.add(id);
                    }
                    // String occupied bytes: <64bit> 2*N + 64 + padding
                    // N = String.toCharArray.size(), padding to be supplemented
                    // to make it as 8*n
                    char[] idChars = "625236407351521280".toCharArray();
                    long size1 = 2 * idChars.length + 64;
                    long size2 = "625236407351521280".getBytes().length;
                    PostIdsHolder content = new PostIdsHolder();
                    content.setPath("./index/tweet_index");
                    content.setIdList(idList);
                    byte[] dataToGo = null;

                    try {
                        dataToGo = serializer.serialize(content);
                    } catch (Exception e1) {
                        // TODO Auto-generated catch block
                        e1.printStackTrace();
                    }
                    logger.debug(
                            "---> id list serialized: original size {} : {}, serialized size {}",
                            size1 * 1000000, size2 * 1000000, dataToGo.length);
                    final byte[] data = dataToGo;

                    Serializer<PostIdsHolder> serializer = new KryoUtils.CustomSerializer<PostIdsHolder>(
                            KryoUtils.PostIdsContainerTypeHandler);
                    PostIdsHolder newcontent = null;
                    try {
                        newcontent = serializer.deserialize(data);
                    } catch (Exception e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                    logger.debug("---> id list deserialized:  {}, size {}",
                            newcontent.getPath(), newcontent.getIdList().size());
//                     assertEquals(content, newcontent);
                }

            }

            );
        }
        List<String> idList = new ArrayList<String>();
        for (int i = 0; i < 100000; i++) {
            idList.add("625236407351521280");
        }
        PostIdsHolder content = new PostIdsHolder();
        content.setPath("./index/tweet_index");
        // String occupied bytes: <64bit> 2*N + 64 + padding
        // N = String.toCharArray.size(), padding to be supplemented to make it
        // as 8*n
        char[] idChars = "625236407351521280".toCharArray();
        long size1 = 2 * idChars.length + 64;
        long size2 = "625236407351521280".getBytes().length;
        content.setIdList(idList);
        byte[] dataToGo = null;
        for (int i = 0; i < 1; i++) {
            dataToGo = serializer.serialize(content);
            logger.debug(
                    "===> id list serialized: original size {} : {}, serialized size {}",
                    size1 * 1000000, size2 * 1000000, dataToGo.length);
            final byte[] data = dataToGo;

            Serializer<PostIdsHolder> serializer1 = new KryoUtils.CustomSerializer<PostIdsHolder>(
                    KryoUtils.PostIdsContainerTypeHandler);
            PostIdsHolder newcontent = null;
            try {
                newcontent = serializer1.deserialize(data);
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            logger.debug("===> id list deserialized:  {}, size {}",
                    newcontent.getPath(), newcontent.getIdList().size());
//            assertEquals(content, newcontent);
        }

    }
    
    public void testTokenSerialization() throws Exception {
        ExecutorService executor = Executors.newFixedThreadPool(100);
        final Serializer<PostIdsHolder> serializer = new KryoUtils.CustomSerializer<PostIdsHolder>(
                KryoUtils.PostIdsContainerTypeHandler);
   
        for (int i = 0; i < 1000; i++) {
            executor.execute(new Runnable() {

                @Override
                public void run() {
                    List<String> idList = new ArrayList<String>();
                    for (int i = 0; i < 3000; i++) {
                        String id = "625236407351521280qweqwewqeqee" + i;
                        idList.add(id);
                    }
                    // String occupied bytes: <64bit> 2*N + 64 + padding
                    // N = String.toCharArray.size(), padding to be supplemented
                    // to make it as 8*n
                    char[] idChars = "625236407351521280".toCharArray();
                    long size1 = 2 * idChars.length + 64;
                    long size2 = "625236407351521280".getBytes().length;
                    PostIdsHolder content = new PostIdsHolder();
                    content.setPath("./index/tweet_index");
                    content.setIdList(idList);
                    byte[] dataToGo = null;

                    try {
                        dataToGo = serializer.serialize(content);
                    } catch (Exception e1) {
                        // TODO Auto-generated catch block
                        e1.printStackTrace();
                    }
                    logger.debug(
                            "---> id list serialized: original size {} : {}, serialized size {}",
                            size1 * 1000000, size2 * 1000000, dataToGo.length);
                    final byte[] data = dataToGo;

                    Serializer<PostIdsHolder> serializer = new KryoUtils.CustomSerializer<PostIdsHolder>(
                            KryoUtils.PostIdsContainerTypeHandler);
                    PostIdsHolder newcontent = null;
                    try {
                        newcontent = serializer.deserialize(data);
                    } catch (Exception e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                    logger.debug("---> id list deserialized:  {}, size {}",
                            newcontent.getPath(), newcontent.getIdList().size());
//                     assertEquals(content, newcontent);
                }

            }

            );
        }
        List<String> idList = new ArrayList<String>();
        for (int i = 0; i < 100000; i++) {
            idList.add("625236407351521280");
        }
        PostIdsHolder content = new PostIdsHolder();
        content.setPath("./index/tweet_index");
        // String occupied bytes: <64bit> 2*N + 64 + padding
        // N = String.toCharArray.size(), padding to be supplemented to make it
        // as 8*n
        char[] idChars = "625236407351521280".toCharArray();
        long size1 = 2 * idChars.length + 64;
        long size2 = "625236407351521280".getBytes().length;
        content.setIdList(idList);
        byte[] dataToGo = null;
        for (int i = 0; i < 1; i++) {
            dataToGo = serializer.serialize(content);
            logger.debug(
                    "===> id list serialized: original size {} : {}, serialized size {}",
                    size1 * 1000000, size2 * 1000000, dataToGo.length);
            final byte[] data = dataToGo;

            Serializer<PostIdsHolder> serializer1 = new KryoUtils.CustomSerializer<PostIdsHolder>(
                    KryoUtils.PostIdsContainerTypeHandler);
            PostIdsHolder newcontent = null;
            try {
                newcontent = serializer1.deserialize(data);
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            logger.debug("===> id list deserialized:  {}, size {}",
                    newcontent.getPath(), newcontent.getIdList().size());
//            assertEquals(content, newcontent);
        }

    }
}
