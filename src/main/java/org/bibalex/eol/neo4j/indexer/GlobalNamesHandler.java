package org.bibalex.eol.neo4j.handlers;

import org.globalnames.parser.ScientificNameParser;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.lang.reflect.Field;

public class GlobalNamesHandler {

    private JSONParser parser;
    private Object att;

    public GlobalNamesHandler(){
        parser = new JSONParser();
    }

    private JSONObject getParsedJson(String name){
        String jsonStr = ScientificNameParser.instance()
                .fromString(name)
                .renderCompactJson();
//       System.out.println("jsonstring"+jsonStr);
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
        Object att = attr.get("value");
            return att == null ? null : att.toString();

    }


    public boolean isHybrid(String name){
        return parseAndGetResult(name, "hybrid");
    }


    public String getCanonicalName(String name )  {
        return parseAndGetResultString(name, "canonical_name");
    }
}
