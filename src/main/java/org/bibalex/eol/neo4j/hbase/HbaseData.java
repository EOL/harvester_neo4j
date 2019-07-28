package org.bibalex.eol.neo4j.hbase;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bibalex.eol.neo4j.parser.Neo4jCommon;
import org.neo4j.driver.Record;
import org.neo4j.driver.StatementResult;

import java.lang.reflect.AnnotatedArrayType;
import java.util.ArrayList;

import static org.neo4j.driver.Values.parameters;

public class HbaseData extends Neo4jCommon{
     Logger logger = LogManager.getLogger(HbaseData.class);

    public ArrayList<String> getAncestors(int generatedNodeId)
    {
        logger.info("Getting Ancestors of Node: " + generatedNodeId);
        ArrayList<String> ancestors = new ArrayList<>();
        String query = "MATCH (n:GNode {generated_auto_id: {generatedNodeId}})<-[:IS_PARENT_OF*]-(p) return p.generated_auto_id";
        StatementResult result = getSession().run(query, parameters("generatedNodeId",generatedNodeId));
        while (result.hasNext())
        {
            Record record = result.next();
            ancestors.add(record.get("p.generated_auto_id").asInt()+ "");
        }
        logger.debug("Ancestors: \n" + ancestors);
       return ancestors;
    }

    public ArrayList<String> getChildren(int generatedNodeId)
    {
        logger.info("Getting Children of Node: " + generatedNodeId);
        ArrayList<String> children = new ArrayList<>();
        String query = "MATCH (n:GNode {generated_auto_id: {generatedNodeId}})-[:IS_PARENT_OF]->(c:Node) return c.generated_auto_id";
        StatementResult result = getSession().run(query, parameters("generatedNodeId",generatedNodeId));
        while (result.hasNext())
        {
            Record record = result.next();
            children.add(record.get("c.generated_auto_id").asInt()+ "");
        }
        logger.debug("Children: " + children);
        return children;
    }


    public ArrayList<String> getSynonyms(int generatedNodeId)
    {
        logger.info("Getting Synonyms of Node: " + generatedNodeId);
        ArrayList<String> synonyms = new ArrayList<>();
        String query = "MATCH (a {generated_auto_id: {generatedNodeId}})<-[:IS_SYNONYM_OF]-(s:Synonym) return s.generated_auto_id";
        StatementResult result = getSession().run(query, parameters("generatedNodeId",generatedNodeId));
        while (result.hasNext())
        {
            Record record = result.next();
            synonyms.add(record.get("s.generated_auto_id").asInt()+ "");
        }
        logger.debug("Synonyms: " + synonyms);
        return synonyms;
    }
}
