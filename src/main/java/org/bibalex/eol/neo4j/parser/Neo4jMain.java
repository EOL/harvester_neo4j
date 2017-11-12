package org.bibalex.eol.neo4j.parser;

import org.bibalex.eol.neo4j.hbase.HbaseData;

public class Neo4jMain {

    public static void main(String[] args)
    {
//        Neo4jCommon instance = new Neo4jCommon();
//        instance.initialze();
//        try(Transaction tx = instance.graphDb.beginTx()) {
//            int auto_id = instance.createIfNotExistNode(1, "TestNode", "Kingdom", "1");
//        int id = instance.createAcceptedNode(1, "10", "Animilia", "Kingdom", 0);
//        int c_id = instance.createAcceptedNode(1, "11", "Acanthocephala", "Phylum", 1);
//        int c1_id = instance.createAcceptedNode(1, "pc", "raccoon", "Class", 2);
//        int sy_id = instance.createSynonymNode(1,"12","Lotor","Class","pc",3);
//        int sy2_id = instance.createSynonymNode(1,"13","pLotor","Class","pc",3);
//        int sy3_id = instance.createSynonymNode(1,"14","Animals","Phylum","pc",2);
//        int id = instance.getSynonymNodeIfExist("pc","Arthopoda",1,"pc",3);
//        System.out.println("Already created node ");
//        HbaseData d = new HbaseData();
//        d.initialze();
//        d.getAncestors(3);
//        d.getChildren(3);
//        d.getSynonyms(3);
//        System.out.print(d.ancestors);
//        System.out.print(d.children);
//        System.out.print(d.synonyms);

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
        System.out.print("Done");




    }
}
