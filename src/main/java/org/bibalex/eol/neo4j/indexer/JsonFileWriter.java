package org.bibalex.eol.neo4j.indexer;

import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;

public class JsonFileWriter {
    Logger logger =  LoggerFactory.getLogger(JsonFileWriter.class);
    JSONObject obj;
    JSONObject node;
    //    JSONObject nodes = new JSONObject();
    ArrayList<JSONObject> nodes = new ArrayList<JSONObject>();
//    FileWriter file = new FileWriter("/home/ba/work/files/result.json");

    public JsonFileWriter()  {

    }


    public void renew ()
    {   logger.info("Create New Object and New Node");
        obj  = new JSONObject();
//        node = new JSONObject();
    }

    public void JsonAddString (String key ,String value)
    {
        logger.info("JSON Object- Key: " + key + ", Value: " + value);
        obj.put(key, value);
    }

    public void JsonAddInt (String key ,int value)
    {
        logger.info("JSON Object- Key: " + key + ", Value: " + value);
        obj.put(key, value);
    }

    public void JsonAddArray (String key,ArrayList list)
    {
        logger.info("JSON Object- Key: " + key);
        logger.debug("Value: " + list);
        obj.put(key, list);
    }

    public void JsonAddNode(int generatedNodeId)
    {

        obj.put("generatedNodeId", generatedNodeId);
        logger.info("Adding Node: " + generatedNodeId + " to Nodes Array");
//        node.put("node", obj);
        nodes.add(obj);

    }

    public ArrayList<JSONObject> getNodes()
    {
        logger.info("Return JSON Object of Nodes");
        logger.debug("JSON Objects Array: \n" + nodes);
        return nodes;
    }

    public void printObj()
    {
//        System.out.println(nodes);
        logger.debug(String.valueOf(nodes));
    }
}
