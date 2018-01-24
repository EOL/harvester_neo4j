package org.bibalex.eol.neo4j.parser;

import org.neo4j.driver.v1.Record;
import org.neo4j.driver.v1.StatementResult;
import org.neo4j.graphdb.Transaction;

import java.util.ArrayList;

import static org.neo4j.driver.v1.Values.parameters;

public class Neo4jAncestryFormat extends Neo4jCommon {

    public int createAncestorIfNotExist(int resourceId, String scientificName, String rank, String nodeId,
                                        int parentGeneratedNodeId)
    {

        int nodeGeneratedNodeId = getAncestoryNodeIfExist(resourceId, scientificName, rank, parentGeneratedNodeId);
        if (nodeGeneratedNodeId == -1)
        {
            logger.debug("Node "+ scientificName +" not found creating a new node");
            String create_query = "CREATE (n:Node {resource_id: {resourceId}, node_id: {nodeId}," +
                    " scientific_name: {scientificName}, rank: {rank}, generated_auto_id: {autoId}, created_at: apoc.date.currentTimestamp(), " +
                    "updated_at: apoc.date.currentTimestamp()})RETURN n.generated_auto_id";
            StatementResult result = getSession().run(create_query, parameters( "resourceId", resourceId,
                    "nodeId", nodeId, "scientificName", scientificName, "rank", rank, "autoId", autoId ));
            if (parentGeneratedNodeId != 0) {
                logger.debug("Parent avaliable with id " + parentGeneratedNodeId);
                createChildParentRelation(parentGeneratedNodeId, autoId);
            }
            autoId ++;
            Record record = result.next();
            return record.get("n.generated_auto_id").asInt();
        }

        else
        {
            logger.debug("Node "+ scientificName +"  found");
            return nodeGeneratedNodeId;

        }

    }

    public int getAncestoryNodeIfExist(int resourceId, String scientificName, String rank, int parentGeneratedNodeId )
    {
        String query = "MATCH (n:Node {resource_id: {resourceId} , scientific_name: {scientificName}," +
                " rank: {rank}})<-[r:IS_PARENT_OF]-(p:Node {generated_auto_id: {parentGeneratedNodeId}}) RETURN" +
                " DISTINCT n.generated_auto_id";
        StatementResult result = getSession().run(query, parameters(query, "resourceId",resourceId,
                "scientificName", scientificName, "rank", rank , "parentGeneratedNodeId", parentGeneratedNodeId));

        if (result.hasNext())
        {
            Record record = result.next();
            logger.debug("The result of search" +record.get("n.generated_auto_id").asInt() );
            return record.get("n.generated_auto_id").asInt();
        }

        else
        {
            logger.debug("The result is -1");
            return -1;
        }

    }

}
