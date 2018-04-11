package org.bibalex.eol.neo4j.parser;

import org.neo4j.driver.v1.Record;
import org.neo4j.driver.v1.StatementResult;

import static org.neo4j.driver.v1.Values.parameters;

public class Neo4jParentFormat extends Neo4jCommon  {

    public int createParentWithPlaceholder(int resourceId, String parentUsageId)
    {
        int nodeGeneratedNodeId = getNodeIfExist(parentUsageId, resourceId);
        if (nodeGeneratedNodeId == -1)
        {
            autoId = getAutoId();
            String create_query = "CREATE (n:Node {resource_id: {resourceId}, node_id: {parentUsageId}," +
                    " scientific_name: {scientificName}, rank: {rank}, generated_auto_id: {autoId}," +
                    " created_at: apoc.date.currentTimestamp(),updated_at: apoc.date.currentTimestamp()})" +
                    "RETURN n.generated_auto_id";
            StatementResult result = getSession().run(create_query, parameters("resourceId", resourceId,
                    "parentUsageId", parentUsageId, "scientificName", "placeholder", "rank", "placeholder", "autoId", autoId));
            autoId++;
            if (result.hasNext()) {
                logger.debug("Node with id " + parentUsageId + " created with placeholders");
                Record record = result.next();
                return record.get("n.generated_auto_id").asInt();
            } else {
                logger.debug("Node with id  " + parentUsageId + " is not created a problem has occurred");
                return -1;
            }
        }
        else
        {
            logger.debug("Node with nodeId "+ parentUsageId +"  found");
            return nodeGeneratedNodeId;
        }

    }


    public boolean deleteNodeParentFormat(String nodeId, int resourceId, String scientificName)
    {
        logger.debug("Deleting Node with nodeId " + nodeId + " of resource " + resourceId );
        int nodeGeneratedId = getAcceptedNodeIfExist(nodeId, scientificName, resourceId);
        if (nodeGeneratedId != -1)
        {
            if (hasChildren(nodeGeneratedId))
            {
                logger.debug("Node has children so just delete");
                deleteNode(nodeGeneratedId);
            }
            else
            {
                CommonDeleteMethod(nodeGeneratedId);

            }
            if(checkIfNodeExists(nodeGeneratedId))
                return false;
            else
                return true;

        }
        else
            return false;

    }


}
