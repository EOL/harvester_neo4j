package org.bibalex.eol.neo4j.parser;

import org.neo4j.driver.v1.Record;
import org.neo4j.driver.v1.StatementResult;

import static org.neo4j.driver.v1.Values.parameters;

public class Neo4jParentFormat extends Neo4jCommon  {

    public int createParentWithPlaceholder(int resourceId, String parentUsageId)
    {
        autoId = getAutoId();
        String create_query = "CREATE (n:Node {resource_id: {resourceId}, node_id: {parentUsageId}," +
                " scientific_name: {scientificName}, rank: {rank}, generated_auto_id: {autoId}," +
                " created_at: apoc.date.currentTimestamp(),updated_at: apoc.date.currentTimestamp()})" +
                "RETURN n.generated_auto_id";
        StatementResult result = getSession().run(create_query, parameters( "resourceId", resourceId,
                "parentUsageId", parentUsageId, "scientificName", "placeholder", "rank", "placeholder", "autoId", autoId ));
        autoId ++;
        if (result.hasNext())
        {
//            logger.debug("Node with id " + parentUsageId + " created with placeholders");
            Record record = result.next();
            return record.get("n.generated_auto_id").asInt();
        }
        else
        {
//            logger.debug("Node with id  "+ parentUsageId + " is not created a problem has occured");
            return -1;
        }

    }
}
