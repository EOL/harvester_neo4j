package org.bibalex.eol.neo4j.parser;

import org.bibalex.eol.neo4j.backend_api.Neo4jTree;
import org.bibalex.eol.neo4j.models.Node;

import java.util.ArrayList;

public class NeoMain {
    public static void main(String[]args)
    {
//        Neo4jCommon instance = new Neo4jCommon();
////        System.out.print(instance.autoId);
////        instance.initialze();
////        try(Transaction tx = instance.graphDb.beginTx()) {
////////            int auto_id = instance.createIfNotExistNode(1, "TestNode", "Kingdom", "1");
//        int id = instance.createAcceptedNode(1, "pc", "Date_internally2", "Kingdom", 0);
//        int c_id = instance.createAcceptedNode(1, "pc", "Acanthocephala", "Phylum", 1);
//       instance.createAcceptedNode(1, "pc", "Tiger", "Phylum", 2);
//        instance.createAcceptedNode(1, "23", "Cat", "Class", 3);

//        instance.createAcceptedNode(1, "155", "SecondParent", "Class", 2);
//        instance.createAcceptedNode(1, "pc", "Dog", "Species", 4);
//        int c1_id = instance.createAcceptedNode(1, "15", "tiger", "Class", 2);
//        int sy_id = instance.createSynonymNode(1,"12","Lotor","Class","pc",3);
//////////        int s = instance.createAncestorIfNotExist(int resourceId, String scientificName, String rank, String nodeId,
//////////        int parentGeneratedNodeId
//        int sy2_id = instance.createSynonymNode(1,"13","pLotor","Class","pc",3);
//        int sy3_id = instance.createSynonymNode(1,"14","Animals","Phylum","pc",2);
//        int idp = instance.getSynonymNodeIfExist("pc","Arthopoda",1,"pc",3);
//        Node n = new Node();
//        n.setResourceId(1);
//        n.setNodeId("1988");
//        n.setScientificName("Date_internally2");
//        n.setRank("Kingdom");
//        n.setParentGeneratedNodeId(0);
//        Node n1 = new Node();
//        n1.setResourceId(1);
//        n1.setNodeId("11");
//        n1.setScientificName("Acanthocephala");
//        n1.setRank("Phylum");
//        n1.setParentGeneratedNodeId(1);
//        Node s = new Node();
//        s.setResourceId(1);
//        s.setNodeId("1988");
//        s.setScientificName("Date_internally2");
//        s.setRank("Kingdom");
//        s.setParentGeneratedNodeId(0);
//        Node s1 = new Node();
//        s1.setResourceId(1);
//        s1.setNodeId("11");
//        s1.setScientificName("Acanthocephala");
//        s1.setRank("Phylum");
//        s1.setParentGeneratedNodeId(1);
//        ArrayList<Node> list1 = new ArrayList<>();
//        list1.add(n);
//        list1.add(n1);
//        ArrayList<Node> list2 = new ArrayList<>();
//        list2.add(s);
//        list2.add(s1);
//        System.out.print(list1.equals(list2));
//        instance.addPageIdtoNode(3,89);
//        int old_parent_id = instance.getParent(5);
//        System.out.println(old_parent_id);
//        HbaseData d = new HbaseData();
//       boolean check = instance.checkPlaceholder(8);
//        System.out.println(check);
//        instance.MarkNodePlaceHolder(1);
//          instance.UpdateRank(8, "Class");
//        instance.UpdateScientificName(8,"THeNewNode");
//        int i = instance.getParentAndDelete(1);
//        System.out.print(i);
//        d.initialze();
//        d.getAncestors(3);
//        d.getChildren(3);
//        d.getSynonyms(3);
//        System.out.print(d.ancestors);
//        System.out.print(d.children);
//        System.out.print(d.synonyms);
//        Neo4jTree n  = new Neo4jTree();
//        Node M = n.getRoot(1);
//        n.getChildren(M);
//        int id = instance.createAcceptedNode(1, "1944", "Parent2", "Kingdom", 0);
//        int pid = instance.createAcceptedNode(1, "1949", "Parent3", "Kingdom", 0);

//        Neo4jForest f = new Neo4jForest();
//        ArrayList<Neo4jTree> trees = new ArrayList<>();
//                trees= f.getTrees("23.01.2018");
//        for(int i = 0; i<trees.size();i++)
//        {
//            System.out.println(trees.get(i).getRoot().getScientificName());
//            for(int j = 0;j<trees.get(i).getChildren().size();j++)
//            System.out.println(trees.get(i).getChildren().get(j).getScientificName());
//        }
//        f.setTrees("23.01.2018");
//        f.getTrees();
//            System.out.println(id);
//            System.out.println(c_id);
//            instance.searchAcceptedNode(Integer.toString(0));
        // Call functions sending the tx
        //see how to execute quesries to create a new node start with a kingdom node
//        String query = "MATCH (n) RETURN n ";
//        Result result = instance.graphDb.execute(query) ;
//        {
//            System.out.print(result);
//            while ( result.hasNext() )
//            {
//                Map<String, Object> row = result.next();
//                for ( String key : result.columns() )
//                {
//
//                    System.out.printf( "%s = %s%n", key, row.get( key ) );
//                }
//            }
//        }
//            System.out.print(id);
//            tx.success();
//        System.out.print("Done");


        TaxonMatching test =  new TaxonMatching();
//     ArrayList<Node> n= test.getChildrenNode(3);
//        System.out.println(n.get(0).getCreated_at());
//      boolean flag= test.addPageIdtoNode(10,3);
//        System.out.println(flag);
        ArrayList<Node> n= test.getAncestorsNodes(3);
         System.out.println(n.get(0).getCreated_at());



    }
}