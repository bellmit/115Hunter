package com.sap.cisp.xhna.data.common;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

import com.sap.cisp.xhna.data.common.Ketama;

public class HashTest {

    public static void main(String[] args) {
                
        //System.out.println("测试 ketama hash");
        //testKetama();
        //System.out.println("\r\n\r\n测试 native hash");
        //testHash(HashAlgorithm.NATIVE_HASH);
        //System.out.println("\r\n\r\n测试 CRC hash"); //max=32767
        //testHash(HashAlgorithm.CRC_HASH);
        //System.out.println("\r\n\r\n测试 FNV1_64_HASH"); 
        //testHash(HashAlgorithm.FNV1_64_HASH);
        //System.out.println("\r\n\r\n测试 FNV1A_64_HASH"); 
        //testHash(HashAlgorithm.FNV1A_64_HASH);
        
        //System.out.println("\r\n\r\n测试 MurmurHash 32"); 
        //testHash(HashAlgorithm.MurmurHash_32);
        
        System.out.println("\r\n\r\n测试 MurmurHash 64"); 
//        testHash(HashAlgorithm.MurmurHash_64);
        testKetama();
    }
    
    private static void testHash(HashAlgorithm hash) {
        Ketama ketama = new Ketama();
        ketama.hashAlgorithm = hash;
        
        List<Node> nodes = new ArrayList<>();
        for(int i=0; i< 10; i++) {
            nodes.add(new Node("name-" + i));
        }
        
        ketama.setKetamaNodes(nodes);
        Iterator<Entry<Long, Node>> it = ketama.hashNodes.entrySet().iterator();
        Entry<Long, Node> prior = it.next();
        while(it.hasNext()) {
            Entry<Long, Node> current = it.next();
            System.out.println("间隔：" + (current.getKey() - prior.getKey()) + "=" + current.getKey() + "-" + prior.getKey());
            prior = current;
        }
        
    }
    
    private static void testKetama() {
        Ketama ketama = new Ketama();
        ketama.hashAlgorithm = HashAlgorithm.KETAMA_HASH;
        
        List<Node> nodes = new ArrayList<>();
        for(int i=0; i< 10; i++) {
            nodes.add(new Node("name-" + i));
        }
        
        ketama.setKetamaNodes(nodes);
        Iterator<Entry<Long, Node>> it = ketama.hashNodes.entrySet().iterator();
        Entry<Long, Node> prior = it.next();
        while(it.hasNext()) {
            Entry<Long, Node> current = it.next();
            System.out.println("间隔：" + (current.getKey() - prior.getKey()));
            prior = current;
        }       
    }

}

