package org.bibalex.eol.neo4j.indexer;

import org.json.simple.JSONObject;

import java.util.ArrayList;
import java.util.logging.Logger;

public class JsonFileWriter {
    java.util.logging.Logger logger =  Logger.getLogger("json object creation");
    JSONObject obj;
    JSONObject node;
    //    JSONObject nodes = new JSONObject();
    ArrayList<JSONObject> nodes = new ArrayList<JSONObject>();
//    FileWriter file = new FileWriter("/home/ba/work/files/result.json");

    public JsonFileWriter()  {

    }


    public void renew ()
    {   logger.info("create new object and new node");
        obj  = new JSONObject();
        node = new JSONObject();
    }

    public void JsonAddString (String key ,String value)
    {
        logger.info("adding string to json object with key: " + key + " and value: " + value);
        obj.put(key, value);

    }

    public void JsonAddArray (String key,ArrayList list)
    {
        logger.info("adding arraylist to json object with key: " + key);
        obj.put(key, list);
    }

    public void JsonAddNode(int generatedNodeId)
    {

        obj.put("generatedNodeId", generatedNodeId);
        logger.info("add node to array of nodes");
        node.put("node" + generatedNodeId, obj);
        nodes.add(node);

    }

    public ArrayList<JSONObject> getNodes()
    {
        logger.info("returni nodes json objects");
        return nodes;
    }

    public void printObj()
    {
        System.out.println(nodes);
    }
}
