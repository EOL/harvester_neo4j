package org.bibalex.eol.neo4j.indexer;

import org.bibalex.eol.neo4j.hbase.HbaseData;
import org.json.simple.JSONObject;
import org.neo4j.driver.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import static org.neo4j.driver.Values.NULL;
import static org.neo4j.driver.Values.parameters;

public class Neo4jIndexer  extends HbaseData {
    java.util.logging.Logger logger =  Logger.getLogger("neo4j indexing");


    GlobalNamesHandler globalNameHandler;
    JsonFileWriter jsonFile;

    public ArrayList<JSONObject> Neo4jToJson (int[] generatedNodeIds) {
        globalNameHandler = new GlobalNamesHandler();
        jsonFile =new JsonFileWriter();
//        for(int j=0 ; j<generatedNodeIds.length ; j++) {
//            int generatedNodeId = generatedNodeIds[j];
            StatementResult result = getnNodeData(generatedNodeIds);


            while (result.hasNext()) {
                jsonFile.renew();
                Record record = result.next();

                //if page_id is null -1 will be retuened
                //scientific name can't be null but can be empty string("") will return "" same for canonical name
                //rank can be null and can be empty string will return in both cases ""
                int generatedNodeId = record.get("n.generated_auto_id").asInt();
                int pageId = record.get("n.page_id")==NULL?-1:record.get("n.page_id").asInt();
                String scientificName =  record.get("n.scientific_name").asString();
                int resourceId =  record.get("n.resource_id").asInt();
                String rank = record.get("n.rank")== NULL ? "" : record.get("n.rank").asString();
                String canonicalName =scientificName.equals("")?"": globalNameHandler.getCanonicalName(scientificName);
                jsonFile.JsonAddString("scientific name", scientificName);
                jsonFile.JsonAddString("Rank", rank);
                jsonFile.JsonAddInt("page id",pageId);
                jsonFile.JsonAddInt("resource id",resourceId);

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
                jsonFile.JsonAddString("canonical name", canonicalName);




                jsonFile.JsonAddNode(generatedNodeId);



            }
//        }

        ArrayList<JSONObject> nodes = jsonFile.getNodes();
        jsonFile.printObj();
        return nodes;
    }

    public  StatementResult getnNodeData (int [] generatedNodeIds)
    {
        logger.info("Getting scientific name and rank of node with autoId" + generatedNodeIds);
//        String query = "MATCH (n:GNode {generated_auto_id : {generatedNodeId}})"+
//                " RETURN n.scientific_name, n.rank, n.resource_id, n.page_id" ;
        String query = "WITH {generatedNodeIds} as generated_node_ids MATCH (n:Node) WHERE n.generated_auto_id in generated_node_ids " +
                "RETURN n.generated_auto_id, n.scientific_name, n.rank, n.resource_id, n.page_id";

        StatementResult result = getSession().run(query, parameters("generatedNodeIds", generatedNodeIds ));

        return result;
    }

    public Map getSynonymsNames(int generatedNodeId, int resource_id)
    {
        logger.info("Getting synonyms of node with autoId" + generatedNodeId);
        ArrayList<String> synonymsSameResource = new ArrayList<>();
        ArrayList<String> synonymsOtherResources = new ArrayList<>();
        String query = "MATCH (a:GNode {generated_auto_id: {generatedNodeId}})<-[:IS_SYNONYM_OF]-(s:Synonym) return s.scientific_name , s.resource_id";
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
