package com.sap.cisp.xhna.data.common;
import java.util.List;
import java.util.TreeMap;

public class Ketama {
    public TreeMap<Long, Node> hashNodes;
    public HashAlgorithm hashAlgorithm;
    
    protected void setKetamaNodes(List<Node> nodes) {
        TreeMap<Long, Node> newNodeMap = new TreeMap<Long, Node>();
        int numReps = 160;
        for (Node node : nodes) {
            if (hashAlgorithm == HashAlgorithm.KETAMA_HASH) {
                for (int i = 0; i < numReps / 4; i++) {
                    byte[] digest = HashAlgorithm.computeMd5(node.getName() + "-" + i);
                    for (int h = 0; h < 4; h++) {
                        Long k = ((long) (digest[3 + h * 4] & 0xFF) << 24) 
                               | ((long) (digest[2 + h * 4] & 0xFF) << 16)
                               | ((long) (digest[1 + h * 4] & 0xFF) << 8) 
                               | (digest[h * 4] & 0xFF);
                        newNodeMap.put(k, node);
                        
                    }
                }
            } else {
                for (int i = 0; i < numReps; i++) {
                    newNodeMap.put(hashAlgorithm.hash(node + "-" + i), node);
                }
            }
        }

        hashNodes = newNodeMap;
    }
}
class Node {
    private String name;
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public Node(String name) {
        this.name = name;
    }
}
