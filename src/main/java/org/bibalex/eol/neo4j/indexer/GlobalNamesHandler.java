package org.bibalex.eol.neo4j.indexer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.globalnames.parser.ScientificNameParser;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class GlobalNamesHandler {

    private JSONParser parser;
    private Object att;
    Logger logger =  LogManager.getLogger(GlobalNamesHandler.class);
    public GlobalNamesHandler(){
        parser = new JSONParser();
    }

    private JSONObject getParsedJson(String name){
        String jsonStr = ScientificNameParser.instance()
                .fromString(name)
                .renderCompactJson();
        try {
            return (JSONObject)parser.parse(jsonStr);
        } catch (ParseException e) {
            logger.error("ParseException: ", e);
//            e.printStackTrace();
        }
        return null;
    }

    private boolean parseAndGetResult(String name, String attribute){
        Object att = (getParsedJson(name)).get(attribute);
        return att == null ? false : (Boolean)att;
    }

    private String parseAndGetResultString(String name, String attribute) {
        JSONObject attr = (JSONObject) getParsedJson(name).get(attribute);
        if(attr == null) return null;
        Object att = attr.get("value");
        return att == null ? null : att.toString();

    }


    public boolean isHybrid(String name){
        logger.info("Check if Node: " +name+" is Hybrid." );
        boolean result = parseAndGetResult(name, "hybrid");
        logger.info("Node: " + name + "is Hybrid? : " + result);
        return result;
    }


    public String getCanonicalName(String name )  {
        logger.info("Getting Canonical Name of Scientific Name: " + name );
        String canonicalName = parseAndGetResultString(name, "canonical_name");
        logger.info("Canonical Name: " + canonicalName);
        return canonicalName;
    }
}
