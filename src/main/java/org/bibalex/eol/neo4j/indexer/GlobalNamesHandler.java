package org.bibalex.eol.neo4j.indexer;

import org.bibalex.eol.neo4j.parser.Constants;
import org.globalnames.parser.ScientificNameParser;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.lang.reflect.Field;
import java.util.logging.Logger;

public class GlobalNamesHandler {

    private JSONParser parser;
    private Object att;
    java.util.logging.Logger logger =  Logger.getLogger("globalNamesHandler");
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
            e.printStackTrace();
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
        logger.info("Getting isHyprid of node with scientific name" + name );
        return parseAndGetResult(name, Constants.HYBRID);
    }

    public String getCanonicalName(String name )  {
        logger.info("Getting canonical node with scientific name" + name );
        return parseAndGetResultString(name, Constants.NODE_ATTRIBUTE_CANONICAL_NAME);
    }
}
