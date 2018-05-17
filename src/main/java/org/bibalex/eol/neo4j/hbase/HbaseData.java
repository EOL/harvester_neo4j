package org.bibalex.eol.neo4j.hbase;

import org.bibalex.eol.neo4j.parser.Neo4jCommon;
import org.neo4j.driver.v1.Record;
import org.neo4j.driver.v1.StatementResult;

import java.lang.reflect.AnnotatedArrayType;
import java.util.ArrayList;
import java.util.logging.Logger;

import static org.neo4j.driver.v1.Values.parameters;

public class HbaseData extends Neo4jCommon{
     Logger logger =  Logger.getLogger("HbaseData");

    public ArrayList<String> getAncestors(int generatedNodeId)
    {
        logger.info("Getting ancestors of node with autoId" + generatedNodeId);
        ArrayList<String> ancestors = new ArrayList<>();
        String query = "MATCH (n:Node {generated_auto_id: {generatedNodeId}})<-[:IS_PARENT_OF*]-(p) return p.generated_auto_id";
        StatementResult result = getSession().run(query, parameters("generatedNodeId",generatedNodeId));
        while (result.hasNext())
        {
            Record record = result.next();
            ancestors.add(record.get("p.generated_auto_id").asInt()+ "");
        }
       return ancestors;
    }

    public ArrayList<String> getChildren(int generatedNodeId)
    {
        logger.info("Getting children of node with autoId" + generatedNodeId);
        ArrayList<String> children = new ArrayList<>();
        String query = "MATCH (n {generated_auto_id: {generatedNodeId}})-[:IS_PARENT_OF]->(c:Node) return c.generated_auto_id";
        StatementResult result = getSession().run(query, parameters("generatedNodeId",generatedNodeId));
        while (result.hasNext())
        {
            Record record = result.next();
            children.add(record.get("c.generated_auto_id").asInt()+ "");
        }
        return children;
    }


    public ArrayList<String> getSynonyms(int generatedNodeId)
    {
        logger.info("Getting synonyms of node with autoId" + generatedNodeId);
        ArrayList<String> synonyms = new ArrayList<>();
        String query = "MATCH (a {generated_auto_id: {generatedNodeId}})<-[:IS_SYNONYM_OF]-(s:Synonym) return s.generated_auto_id";
        StatementResult result = getSession().run(query, parameters("generatedNodeId",generatedNodeId));
        while (result.hasNext())
        {
            Record record = result.next();
            synonyms.add(record.get("s.generated_auto_id").asInt()+ "");
        }
        return synonyms;
    }
}
