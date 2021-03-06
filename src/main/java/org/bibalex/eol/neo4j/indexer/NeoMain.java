package org.bibalex.eol.neo4j.indexer;
import org.bibalex.eol.neo4j.indexer.Neo4jIndexer;
import org.bibalex.eol.neo4j.parser.Neo4jCommon;

import java.io.IOException;
import java.util.ArrayList;

public class NeoMain {
    public static void main(String[]args) {
//        Neo4jCommon instance = new Neo4jCommon();
//        System.out.print(instance.autoId);
//
////            int auto_id = instance.createIfNotExistNode(1, "TestNode", "Kingdom", "1");
//            int id = instance.createAcceptedNode(1, "1988", "Bringocythere pumilus Lord, Malz & Whittaker, 2006", "Kingdom", 0);
//            int c_id = instance.createAcceptedNode(1, "11", "Theriosynoecum silvai (Silva, 1978)", "Phylum", 1);
//            int c1_id = instance.createAcceptedNode(1, "pc", "Jenningsina paffrathensis Krommelbein, 1954", "Class", 2);
//
//
//            int sy_id = instance.createSynonymNode(1, "12", "Ogmoconcha owthropensis (Anderson, 1964)", "Class", "pc", 3);
////        int s = instance.createAncestorIfNotExist(int resourceId, String scientificName, String rank, String nodeId,
////        int parentGeneratedNodeId
//            int sy4_id = instance.createSynonymNode(2, "10", "Isotelus frognoensis Owen, 1981", "Class", "pc", 3);
//
//            int sy2_id = instance.createSynonymNode(1, "13", "Phylacops bituberculatus Weir, 1959", "Class", "pc", 3);
//            int sy3_id = instance.createSynonymNode(1, "14", "Thalassaphorura bapen", "Phylum", "pc", 2);
//            int id1 = instance.getSynonymNodeIfExist("pc", "Symphysurina parva Raymond, 1937", 1, "pc", 3);
//            System.out.println("Already created node ");
//
//           int c2_id = instance.createAcceptedNode(1, "pc", "Opsimasaphus nobilis (Barrande, 1846)", "Class", 3);
//            int id2 = instance.createAcceptedNode(1, "1944", "Aulacoparia quadrata (Hintze, 1952)", "Kingdom", 0);
//            int pid = instance.createAcceptedNode(1, "1949", "Opsimasaphus ingens (Barrande, 1846)", "Kingdom", 0);
//              instance.addPageIdtoNode(3,2);
//              instance.addPageIdtoNode(2,1);



        Neo4jIndexer instance = new Neo4jIndexer();
//        ArrayList<Integer> ids= new ArrayList<>();
//        ids.add(1);
//        ids.add(2);
//        ids.add(3);
//    instance.Neo4jToJson(ids);
        int[] ids ={1, 2, 3, 4,9,16};
        instance.Neo4jToJson(ids);
    }
}