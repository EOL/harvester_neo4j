package org.bibalex.eol.neo4j.parser;

import org.bibalex.eol.neo4j.handlers.GlobalNamesHandler;
import org.bibalex.eol.neo4j.hbase.HbaseData;
import org.neo4j.driver.v1.*;

import java.util.ArrayList;
import java.util.logging.Logger;

import static org.neo4j.driver.v1.Values.parameters;

public class Neo4jIndexer  extends HbaseData {
    java.util.logging.Logger logger =  Logger.getLogger("getJsonFile");
    ArrayList<String> synonymsSameResource = new ArrayList<>();
    ArrayList<String> synonymsOtherResources = new ArrayList<>();
    GlobalNamesHandler globalNameHandler;

    public void getJsonFile (int generatedNodeId)  {
        StatementResult result = getnNodeData ( generatedNodeId);
        globalNameHandler = new GlobalNamesHandler();

        if (result.hasNext())
        {
            Record record = result.next();
            String scientificName = record.get("n.scientific_name").asString();
//            System.out.println(record);
//            System.out.println("node's scientific name : "+record.get("n.scientific_name").asString());
//            System.out.println("rank : " + record.get("n.rank").asString());
//            System.out.println("resource : " + record.get("n.resource_id").asInt());
//
            getSynonymsNames( generatedNodeId,record.get("n.resource_id").asInt());
//            for(int i=0;i< synonymsSameResource.size();i++)
//            {
//                System.out.println("same : " + synonymsSameResource.get(i));
//            }
//            for(int i=0;i< synonymsOtherResources.size();i++)
//            {
//                System.out.println("other : " + synonymsOtherResources.get(i));
//            }
//
            ArrayList<String> ancestors = getAncestors( generatedNodeId);
//            for(int i=0;i< ancestors.size();i++)
//            {
//                System.out.println(ancestors.get(li));
//            }
//
            ArrayList<String>children = getChildrenName( generatedNodeId);
//            for(int i=0;i< children.size();i++)
//            {
//                System.out.println(children.get(i));
//            }


            for(int i=0;i< synonymsSameResource.size();i++)
            {
                System.out.println("same "+i+" "+globalNameHandler.getCanonicalName(synonymsSameResource.get(i)));
            }
            for(int i=0;i< synonymsOtherResources.size();i++)
            {
                System.out.println("other "+i+" "+globalNameHandler.getCanonicalName(synonymsOtherResources.get(i)));
            }


            System.out.println(globalNameHandler.isHybrid(scientificName));
            System.out.println(globalNameHandler.getCanonicalName(scientificName));

//            if(globalNameHandler.isHybrid(record.get("n.scientific_name").asString())){
//                System.out.println();
//            }

        }



    }

    public  StatementResult getnNodeData (int generatedNodeId)
    {
        String query = "MATCH (n:Node{generated_auto_id : {generatedNodeId}})"+
                " RETURN n.scientific_name , n.rank , n.resource_id " ;

        StatementResult result = getSession().run(query, parameters("generatedNodeId",generatedNodeId ));

        return result;
    }

    public void getSynonymsNames(int generatedNodeId, int resource_id)
    {
        logger.info("Getting synonyms of node with autoId" + generatedNodeId);
        String query = "MATCH (a:Node {generated_auto_id: {generatedNodeId}})<-[:IS_SYNONYM_OF]-(s:Synonym) return s.scientific_name , s.resource_id";
        StatementResult result = getSession().run(query, parameters("generatedNodeId",generatedNodeId));
        while (result.hasNext())
        {
            Record record = result.next();
            if(resource_id ==record.get("s.resource_id").asInt())
            {
                synonymsSameResource.add(record.get("s.scientific_name").asString()+ "")  ;
            }

            else
            {
                synonymsOtherResources.add(record.get("s.scientific_name").asString()+ "")  ;
            }

        }

    }

    public ArrayList<String> getChildrenName(int generatedNodeId)
    {
        logger.info("Getting children of node with autoId" + generatedNodeId);
        ArrayList<String> children = new ArrayList<>();
        String query = "MATCH (n:Node {generated_auto_id: {generatedNodeId}})-[:IS_PARENT_OF]->(c:Node) return c.scientific_name";
        StatementResult result = getSession().run(query, parameters("generatedNodeId",generatedNodeId));
        while (result.hasNext())
        {
            Record record = result.next();
            children.add(record.get("c.scientific_name").asString()+ "");
        }
        return children;
    }
}
