package org.bibalex.eol.neo4j.indexer;

import org.bibalex.eol.neo4j.hbase.HbaseData;
import org.json.simple.JSONObject;
import org.neo4j.driver.v1.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import static org.neo4j.driver.v1.Values.NULL;
import static org.neo4j.driver.v1.Values.parameters;

public class Neo4jIndexer  extends HbaseData {
    java.util.logging.Logger logger =  Logger.getLogger("neo4j indexing");


    GlobalNamesHandler globalNameHandler;
    JsonFileWriter jsonFile;

    public ArrayList<JSONObject> Neo4jToJson (int[] generatedNodeIds) {
        globalNameHandler = new GlobalNamesHandler();
        jsonFile =new JsonFileWriter();
        for(int j=0 ; j<generatedNodeIds.length ; j++) {
            int generatedNodeId = generatedNodeIds[j];
            StatementResult result = getnNodeData(generatedNodeId);


            if (result.hasNext()) {
                jsonFile.renew();
                Record record = result.next();
//                System.out.println(record.get("n.page_id"));
                int pageId = record.get("n.page_id")==NULL?-1:record.get("n.page_id").asInt();
                String scientificName = record.get("n.scientific_name").asString();
                String rank = record.get("n.rank").asString();
                jsonFile.JsonAddString("scientific name", scientificName);
                jsonFile.JsonAddString("Rank", rank);
                jsonFile.JsonAddInt("page id",pageId);

                Map<String,ArrayList<String>> synonymsMap=getSynonymsNames(generatedNodeId, record.get("n.resource_id").asInt());
                jsonFile.JsonAddArray("synonyms", synonymsMap.get("synonyms same resource"));
                jsonFile.JsonAddArray("other synonyms", synonymsMap.get("synonyms other resources"));


                ArrayList<String> ancestors = getAncestors(generatedNodeId);
                jsonFile.JsonAddArray("ancestors IDS", ancestors);

                ArrayList<String> children = getChildren(generatedNodeId);
                jsonFile.JsonAddArray("children IDS", children);


                jsonFile.JsonAddArray("canonical synonyms", getCanonicalSynonymsSameResource(synonymsMap.get("synonyms same resource")));
                jsonFile.JsonAddArray("other canonical synonyms", getCanonicalSynonymsOtherResources(synonymsMap.get("synonyms other resources")));
                jsonFile.JsonAddString("is_hybrid", String.valueOf(globalNameHandler.isHybrid(scientificName)));
                jsonFile.JsonAddString("canonical name", globalNameHandler.getCanonicalName(scientificName));




                jsonFile.JsonAddNode(generatedNodeId);



            }
        }

        ArrayList<JSONObject> nodes = jsonFile.getNodes();
        jsonFile.printObj();
        return nodes;
    }

    public  StatementResult getnNodeData (int generatedNodeId)
    {
        logger.info("Getting scientific name and rank of node with autoId" + generatedNodeId);
        String query = "MATCH (n {generated_auto_id : {generatedNodeId}})"+
                " RETURN n.scientific_name, n.rank, n.resource_id, n.page_id" ;

        StatementResult result = getSession().run(query, parameters("generatedNodeId", generatedNodeId ));

        return result;
    }

    public Map getSynonymsNames(int generatedNodeId, int resource_id)
    {
        logger.info("Getting synonyms of node with autoId" + generatedNodeId);
        ArrayList<String> synonymsSameResource = new ArrayList<>();
        ArrayList<String> synonymsOtherResources = new ArrayList<>();
        String query = "MATCH (a {generated_auto_id: {generatedNodeId}})<-[:IS_SYNONYM_OF]-(s:Synonym) return s.scientific_name , s.resource_id";
        StatementResult result = getSession().run(query, parameters("generatedNodeId", generatedNodeId));
        while (result.hasNext())
        {
            Record record = result.next();
            if(resource_id ==record.get("s.resource_id").asInt())
            {
                synonymsSameResource.add(record.get("s.scientific_name").asString() + "")  ;
            }

            else
            {
                synonymsOtherResources.add(record.get("s.scientific_name").asString() + "")  ;
            }

        }
        Map<String,ArrayList<String>> map =new HashMap();
        map.put("synonyms same resource",synonymsSameResource);
        map.put("synonyms other resources",synonymsOtherResources);
        return map;

    }

    public ArrayList<String> getChildrenName(int generatedNodeId)
    {
        logger.info("Getting children of node with autoId" + generatedNodeId);
        ArrayList<String> children = new ArrayList<>();
        String query = "MATCH (n {generated_auto_id: {generatedNodeId}})-[:IS_PARENT_OF]->(c) return c.scientific_name";
        StatementResult result = getSession().run(query, parameters("generatedNodeId",generatedNodeId));
        while (result.hasNext())
        {
            Record record = result.next();
            children.add(record.get("c.scientific_name").asString() + "");
        }
        return children;
    }

    public  ArrayList<String> getCanonicalSynonymsSameResource(ArrayList<String> synonymsSameResource)
    {   logger.info("Getting canonocal synonyms same resource of node" );
        ArrayList<String> canonicalSynonymsSameResource = getCanonical(synonymsSameResource);
        return canonicalSynonymsSameResource;

    }

    public  ArrayList<String> getCanonicalSynonymsOtherResources(ArrayList<String> synonymsOtherResources)
    {   logger.info("Getting canonocal synonyms same resource of node" );
        ArrayList<String> canonicalSynonymsOtherResources = getCanonical(synonymsOtherResources);
        return canonicalSynonymsOtherResources ;
    }

    public ArrayList<String> getCanonical(ArrayList<String> synonymArray)
    {
        ArrayList<String> canonicalArray = new ArrayList<>();

        for(int i=0 ; i < synonymArray.size() ; i++)
        {
            canonicalArray.add(globalNameHandler.getCanonicalName(synonymArray.get(i)));
        }
        return canonicalArray;
    }

}
