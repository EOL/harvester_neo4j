package org.bibalex.eol.neo4j.parser;

import org.bibalex.eol.neo4j.models.Node;
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

    public int getNodeGivenParentIfExists(int resourceId, String scientificName, String rank, String nodeId, int parentGeneratedNodeId)
    {
        String query = "MATCH (n {resource_id: {resourceId} , scientific_name: {scientificName}," +
                " rank: {rank}, nodeId: {nodeId}})<-[r:IS_PARENT_OF]-(p {generated_auto_id: {parentGeneratedNodeId}}) RETURN n.generated_auto_id UNION" +
                " MATCH (n:Root {resource_id: {resourceId} , scientific_name: {scientificName}, rank: {rank}}) RETURN n.generated_auto_id";
        StatementResult result = getSession().run(query, parameters( "resourceId",resourceId,
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

    public boolean UpdateNodeParentFormat (Node new_node, Node new_parent)
    {
        int nodegeneratedId = getNodeIfExist(new_node.getNodeId(), new_node.getResourceId());
        if (nodegeneratedId != -1)
        {
            Node old_node = getNodeProperties(nodegeneratedId);
            if (!old_node.getScientificName().equals(new_node.getScientificName()))
            {
                logger.debug("Update scientific Name of the node");
                UpdateScientificName(nodegeneratedId, new_node.getScientificName());
            }
            if (!old_node.getRank().equals(old_node.getRank()))
            {
                logger.debug("Update rank of the node");
                UpdateRank(nodegeneratedId, new_node.getRank());
            }
            else
            {
                logger.debug("Update ancestry in parent format");
                int result = UpdateHierarchy(new_node, new_parent);
                if (result == 200)
                {
                   return true;
                }
                if (result == 400)
                {
                   return false;
                }
                return false;
            }

        }
        else
        {
            return false;
        }
        return true;
        // update and check return valuse and test
    }

    public int UpdateHierarchy(Node new_node, Node new_parent)
    {
        int old_parent_id = getParent(new_node.getGeneratedNodeId());
        Node old_parent = getNodeProperties(old_parent_id);
        int new_nodeGeneratedId = getNodeGivenParentIfExists(new_node.getResourceId(), new_node.getScientificName(),
                new_node.getRank(), new_node.getNodeId(), new_parent.getGeneratedNodeId());
        if (new_nodeGeneratedId == -1)
        {
            logger.debug("Node with this new parent is not found");
            new_nodeGeneratedId = createAcceptedNode(new_node.getResourceId(), new_node.getNodeId(), new_node.getScientificName(),
                    new_node.getRank(), -1);
            boolean new_parent_exists = checkIfNodeExists(new_parent.getGeneratedNodeId());
            if(new_parent_exists)
            {
                logger.debug("New Parent exists as a node");
                createChildParentRelation(new_parent.getGeneratedNodeId(), new_nodeGeneratedId);
                if (!parenthasNodeId(old_parent_id))
                {
                    logger.debug("Old parent is placeholder");
                    deleteNode(old_parent_id);
                }
                return 200;
            }
            else
            {
                logger.debug("Missing parent is sent to parser");
                return 400;
            }


        }

    }

}
